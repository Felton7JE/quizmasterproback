package quizmaster.quiz.dto;

import lombok.Data;
import java.util.List;

@Data
public class LeaderboardUpdateMessage {
    private Long gameId;
    private List<PlayerResultResponse> leaderboard;
    private Long timestamp;
}
