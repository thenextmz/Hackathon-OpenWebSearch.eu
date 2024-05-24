package eu.ows.mosaic;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plugin manager class to manage the plugins (i.e., components and modules) for the search service.
 */
public class PluginManager {

    private static Logger LOGGER = LoggerFactory.getLogger(PluginManager.class);

    private static PluginManager INSTANCE;

    private Map<String, CoreComponent> components;
    private Map<String, MetadataModule> modules;

    public static final String ANALYZER = "analyzer";
    public static final String QUERY = "query";

    private PluginManager() {}

    public static PluginManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PluginManager();
        }
        return INSTANCE;
    }

    public Map<String, CoreComponent> getComponents() {
        return components;
    }

    public Map<String, MetadataModule> getModules() {
        return modules;
    }

    /**
     * Load the components for the search service.
     * All components must implement the CoreComponent interface.
     * @return Loaded components
     */
    public Map<String, CoreComponent> loadComponents() {

        LOGGER.info("Loading components");

        components = new TreeMap<>();
        CoreConfig config = CoreConfig.getInstance();

        try {
            if (config.isPluginEnabled(ANALYZER)) {
                Class<?> pluginClass = Class.forName(CoreConfig.getInstance().getPluginClassname(ANALYZER));
                if (CoreComponent.class.isAssignableFrom(pluginClass)) {
                    AnalyzerComponent component = (AnalyzerComponent) pluginClass.getDeclaredConstructor().newInstance();
                    components.put("analyzer", component);
                }
            }
            if (config.isPluginEnabled(QUERY)) {
                Class<?> pluginClass = Class.forName(CoreConfig.getInstance().getPluginClassname(QUERY));
                if (CoreComponent.class.isAssignableFrom(pluginClass)) {
                    QueryComponent component = (QueryComponent) pluginClass.getDeclaredConstructor().newInstance();
                    components.put("query", component);
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            LOGGER.error("Error loading components", e);
        }

        LOGGER.info("Components loaded successfully: {}", components.keySet());
        return components;
    }

    /**
     * Load the modules which are enabled in the configuration file for the search service.
     * All modules must implement the MetadataModule interface.
     * @return Loaded modules
     */
    public Map<String, MetadataModule> loadModules() {

        LOGGER.info("Loading modules");

        modules = new TreeMap<>();
        CoreConfig config = CoreConfig.getInstance();

        try {
            for (Map.Entry<String, String> module : config.getPlugins().entrySet()) {
                Class<?> pluginClass = Class.forName(CoreConfig.getInstance().getPluginClassname(module.getKey()));
                if (MetadataModule.class.isAssignableFrom(pluginClass)) {
                    MetadataModule plugin = (MetadataModule) pluginClass.getDeclaredConstructor().newInstance();
                    modules.put(module.getKey(), plugin);
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            LOGGER.error("Error loading modules", e);
        }

        LOGGER.info("Modules loaded successfully: {}", modules.keySet());
        return modules;
    }
}