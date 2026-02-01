package dev.nxms.worldclear.task;

import dev.nxms.worldclear.WorldClear;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

/**
 * Task that handles countdown messages before clearing.
 * Broadcasts countdown every second until clear.
 */
public class CountdownTask extends BukkitRunnable {

    private final WorldClear plugin;
    private int secondsRemaining;

    public CountdownTask(WorldClear plugin, int seconds) {
        this.plugin = plugin;
        this.secondsRemaining = seconds;
    }

    @Override
    public void run() {
        if (secondsRemaining <= 0) {
            // Execute clear
            int count = plugin.getClearManager().clearItems();

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("count", String.valueOf(count));
            plugin.getMessageManager().broadcast("clear-success", placeholders);

            cancel();
            return;
        }

        // Broadcast countdown message
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("seconds", String.valueOf(secondsRemaining));
        plugin.getMessageManager().broadcast("clear-countdown", placeholders);

        secondsRemaining--;
    }
}