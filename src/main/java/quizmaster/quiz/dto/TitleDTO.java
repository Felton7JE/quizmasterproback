package quizmaster.quiz.dto;

import lombok.Data;
import quizmaster.quiz.enums.TitleConditionType;

@Data
public class TitleDTO {
    private Long id;
    private String name;
    private String description;
    private TitleConditionType conditionType;
    private Integer conditionValue;
    private boolean isUnlocked;
}
