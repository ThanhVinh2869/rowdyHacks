package rules;

import java.time.Duration;
import java.time.LocalDateTime;
import model.Account;
import model.Transaction;

public class LocationChangeRule extends AbstractRule {

    // example: 2 hours between transactions in different countries.
    private static final Duration MIN_PLAUSIBLE_TRAVEL_TIME = Duration.ofHours(2);

    public LocationChangeRule() {
        super("Location change", 20);
    }

    @Override
    public RuleResult evaluate(Transaction transaction, Account account) {
        if (transaction == null || account == null) {
            return RuleResult.notTriggered();
        }

        Transaction previousTransaction = account.getLastTransaction().orElse(null);
        if (previousTransaction == null) {
            return RuleResult.notTriggered();
        }

        String previousCountry = previousTransaction.getCountry();
        String currentCountry = transaction.getCountry();

        if (previousCountry == null || currentCountry == null) {
            return RuleResult.notTriggered();
        }

        if (previousCountry.equalsIgnoreCase(currentCountry)) {
            return RuleResult.notTriggered();
        }

        LocalDateTime previousTime = previousTransaction.getTimestamp();
        LocalDateTime currentTime = transaction.getTimestamp();

        if (previousTime == null || currentTime == null || currentTime.isBefore(previousTime)) {
            return RuleResult.notTriggered();
        }

        Duration timeGap = Duration.between(previousTime, currentTime);

        if (timeGap.compareTo(MIN_PLAUSIBLE_TRAVEL_TIME) >= 0) {
            return RuleResult.notTriggered();
        }

        return new RuleResult(true, getWeight(),
                "Location changed faster than possible for a normal trip");
    }
}