package engine;

import model.Account;
import model.FraudResult;
import model.RiskLevel;
import model.Transaction;
import rules.FraudRule;
import rules.RuleResult;
import java.util.ArrayList;

public class FraudDetector {
    private final ArrayList<FraudRule> rules = new ArrayList<>();

    public void addRule(FraudRule rule) { rules.add(rule); }

    public FraudResult analyze(Transaction tx, Account account) {
        int score = 0;
        ArrayList<String> reasons = new ArrayList<>();
        for (FraudRule rule : rules) {
            RuleResult r = rule.evaluate(tx, account);
            if (r.isTriggered()) {
                score += r.getPoints();
                reasons.add(r.getReason());
            }
        }
        return new FraudResult(tx, score, RiskLevel.fromScore(score), reasons);
    }
}
