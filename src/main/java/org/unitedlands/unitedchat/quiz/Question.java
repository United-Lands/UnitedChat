package org.unitedlands.unitedchat.quiz;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record Question(
        int          id,
        QuestionType type,
        String       question,
        List<String> choices,
        List<String> correct,
        boolean      ulSpecific
) {

    @SuppressWarnings("unchecked")
    public static Question fromMap(Map<String, Object> m) {
        var id         = (int) m.get("id");
        var type       = QuestionType.fromKey((String) m.get("type"));
        var question   = (String) m.get("question");
        var choices    = m.containsKey("choices") ? (List<String>) m.get("choices") : new ArrayList<String>();
        var correct    = m.get("correct");
        var ulSpecific = Boolean.TRUE.equals(m.get("ul-specific"));

        var correctList = correct instanceof List<?>
                ? (List<String>) correct
                : List.of((String) correct);

        return new Question(id, type, question, choices, correctList, ulSpecific);
    }
}
