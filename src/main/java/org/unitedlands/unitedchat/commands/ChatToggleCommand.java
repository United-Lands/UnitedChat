package org.unitedlands.unitedchat.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.unitedchat.UnitedChat;
import org.unitedlands.unitedchat.managers.BroadcastManager;

import org.unitedlands.unitedchat.utils.Config;
import org.unitedlands.unitedchat.utils.Messages;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.component;

import java.util.List;

public class ChatToggleCommand implements CommandExecutor {

    private final UnitedChat plugin;
    private final List<String> chatFeatures;

    public ChatToggleCommand(UnitedChat plugin) {
        this.plugin = plugin;
        chatFeatures = Config.get().features();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                             @NotNull String @NotNull [] args) {

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("united.chat.admin")) {
                sender.sendMessage(plugin.getChatMessageManager().getMessage(Messages.NO_PERM));
                return false;
            }

            plugin.reloadConfig();
            Config.reload(plugin.getConfig());

            // Reapply runtime configuration
            Bukkit.getScheduler().cancelTasks(plugin);
            var intervalTicks = Config.get().broadcastInterval() * 60 * 20;
            new BroadcastManager(plugin).runTaskTimer(plugin, intervalTicks, intervalTicks);

            sender.sendMessage(plugin.getChatMessageManager().getMessage(Messages.RELOADED));
            return true;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("quiz")) {
            if (args[1].equalsIgnoreCase("force")) {
                if (!sender.hasPermission("united.chat.admin")) {
                    sender.sendMessage(plugin.getChatMessageManager().getMessage(Messages.NO_PERM));
                    return true;
                }
                sender.sendMessage(plugin.getChatMessageManager().getMessage(Messages.QUIZ_FORCED));
                plugin.getQuizManager().forceNext();
                return true;
            }
            if (args[1].equalsIgnoreCase("toggle") && args.length == 3) {
                if (!sender.hasPermission("united.chat.admin")) {
                    sender.sendMessage(plugin.getChatMessageManager().getMessage(Messages.NO_PERM));
                    return true;
                }
                var on = args[2].equalsIgnoreCase("on");
                plugin.getQuizManager().setEnabled(on);
                sender.sendMessage(plugin.getChatMessageManager().getMessage(Messages.QUIZ_TOGGLED,
                        component("toggle", text(on ? "enabled" : "disabled"))));
                return true;
            }
            if (args[1].equalsIgnoreCase("highscore")) {
                int page = args.length >= 3 ? parsePageArg(args[2]) : 1;
                plugin.getQuizManager().buildHighscore(page).forEach(sender::sendMessage);
                return true;
            }
        }

        if (!(sender instanceof Player player))
            return true;

        if (args.length == 0) {
            player.sendMessage(plugin.getChatMessageManager().getMessage(Messages.CHAT_TOGGLE_COMMAND));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reset")) {
            handleReset(player);
            return true;
        }

        if (args.length == 3) {
            var feature = args[1];
            if (!chatFeatures.contains(feature)) {
                player.sendMessage(plugin.getChatMessageManager().getMessage(Messages.INVALID_FEATURE));
                return false;
            }

            boolean toggleOn = args[2].equalsIgnoreCase("on");

            if (feature.equalsIgnoreCase("quiz")) {
                player.sendMessage(plugin.getChatMessageManager().getMessage(Messages.TOGGLED_FEATURE,
                        component("feature", text("Quizzes")),
                        component("toggle", text(toggleOn ? "on" : "off")))
                );
                toggleQuiz(player, toggleOn);
                return true;
            }

            plugin.getChatSettingsManager().setKeyValue(player, feature, toggleOn ? "on" : "off");

            player.sendMessage(plugin.getChatMessageManager().getMessage(Messages.TOGGLED_FEATURE,
                    component("feature", text(feature)),
                    component("toggle", text(toggleOn ? "on" : "off"))));
            return true;
        }

        player.sendMessage(plugin.getChatMessageManager().getMessage(Messages.CHAT_TOGGLE_COMMAND));
        return true;
    }


    private int parsePageArg(String arg) {
        try { return Math.max(1, Integer.parseInt(arg)); } catch (NumberFormatException e) { return 1; }
    }

    private void toggleQuiz(Player player, Boolean toggleOn) {
        plugin.getChatSettingsManager().setQuizEnabled(player, toggleOn);
        plugin.getQuizManager().toggleQuizFor(player, toggleOn);
    }

    private void handleReset(Player player) {
        plugin.getChatSettingsManager().removeKey(player, "gradient");
        plugin.getChatSettingsManager().removeKey(player, "gradient-enabled");

        plugin.getChatSettingsManager().removeKey(player, "chatrank");
        plugin.getChatSettingsManager().removeKey(player, "chatprefix");
        plugin.getChatSettingsManager().removeKey(player, "tabprefix");
        plugin.getChatSettingsManager().removeKey(player, "chatnation");
        plugin.getChatSettingsManager().removeKey(player, "tabnation");
        plugin.getChatSettingsManager().removeKey(player, "chattown");
        plugin.getChatSettingsManager().removeKey(player, "tabtown");
        plugin.getChatSettingsManager().removeKey(player, "quiz");

        plugin.getChatSettingsManager().setKeyValue(player, "chatrank", "on");
        plugin.getChatSettingsManager().setKeyValue(player, "chatprefix", "on");
        plugin.getChatSettingsManager().setKeyValue(player, "quiz", "on");

        // Legacy settings, remove those to keep everything clean
        plugin.getChatSettingsManager().removeKey(player, "prefixes");
        plugin.getChatSettingsManager().removeKey(player, "ranks");
        plugin.getChatSettingsManager().removeKey(player, "broadcasts");
        plugin.getChatSettingsManager().removeKey(player, "gradients");
        plugin.getChatSettingsManager().removeKey(player, "games");

        player.sendMessage(plugin.getChatMessageManager().getMessage(Messages.RESET));
    }

}
