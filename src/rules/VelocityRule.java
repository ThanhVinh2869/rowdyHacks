package rules;
import java.time.Duration;
import java.time.LocalDateTime;

import model.Account;
import model.Transaction;

public class VelocityRule extends AbstractRule {
    private final int maxTransactions = 5;
    private int transactionCount;

    // Define the time interval for the velocity rule (e.g., 5 minutes)
    LocalDateTime start = LocalDateTime.now();
    Duration interval = Duration.ofMinutes(5);
    LocalDateTime end = start.plus(interval);

    public VelocityRule(int maxTransactions) {
    super("VelocityRule", 20);
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

    // Check if the amount of transactions is under a certain threshold
    public boolean isTransactionAllowed() {
        return transactionCount < maxTransactions;
    }

    // Check if the transaction is allowed based on the the time interval between transactions
    public boolean checkTransaction(Transaction transaction, Account account) {
        if (transaction.getTimestamp().isAfter(end)) {
            start = transaction.getTimestamp(); 
            end = start.plus(interval);
            transactionCount = 0;
        }

        // Call isTransactionAllowed() and increment transactionCount
        if (isTransactionAllowed()) {
            transactionCount++;
            return true;
        } else {
            return false;
        }
    }

    // Evaluate the transaction based on the velocity rule
    @Override
    public RuleResult evaluate(Transaction tx, Account account) {
        throw new UnsupportedOperationException("Unimplemented method 'evaluate'");
    }

}
