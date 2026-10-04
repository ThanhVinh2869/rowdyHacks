package src;

import reader.CsvTransactionReader;
import engine.AccountRegistry;
import engine.FraudDetector;
import model.Account;
import model.FraudResult;
import model.Transaction;
import report.ReportPrinter;
import rules.HighAmountRule;
import rules.LocationChangeRule;
import rules.OddHourRule;
import rules.VelocityRule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String path = args.length > 0 ? args[0] : "data/transactions_data.csv";

        // Step 0: set up the detector, registry and printer
        FraudDetector detector = new FraudDetector();
        detector.addRule(new HighAmountRule(5.0, 3));
        detector.addRule(new VelocityRule());
        detector.addRule(new LocationChangeRule());
        detector.addRule(new OddHourRule());

        AccountRegistry registry = new AccountRegistry();
        ReportPrinter printer = new ReportPrinter(false); // change to true to print every transaction

        // Step 2: read the CSV (already sorted by timestamp)
        List<Transaction> transactions;
        try {
            transactions = new CsvTransactionReader().read(path);
        } catch (IOException e) {
            System.err.println("Could not read " + path + ": " + e.getMessage());
            System.err.println("Working directory is: " + System.getProperty("user.dir"));
            return;
        }
        System.out.println("Loaded " + transactions.size() + " transactions from " + path);
        System.out.println();

        // Steps 3-6: process each transaction in order
        List<FraudResult> results = new ArrayList<>();
        for (Transaction tx : transactions) {
            Account account = registry.getOrCreate(tx.getUserId()); // step 3: find account
            FraudResult result = detector.analyze(tx, account);     // step 4: analyze with history so far
            account.addTransaction(tx);                             // step 5: record AFTER analyzing
            printer.print(result);                                  // step 6: report
            results.add(result);
        }

        // Step 7: summary
        printer.printSummary(results);
    }
}