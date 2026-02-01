package dev.nxms.worldclear.manager;

import dev.nxms.worldclear.WorldClear;
import dev.nxms.worldclear.task.ClearTask;
import dev.nxms.worldclear.task.CountdownTask;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitTask;

/**
 * Manages the item clearing functionality.
 * Handles auto clear scheduling and manual clear execution.
 */
public class ClearManager {

    private final WorldClear plugin;
    private BukkitTask autoClearTask;
    private BukkitTask countdownTask;
    private long nextClearTime;

    public ClearManager(WorldClear plugin) {
        this.plugin = plugin;
    }

    /**
     * Starts the automatic clear scheduler.
     */
    public void startAutoClear() {
        stopAutoClear();

        long intervalSeconds = plugin.getConfigManager().getIntervalSeconds();
        if (intervalSeconds < 60) {
            plugin.getLogger().warning("Invalid interval. Auto clear not started.");
            return;
        }

        long intervalTicks = intervalSeconds * 20L;
        int countdownStart = plugin.getConfigManager().getCountdownStart();

        // Calculate initial delay (interval minus countdown)
        long initialDelay = intervalTicks - (countdownStart * 20L);
        if (initialDelay < 0) {
            initialDelay = intervalTicks;
        }

        // Update next clear time
        nextClearTime = System.currentTimeMillis() + (intervalSeconds * 1000L);

        // Schedule the repeating clear task
        autoClearTask = new ClearTask(plugin).runTaskTimer(plugin, initialDelay, intervalTicks);

        plugin.getLogger().info("Auto clear started with interval: " +
                plugin.getConfigManager().getIntervalString());
    }

    /**
     * Stops the automatic clear scheduler.
     */
    public void stopAutoClear() {
        if (autoClearTask != null) {
            autoClearTask.cancel();
            autoClearTask = null;
        }
        stopCountdown();
    }

    /**
     * Starts the countdown before clearing.
     */
    public void startCountdown() {
        stopCountdown();

        int countdownStart = plugin.getConfigManager().getCountdownStart();
        countdownTask = new CountdownTask(plugin, countdownStart).runTaskTimer(plugin, 0L, 20L);
    }

    /**
     * Stops any running countdown.
     */
    public void stopCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
    }

    /**
     * Clears all dropped items from all worlds.
     *
     * @return number of items cleared
     */
    public int clearItems() {
        int count = 0;

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item) {
                    entity.remove();
                    count++;
                }
            }
        }

        // Reset next clear time if auto clear is enabled
        if (plugin.getConfigManager().isAutoClearEnabled()) {
            long intervalSeconds = plugin.getConfigManager().getIntervalSeconds();
            nextClearTime = System.currentTimeMillis() + (intervalSeconds * 1000L);
        }

        return count;
    }

    /**
     * Executes an immediate clear with countdown.
     */
    public void executeClearWithCountdown() {
        startCountdown();
    }

    /**
     * Executes an immediate clear without countdown.
     *
     * @return number of items cleared
     */
    public int executeImmediateClear() {
        plugin.getMessageManager().broadcast("clear-now");
        return clearItems();
    }

    /**
     * Gets the time remaining until the next clear.
     *
     * @return time remaining in seconds
     */
    public long getTimeUntilNextClear() {
        if (!plugin.getConfigManager().isAutoClearEnabled() || autoClearTask == null) {
            return -1;
        }

        long remaining = (nextClearTime - System.currentTimeMillis()) / 1000L;
        return Math.max(0, remaining);
    }

    /**
     * Checks if auto clear is currently running.
     *
     * @return true if auto clear is active
     */
    public boolean isAutoClearRunning() {
        return autoClearTask != null;
    }
}