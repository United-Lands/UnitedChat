package org.unitedlands.unitedchat.quiz;

import java.util.Arrays;

public enum QuestionType {
    MULTIPLE_CHOICE ("multiple_choice"),
    OPEN_TEXT       ("open_text"),
    MATH_FORMULA    ("math_formula"),
    WORD_SCRAMBLE   ("word_scramble");

    public final String key;

    QuestionType(String key) {
        this.key = key;
    }

    public static QuestionType fromKey(String key) {
        return Arrays.stream(values())
                .filter(type -> type.key.equals(key))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown question type: " + key));
    }
}
