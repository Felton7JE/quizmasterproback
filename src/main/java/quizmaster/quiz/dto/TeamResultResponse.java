package quizmaster.quiz.dto;

import lombok.Data;

@Data
public class TeamResultResponse {
    private Integer teamAPoints;
    private Integer teamBPoints;
    private String winnerTeam;
    private Integer teamACorrectAnswers;
    private Integer teamBCorrectAnswers;
}