package net.nightzy.echestplus.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides tab completion for the EchestPlus admin command.
 * Suggests subcommands, player names, and size values.
 */
public class AdminCommandTabCompleter implements TabCompleter {

    // ============================================================
    // Constants
    // ============================================================

    private static final List<String> SUBCOMMANDS =
            Arrays.asList("getItem", "open", "size", "reload"); // Available admin subcommands

    private static final List<String> LINES =
            Arrays.asList("3", "4", "5", "6"); // Valid line counts for the size command

    // ============================================================
    // Tab Completion Logic
    // ============================================================

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        // No permission -> no suggestions
        if (!sender.hasPermission("echestplus.admin")) {
            return new ArrayList<>();
        }

        // First argument -> subcommands
        if (args.length == 1) {
            return getMatches(args[0], SUBCOMMANDS);
        }

        // Second argument -> online player names for getItem, open, and size
        if (args.length == 2 &&
                (args[0].equalsIgnoreCase("getItem") ||
                 args[0].equalsIgnoreCase("open") ||
                 args[0].equalsIgnoreCase("size"))) {

            List<String> players = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                players.add(p.getName());
            }
            return getMatches(args[1], players);
        }

        // Third argument for "size" -> available line values
        if (args.length == 3 && args[0].equalsIgnoreCase("size")) {
            return getMatches(args[2], LINES);
        }

        return new ArrayList<>();
    }

    // ============================================================
    // Utility Methods
    // ============================================================

    /**
     * Filters available options based on current input
     *
     * @param input   The current user input
     * @param options The list of possible options
     * @return Filtered list of matching options
     */
    private List<String> getMatches(String input, List<String> options) {

        List<String> matches = new ArrayList<>();
        String lower = input.toLowerCase();

        for (String option : options) {
            if (option.toLowerCase().startsWith(lower)) {
                matches.add(option);
            }
        }

        return matches;
    }
}
