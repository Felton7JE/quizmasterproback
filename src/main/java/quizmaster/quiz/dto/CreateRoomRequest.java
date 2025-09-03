package quizmaster.quiz.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import quizmaster.quiz.enums.AssignmentType;
import quizmaster.quiz.enums.CategoryAssignmentMode;
import quizmaster.quiz.enums.Difficulty;
import quizmaster.quiz.enums.GameMode;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class CreateRoomRequest {
    private String roomName;
    private String password;
    private GameMode gameMode;
    private Difficulty difficulty;
    private Integer maxPlayers;
    private Integer questionTime;
    private Integer questionCount;
    private List<Long> categoryIds;
    private AssignmentType assignmentType;
    private CategoryAssignmentMode categoryAssignmentMode;
    private Boolean allowSpectators;
    private Boolean enableChat;
    private Boolean showRealTimeRanking;
    private Boolean allowReconnection;
    private Long hostId;
}