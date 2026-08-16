package quizmaster.quiz.dto;

import lombok.Data;

@Data
public class UserTitleDTO {
    private Long id;
    private TitleDTO title;
    private Boolean isEquipped;
}
