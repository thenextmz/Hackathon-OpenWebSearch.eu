package eu.ows.mosaic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton class for managing resources that are used by the application
 */
public class ResourceManager {

    private static Logger LOGGER = LoggerFactory.getLogger(ResourceManager.class);

    private static ResourceManager INSTANCE;

    private static Map<String, FSDirectory> indexes; // Index Name -> Lucene Directory
    private static List<String> metadataDirectoryNames;

    private ResourceManager() {
        try {
            readLuceneIndexes();
            readParquetDirectories();
        } catch (IOException e) {
            LOGGER.error("Error reading indexes or parquet directories: {}", e.getMessage());
        }
    }

    public static ResourceManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ResourceManager();
        }

        return INSTANCE;
    }

    /**
     * Adds all available indexes from the lucene directory to a map
     * @throws IOException 
     */
    private void readLuceneIndexes() throws IOException {
        LOGGER.info("Reading lucene indexes in {}", CoreUtils.getIndexDirPath());
        indexes = new TreeMap<String, FSDirectory>();
        File[] indexDirectories = new File(CoreUtils.getIndexDirPath()).listFiles(File::isDirectory);
        if (indexDirectories == null) {
            throw new RuntimeException("No indexes found in " + CoreUtils.getIndexDirPath());
        }

        for (File indexDirectory : indexDirectories) {
            if (indexDirectory.getName() != null && indexDirectory.isDirectory()) {
                String indexName = indexDirectory.getName();
                LOGGER.info("Adding {} to indexes map", indexName);
                indexes.put(indexName, FSDirectory.open(Paths.get(CoreUtils.getIndexDirPath() + indexName)));
            }
        }
    }

    /**
     * Adds all available directories that contain Parquet files from the Parquet directory to a map
     */
    private void readParquetDirectories() {
        LOGGER.info("Reading parquet directory {}", CoreUtils.getParquetDirPath());
        metadataDirectoryNames = new LinkedList<String>();
        File[] parquetDirectories = new File(CoreUtils.getParquetDirPath()).listFiles(File::isDirectory);
        if (parquetDirectories == null) {
            throw new RuntimeException("No parquet directories found in " + CoreUtils.getParquetDirPath());
        }

        for (File parquetDirectory : parquetDirectories) {
            if (parquetDirectory.getName() != null && parquetDirectory.isDirectory()) {
                String parquetDirectoryName = parquetDirectory.getName();
                LOGGER.info("Adding {} to metadata map", parquetDirectoryName);
                metadataDirectoryNames.add(parquetDirectoryName);
            }
        }
    }

    /**
     * Getter method for the indexes map
     * @return Map of index names to Lucene directories
     */
    public Map<String, FSDirectory> getIndexes() {
        return indexes;
    }

    /**
     * Getter method for the metadata directory names list
     * @return List of metadata directory names
     */
    public List<String> getMetadataDirectoryNames() {
        return metadataDirectoryNames;
    }
}
