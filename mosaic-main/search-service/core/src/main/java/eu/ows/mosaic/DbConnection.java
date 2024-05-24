package eu.ows.mosaic;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to manage the connection to the DuckDB database and to create tables for indexes.
 */
public class DbConnection {

    private static Logger LOGGER = LoggerFactory.getLogger(DbConnection.class);
    
    private Connection conn;

    public DbConnection(boolean isReadOnly) throws SQLException {
        Properties properties = new Properties();
        if (isReadOnly) {
            properties.setProperty("duckdb.read_only", "true");
        }
        conn = DriverManager.getConnection("jdbc:duckdb:" + CoreUtils.getDatabaseFilePath(), properties);
    }

    public DbConnection() throws SQLException {
        this(true);
    }

    /**
     * Create tables for indexes if they do not already exist.
     * @param numPlainTextCharacters The number of plain text characters to limit the plain text to
     * @throws SQLException If an error occurs while creating the tables
     */
    public void createTables(Long numPlainTextCharacters) throws SQLException {
        LOGGER.info("Checking if tables for indexes already exist");

        // Check if tables for indexes already exist
        Set<String> tablesToCreate = new HashSet<>();
        for (String indexName : ResourceManager.getInstance().getIndexes().keySet()) {
            if (!tableExists(indexName)) {
                LOGGER.info("Table for index {} does not exist", indexName);
                tablesToCreate.add(indexName);
            }
        }

        // Create tables for indexes that do not already exist
        for (String indexName : tablesToCreate) {
            LOGGER.info("Creating table using DuckDB for index {}", indexName);

            // Retrieve columns from Parquet schema and filter out columns that are not in the metadata schema
            Set<String> parquetSchemaColumns = this.retrieveParquetSchema(indexName);
            Set<String> metadataColumns = new TreeSet<>();
            PluginManager.getInstance().getModules().values().forEach(module -> metadataColumns.addAll(module.getMetadataColumns()));
            LOGGER.info("Metadata schema columns: {}", metadataColumns);
            parquetSchemaColumns.retainAll(metadataColumns);
            parquetSchemaColumns.add("id");
            LOGGER.info("Parquet schema columns: {}", parquetSchemaColumns);

            String columns = String.join(",", parquetSchemaColumns);

            // Limit the number of plain text characters if specified
            if (numPlainTextCharacters != null && numPlainTextCharacters.longValue() > 1 && columns.contains("plain_text")) {
                LOGGER.info("Limiting the number of plain text characters to {}", numPlainTextCharacters);
                columns = columns.replace("plain_text", "substring(plain_text, 1, " + numPlainTextCharacters + ") as plain_text");
            }

            String sql = "CREATE TABLE " + indexName.replace('-', '_') + " AS " + 
                         "SELECT " + columns + " " +
                         "FROM read_parquet('" + CoreUtils.getParquetDirPath() + indexName + File.separator + "*.parquet*') " + 
                         "ORDER BY " + CoreUtils.getIdColumn();
            try {
                conn.createStatement().execute(sql);
            } catch (SQLException e) {
                LOGGER.error("Failed to create table using DuckDB for index " + indexName, e);
            }
            
        } 
    }

    /**
     * Checks if a table for an index already exists.
     * @param indexName The name of the index
     * @return True if the table exists, false otherwise
     * @throws SQLException If an error occurs while checking if the table exists
     */
    private boolean tableExists(String indexName) throws SQLException {
        LOGGER.info("Checking if table for index {} already exists", indexName);

        String sql = "SELECT count(*) " +
                     "FROM information_schema.tables " +
                     "WHERE table_name = ? " +
                     "LIMIT 1;";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, indexName.replace('-', '_'));

        ResultSet rs = ps.executeQuery();
        rs.next();
        boolean tableExists = rs.getInt(1) != 0;

        ps.close();
        rs.close();

