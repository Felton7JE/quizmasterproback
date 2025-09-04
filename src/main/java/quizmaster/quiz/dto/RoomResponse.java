package quizmaster.quiz.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import quizmaster.quiz.enums.AssignmentType;
import quizmaster.quiz.enums.CategoryAssignmentMode;
import quizmaster.quiz.enums.Difficulty;
import quizmaster.quiz.enums.GameMode;
import quizmaster.quiz.enums.RoomStatus;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class RoomResponse {
    private Long id;
    private String roomCode;
    private String roomName;
    private GameMode gameMode;
    private Difficulty difficulty;
    private LocalDateTime startsAt;

    private Integer maxPlayers;
    private Integer questionTime;
    private Integer questionCount;
    private List<CategoryResponse> categories;
    private AssignmentType assignmentType;
    private CategoryAssignmentMode categoryAssignmentMode;
    private RoomStatus status;
    private Long hostId;
    private String hostName;
    private LocalDateTime createdAt;
    private List<PlayerResponse> players;
    private Integer currentPlayers;
    private Boolean allowSpectators;
    private Boolean enableChat;
    private Boolean showRealTimeRanking;
    private Boolean allowReconnection;
    private Boolean isPrivate;
}