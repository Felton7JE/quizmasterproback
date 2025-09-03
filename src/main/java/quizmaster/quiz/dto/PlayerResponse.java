package quizmaster.quiz.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import quizmaster.quiz.models.Category;
import quizmaster.quiz.enums.Team;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class PlayerResponse {
    private Long userId;
    private String username;
    private String avatar;
    private Team team;
    private Category preferredCategory;
    private Category assignedCategory;
    private Boolean isHost;
    private Boolean isReady;
}