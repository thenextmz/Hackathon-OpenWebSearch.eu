package eu.ows.mosaic;

import org.apache.lucene.analysis.Analyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom Analyzer class to define the Analyzer for the Lucene index.
 * Change the implementation of the getAnalyzer method stub to return the desired Analyzer.
 */
public class CustomAnalysis implements AnalyzerComponent {

    private static Logger LOGGER = LoggerFactory.getLogger(CustomAnalysis.class);

    /**
     * Returns the appropriate Lucene Analyzer. Use this stub to define the Analyzer for the Lucene index.
     * As an alternative to the default analyzer, use either an existing Lucene Analyzer or create a custom one.
     * Change the implementation of this method stub to return the desired Analyzer.
     * @param defaultAnalyzer Default Lucene Analyzer defined in the core module
     * @return Custom Lucene Analyzer
     */
    public Analyzer getAnalyzer(Analyzer defaultAnalyzer) {

        // Implement the desired analyzer

        LOGGER.info("Using default analyzer");
        Analyzer analyzer = defaultAnalyzer;
        return analyzer;
    }

}