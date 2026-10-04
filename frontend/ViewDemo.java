package view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.FraudResult;
import model.RiskLevel;
import model.Transaction;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * PLACEHOLDER CONTROLLER so you can run the screens without the backend.
 * Replace DemoController with your real controller, which should call
 * CsvTransactionReader + FraudDetector + AccountRegistry on a background
 * thread and report back via view.setProgress(...) / view.showResults(...)
 * using Platform.runLater(...).
 */
public class ViewDemo extends Application {

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage stage) {
        DemoController controller = new DemoController();
        MainView view = new MainView(controller);
        controller.bind(view);
        view.setRules(List.of("High amount (HighAmountRule)", "Velocity (VelocityRule)",
                "Location change (LocationChangeRule)", "Odd hour (OddHourRule)"));

        stage.setTitle("Fraud Analyzer");
        stage.setScene(new Scene(view, 1100, 720));
        stage.show();
    }

    static class DemoController implements ViewListener {
        private MainView view;
        private Timeline timeline;
        private static final int TOTAL = 1200;

        void bind(MainView v) { this.view = v; }

        @Override public void onFileChosen(File file) { /* TODO */ }

        @Override public void onAnalyze(File file) {
            // TODO: run real analysis on a background Task and update the view with Platform.runLater
            String[] stages = {"Reading CSV\u2026", "Running HighAmountRule\u2026", "Running VelocityRule\u2026",
                    "Running LocationChangeRule\u2026", "Running OddHourRule\u2026", "Building summary\u2026"};
            int[] done = {0};
            timeline = new Timeline(new KeyFrame(Duration.millis(120), e -> {
                done[0] = Math.min(TOTAL, done[0] + 60);
                view.setProgress(done[0], TOTAL, stages[Math.min(stages.length - 1, done[0] * stages.length / TOTAL)]);
                if (done[0] >= TOTAL) {
                    timeline.stop();
                    view.showResults(mockResults());
                }
            }));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
        }

        @Override public void onCancel() { if (timeline != null) timeline.stop(); /* TODO: abort backend task */ }
        @Override public void onExport(List<FraudResult> flagged) { System.out.println("TODO export " + flagged.size() + " rows"); }
        @Override public void onReset() { /* TODO */ }

        private List<FraudResult> mockResults() {
            String[] why = {"Transaction amount exceeds the configured limit or recent average threshold",
                    "Rate limit exceeded: 5 transactions in 5 minutes.",
                    "Location changed faster than possible for a normal trip",
                    "Transaction occurred during an odd hour"};
            String[] merchants = {"Amazon", "Shell", "Walmart", "Steam", "Uber", "Apple"};
            String[] countries = {"US", "US", "US", "GB", "DE", "BR", "JP"};
            Random rnd = new Random();
            List<FraudResult> out = new ArrayList<>();
            for (int i = 1; i <= TOTAL; i++) {
                Transaction tx = new Transaction(i, "user" + (1 + rnd.nextInt(80)),
                        5 + rnd.nextDouble() * rnd.nextDouble() * 6000, merchants[i % 6], countries[i % 7],
                        LocalDateTime.of(2025, 3, 1 + i % 28, i % 24, (i * 7) % 60));
                List<String> reasons = new ArrayList<>(List.of(why));
                Collections.shuffle(reasons, rnd);
                reasons = reasons.subList(0, rnd.nextDouble() < 0.78 ? 0 : 1 + rnd.nextInt(3));
                int score = reasons.size() * 20;
                out.add(new FraudResult(tx, score, RiskLevel.fromScore(score), reasons));
            }
            return out;
        }
    }
}