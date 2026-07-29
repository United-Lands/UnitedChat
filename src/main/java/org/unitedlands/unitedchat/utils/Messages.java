package org.unitedlands.unitedchat.utils;

public enum Messages {
    NO_PERM                 ("no-perm"),
    RELOADED                ("reloaded"),
    RESET                   ("reset"),
    CHAT_TOGGLE_COMMAND     ("chat-toggle-command"),
    INVALID_FEATURE         ("invalid-feature"),
    TOGGLED_FEATURE         ("toggled-feature"),
    QUIZ_FORCED             ("quiz-forced"),
    QUIZ_TOGGLED            ("quiz-toggled"),
    PAYOUT_MESSAGE          ("payout-message"),
    GRADIENT_COMMAND        ("gradient-command"),
    GRADIENT_SET_COMMAND    ("gradient-set-command"),
    GRADIENT_CHANGED        ("gradient-changed"),
    GRADIENT_UNKNOWN_PRESET ("gradient-unknown-preset"),
    GRADIENT_ON             ("gradient-on"),
    GRADIENT_OFF            ("gradient-off"),
    GRADIENT_IS_ON          ("gradient-is-on"),
    GRADIENT_IS_OFF         ("gradient-is-off"),
    CURRENT_GRADIENT        ("current-gradient"),
    QUIZ_NEXT               ("quiz-next"),
    QUIZ_NEXT_ACTIVE        ("quiz-next-active"),
    QUIZ_NEXT_DISABLED      ("quiz-next-disabled"),
    QUIZ_NEXT_NOT_RUNNING   ("quiz-next-not-running");

    public final String key;

    Messages(String key) {
        this.key = key;
    }
}
