package dev.nxms.worldclear.task;

import dev.nxms.worldclear.WorldClear;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Task that runs periodically to initiate item clearing.
 * Triggers countdown before actual clearing.
 */
public class ClearTask extends BukkitRunnable {

    private final WorldClear plugin;
    private boolean firstRun = true;

    public ClearTask(WorldClear plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // On first run, just start countdown (task was scheduled with initial delay)
        // On subsequent runs, we're at the start of countdown period
        plugin.getClearManager().startCountdown();
    }
}