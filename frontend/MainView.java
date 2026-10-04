package frontend;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import model.FraudResult;

import java.io.File;
import java.util.List;

/**
 * root view for the three screens and the step indicator
 */
public class MainView extends BorderPane {
    private UploadView upload;
    private ProcessingView processing;
    private ResultsView results;
    private final Label[] steps = {new Label("1  Upload"), new Label("2  Processing"), new Label("3  Results")};
    private String currentFileName = "";

    public MainView(ViewListener listener) {
        upload = new UploadView(listener::onFileChosen, file -> {
            currentFileName = file.getName();
            processing.start(currentFileName);
            show(1);
            listener.onAnalyze(file);
        });
        processing = new ProcessingView(() -> {
            listener.onCancel();
            show(0);
        });
        results = new ResultsView(listener::onExport, () -> {
            upload.reset();
            show(0);
            listener.onReset();
        });

        Label logo = new Label("\uD83D\uDEE1 Fraud Analyzer");
        logo.setStyle("-fx-font-size:18px;-fx-font-weight:bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox bar = new HBox(8, logo, spacer);
        bar.getChildren().addAll(steps);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14, 24, 14, 24));
        bar.setStyle("-fx-background-color:white;-fx-border-color:#e3e6eb;-fx-border-width:0 0 1 0;");

        StackPane stack = new StackPane(upload, processing, results);
        setTop(bar);
        setCenter(stack);
        setStyle("-fx-background-color:#f5f6f8;");
        show(0);
    }

    /** rule names shown on the upload screen */
    public void setRules(List<String> ruleNames) { upload.setRules(ruleNames); }

    /** progress update while analyzing */
    public void setProgress(int done, int total, String stage) { processing.setProgress(done, total, stage); }

    /** switch to results */
    public void showResults(List<FraudResult> list) {
        results.showResults(currentFileName, list);
        show(2);
    }

    /** go back to upload with an error message */
    public void showError(String message) {
        show(0);
        upload.showError(message);
    }


    private void show(int index) {
        upload.setVisible(index == 0);
        processing.setVisible(index == 1);
        results.setVisible(index == 2);
        for (int i = 0; i < steps.length; i++) {
            String base = "-fx-padding:4 14;-fx-background-radius:99;";
            if (i == index) steps[i].setStyle(base + "-fx-background-color:#2f5bea;-fx-text-fill:white;");
            else if (i < index) steps[i].setStyle(base + "-fx-text-fill:#198754;");
            else steps[i].setStyle(base + "-fx-text-fill:#6b7280;");
        }
    }
}
