package rules;

import java.time.LocalDateTime;
import java.util.List;

import model.Transaction;
import model.Account;

public class HighAmountRule extends AbstractRule {

    // max amount for new accounts with less than a month of transaction history
    private static final double NEW_ACCOUNT_MAX_LIMIT = 5000;

    private double maxLimit;
    private int multiplier;
    private double averageAmount;

    public HighAmountRule(double maxLimit, int multiplier) {
        super("HighAmountRule", 20);
        this.maxLimit = maxLimit;
        this.multiplier = multiplier;
    }

    public double getMaxLimit() {
        return maxLimit;
    }

    public double getAverageAmount() {
        return averageAmount;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public void setMaxLimit(double maxLimit) {
        this.maxLimit = maxLimit;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }

    public boolean isTransactionAllowed(Transaction transaction, Account account) {
        
        // Pull array of transactions from account history
        List<Transaction> history = account.getHistory();
        LocalDateTime transactionTime = transaction.getTimestamp();
        LocalDateTime monthStart = transactionTime == null
            ? null
            : transactionTime.minusMonths(1);
        // Check if the account has at least a month of transaction history
        boolean hasMonthOfHistory = monthStart != null
            && history.stream().anyMatch(past -> past.getTimestamp() != null
                && !past.getTimestamp().isAfter(monthStart));
        // Filter the account history to only include transactions from the last month
        List<Transaction> monthlyHistory = transactionTime == null
            ? List.of()
            : history.stream()
                .filter(past -> past.getTimestamp() != null
                    && !past.getTimestamp().isBefore(transactionTime.minusMonths(1))
                    && past.getTimestamp().isBefore(transactionTime))
                .toList();
        // Calculate the average amount of transactions in the last month
        averageAmount = monthlyHistory.stream()
            .mapToDouble(Transaction::getAmount)
            .average()
            .orElse(0);
        double amount = transaction.getAmount();
        // Calculate the allowed amount based on the rule
        if (!hasMonthOfHistory) {
            return amount <= NEW_ACCOUNT_MAX_LIMIT;
        }

        return amount <= maxLimit
            && (monthlyHistory.isEmpty() || amount <= averageAmount * multiplier);
    }

    @Override
    public RuleResult evaluate(Transaction tx, Account account) {
        // Return not triggered if either the transaction or account is null
        if (tx == null || account == null) {
            return RuleResult.notTriggered();
        }

        // Return result based on whether the transaction is allowed or not
        if (isTransactionAllowed(tx, account)) {
            return RuleResult.notTriggered();
        }

        return new RuleResult(true, getWeight(),
                "Transaction amount exceeds the configured limit or recent average threshold");
    }

}
