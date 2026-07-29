package org.unitedlands.unitedchat.commands.tabcompleters;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.Nullable;
import org.unitedlands.unitedchat.UnitedChat;
import org.unitedlands.unitedchat.utils.Config;

public class GradientCommandTabCompleter implements TabCompleter {

    private final UnitedChat plugin;

    private final List<String> gradientCommands = Arrays.asList("toggle", "set");
    private final List<String> gradientToggleCommands = Arrays.asList("off", "on");

    public GradientCommandTabCompleter(UnitedChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        List<String> options = null;
        String input = args[args.length - 1];

        switch (args.length) {
            case 1 -> options = gradientCommands;
            case 2 -> {
                if (args[0].equalsIgnoreCase("toggle"))
                    options = gradientToggleCommands;
                else if (args[0].equalsIgnoreCase("set"))
                    options = Config.get().presets().keySet().stream().toList();
            }
        }

        List<String> completions = null;
        if (options != null)
            completions = options.stream()
                    .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
                    .sorted()
                    .toList();

        return completions;
    }

}
