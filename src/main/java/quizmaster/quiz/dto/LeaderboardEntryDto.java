package quizmaster.quiz.dto;

public class LeaderboardEntryDto {
    private String username;
    private int score;
    private int highestStreak;

    public LeaderboardEntryDto() {}

    public LeaderboardEntryDto(String username, int score, int highestStreak) {
        this.username = username;
        this.score = score;
        this.highestStreak = highestStreak;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getHighestStreak() {
        return highestStreak;
    }

    public void setHighestStreak(int highestStreak) {
        this.highestStreak = highestStreak;
    }
}
