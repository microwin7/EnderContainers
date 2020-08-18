package fr.utarwyn.endercontainers.dependency;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Resolve a dependency instance with a custom version matching.
 *
 * @author Utarwyn <maxime.malgorn@laposte.net>
 * @since 2.2.0
 */
public class DependencyResolver {

    /**
     * Bukkit plugin manager
     */
    private final PluginManager pluginManager;

    /**
     * Patterns to match dependency's versions
     */
    private final Map<Pattern, Class<? extends Dependency>> patterns;

    /**
     * Name of the dependency instance to build
     */
    private String name;

    /**
     * Construct a new dependency resolver.
     *
     * @param pluginManager Bukkit plugin manager
     */
    public DependencyResolver(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
        this.patterns = new HashMap<>();
    }

    /**
     * Set the name of the resolved dependency.
     * Should match the plugin exact name.
     *
     * @param name name of the dependency
     * @return this instance
     */
    public DependencyResolver name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Match a specific dependency class from a custom version pattern.
     *
     * @param expression pattern to match the dependency's version
     * @param clazz      class to use for for the targeted version
     * @return this instance
     */
    public DependencyResolver matchVersion(String expression, Class<? extends Dependency> clazz) {
        this.patterns.put(Pattern.compile(expression), clazz);
        return this;
    }

    /**
     * Use a dependency class for any versions of the plugin.
     *
     * @param clazz class to use for with all versions
     * @return this instance
     */
    public DependencyResolver use(Class<? extends Dependency> clazz) {
        return this.matchVersion(".*", clazz);
    }

    /**
     * Resolve the dependency if the concerned plugin is loaded and a matcher
     * has targeted the plugin's version from the plugin manager.
     *
     * @return resolved dependency instance if present
     */
    public Optional<Dependency> resolve() {
        if (this.name == null) {
            throw new NullPointerException("Dependency name cannot be null!");
        }
        if (this.patterns.isEmpty()) {
            throw new NullPointerException("A builder must have at least one matcher!");
        }

        if (this.pluginManager.isPluginEnabled(this.name)) {
            return this.constructInstance(this.pluginManager.getPlugin(this.name));
        }

        return Optional.empty();
    }

    /**
     * Construct a dependency instance from patterns and version of the loaded plugin.
     *
     * @param plugin loaded Bukkit plugin instance
     * @return dependency instance if present
     */
    private Optional<Dependency> constructInstance(Plugin plugin) {
        String pluginVersion = plugin.getDescription().getVersion();

        return this.patterns.entrySet().stream()
                .filter(entry -> entry.getKey().matcher(pluginVersion).find())
                .map(Map.Entry::getValue)
                .findFirst()
                .map(clazz -> {
                    try {
                        return clazz.getDeclaredConstructor(Plugin.class).newInstance(plugin);
                    } catch (ReflectiveOperationException e) {
                        return null;
                    }
                });
    }

}
