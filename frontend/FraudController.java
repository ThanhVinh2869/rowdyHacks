package frontend;

import engine.AccountRegistry;
import engine.FraudDetector;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Account;
import model.FraudResult;
import model.Transaction;
import reader.CsvTransactionReader;
import rules.HighAmountRule;
import rules.LocationChangeRule;
import rules.OddHourRule;
import rules.VelocityRule;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** connects the javafx screens to the backend and runs analysis in the background */
public class FraudController implements ViewListener {

    /** rule descriptions shown on the upload screen */
    public static final List<String> RULE_DESCRIPTIONS = List.of(
            "High amount \u2013 above recent monthly average",
            "Velocity \u2013 5 or more transactions in 1 day",
            "Location change \u2013 new country within 2 hours",
            "Odd hour \u2013 activity between 00:00 and 06:00");

    private final Stage stage;
    private MainView view;
    private Task<List<FraudResult>> currentTask;

    public FraudController(Stage stage) {
        this.stage = stage;
    }

    public void bind(MainView view) {
        this.view = view;
    }

    // viewlistener callbacks

    @Override
    public void onFileChosen(File file) {
        // analysis starts when the user presses the analyze button
    }

    @Override
    public void onAnalyze(File file) {
        cancelCurrent();

        Task<List<FraudResult>> task = new Task<>() {
            @Override
            protected List<FraudResult> call() throws Exception {
                return runAnalysis(file, this);
            }
        };
        currentTask = task;

        task.setOnSucceeded(e -> {
            if (task != currentTask) return; // ignore cancelled or replaced tasks
            List<FraudResult> results = task.getValue();
            if (results.isEmpty()) {
                view.showError("No valid transactions were found in the file. Check that it matches the expected format.");
            } else {
                view.showResults(results);
            }
        });

        task.setOnFailed(e -> {
            if (task != currentTask) return;
            Throwable ex = task.getException();
            ex.printStackTrace();
            if (ex instanceof IOException) {
                view.showError("Could not read the file: " + ex.getMessage());
            } else {
                view.showError("Analysis failed: " + ex.getMessage());
            }
        });

        Thread t = new Thread(task, "fraud-analysis");
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void onCancel() {
        cancelCurrent();
    }

    @Override
    public void onReset() {
        cancelCurrent();
    }

    @Override
    public void onExport(List<FraudResult> flagged) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save flagged transactions");
        fc.setInitialFileName("flagged_transactions.csv");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        File out = fc.showSaveDialog(stage);
        if (out == null) return; // the user cancelled the dialog

        try {
            Files.writeString(out.toPath(), toCsv(flagged), StandardCharsets.UTF_8);
            new Alert(Alert.AlertType.INFORMATION,
                    "Saved " + flagged.size() + " flagged transactions to:\n" + out.getAbsolutePath(),
                    ButtonType.OK).showAndWait();
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, "Could not save the file: " + ex.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    // backend analysis pipeline

    private List<FraudResult> runAnalysis(File file, Task<?> task) throws IOException {
        progress(task, 0, 0, "Reading CSV\u2026");
        List<Transaction> transactions = new CsvTransactionReader().read(file.getAbsolutePath());
        if (task.isCancelled()) return List.of();

        // create fresh analysis state for every run
        FraudDetector detector = buildDetector();
        AccountRegistry registry = new AccountRegistry();

        int total = transactions.size();
        int step = Math.max(1, total / 100);
        List<FraudResult> results = new ArrayList<>(total);

        for (int i = 0; i < total; i++) {
            if (task.isCancelled()) return List.of();
            Transaction tx = transactions.get(i);
            Account account = registry.getOrCreate(tx.getUserId());
            FraudResult result = detector.analyze(tx, account); // analyze with history so far
            account.addTransaction(tx);                         // record after analyzing
            results.add(result);
            if (i % step == 0) progress(task, i + 1, total, "Applying fraud rules\u2026");
        }
        progress(task, total, total, "Building summary\u2026");
        return results;
    }

    private FraudDetector buildDetector() {
        FraudDetector detector = new FraudDetector();
        detector.addRule(new HighAmountRule());
        detector.addRule(new VelocityRule());
        detector.addRule(new LocationChangeRule());
        detector.addRule(new OddHourRule());
        return detector;
    }

    // helper methods

    private void progress(Task<?> task, int done, int total, String stageText) {
        Platform.runLater(() -> {
            if (task == currentTask && !task.isCancelled()) {
                view.setProgress(done, total, stageText);
            }
        });
    }

    private void cancelCurrent() {
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
        }
    }

    private String toCsv(List<FraudResult> list) {
        StringBuilder sb = new StringBuilder("id,userId,amount,merchant,country,timestamp,score,level,reasons\n");
        for (FraudResult r : list) {
            Transaction tx = r.getTransaction();
            sb.append(tx.getId()).append(',')
              .append(quote(tx.getUserId())).append(',')
              .append(String.format(java.util.Locale.US, "%.2f", tx.getAmount())).append(',')
              .append(quote(tx.getMerchant())).append(',')
              .append(quote(tx.getCountry())).append(',')
              .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(tx.getTimestamp())).append('Z').append(',')
              .append(r.getScore()).append(',')
              .append(r.getLevel()).append(',')
              .append(quote(String.join("; ", r.getReasons())))
              .append('\n');
        }
        return sb.toString();
    }

    private String quote(String s) {
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
