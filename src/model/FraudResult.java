package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FraudResult {
    private final Transaction transaction;
    private final int score;
    private final RiskLevel level;
    private final List<String> reasons;

    public FraudResult(Transaction transaction, int score, RiskLevel level, List<String> reasons) {
        if (transaction == null || level == null || reasons == null) {
            throw new IllegalArgumentException("transaction, level and reasons must not be null");
        }
        this.transaction = transaction;
        this.score = score;
        this.level = level;
        this.reasons = new ArrayList<>(reasons); // defensive copy
    }

    public Transaction getTransaction() { return transaction; }
    public int getScore() { return score; }
    public RiskLevel getLevel() { return level; }

    public List<String> getReasons() {
        return Collections.unmodifiableList(reasons); // callers can't modify it
    }

    public boolean isFlagged() {
        return level != RiskLevel.LOW;
    }

    @Override
    public String toString() {
        return String.format("TX #%d | $%.2f | %s (%d) | Reasons: %s",
                transaction.getId(),
                transaction.getAmount(),
                level,
                score,
                reasons.isEmpty() ? "none" : String.join("; ", reasons));
    }
}