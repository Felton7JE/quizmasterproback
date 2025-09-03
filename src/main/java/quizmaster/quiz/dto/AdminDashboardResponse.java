package quizmaster.quiz.dto;

import lombok.Data;

@Data
public class AdminDashboardResponse {
    private Long totalUsers;
    private Long activeUsers;
    private Long totalRooms;
    private Long activeRooms;
    private Long totalGames;
    private Long gamesThisWeek;
    private Long totalQuestions;
}
