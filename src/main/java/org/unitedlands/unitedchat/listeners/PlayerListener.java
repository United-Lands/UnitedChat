package org.unitedlands.unitedchat.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.unitedlands.unitedchat.utils.Config;
import org.unitedlands.unitedchat.utils.Formatter;
import org.unitedlands.unitedchat.UnitedChat;

import com.palmergames.bukkit.TownyChat.events.AsyncChatHookEvent;

public class PlayerListener implements Listener {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Formatter formatter = new Formatter();
    private final UnitedChat plugin;

    public PlayerListener(UnitedChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        var motd = player.hasPlayedBefore()
                ? Config.get().motd()
                : Config.get().firstJoinMotd();

        motd.forEach(str -> player.sendMessage(miniMessage.deserialize(PlaceholderAPI.setPlaceholders(player, str))));
    }

    @EventHandler
    public void onChat(AsyncChatHookEvent event) {
        
        Player player = event.getPlayer();

        String message = event.getMessage().replace("§f", ""); // remove weird color
        String finalizedMessage = formatter.finalizeMessage(player, message);

        if (plugin.getChatSettingsManager().isGradientEnabled(player)) {
            event.setMessage(formatter.gradientMessage(finalizedMessage, plugin.getChatSettingsManager().getGradient(player)));
            return;
        }
        event.setMessage(formatter.colorMessage(finalizedMessage));
        event.setMessage(message);
    }

}
