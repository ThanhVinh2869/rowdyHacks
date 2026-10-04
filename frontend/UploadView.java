package frontend;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

/** screen for choosing a csv file */
public class UploadView extends VBox {
    private static final long MAX_BYTES = 25L * 1024 * 1024;
    private static final String DROP_IDLE = "-fx-border-color:#c5cad3;-fx-border-style:dashed;-fx-border-width:2;-fx-border-radius:12;-fx-background-radius:12;-fx-background-color:#fafbfc;-fx-cursor:hand;";
    private static final String DROP_OVER = DROP_IDLE.replace("#c5cad3", "#2f5bea").replace("#fafbfc", "#eef2ff");

    private final Consumer<File> onChosen;
    private final Consumer<File> onAnalyze;

    private final VBox dropZone = new VBox(6);
    private final HBox fileBox = new HBox(12);
    private final Label fileName = new Label();
    private final Label fileSize = new Label();
    private final Label error = new Label();
    private final Button analyzeBtn = new Button("Analyze file");
    private final VBox rulesBox = new VBox(4);
    private File selected;

    public UploadView(Consumer<File> onChosen, Consumer<File> onAnalyze) {
        this.onChosen = onChosen;
        this.onAnalyze = onAnalyze;
        setSpacing(18);
        setPadding(new Insets(24));

        Label title = new Label("Upload transactions");
        title.setStyle("-fx-font-size:22px;-fx-font-weight:bold;");
        Label sub = new Label("Choose a CSV file to scan with the fraud rules.");
        sub.setStyle("-fx-text-fill:#6b7280;");

        // file drop area
        Label icon = new Label("\uD83D\uDCC4");
        icon.setStyle("-fx-font-size:34px;");
        Label drop = new Label("Drag & drop a CSV here");
        drop.setStyle("-fx-font-weight:bold;-fx-font-size:15px;");
        Label browse = new Label("or click to browse");
        browse.setStyle("-fx-text-fill:#6b7280;");
        dropZone.getChildren().addAll(icon, drop, browse);
        dropZone.setAlignment(Pos.CENTER);
        dropZone.setPadding(new Insets(40));
        dropZone.setStyle(DROP_IDLE);
        dropZone.setOnMouseClicked(e -> browse());
        dropZone.setOnDragOver(this::dragOver);
        dropZone.setOnDragExited(e -> dropZone.setStyle(DROP_IDLE));
        dropZone.setOnDragDropped(this::dragDropped);

        // selected file details
        VBox names = new VBox(fileName, fileSize);
        fileName.setStyle("-fx-font-weight:bold;");
        fileSize.setStyle("-fx-text-fill:#6b7280;");
        Button remove = new Button("Remove");
        remove.setOnAction(e -> setFile(null));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        fileBox.getChildren().addAll(names, spacer, remove);
        fileBox.setAlignment(Pos.CENTER_LEFT);
        fileBox.setPadding(new Insets(10, 14, 10, 14));
        fileBox.setStyle("-fx-border-color:#e3e6eb;-fx-border-radius:10;");
        fileBox.setVisible(false);
        fileBox.setManaged(false);

        // validation error
        error.setStyle("-fx-text-fill:#d92d20;-fx-background-color:#fde8e6;-fx-padding:8 12;-fx-background-radius:8;");
        error.setVisible(false);
        error.setManaged(false);

        analyzeBtn.setDisable(true);
        analyzeBtn.setStyle("-fx-background-color:#2f5bea;-fx-text-fill:white;-fx-padding:9 20;-fx-background-radius:8;");
        analyzeBtn.setOnAction(e -> this.onAnalyze.accept(selected));

        // file format and rule details
        Label fmtTitle = new Label("Expected format");
        fmtTitle.setStyle("-fx-font-weight:bold;");
        Label fmt = new Label("id,userId,amount,merchant,country,timestamp\n1,user42,129.99,Amazon,US,2025-03-14T02:15:00Z");
        fmt.setStyle("-fx-font-family:monospace;-fx-background-color:#f1f3f6;-fx-padding:8;-fx-background-radius:6;");
        Label notes = new Label("\u2022 First row is a header (skipped)\n\u2022 Timestamps are ISO 8601 UTC (ending in Z)\n\u2022 Malformed rows are skipped (details in the console)");
        notes.setStyle("-fx-text-fill:#6b7280;");
        VBox formatCard = card(fmtTitle, fmt, notes);

        Label rulesTitle = new Label("Rules applied");
        rulesTitle.setStyle("-fx-font-weight:bold;");
        VBox rulesCard = card(rulesTitle, rulesBox);

        HBox cards = new HBox(16, formatCard, rulesCard);
        HBox.setHgrow(formatCard, Priority.ALWAYS);
        HBox.setHgrow(rulesCard, Priority.ALWAYS);

        getChildren().addAll(title, sub, dropZone, fileBox, error, analyzeBtn, cards);
    }

    private VBox card(javafx.scene.Node... nodes) {
        VBox box = new VBox(8, nodes);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color:white;-fx-border-color:#e3e6eb;-fx-border-radius:12;-fx-background-radius:12;");
        return box;
    }

    /** show the active rules */
    public void setRules(List<String> names) {
        rulesBox.getChildren().clear();
        for (String n : names) rulesBox.getChildren().add(new Label("\u2714 " + n));
    }

    public void showError(String message) {
        boolean has = message != null && !message.isBlank();
        error.setText(has ? message : "");
        error.setVisible(has);
        error.setManaged(has);
    }

    /** reset the screen */
    public void reset() {
        setFile(null);
        showError(null);
    }

    private void browse() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select transactions CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        File f = fc.showOpenDialog(getScene().getWindow());
        if (f != null) accept(f);
    }

    private void dragOver(DragEvent e) {
        if (e.getDragboard().hasFiles()) {
            e.acceptTransferModes(TransferMode.COPY);
            dropZone.setStyle(DROP_OVER);
        }
        e.consume();
    }

    private void dragDropped(DragEvent e) {
        boolean ok = false;
        if (e.getDragboard().hasFiles()) {
            accept(e.getDragboard().getFiles().get(0));
            ok = true;
        }
        e.setDropCompleted(ok);
        e.consume();
    }

    /** check file type and size before backend parsing */
    private void accept(File f) {
        showError(null);
        if (!f.getName().toLowerCase().endsWith(".csv")) {
            setFile(null);
            showError("Please choose a .csv file.");
        } else if (f.length() > MAX_BYTES) {
            setFile(null);
            showError("File is larger than 25 MB.");
        } else {
            setFile(f);
            onChosen.accept(f);
        }
    }

    private void setFile(File f) {
        selected = f;
        analyzeBtn.setDisable(f == null);
        fileBox.setVisible(f != null);
        fileBox.setManaged(f != null);
        if (f != null) {
            fileName.setText(f.getName());
            fileSize.setText(String.format("%.1f KB", f.length() / 1024.0));
        }
    }
}
