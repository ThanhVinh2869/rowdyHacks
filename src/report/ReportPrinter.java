package report;

import model.FraudResult;
import model.RiskLevel;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Handles all console output, so no other class needs System.out calls.
 */
public class ReportPrinter {

    private final boolean showAll; // true = print every transaction, false = only flagged ones

    public ReportPrinter(boolean showAll) {
        this.showAll = showAll;
    }

    /** Prints one result. Unflagged (LOW) results are skipped unless showAll is true. */
    public void print(FraudResult result) {
        if (!showAll && !result.isFlagged()) {
            return;
        }
        System.out.println(result); // uses FraudResult.toString()
    }

    /** Prints totals after all transactions have been processed. */
    public void printSummary(List<FraudResult> results) {
        Map<RiskLevel, Integer> counts = new EnumMap<>(RiskLevel.class);
        for (RiskLevel level : RiskLevel.values()) {
            counts.put(level, 0);
        }
        for (FraudResult r : results) {
            counts.merge(r.getLevel(), 1, Integer::sum);
        }

        long flagged = results.stream().filter(FraudResult::isFlagged).count();

        System.out.println();
        System.out.println("========== SUMMARY ==========");
        System.out.println("Transactions processed : " + results.size());
        for (RiskLevel level : RiskLevel.values()) {
            System.out.printf("  %-8s: %d%n", level, counts.get(level));
        }
        System.out.println("Flagged (MEDIUM+HIGH)  : " + flagged);
        System.out.println("=============================");
    }
}