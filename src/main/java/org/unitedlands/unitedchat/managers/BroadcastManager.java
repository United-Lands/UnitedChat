package org.unitedlands.unitedchat.managers;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;
import org.unitedlands.unitedchat.UnitedChat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BroadcastManager extends BukkitRunnable {

    private final UnitedChat plugin;
    private final List<List<String>> broadcasts;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Random random = new Random();

    private final String soundName;
    private final float soundVolume;
    private final float soundPitch;
    private final boolean playSound;

    public BroadcastManager(UnitedChat plugin) {
        this.plugin = plugin;
        this.broadcasts = loadBroadcasts();

        String configuredSound = plugin.getConfig().getString("broadcaster.sound.name", "").trim();
        this.soundVolume = (float) plugin.getConfig().getDouble("broadcaster.sound.volume", 1.0);
        this.soundPitch = (float) plugin.getConfig().getDouble("broadcaster.sound.pitch", 1.2);

        if (configuredSound.isEmpty()) {
            this.soundName = null;
            this.playSound = false;
        } else {
            // Validate the sound name using the registry.
            NamespacedKey key = NamespacedKey.fromString(configuredSound);
            Sound foundSound = (key != null) ? Registry.SOUNDS.get(key) : null;
            if (foundSound != null) {
                this.soundName = configuredSound;
                this.playSound = true;
            } else {
                plugin.getLogger().warning("[BroadcastManager] Invalid sound name in config: '" + configuredSound + "'");
                this.soundName = null;
                this.playSound = false;
            }
        }
    }

    private List<List<String>> loadBroadcasts() {
        List<List<String>> messages = new ArrayList<>();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("broadcaster.messages");

        if (section != null) {
            for (String key : section.getKeys(false)) {
                List<String> lines = section.getStringList(key);
                if (!lines.isEmpty()) {
                    messages.add(lines);
                }
            }
        }
        return messages;
    }

    @Override
    public void run() {
        if (broadcasts.isEmpty()) return;

        List<String> broadcast = broadcasts.get(random.nextInt(broadcasts.size()));
        for (String line : broadcast) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                player.sendMessage(miniMessage.deserialize(line));
                if (playSound) {
                    player.playSound(player.getLocation(), soundName, SoundCategory.MASTER, soundVolume, soundPitch);
                }
            });
        }
    }
}
