package quizmaster.quiz.dto;

import lombok.Data;

@Data
public class PlayerResultResponse {
    private Long userId;
    private String username;
    private String avatar;
    private String team;
    private Integer correctAnswers;
    private Integer totalQuestions;
    private Integer totalPoints;
    private Double accuracy;
    private Integer bestStreak;
    private Long totalTime;
    private Integer position;
}