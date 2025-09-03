package quizmaster.quiz.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GameResultResponse {
    private Long gameId;
    private String gameMode;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private List<PlayerResultResponse> results;
    private PlayerResultResponse winner;
    private TeamResultResponse teamResults;
}