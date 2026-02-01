package dev.nxms.worldclear.manager;

import dev.nxms.worldclear.WorldClear;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages plugin messages with support for modal prefixes.
 * Handles language file loading with fallback to English.
 */
public class MessageManager {

    private final WorldClear plugin;
    private FileConfiguration messages;
    private final Map<String, String> prefixes;

    // Pattern for matching prefix placeholders like {prefix-info}
    private static final Pattern PREFIX_PATTERN = Pattern.compile("\\{(prefix-[^}]+)}");

    // Serializer for converting legacy color codes to Adventure components
    private static final LegacyComponentSerializer LEGACY_SERIALIZER =
            LegacyComponentSerializer.legacyAmpersand();

    public MessageManager(WorldClear plugin) {
        this.plugin = plugin;
        this.prefixes = new HashMap<>();
        reload();
    }

    /**
     * Reloads messages from the appropriate language file.
     * Falls back to English if the selected language file doesn't exist.
     */
    public void reload() {
        prefixes.clear();

        String language = plugin.getConfigManager().getLanguage();
        String fileName = "messages_" + language + ".yml";
        File messagesFile = new File(plugin.getDataFolder(), fileName);

        // Try to load the selected language file
        if (messagesFile.exists()) {
            messages = YamlConfiguration.loadConfiguration(messagesFile);
            plugin.getLogger().info("Loaded messages from " + fileName);
        } else {
            // Fallback to English
            if (!language.equals("en")) {
                plugin.getLogger().warning("Language file " + fileName + " not found. Falling back to English.");
            }
            loadDefaultMessages();
        }

        // Load all prefixes from the messages file
        loadPrefixes();
    }

    /**
     * Loads the default English messages from resources.
     */
    private void loadDefaultMessages() {
        File defaultFile = new File(plugin.getDataFolder(), "messages_en.yml");

        if (!defaultFile.exists()) {
            plugin.saveResource("messages_en.yml", false);
        }

        messages = YamlConfiguration.loadConfiguration(defaultFile);

        // Load defaults from jar as fallback
        InputStream defaultStream = plugin.getResource("messages_en.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8)
            );
            messages.setDefaults(defaultConfig);
        }
    }

    /**
     * Loads all prefix definitions from the messages file.
     * Prefixes are keys starting with "prefix-".
     */
    private void loadPrefixes() {
        for (String key : messages.getKeys(false)) {
            if (key.startsWith("prefix-")) {
                String value = messages.getString(key, "");
                prefixes.put(key, value);
            }
        }
    }

    /**
     * Gets a raw message from the messages file.
     *
     * @param key message key
     * @return raw message string
     */
    public String getRaw(String key) {
        return messages.getString(key, "&cMissing message: " + key);
    }

    /**
     * Gets a message with prefixes applied.
     *
     * @param key message key
     * @return message with prefixes replaced
     */
    public String get(String key) {
        String message = getRaw(key);
        return applyPrefixes(message);
    }

    /**
     * Gets a message with prefixes and placeholders applied.
     *
     * @param key message key
     * @param placeholders map of placeholder names to values
     * @return formatted message
     */
    public String get(String key, Map<String, String> placeholders) {
        String message = get(key);
        return applyPlaceholders(message, placeholders);
    }

    /**
     * Applies modal prefixes to a message.
     * Replaces {prefix-name} with the corresponding prefix value.
     *
     * @param message message to process
     * @return message with prefixes applied
     */
    private String applyPrefixes(String message) {
        Matcher matcher = PREFIX_PATTERN.matcher(message);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String prefixKey = matcher.group(1);
            String prefixValue = prefixes.getOrDefault(prefixKey, "");
            matcher.appendReplacement(result, Matcher.quoteReplacement(prefixValue));
        }

        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Applies placeholders to a message.
     *
     * @param message message to process
     * @param placeholders map of placeholder names to values
     * @return message with placeholders replaced
     */
    private String applyPlaceholders(String message, Map<String, String> placeholders) {
        String result = message;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    /**
     * Converts a message string to an Adventure Component.
     *
     * @param message message to convert
     * @return Adventure Component
     */
    public Component toComponent(String message) {
        return LEGACY_SERIALIZER.deserialize(message);
    }

    /**
     * Sends a message to a CommandSender.
     *
     * @param sender recipient
     * @param key message key
     */
    public void send(CommandSender sender, String key) {
        String message = get(key);
        sender.sendMessage(toComponent(message));
    }

    /**
     * Sends a message to a CommandSender with placeholders.
     *
     * @param sender recipient
     * @param key message key
     * @param placeholders map of placeholder names to values
     */
    public void send(CommandSender sender, String key, Map<String, String> placeholders) {
        String message = get(key, placeholders);
        sender.sendMessage(toComponent(message));
    }

    /**
     * Sends a message to a CommandSender with a single placeholder.
     *
     * @param sender recipient
     * @param key message key
     * @param placeholder placeholder name
     * @param value placeholder value
     */
    public void send(CommandSender sender, String key, String placeholder, String value) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(placeholder, value);
        send(sender, key, placeholders);
    }

    /**
     * Sends a message to a CommandSender with two placeholders.
     *
     * @param sender recipient
     * @param key message key
     * @param placeholder1 first placeholder name
     * @param value1 first placeholder value
     * @param placeholder2 second placeholder name
     * @param value2 second placeholder value
     */
    public void send(CommandSender sender, String key, String placeholder1, String value1,
                     String placeholder2, String value2) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(placeholder1, value1);
        placeholders.put(placeholder2, value2);
        send(sender, key, placeholders);
    }

    /**
     * Broadcasts a message to all online players.
     *
     * @param key message key
     */
    public void broadcast(String key) {
        String message = get(key);
        Component component = toComponent(message);
        Bukkit.getServer().sendMessage(component);
    }

    /**
     * Broadcasts a message to all online players with placeholders.
     *
     * @param key message key
     * @param placeholders map of placeholder names to values
     */
    public void broadcast(String key, Map<String, String> placeholders) {
        String message = get(key, placeholders);
        Component component = toComponent(message);
        Bukkit.getServer().sendMessage(component);
    }

    /**
     * Broadcasts a message to all online players with a single placeholder.
     *
     * @param key message key
     * @param placeholder placeholder name
     * @param value placeholder value
     */
    public void broadcast(String key, String placeholder, String value) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(placeholder, value);
        broadcast(key, placeholders);
    }
}