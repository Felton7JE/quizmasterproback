package quizmaster.quiz.dto;

import lombok.Data;

@Data
public class TeamAssignedMessage {
    private Long userId;
    private String username;
    private String team;
    private String roomCode;
    private Long timestamp;
}
