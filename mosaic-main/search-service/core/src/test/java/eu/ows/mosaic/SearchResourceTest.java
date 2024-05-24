package eu.ows.mosaic;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.sql.SQLException;

@QuarkusTest
class SearchResourceTest {
    @Test
    void testSearchEndpoint() throws IOException, SQLException {
        CoreUtils.setIndexDirPath(CoreUtils.DEFAULT_INDEX_DIR_PATH);
        CoreUtils.setParquetDirPath(CoreUtils.DEFAULT_PARQUET_DIR_PATH);
        CoreUtils.setIdColumn(CoreUtils.DEFAULT_ID_COLUMN);
        CoreUtils.setConfigFilePath(CoreUtils.DEFAULT_CONFIG_FILE_PATH);
        CoreUtils.setDatabaseFilePath(CoreUtils.DEFAULT_DATABASE_FILE_PATH);

        PluginManager.getInstance().loadComponents();
        PluginManager.getInstance().loadModules();

        CoreConfig.getInstance();
        ResourceManager.getInstance();

        DbConnection dbConn = new DbConnection(false);
        dbConn.createTables(-1L);
        dbConn.closeConnection();

        given()
          .when().get("/search")
          .then()
             .statusCode(200);
    }

}