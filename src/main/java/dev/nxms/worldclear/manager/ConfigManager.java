package dev.nxms.worldclear.manager;

import dev.nxms.worldclear.WorldClear;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages plugin configuration loading and access.
 * Handles config.yml operations and interval parsing.
 */
public class ConfigManager {

    private final WorldClear plugin;
    private FileConfiguration config;

    // Pattern for parsing time intervals (e.g., 1d2h30m, 30m, 2h)
    private static final Pattern INTERVAL_PATTERN = Pattern.compile(
            "(?:([0-9]+)d)?(?:([0-9]+)h)?(?:([0-9]+)m)?",
            Pattern.CASE_INSENSITIVE
    );

    public ConfigManager(WorldClear plugin) {
        this.plugin = plugin;
        reload();
    }

    /**
     * Reloads the configuration from disk.
     */
    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    /**
     * Gets the configured language code.
     *
     * @return language code (e.g., "en", "pl")
     */
    public String getLanguage() {
        return config.getString("language", "en");
    }

    /**
     * Checks if auto clear is enabled.
     *
     * @return true if auto clear is enabled
     */
    public boolean isAutoClearEnabled() {
        return config.getBoolean("auto-clear.enabled", true);
    }

    /**
     * Sets the auto clear enabled state and saves to config.
     *
     * @param enabled new enabled state
     */
    public void setAutoClearEnabled(boolean enabled) {
        config.set("auto-clear.enabled", enabled);
        plugin.saveConfig();
    }

    /**
     * Gets the clear interval in seconds.
     *
     * @return interval in seconds
     */
    public long getIntervalSeconds() {
        String interval = config.getString("auto-clear.interval", "30m");
        return parseInterval(interval);
    }

    /**
     * Gets the raw interval string from config.
     *
     * @return interval string (e.g., "30m", "1h")
     */
    public String getIntervalString() {
        return config.getString("auto-clear.interval", "30m");
    }

    /**
     * Sets the clear interval and saves to config.
     *
     * @param interval interval string
     */
    public void setInterval(String interval) {
        config.set("auto-clear.interval", interval);
        plugin.saveConfig();
    }

    /**
     * Gets the countdown start time in seconds.
     *
     * @return countdown start seconds
     */
    public int getCountdownStart() {
        return config.getInt("countdown.start-at", 10);
    }

    /**
     * Parses a time interval string to seconds.
     * Supports formats: Xd (days), Xh (hours), Xm (minutes)
     *
     * @param interval interval string to parse
     * @return total seconds, or -1 if invalid
     */
    public long parseInterval(String interval) {
        if (interval == null || interval.isEmpty()) {
            return -1;
        }

        Matcher matcher = INTERVAL_PATTERN.matcher(interval.trim());
        if (!matcher.matches()) {
            return -1;
        }

        long totalSeconds = 0;

        // Parse days
        String days = matcher.group(1);
        if (days != null) {
            totalSeconds += Long.parseLong(days) * 86400;
        }

        // Parse hours
        String hours = matcher.group(2);
        if (hours != null) {
            totalSeconds += Long.parseLong(hours) * 3600;
        }

        // Parse minutes
        String minutes = matcher.group(3);
        if (minutes != null) {
            totalSeconds += Long.parseLong(minutes) * 60;
        }

        return totalSeconds > 0 ? totalSeconds : -1;
    }

    /**
     * Validates if an interval string is valid and at least 1 minute.
     *
     * @param interval interval string to validate
     * @return true if valid
     */
    public boolean isValidInterval(String interval) {
        long seconds = parseInterval(interval);
        return seconds >= 60;
    }

    /**
     * Formats seconds into a human-readable time string.
     *
     * @param totalSeconds total seconds to format
     * @return formatted time string (e.g., "1d 2h 30m")
     */
    public String formatTime(long totalSeconds) {
        if (totalSeconds < 0) {
            return "0m";
        }

        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();

        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("m ");
        }
        if (seconds > 0 && days == 0 && hours == 0) {
            sb.append(seconds).append("s");
        }

        return sb.toString().trim();
    }
}