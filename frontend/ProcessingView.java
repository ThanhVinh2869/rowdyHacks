package frontend;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** progress screen while the backend analyzes the file */
public class ProcessingView extends VBox {
    private final Label fileLabel = new Label();
    private final ProgressBar bar = new ProgressBar(ProgressBar.INDETERMINATE_PROGRESS);
    private final Label count = new Label("0 of 0 transactions");
    private final Label percent = new Label("0%");
    private final Label stage = new Label("Reading CSV\u2026");

    public ProcessingView(Runnable onCancel) {
        setSpacing(14);
        setPadding(new Insets(24));

        Label title = new Label("Analyzing\u2026");
        title.setStyle("-fx-font-size:22px;-fx-font-weight:bold;");
        fileLabel.setStyle("-fx-text-fill:#6b7280;");
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(14);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox numbers = new HBox(count, spacer, percent);
        count.setStyle("-fx-text-fill:#6b7280;");
        percent.setStyle("-fx-text-fill:#6b7280;");

        Label stageTitle = new Label("Current stage");
        stageTitle.setStyle("-fx-font-weight:bold;");
        Button cancel = new Button("Cancel");
        cancel.setOnAction(e -> onCancel.run());

        getChildren().addAll(title, fileLabel, bar, numbers, stageTitle, stage, cancel);
    }

    public void start(String fileName) {
        fileLabel.setText(fileName);
        setProgress(0, 0, "Reading CSV\u2026");
    }

    /** zero total shows an indeterminate bar while the file is being read */
    public void setProgress(int done, int total, String stageText) {
        bar.setProgress(total == 0 ? ProgressBar.INDETERMINATE_PROGRESS : (double) done / total);
        count.setText(String.format("%,d of %,d transactions", done, total));
        percent.setText(total == 0 ? "" : (int) Math.round(100.0 * done / total) + "%");
        if (stageText != null) stage.setText(stageText);
    }
}
