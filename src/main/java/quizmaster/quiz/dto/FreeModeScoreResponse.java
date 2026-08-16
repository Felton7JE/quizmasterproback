package quizmaster.quiz.dto;

public class FreeModeScoreResponse {
    private boolean isNewRecord;
    private int personalBest;
    private String nextPlayerName;
    private Integer nextPlayerScore;
    private int coinsEarned;
    private int xpGained;

    public FreeModeScoreResponse() {}

    public FreeModeScoreResponse(boolean isNewRecord, int personalBest, String nextPlayerName, Integer nextPlayerScore, int coinsEarned, int xpGained) {
        this.isNewRecord = isNewRecord;
        this.personalBest = personalBest;
        this.nextPlayerName = nextPlayerName;
        this.nextPlayerScore = nextPlayerScore;
        this.coinsEarned = coinsEarned;
        this.xpGained = xpGained;
    }

    public boolean isNewRecord() {
        return isNewRecord;
    }

    public void setNewRecord(boolean newRecord) {
        isNewRecord = newRecord;
    }

    public int getPersonalBest() {
        return personalBest;
    }

    public void setPersonalBest(int personalBest) {
        this.personalBest = personalBest;
    }

    public String getNextPlayerName() {
        return nextPlayerName;
    }

    public void setNextPlayerName(String nextPlayerName) {
        this.nextPlayerName = nextPlayerName;
    }

    public Integer getNextPlayerScore() {
        return nextPlayerScore;
    }

    public void setNextPlayerScore(Integer nextPlayerScore) {
        this.nextPlayerScore = nextPlayerScore;
    }

    public int getCoinsEarned() {
        return coinsEarned;
    }

    public void setCoinsEarned(int coinsEarned) {
        this.coinsEarned = coinsEarned;
    }

    public int getXpGained() {
        return xpGained;
    }

    public void setXpGained(int xpGained) {
        this.xpGained = xpGained;
    }
}
