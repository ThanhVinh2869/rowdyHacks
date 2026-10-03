package model;

public enum RiskLevel {
    LOW(0),
    MEDIUM(30),
    HIGH(60);

    private final int minScore; // lowest score that belongs to this level

    RiskLevel(int minScore) {
        this.minScore = minScore;
    }

    public int getMinScore() {
        return minScore;
    }

    public static RiskLevel fromScore(int score) {
        if (score >= HIGH.minScore) return HIGH;
        if (score >= MEDIUM.minScore) return MEDIUM;
        return LOW;
    }
}