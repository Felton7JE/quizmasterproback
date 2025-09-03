package quizmaster.quiz.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private Integer totalPoints;
    private Integer gamesPlayed;
    private Integer gamesWon;
    private Double accuracy;
    private Integer bestStreak;
    private LocalDateTime createdAt;
}