package model;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Account {
    private final String userId;
    private final List<Transaction> history = new ArrayList<>();

    public Account(String userId) {
        this.userId = userId;
    }

    public List<Transaction> getHistory() { return Collections.unmodifiableList(history); }
    public void addTransaction(Transaction tx) { history.add(tx); }

    public double getAverageAmount() {
        return history.stream().mapToDouble(Transaction::getAmount).average().orElse(0);
    }

    public Optional<Transaction> getLastTransaction() {
        return history.isEmpty() ? Optional.empty()
                : Optional.of(history.get(history.size() - 1));
    }

    public String getUserId() {
        return userId;
    }
}