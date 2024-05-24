package eu.ows.mosaic;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

/**
 * Core configuration class to define the core configuration for the search service as defined in the configuration file.
 */
public class CoreConfig {
    
    private static Logger LOGGER = LoggerFactory.getLogger(CoreConfig.class);

    private static CoreConfig INSTANCE;

    private String baseUrl;
    private String openSearchTemplateUrl;
    private Map<String, String> plugins;

    private CoreConfig(String baseUrl, String openSearchTemplateUrl, Map<String, String> plugins) {
        this.baseUrl = baseUrl;
        this.openSearchTemplateUrl = openSearchTemplateUrl;
        this.plugins = plugins;
    }

    public static CoreConfig getInstance() {
        if (INSTANCE == null) {
            String configFilePath = CoreUtils.getConfigFilePath();
            LOGGER.info("Reading core configuration from {}", configFilePath);
            try {
                INSTANCE = new Gson().fromJson(new JsonReader(new FileReader(configFilePath)), CoreConfig.class);
            } catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
                LOGGER.error("Failed to read core configuration from {}", configFilePath, e);
            }
            LOGGER.info("Created instance of CoreConfig");
        }

        return INSTANCE;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getOpenSearchTemplateUrl() {
        return openSearchTemplateUrl;
    }

    public Map<String, String> getPlugins() {
        return plugins;
    }

    public boolean isPluginEnabled(String pluginName) {
        return plugins.containsKey(pluginName);
    }

    public String getPluginClassname(String pluginName) {
        return plugins.get(pluginName);
    }
    
}
