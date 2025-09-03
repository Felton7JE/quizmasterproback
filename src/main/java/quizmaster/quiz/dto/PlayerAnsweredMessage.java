package quizmaster.quiz.dto;

import lombok.Data;

@Data
public class PlayerAnsweredMessage {
    private Long gameId;
    private Long userId;
    private String username;
    private Long questionId;
    private String answer;
    private Long timestamp;
}
