package rules;
import java.time.Duration;
import java.time.LocalDateTime;

import model.Account;
import model.Transaction;

public class VelocityRule extends AbstractRule {
    private final int maxTransactions = 5;
    private int transactionCount;
    LocalDateTime start = LocalDateTime.now();
    Duration interval = Duration.ofMinutes(5);
    LocalDateTime end = start.plus(interval);

    public VelocityRule(int maxTransactions) {
    super("VelocityRule", maxTransactions);
    this.transactionCount = 0;
    }

    public int getMaxTransactions() {
        return maxTransactions;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
    }

    public boolean isTransactionAllowed() {
        return transactionCount < maxTransactions;
    }

    public boolean checkTransaction(Transaction transaction, Account account) {
        if (transaction.getTimestamp().isAfter(end)) {
            start = transaction.getTimestamp();
            end = start.plus(interval);
            transactionCount = 0;
        }

        if (isTransactionAllowed()) {
            transactionCount++;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public RuleResult evaluate(Transaction tx, Account account) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'evaluate'");
    }

}
