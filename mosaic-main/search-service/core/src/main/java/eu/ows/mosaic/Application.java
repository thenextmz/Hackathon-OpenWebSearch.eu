package eu.ows.mosaic;

import io.quarkus.runtime.annotations.QuarkusMain;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;

/**
 * Entry point of the application.
 */
@QuarkusMain  
public class Application {

    private static Logger LOGGER = LoggerFactory.getLogger(Application.class);

    /**
     * Starts the server.
     */
    public static void main(String ... args) {
        LOGGER.info("Starting MOSAIC search service");
        Quarkus.run(MosaicApplication.class,
                    (exitCode, exception) -> {
                        if (exception != null) {
                            LOGGER.error("MOSAIC search service failed to start", exception);
                        }
                    },
                    args);
    }

    /**
     * Quarkus application class.
     * This class is used to start the server and to initialize the application.
     * Command line arguments are parsed and all the necessary configurations are set here.
     */
    public static class MosaicApplication implements QuarkusApplication {

        private static Logger LOGGER = LoggerFactory.getLogger(MosaicApplication.class);

        @Override
        public int run(String... args) throws Exception {
            Options options = new Options();

            options.addOption(Option.builder("l")
                .argName("luceneDirPath").longOpt("lucene-dir-path")
                .hasArg()
                .desc("Path of directory containing the Lucene index(es)")
                .build());
            options.addOption(Option.builder("p")
                .argName("parquetDirPath").longOpt("parquet-dir-path")
                .hasArg()
                .desc("Path of directory containing the Parquet file(s)")
                .build());
            options.addOption(Option.builder("i")
                .argName("idColumn").longOpt("id-column")
                .hasArg()
                .desc("Column name of the ID in Parquet file used in Lucene index")
                .build());
            options.addOption(Option.builder("n")
                .argName("numCharacters").longOpt("num-characters")
                .hasArg()
                .type(Number.class)
                .desc("Number of characters selected from the plain text column")
                .build());
            options.addOption(Option.builder("d")
                .argName("dbFile").longOpt("db-file-path")
                .hasArg()
                .desc("Database file path")
                .build());

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = null;
            try {
                cmd = parser.parse(options, args);
            } catch (org.apache.commons.cli.ParseException e) {
                System.err.println("Error: " + e.getMessage());
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("<executable or script>", options);
                System.exit(1);
            }

            CoreUtils.setIndexDirPath(cmd.getOptionValue("l", CoreUtils.DEFAULT_INDEX_DIR_PATH));
            CoreUtils.setParquetDirPath(cmd.getOptionValue("p", CoreUtils.DEFAULT_PARQUET_DIR_PATH));
            CoreUtils.setIdColumn(cmd.getOptionValue("i", CoreUtils.DEFAULT_ID_COLUMN));
            CoreUtils.setConfigFilePath(CoreUtils.DEFAULT_CONFIG_FILE_PATH);
            CoreUtils.setDatabaseFilePath(cmd.getOptionValue("d", CoreUtils.DEFAULT_DATABASE_FILE_PATH));

            PluginManager.getInstance().loadComponents();
            PluginManager.getInstance().loadModules();

            DbConnection dbConn = new DbConnection(false);
            dbConn.createTables((Long) cmd.getParsedOptionValue("n"));
            dbConn.closeConnection();

            CoreUtils.setOpenSearchUrlTemplate(CoreConfig.getInstance().getOpenSearchTemplateUrl());

            LOGGER.info("MOSAIC search service started. Waiting for requests...");

            try {
                ResourceManager.getInstance();
            } catch (Exception e) {
                LOGGER.error("Failed to start MOSAIC search service", e);
                return 1;
            }

            Quarkus.waitForExit();
            LOGGER.info("MOSAIC search service exited");
            return 0;
        }
    }
}