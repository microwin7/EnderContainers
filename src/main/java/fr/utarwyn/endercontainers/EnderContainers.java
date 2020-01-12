package fr.utarwyn.endercontainers;

import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.command.CommandManager;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.database.DatabaseManager;
import fr.utarwyn.endercontainers.dependency.DependenciesManager;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.hologram.HologramManager;
import fr.utarwyn.endercontainers.menu.MenuManager;
import fr.utarwyn.endercontainers.storage.StorageManager;
import fr.utarwyn.endercontainers.util.Updater;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;

/**
 * Main class of the plugin. Everything starts here.
 * The plugin is under license. Please see LICENSE file to have more info.
 *
 * @author Utarwyn
 * @since 1.0.0
 */
public class EnderContainers extends JavaPlugin {

    /**
     * The plugin prefix
     */
    public static final String PREFIX = "§8[§6EnderContainers§8] §7";

    /**
     * The Endercontainers instance
     */
    private static EnderContainers instance;

    /**
     * Allows to get the main instance of the plugin from all classes of the plugin.
     *
     * @return The EnderContainers plugin instance.
     */
    public static EnderContainers getInstance() {
        return EnderContainers.instance;
    }

    /**
     * Called when the plugin loads
     */
    @Override
    public void onEnable() {
        instance = this;

        // Load config files
        if (!Files.initConfiguration(this)) {
            this.getLogger().log(Level.SEVERE, "Cannot load the plugin's configuration. Please check the above log. Plugin loading failed.");
            return;
        }
        if (!Files.initLocale(this)) {
            this.getLogger().log(Level.SEVERE, "Cannot load the plugin's locale. Please check the above log. Plugin loading failed.");
            return;
        }

        // Load all managers
        Managers.register(this, CommandManager.class);
        Managers.register(this, MenuManager.class);
        Managers.register(this, DependenciesManager.class);
        Managers.register(this, DatabaseManager.class);
        Managers.register(this, StorageManager.class);
        Managers.register(this, EnderChestManager.class);
        Managers.register(this, BackupManager.class);
        Managers.register(this, HologramManager.class);
        Managers.register(this, Updater.class);

        // Registering commands
        Objects.requireNonNull(Managers.get(CommandManager.class)).registerCommands();

        // And load Metrics!
        new Metrics(this);
    }

    /**
     * Called when the plugin disables
     */
    @Override
    public void onDisable() {
        Managers.unregisterAll();
    }

}
