package dev.nxms.worldclear.command;

import dev.nxms.worldclear.WorldClear;
import dev.nxms.worldclear.manager.ClearManager;
import dev.nxms.worldclear.manager.ConfigManager;
import dev.nxms.worldclear.manager.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles all /worldclear command executions.
 * Delegates to appropriate methods based on subcommand.
 */
public class WorldClearCommand implements CommandExecutor {

    private final WorldClear plugin;
    private final ConfigManager configManager;
    private final MessageManager messageManager;
    private final ClearManager clearManager;

    public WorldClearCommand(WorldClear plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.messageManager = plugin.getMessageManager();
        this.clearManager = plugin.getClearManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        // Check base permission
        if (!sender.hasPermission("worldclear.command")) {
            messageManager.send(sender, "no-permission");
            return true;
        }

        // No arguments - show usage
        if (args.length == 0) {
            messageManager.send(sender, "usage");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "clear" -> handleClear(sender);
            case "on" -> handleOn(sender);
            case "off" -> handleOff(sender);
            case "reload" -> handleReload(sender);
            case "set" -> handleSet(sender, args);
            case "info" -> handleInfo(sender);
            default -> {
                messageManager.send(sender, "unknown-command");
                messageManager.send(sender, "usage");
            }
        }

        return true;
    }

    /**
     * Handles /worldclear clear command.
     */
    private void handleClear(CommandSender sender) {
        if (!sender.hasPermission("worldclear.clear")) {
            messageManager.send(sender, "no-permission");
            return;
        }

        clearManager.executeClearWithCountdown();
    }

    private void handleInfo(CommandSender sender) {
        if (!sender.hasPermission("worldclear.info")) {
            messageManager.send(sender, "no-permission");
            return;
        }

        if (configManager.isAutoClearEnabled()) {
            long remaining = clearManager.getTimeUntilNextClear();
            String time = configManager.formatTime(remaining);
            messageManager.send(sender, "status-enabled", "time", time);
        } else {
            messageManager.send(sender, "status-disabled");
        }
    }

    /**
     * Handles /worldclear on command.
     */
    private void handleOn(CommandSender sender) {
        if (!sender.hasPermission("worldclear.on")) {
            messageManager.send(sender, "no-permission");
            return;
        }

        if (configManager.isAutoClearEnabled()) {
            messageManager.send(sender, "already-enabled");
            return;
        }

        configManager.setAutoClearEnabled(true);
        clearManager.startAutoClear();
        messageManager.send(sender, "enabled");
    }

    /**
     * Handles /worldclear off command.
     */
    private void handleOff(CommandSender sender) {
        if (!sender.hasPermission("worldclear.off")) {
            messageManager.send(sender, "no-permission");
            return;
        }

        if (!configManager.isAutoClearEnabled()) {
            messageManager.send(sender, "already-disabled");
            return;
        }

        configManager.setAutoClearEnabled(false);
        clearManager.stopAutoClear();
        messageManager.send(sender, "disabled");
    }

    /**
     * Handles /worldclear reload command.
     */
    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("worldclear.reload")) {
            messageManager.send(sender, "no-permission");
            return;
        }

        if (plugin.reload()) {
            messageManager.send(sender, "reload-success");
        } else {
            messageManager.send(sender, "reload-failed");
        }
    }

    /**
     * Handles /worldclear set command.
     */
    private void handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("worldclear.set")) {
            messageManager.send(sender, "no-permission");
            return;
        }

        if (args.length < 2) {
            messageManager.send(sender, "set-usage");
            return;
        }

        String interval = args[1].toLowerCase();

        // Validate format
        if (!interval.matches("(?:\\d+d)?(?:\\d+h)?(?:\\d+m)?") || interval.isEmpty()) {
            messageManager.send(sender, "set-invalid-format");
            return;
        }

        // Validate minimum value
        if (!configManager.isValidInterval(interval)) {
            messageManager.send(sender, "set-invalid-value");
            return;
        }

        // Save and apply new interval
        configManager.setInterval(interval);

        // Restart auto clear with new interval if enabled
        if (configManager.isAutoClearEnabled()) {
            clearManager.stopAutoClear();
            clearManager.startAutoClear();
        }

        messageManager.send(sender, "set-success", "interval", interval);
    }
}