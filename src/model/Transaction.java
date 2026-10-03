package model;
import java.time.LocalDateTime;

public class Transaction {
    private final int id;
    private final String userId;
    private final double amount;
    private final String merchant;
    private final String country;
    private final LocalDateTime timestamp;

    public Transaction(int id, String userId, double amount, String merchant, String country, LocalDateTime timestamp) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.merchant = merchant;
        this.country = country;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public double getAmount() {
        return amount;
    }

    public String getMerchant() {
        return merchant;
    }

    public String getCountry() {
        return country;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
