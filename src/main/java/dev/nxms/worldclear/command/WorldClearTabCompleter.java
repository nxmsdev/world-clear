package dev.nxms.worldclear.command;

import dev.nxms.worldclear.WorldClear;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides tab completion for WorldClear commands.
 * Hides commands from users without appropriate permissions.
 */
public class WorldClearTabCompleter implements TabCompleter {

    private final WorldClear plugin;

    // All subcommands with their required permissions
    private static final List<SubCommand> SUB_COMMANDS = Arrays.asList(
            new SubCommand("clear", "worldclear.clear"),
            new SubCommand("on", "worldclear.on"),
            new SubCommand("off", "worldclear.off"),
            new SubCommand("reload", "worldclear.reload"),
            new SubCommand("set", "worldclear.set"),
            new SubCommand("info", "worldclear.info")
    );

    // Example intervals for tab completion
    private static final List<String> INTERVAL_EXAMPLES = Arrays.asList(
            "<interval>", "10m", "30m", "1h", "2h", "6h", "12h", "1d", "1d12h"
    );

    public WorldClearTabCompleter(WorldClear plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {

        // Check base permission
        if (!sender.hasPermission("worldclear.command")) {
            return new ArrayList<>();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First argument - show available subcommands
            String input = args[0].toLowerCase();

            completions = SUB_COMMANDS.stream()
                    .filter(sub -> sender.hasPermission(sub.permission()))
                    .map(SubCommand::name)
                    .filter(name -> name.startsWith(input))
                    .collect(Collectors.toList());

        } else if (args.length == 2) {
            // Second argument - depends on subcommand
            String subCommand = args[0].toLowerCase();
            String input = args[1].toLowerCase();

            if (subCommand.equals("set") && sender.hasPermission("worldclear.set")) {
                completions = INTERVAL_EXAMPLES.stream()
                        .filter(interval -> interval.startsWith(input))
                        .collect(Collectors.toList());
            }
        }

        return completions;
    }

    /**
     * Record for storing subcommand data.
     */
    private record SubCommand(String name, String permission) {
    }
}