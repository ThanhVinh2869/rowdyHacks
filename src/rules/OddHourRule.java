package rules;

import model.Account;
import model.Transaction;

public class OddHourRule extends AbstractRule {
    // start of the odd hour window
    private final int startHour;
    // end of the odd hour window
    private final int endHour;

    // default rule window for late night hours
    public OddHourRule() {
        this(20, 0, 6);
    }

    // build the rule with a custom weight and time range
    public OddHourRule(int weight, int startHour, int endHour) {
        super("Odd hour", weight);
        this.startHour = startHour;
        this.endHour = endHour;
    }

    // check whether the current transaction qualifies for the rule
    @Override
    public RuleResult evaluate(Transaction transaction, Account userAccount) {
        // reject missing data early
        if (transaction == null || transaction.getTimestamp() == null || userAccount == null) {
            return RuleResult.notTriggered();
        }

        // get the current hour and skip anything outside the rule range
        int hour = transaction.getTimestamp().getHour();
        if (hour < startHour || hour >= endHour) {
            return RuleResult.notTriggered();
        }

        // count all past transactions for the account
        long totalTransactions = userAccount.getHistory().size();
        if (totalTransactions == 0) {
            return new RuleResult(true, getWeight(), "Transaction occurred during an odd hour");
        }

        // count only previous transactions in the same odd hour range
        long oddHourTransactions = userAccount.getHistory().stream()
                .filter(historyTransaction -> historyTransaction != null && historyTransaction.getTimestamp() != null)
                .filter(historyTransaction -> {
                    int historicalHour = historyTransaction.getTimestamp().getHour();
                    return historicalHour >= startHour && historicalHour < endHour;
                })
                .count();

        // trigger only when this time range is rare for the user
        if ((double) oddHourTransactions / totalTransactions < 0.10) {
            return new RuleResult(true, getWeight(), "Transaction occurred during an 'odd' hour");
        }

        // no alert when the pattern is common enough
        return RuleResult.notTriggered();
    }
}
