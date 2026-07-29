package org.unitedlands.unitedchat.commands;

import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.unitedchat.UnitedChat;
import org.unitedlands.unitedchat.managers.ChatMessageManager;
import org.unitedlands.unitedchat.managers.ChatSettingsManager;
import org.unitedlands.unitedchat.utils.Config;
import org.unitedlands.unitedchat.utils.Messages;

public class GradientCommand implements CommandExecutor {

    private final UnitedChat plugin;
    private final ChatMessageManager messageManager;
    private final ChatSettingsManager settingsManager;

    public GradientCommand(UnitedChat plugin, ChatMessageManager messageManager, ChatSettingsManager settingsManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.settingsManager = settingsManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(messageManager.getMessage(Messages.GRADIENT_COMMAND));
            return false;
        }

        if (!(sender instanceof Player player)) {
            return false;
        }

        if (!player.hasPermission("united.chat.gradient")) {
            player.sendMessage(messageManager.getMessage(Messages.NO_PERM));
            return false;
        }

        switch (args[0]) {
            case "info" -> handleGradientInfo(player);
            case "toggle" -> handleGradientToggle(player, args);
            case "set" -> handleSetGradient(player, args);
        }

        return false;
    }

    private void handleGradientInfo(Player player) {
        String gradient = settingsManager.getGradient(player).replace(":", "\n- ");
        TextReplacementConfig gradientPlaceholder = TextReplacementConfig.builder().match("<gradient>")
                .replacement(gradient).build();
        player.sendMessage(messageManager.getMessage(Messages.CURRENT_GRADIENT).replaceText(gradientPlaceholder));
    }

    private void handleGradientToggle(Player player, String[] args) {
        if (args.length != 2)
            return;

        switch (args[1]) {
            case "on" -> toggleGradient(player, true);
            case "off" -> toggleGradient(player, false);
        }
    }

    private void handleSetGradient(Player player, String[] args) {

        if (args.length != 2) {
            player.sendMessage(messageManager.getMessage(Messages.GRADIENT_SET_COMMAND));
            return;
        }

        if (Config.get().presets().containsKey(args[1])) {
            setGradientPreset(player, args[1]);
            return;
        }

        if (args[1].contains("#")) {
            if (!player.hasPermission("united.chat.gradient.all"))
                return;

            settingsManager.setGradient(player, args[1]);
            player.sendMessage(messageManager.getMessage(Messages.GRADIENT_CHANGED));
        } else {
            player.sendMessage(messageManager.getMessage(Messages.GRADIENT_SET_COMMAND));
        }
    }

    private void setGradientPreset(Player player, String presetName) {
        if (Config.get().presets().get(presetName) == null) {
            player.sendMessage(messageManager.getMessage(Messages.GRADIENT_UNKNOWN_PRESET));
            return;
        }

        if (player.hasPermission("united.chat.gradient." + presetName)) {
            String preset = Config.get().presets().get(presetName).toString();
            settingsManager.setGradient(player, preset);
            player.sendMessage(messageManager.getMessage(Messages.GRADIENT_CHANGED));
        } else {
            player.sendMessage(messageManager.getMessage(Messages.NO_PERM));
        }
    }

    private void toggleGradient(Player player, Boolean enable) {
        if (enable) {
            if (!settingsManager.isGradientEnabled(player)) {
                settingsManager.setGradientEnabled(player, true);
                player.sendMessage(messageManager.getMessage(Messages.GRADIENT_ON));
            } else {
                player.sendMessage(messageManager.getMessage(Messages.GRADIENT_IS_ON));
            }
        } else {
            if (settingsManager.isGradientEnabled(player)) {
                settingsManager.setGradientEnabled(player, false);
                player.sendMessage(messageManager.getMessage(Messages.GRADIENT_OFF));
            } else {
                player.sendMessage(messageManager.getMessage(Messages.GRADIENT_IS_OFF));
            }
        }

    }

}
