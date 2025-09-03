package quizmaster.quiz.dto;

import lombok.Data;

@Data
public class UpdateRoomRequest {
    private String name;
    private String password;
    private Integer maxPlayers;
    private String gameMode;
    private String category;
    private String difficulty;
    private Integer questionCount;
    private Integer timePerQuestion;
    private Boolean isPrivate;
    private Long hostId;
}
