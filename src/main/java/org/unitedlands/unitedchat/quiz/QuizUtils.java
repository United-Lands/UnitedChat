package org.unitedlands.unitedchat.quiz;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.unitedlands.unitedchat.UnitedChat;
import org.unitedlands.unitedchat.utils.Config;
import org.unitedlands.utils.DiscordService;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public final class QuizUtils {

    public static final MiniMessage   MM      = MiniMessage.miniMessage();
    public static final List<String>  CIRCLED = List.of(
            "Ⓐ","Ⓑ","Ⓒ","Ⓓ","Ⓔ","Ⓕ","Ⓖ","Ⓗ","Ⓘ","Ⓙ","Ⓚ","Ⓛ","Ⓜ",
            "Ⓝ","Ⓞ","Ⓟ","Ⓠ","Ⓡ","Ⓢ","Ⓣ","Ⓤ","Ⓥ","Ⓦ","Ⓧ","Ⓨ","Ⓩ"
    );
    public static final int CD_BARS = 32;

    public static boolean isPlausibleMcAnswer(String raw, Question question) {
        var input = raw.toLowerCase(Locale.ROOT).strip().replaceAll("[).]+$", "");
        if (input.isBlank()) return false;

        if (input.length() == 1) {
            var idx = Character.toUpperCase(input.charAt(0)) - 'A';
            if (idx >= 0 && idx < question.choices().size()) return true;
        }

        if (CIRCLED.contains(input)) return true;

        return question.choices().stream().anyMatch(c -> c.equalsIgnoreCase(input));
    }

    public static boolean isCorrect(String raw, Question question) {
        var input = raw.toLowerCase(Locale.ROOT)
                .strip()
                .replaceAll("[).]+$", "");

        if (input.isBlank())
            return false;

        return switch (question.type()) {

            case MULTIPLE_CHOICE -> {
                if (input.length() == 1) {
                    var idx = Character.toUpperCase(input.charAt(0)) - 'A';
                    if (idx >= 0 && idx < question.choices().size())
                        yield question.choices().get(idx).equalsIgnoreCase(question.correct().getFirst());
                }

                var ci = CIRCLED.indexOf(input);
                if (ci >= 0 && ci < question.choices().size())
                    yield question.choices().get(ci).equalsIgnoreCase(question.correct().getFirst());

                yield matchesCorrect(input, question);
            }

            case OPEN_TEXT, MATH_FORMULA, WORD_SCRAMBLE -> matchesCorrect(input, question);

        };
    }

    private static boolean matchesCorrect(String input, Question question) {
        return question.correct().stream()
                .anyMatch(c -> c.equalsIgnoreCase(input));
    }

    public static List<Component> buildMessage(Question question) {
        var cfg   = Config.get();
        var lines = new ArrayList<Component>();

        lines.add(MM.deserialize(cfg.quizHeader()));

        wordWrap(question.question()).forEach(l -> lines.add(MM.deserialize(" " + l)));
        lines.add(Component.empty());

        if (question.type() == QuestionType.MULTIPLE_CHOICE) {
            question.choices().forEach(choice -> lines.add(MM.deserialize(cfg.quizChoiceFormat()
                    .replace("%letter%", CIRCLED.get(question.choices().indexOf(choice)))
                    .replace("%choice%", choice))));

            lines.add(Component.empty());
        }

        lines.add(MM.deserialize(cfg.quizAnswerPrompt()));
        lines.add(MM.deserialize(cfg.quizFooter()));
        return lines;
    }

    public static Component cooldownBar(long remaining, long totalMs) {
        var green = Math.min(CD_BARS, Math.round((1f - remaining / (float) totalMs) * CD_BARS));
        var sb    = new StringBuilder();

        if (green > 0)       sb.append("<green>").append("|".repeat(green)).append("</green>");
        if (green < CD_BARS) sb.append("<dark_gray>").append("|".repeat(CD_BARS - green)).append("</dark_gray>");

        return MM.deserialize(Config.get().quizCooldownBar().replace("%bar%", sb.toString()));
    }

    public static BossBar createQuizBossBar() {
        return BossBar.bossBar(
                MM.deserialize(Config.get().quizBossBarTitle()),
                1f,
                BossBar.Color.GREEN,
                BossBar.Overlay.NOTCHED_12
        );
    }

    public static List<String> wordWrap(String text) {
        var result  = new ArrayList<String>();
        var line    = new StringBuilder();
        var lineLen = 0;
        var limit   = Config.get().quizWordWrapLength();

        for (var word : text.split("\\s+")) {
            var visibleLen = PlainTextComponentSerializer.plainText().serialize(MM.deserialize(word)).length();
            var newLen     = lineLen + (line.isEmpty() ? 0 : 1) + visibleLen;

            if (newLen > limit && !line.isEmpty()) {
                result.add(line.toString());
                line.setLength(0);
                lineLen = 0;
            }

            if (!line.isEmpty()) { line.append(' '); lineLen++; }
            line.append(word);
            lineLen += visibleLen;
        }

        if (!line.isEmpty()) result.add(line.toString());
        return result;
    }

    public static List<Player> getQuizEnabledPlayers(UnitedChat plugin) {
        return plugin.getServer().getOnlinePlayers().stream()
                .map(Player.class::cast)
                .filter(p -> plugin.getChatSettingsManager().isQuizEnabled(p))
                .toList();
    }

    public static void sendDiscordAnnouncement(Player winner, String answer, Question question, UnitedChat plugin) {
        var cfg = Config.get();
        var webhookUrl = cfg.quizDiscordWebhookUrl();
        if (webhookUrl.isBlank() || !webhookUrl.startsWith("https://"))
            return;

        var discordMsg = cfg.quizDiscordAnnouncement()
                .replace("%name%", winner != null ? winner.getName() : "No one")
                .replace("%answer%", answer)
                .replace("%question%", plainQuestion(question));

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> DiscordService.sendDiscordEmbed(
                webhookUrl,
                "{\"embeds\":[{\"title\":\"Quiz finished\",\"description\":\"" + discordMsg + "\",\"color\":{color}}]}",
                null,
                new HashMap<>(Map.of(
                        "color", winner != null ? "65280" : "16711680")
                )
        ));
    }

    public static Character getCorrectLetter(Question question) {
        if (question.type() != QuestionType.MULTIPLE_CHOICE)
            return null;

        var correct = question.correct().getFirst();
        var idx     = question.choices().indexOf(correct);
        if (idx < 0 || idx >= CIRCLED.size())
            return null;

        return CIRCLED.get(idx).charAt(0);
    }

    private static String plainQuestion(Question question) {
        return PlainTextComponentSerializer.plainText().serialize(MM.deserialize(question.question()));
    }

    public static ArrayList<Question> loadQuestions(UnitedChat plugin) {
        var file = plugin.getDataFolder().toPath().resolve("quiz/questions.yml").toFile();
        if (!file.exists())
            plugin.saveResource("quiz/questions.yml", false);

        try (var in = new FileInputStream(file)) {
            List<Map<String, Object>> raw = new Yaml().load(in);

            return raw.stream()
                    .map(m -> {
                        try {
                            return Question.fromMap(m);
                        } catch (Exception e) {
                            plugin.getLogger().warning("Skipping invalid question: " + e.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load questions.yml: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static List<String> loadWords(UnitedChat plugin) {
        var file = new File(plugin.getDataFolder(), "quiz/words.txt");
        try (var reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            return reader.lines()
                    .map(String::trim)
                    .filter(w -> w.length() >= 4)
                    .distinct()
                    .toList();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load words.txt: " + e.getMessage());
            return List.of();
        }
    }

    public static String buildConsoleLog(Question question) {
        var plain = PlainTextComponentSerializer.plainText().serialize(MM.deserialize(question.question()));
        var log   = new StringBuilder(question.type().toString()).append(" | ").append(plain);

        switch (question.type()) {

            case MULTIPLE_CHOICE -> {
                var choices = question.choices();
                for (var i = 0; i < choices.size(); i++)
                    log.append(" ").append((char) (0x24B6 + i)).append(" ").append(choices.get(i));
                log.append(" | → | ").append(question.correct().getFirst());
            }

            case OPEN_TEXT -> log.append(" | → | ").append(String.join(", ", question.correct()));
            default        -> log.append(" | → | ").append(question.correct().getFirst());
        }
        return log.toString();
    }

}
