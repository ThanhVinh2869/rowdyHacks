package rules;
import model.Account;
import model.Transaction;

public interface FraudRule {
    String getName();
    RuleResult evaluate(Transaction tx, Account account);
}