        return tableExists;
    }

    /**
     * Retrieve the Parquet schema for an index.
     * @param indexName The name of the index
     * @return The Parquet schema columns
     * @throws SQLException If an error occurs while retrieving the Parquet schema
     */
    private Set<String> retrieveParquetSchema(String indexName) throws SQLException {
        LOGGER.info("Retrieving Parquet schema for index {}", indexName);

        String sql = "SELECT DISTINCT(name) " +
                     "FROM parquet_schema('" + CoreUtils.getParquetDirPath() + indexName + File.separator + "*.parquet*') ";

        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        Set<String> parquetSchemaColumns = new HashSet<>();
        while (rs.next()) {
            parquetSchemaColumns.add(rs.getString("name"));
        }

        rs.close();
        ps.close();

        return parquetSchemaColumns;
    }

    /**
     * Retrieve the available metadata table columns for an index.
     * @param indexName The name of the index
     * @return The available metadata columns
     * @throws SQLException If an error occurs while retrieving the metadata columns
     */
    public Set<String> retrieveMetadataColumns(String indexName) throws SQLException {
        LOGGER.info("Retrieving available metadata columns for index {}", indexName);

        String sql = "SELECT column_name " +
                     "FROM information_schema.columns " +
                     "WHERE table_name = ?";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, indexName.replace('-', '_'));
        ResultSet rs = ps.executeQuery();

        Set<String> metadataColumns = new HashSet<>();
        while (rs.next()) {
            metadataColumns.add(rs.getString("column_name"));
        }

        rs.close();
        ps.close();

        return metadataColumns;
    }

    /**
     * Build a metadata query for an index.
     * The query is built based on the metadata columns and query parameters.
     * Additional filter clauses can be added by metadata modules.
     * @param indexName The name of the index
     * @param metadataColumns The metadata columns to retrieve
     * @param queryParams The query parameters
     * @return The metadata query
     */
    public String buildMetadataQuery(String indexName, Set<String> metadataColumns, Map<String, Object> queryParams) {
        LOGGER.info("Building metadata query for index {}", indexName);

        String columns = String.join(", ", metadataColumns);
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT ").append(columns)
                  .append(" FROM ").append(indexName.replace('-', '_'))
                  .append(" WHERE ").append(CoreUtils.getIdColumn()).append(" = ? ");

        // Add additional filter clauses from metadata modules
        PluginManager.getInstance().getModules().forEach((k, v) -> sqlBuilder.append(v.getSqlFilterClauses(queryParams, metadataColumns)));
        String sql = sqlBuilder.toString();
        LOGGER.info("Built metadata query: {}", sql);

        return sql;
    }

    /**
     * Retrieve metadata for an index and id.
     * @param indexName The name of the index
     * @param id The id of the document
     * @param metadataQuery The metadata query
     * @param queryParams The query parameters
     * @param metadataColumns The metadata columns to retrieve
     * @return The metadata result set
     * @throws SQLException If an error occurs while retrieving the metadata
     */
    public ResultSet retrieveMetadataForDocument(String indexName, String id, String metadataQuery, Map<String, Object> queryParams, Set<String> metadataColumns) throws SQLException {
        LOGGER.info("Retrieving metadata for index {} and id {}", indexName, id);

        PreparedStatement ps = conn.prepareStatement(metadataQuery);
        int parameterIndex = 1;
        ps.setString(parameterIndex++, id);

        // Add additional filter values from metadata modules
        for (Entry<String, MetadataModule> module : PluginManager.getInstance().getModules().entrySet()) {
            for (Object value : module.getValue().getSqlFilterValues(queryParams, metadataColumns)) {
                ps.setObject(parameterIndex++, value);
            }
        }

        ResultSet rs = ps.executeQuery();
        return rs;
    }

    /**
     * Retrieve the languages for an index.
     * @param indexName The name of the index
     * @return The languages contained in the index
     * @throws SQLException If an error occurs while retrieving the languages
     */
    public List<String> retrieveIndexInfo(String indexName) throws SQLException {
        LOGGER.info("Retrieving index info for index {}", indexName);

        String sql = "SELECT DISTINCT language " +
                     "FROM " + indexName.replace('-', '_') + " " +
                     "ORDER BY language ASC";

        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        List<String> languages = new ArrayList<>();
        while (rs.next()) {
            languages.add(rs.getString("language"));
        }

        rs.close();
        ps.close();

        return languages;
    }

    /**
     * Retrieve the full text for an index and id.
     * @param indexName The name of the index
     * @param id The id of the document
     * @param idColumn The id column
     * @return The full text of the document
     * @throws SQLException If an error occurs while retrieving the full text
     */
    public String retrieveFullText(String indexName, String id, String idColumn) throws SQLException {
        LOGGER.info("Retrieving full text for index {} and id {}", indexName, id);

        String sql = "SELECT plain_text " +
                     "FROM read_parquet('" + CoreUtils.getParquetDirPath() + indexName + File.separator + "*.parquet*') " +
                     "WHERE " + idColumn + " = ?";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, id);
        ResultSet rs = ps.executeQuery();
        String fullText = null;
        if (rs.next()) {
            fullText = rs.getString("plain_text");
        }

        rs.close();
        ps.close();

        return fullText;
    }

    /**
     * Close the connection to the DuckDB database.
     */
    public void closeConnection() {
        try {
            conn.close();
        } catch (SQLException e) {
            LOGGER.error("Failed to close connection", e);
        }
    }

}
