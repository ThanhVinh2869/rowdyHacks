package rules;
import java.time.Duration;
import java.time.LocalDateTime;

import model.Account;
import model.Transaction;

public class VelocityRule extends AbstractRule {
    private static final int MAX_TRANSACTIONS = 5;
    private static final Duration INTERVAL = Duration.ofMinutes(5);

    private int transactionCount;

    public VelocityRule() {
        super("VelocityRule", 20);
        this.transactionCount = 0;
    }

    public int getMaxTransactions() {
        return MAX_TRANSACTIONS;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
    }

    public boolean isTransactionAllowed() {
        return transactionCount < MAX_TRANSACTIONS;
    }

    public boolean checkTransaction(Transaction transaction, Account account) {
        if (transaction == null || account == null || transaction.getTimestamp() == null) {
            return false;
        }

        LocalDateTime currentTime = transaction.getTimestamp();
        LocalDateTime windowStart = currentTime.minus(INTERVAL);

        transactionCount = (int) account.getHistory().stream()
                .filter(past -> past != null && past.getTimestamp() != null)
                .filter(past -> !past.getTimestamp().isBefore(windowStart)
                        && !past.getTimestamp().isAfter(currentTime))
                .count();

        return isTransactionAllowed();
    }

    @Override
    public RuleResult evaluate(Transaction tx, Account account) {
        if (tx == null || account == null || tx.getTimestamp() == null) {
            return RuleResult.notTriggered();
        }

        LocalDateTime currentTime = tx.getTimestamp();
        LocalDateTime windowStart = currentTime.minus(INTERVAL);

        long recentTransactions = account.getHistory().stream()
                .filter(past -> past != null && past.getTimestamp() != null)
                .filter(past -> !past.getTimestamp().isBefore(windowStart)
                        && !past.getTimestamp().isAfter(currentTime))
                .count();

        if (recentTransactions >= MAX_TRANSACTIONS) {
            return new RuleResult(true, getWeight(),
                    "Rate limit exceeded: " + MAX_TRANSACTIONS + " transactions in "
                            + INTERVAL.toMinutes() + " minutes.");
        }

        return RuleResult.notTriggered();
    }
}
