package rules;

import java.time.Duration;
import java.time.LocalDateTime;
import model.Account;
import model.Transaction;

public class LocationChangeRule extends AbstractRule {

    // this is the shortest time a normal trip should take between countries
    private static final Duration MIN_PLAUSIBLE_TRAVEL_TIME = Duration.ofHours(2);

    // default rule text and weight
    public LocationChangeRule() {
        super("Location change", 20);
    }

    // check whether a rapid country change looks suspicious
    @Override
    public RuleResult evaluate(Transaction transaction, Account account) {
        // ignore missing input
        if (transaction == null || account == null) {
            return RuleResult.notTriggered();
        }

        // look at the most recent past transaction
        Transaction previousTransaction = account.getLastTransaction().orElse(null);
        if (previousTransaction == null) {
            return RuleResult.notTriggered();
        }

        // pull the country values from both transactions
        String previousCountry = previousTransaction.getCountry();
        String currentCountry = transaction.getCountry();

        // skip if either country is missing
        if (previousCountry == null || currentCountry == null) {
            return RuleResult.notTriggered();
        }

        // same country means no unusual move
        if (previousCountry.equalsIgnoreCase(currentCountry)) {
            return RuleResult.notTriggered();
        }

        // grab the timestamps to compare travel time
        LocalDateTime previousTime = previousTransaction.getTimestamp();
        LocalDateTime currentTime = transaction.getTimestamp();

        // skip if timestamps are missing or out of order
        if (previousTime == null || currentTime == null || currentTime.isBefore(previousTime)) {
            return RuleResult.notTriggered();
        }

        // measure the gap between the two transactions
        Duration timeGap = Duration.between(previousTime, currentTime);

        // if enough time passed, the move is plausible
        if (timeGap.compareTo(MIN_PLAUSIBLE_TRAVEL_TIME) >= 0) {
            return RuleResult.notTriggered();
        }

        // otherwise the location change is too fast to be normal
        return new RuleResult(true, getWeight(),
                "Location changed faster than possible for a normal trip");
    }
}