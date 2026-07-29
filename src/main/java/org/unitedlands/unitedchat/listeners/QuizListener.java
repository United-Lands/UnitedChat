package org.unitedlands.unitedchat.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.unitedlands.unitedchat.UnitedChat;
import org.unitedlands.unitedchat.quiz.QuizUtils;
import org.unitedlands.unitedchat.utils.Config;

public class QuizListener implements Listener {

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    private final UnitedChat  plugin;

    public QuizListener(UnitedChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        var quizManager = plugin.getQuizManager();
        if (!quizManager.isActive()) return;

        var text = PLAIN.serialize(event.message());
        Bukkit.getScheduler().runTask(plugin, () -> quizManager.checkAnswer(event.getPlayer(), text));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (QuizUtils.getQuizEnabledPlayers(plugin).size() < Config.get().quizMinPlayers())
                return;

            plugin.getQuizManager().start();
            plugin.getQuizManager().sendQuizQuestionTo(event.getPlayer());
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (QuizUtils.getQuizEnabledPlayers(plugin).size() < Config.get().quizMinPlayers())
                plugin.getQuizManager().stop();
        });
    }

}
