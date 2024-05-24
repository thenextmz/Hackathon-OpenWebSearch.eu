package eu.ows.mosaic;

import org.apache.lucene.analysis.Analyzer;

/**
 * Custom Analyzer class to define the Analyzer for the Lucene index.
 * Change the implementation of the getAnalyzer method stub to return the desired Analyzer.
 */
public interface AnalyzerComponent extends CoreComponent {
    Analyzer getAnalyzer(Analyzer defaultAnalyzer);
}
