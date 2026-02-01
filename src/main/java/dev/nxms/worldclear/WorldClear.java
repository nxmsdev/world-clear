package dev.nxms.worldclear;

import dev.nxms.worldclear.command.WorldClearCommand;
import dev.nxms.worldclear.command.WorldClearTabCompleter;
import dev.nxms.worldclear.manager.ClearManager;
import dev.nxms.worldclear.manager.ConfigManager;
import dev.nxms.worldclear.manager.MessageManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for WorldClear.
 * Handles initialization and provides access to all managers.
 */
public class WorldClear extends JavaPlugin {

    private static WorldClear instance;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private ClearManager clearManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize managers in order of dependency
        this.configManager = new ConfigManager(this);
        this.messageManager = new MessageManager(this);
        this.clearManager = new ClearManager(this);

        // Register commands
        registerCommands();

        // Start auto clear if enabled in config
        if (configManager.isAutoClearEnabled()) {
            clearManager.startAutoClear();
        }

        getLogger().info("WorldClear has been enabled!");
    }

    @Override
    public void onDisable() {
        // Stop all tasks
        if (clearManager != null) {
            clearManager.stopAutoClear();
        }

        getLogger().info("WorldClear has been disabled!");
    }

    /**
     * Registers plugin commands and tab completers.
     */
    private void registerCommands() {
        PluginCommand command = getCommand("worldclear");
        if (command != null) {
            WorldClearCommand commandExecutor = new WorldClearCommand(this);
            command.setExecutor(commandExecutor);
            command.setTabCompleter(new WorldClearTabCompleter(this));
        }
    }

    /**
     * Reloads all plugin configurations and messages.
     *
     * @return true if reload was successful
     */
    public boolean reload() {
        try {
            configManager.reload();
            messageManager.reload();

            // Restart auto clear with new settings if enabled
            clearManager.stopAutoClear();
            if (configManager.isAutoClearEnabled()) {
                clearManager.startAutoClear();
            }

            return true;
        } catch (Exception e) {
            getLogger().severe("Failed to reload configuration: " + e.getMessage());
            return false;
        }
    }

    public static WorldClear getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public ClearManager getClearManager() {
        return clearManager;
    }
}