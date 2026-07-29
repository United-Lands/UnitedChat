package org.unitedlands.unitedchat.quiz;

import org.unitedlands.unitedchat.utils.Config;

import java.io.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class QuizGenerator {

    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    public static QuestionType pickWeightedType() {
        var cfg   = Config.get();
        var total = cfg.quizWeightMc() + cfg.quizWeightText() + cfg.quizWeightMath() + cfg.quizWeightWord();
        var roll  = RANDOM.nextInt(Math.max(total, 1));

        var weight = cfg.quizWeightMc();   if (roll < weight) return QuestionType.MULTIPLE_CHOICE;
        weight    += cfg.quizWeightText(); if (roll < weight) return QuestionType.OPEN_TEXT;
        weight    += cfg.quizWeightMath(); if (roll < weight) return QuestionType.MATH_FORMULA;
        return QuestionType.WORD_SCRAMBLE;
    }

    public static Question generateMath() {
        var op = MathOperator.values()[RANDOM.nextInt(MathOperator.values().length)];
        int a, b;

        if (op == MathOperator.DIV) {
            b = RANDOM.nextInt(2, 15);

            var result = RANDOM.nextInt(2, 15);
            a = result * b;
        } else {
            a = RANDOM.nextInt(2, 50);
            b = RANDOM.nextInt(2, 50);

            if (op == MathOperator.SUB && b > a) {
                var tmp = a;
                a = b;
                b = tmp;
            }
        }

        return math(a + " " + op.symbol + " " + b, op.calculate(a, b));
    }

    public static Question generateWordScramble(List<String> words) {
        var word      = words.get(RANDOM.nextInt(words.size()));
        var scrambled = scramble(word);

        return new Question(0, QuestionType.WORD_SCRAMBLE,
                Config.get().quizWordScramblePrompt().replace("%word%", scrambled.toUpperCase()),
                List.of(), List.of(word.toLowerCase()), false);
    }

    private static Question math(String expr, int result) {
        return new Question(0, QuestionType.MATH_FORMULA,
                Config.get().quizMathPrompt().replace("%formula%", expr), List.of(), List.of(String.valueOf(result)), false);
    }

    private static String scramble(String word) {
        var first = word.charAt(0);
        var chars = word.substring(1).toCharArray();

        do {
            for (var i = chars.length - 1; i > 0; i--) {
                var j   = RANDOM.nextInt(i + 1);
                var tmp = chars[i]; chars[i] = chars[j]; chars[j] = tmp;
            }
        } while ((first + new String(chars)).equalsIgnoreCase(word));

        return "<u>" + first + "</u>" + new String(chars);
    }

}
