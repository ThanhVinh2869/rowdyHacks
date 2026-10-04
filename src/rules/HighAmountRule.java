package rules;

import java.time.LocalDateTime;
import java.util.List;

import model.Transaction;
import model.Account;

public class HighAmountRule extends AbstractRule {

    private double maxLimit;
    private int multiplier;
    private double averageAmount;

    public HighAmountRule() {
        super("HighAmountRule", 20);
    }

    public double getAverageAmount() {
        return averageAmount;
    }

    public double getMaxLimit() {
        return maxLimit;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double amount) {
        int amountRange = amount <= 200 ? 1 : amount <= 500 ? 2 : 3;
        switch (amountRange) {
            case 1:
                multiplier = 5;
                break;
            case 2:
                multiplier = 3;
                break;
            default:
                multiplier = 2;
                break;
        }
    }

    public boolean isTransactionAllowed(Transaction transaction, Account account) {
        LocalDateTime transactionTime = transaction.getTimestamp();
        List<Transaction> monthlyHistory = account.getHistory().stream()
            .filter(past -> past.getTimestamp() != null
                && !past.getTimestamp().isBefore(transactionTime.minusMonths(1))
                && past.getTimestamp().isBefore(transactionTime))
            .toList();

        averageAmount = monthlyHistory.stream()
            .mapToDouble(Transaction::getAmount)
            .average()
            .orElse(0);

        setMultiplier(transaction.getAmount());
        maxLimit = averageAmount * multiplier;
        return transaction.getAmount() <= maxLimit;
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
                "Transaction amount exceeds the recent average threshold");
    }

}
