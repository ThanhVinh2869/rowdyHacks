package frontend;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.FraudResult;
import model.RiskLevel;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/** summary cards and a table of results */
public class ResultsView extends VBox {
    private static final String FLAGGED = "Flagged only (MEDIUM + HIGH)";
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Label fileLabel = new Label();
    private final HBox stats = new HBox(12);
    private final ComboBox<String> filter = new ComboBox<>();
    private final Label shown = new Label();
    private final TableView<FraudResult> table = new TableView<>();
    private List<FraudResult> all = List.of();

    public ResultsView(Consumer<List<FraudResult>> onExport, Runnable onAgain) {
        setSpacing(14);
        setPadding(new Insets(24));

        Label title = new Label("Results");
        title.setStyle("-fx-font-size:22px;-fx-font-weight:bold;");
        fileLabel.setStyle("-fx-text-fill:#6b7280;");
        VBox heading = new VBox(title, fileLabel);

        Button export = new Button("Export flagged (CSV)");
        export.setOnAction(e -> onExport.accept(all.stream().filter(FraudResult::isFlagged).toList()));
        Button again = new Button("Analyze another file");
        again.setStyle("-fx-background-color:#2f5bea;-fx-text-fill:white;-fx-background-radius:8;");
        again.setOnAction(e -> onAgain.run());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(10, heading, spacer, export, again);
        header.setAlignment(Pos.CENTER_LEFT);

        filter.getItems().addAll(FLAGGED, "All", "HIGH", "MEDIUM", "LOW");
        filter.setValue(FLAGGED);
        filter.setOnAction(e -> applyFilter());
        shown.setStyle("-fx-text-fill:#6b7280;");
        HBox filterRow = new HBox(12, filter, shown);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        buildColumns();
        table.setPlaceholder(new Label("No transactions match."));
        VBox.setVgrow(table, Priority.ALWAYS);

        getChildren().addAll(header, stats, filterRow, table);
    }

    /** show the backend results */
    public void showResults(String fileName, List<FraudResult> results) {
        all = results;
        fileLabel.setText(fileName);
        long high = count(RiskLevel.HIGH), med = count(RiskLevel.MEDIUM), low = count(RiskLevel.LOW);
        stats.getChildren().setAll(
                statCard("Processed", all.size(), "#1a1f2b"),
                statCard("High risk", high, "#d92d20"),
                statCard("Medium risk", med, "#d98a00"),
                statCard("Low risk", low, "#198754"),
                statCard("Flagged (MED+HIGH)", high + med, "#1a1f2b"));
        filter.setValue(FLAGGED);
        applyFilter();
    }

    private long count(RiskLevel level) {
        return all.stream().filter(r -> r.getLevel() == level).count();
    }

    private VBox statCard(String label, long n, String color) {
        Label num = new Label(String.format("%,d", n));
        num.setStyle("-fx-font-size:24px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
        Label l = new Label(label);
        l.setStyle("-fx-text-fill:#6b7280;-fx-font-size:12px;");
        VBox box = new VBox(2, num, l);
        box.setPadding(new Insets(12, 16, 12, 16));
        box.setStyle("-fx-background-color:white;-fx-border-color:#e3e6eb;-fx-border-radius:12;-fx-background-radius:12;");
        HBox.setHgrow(box, Priority.ALWAYS);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private void applyFilter() {
        String f = filter.getValue();
        List<FraudResult> list = all.stream().filter(r -> switch (f) {
            case FLAGGED -> r.isFlagged();
            case "All" -> true;
            default -> r.getLevel().name().equals(f);
        }).toList();
        table.setItems(FXCollections.observableArrayList(list));
        table.sort();
        shown.setText(String.format("%,d shown", list.size()));
    }

    private void buildColumns() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().add(numCol("TX #", 70, r -> "#" + r.getTransaction().getId()));
        table.getColumns().add(col("User", 90, r -> r.getTransaction().getUserId()));
        table.getColumns().add(numCol("Amount", 90, r -> String.format("$%,.2f", r.getTransaction().getAmount())));
        table.getColumns().add(col("Merchant", 110, r -> r.getTransaction().getMerchant()));
        table.getColumns().add(col("Country", 70, r -> r.getTransaction().getCountry()));
        table.getColumns().add(col("Time (UTC)", 130, r -> r.getTransaction().getTimestamp().format(TIME)));

        TableColumn<FraudResult, String> score = numCol("Score", 60, r -> String.valueOf(r.getScore()));
        score.setSortType(TableColumn.SortType.DESCENDING);
        table.getColumns().add(score);

        TableColumn<FraudResult, String> level = col("Level", 90, r -> r.getLevel().name());
        level.setComparator(Comparator.comparingInt(s -> RiskLevel.valueOf(s).ordinal()));
        level.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label pill = new Label(item);
                String[] colors = switch (item) {
                    case "HIGH" -> new String[]{"#d92d20", "#fde8e6"};
                    case "MEDIUM" -> new String[]{"#d98a00", "#fff3d6"};
                    default -> new String[]{"#198754", "#e0f3e8"};
                };
                pill.setStyle("-fx-font-weight:bold;-fx-padding:2 9;-fx-background-radius:99;-fx-text-fill:" + colors[0] + ";-fx-background-color:" + colors[1] + ";");
                setGraphic(pill);
            }
        });
        table.getColumns().add(level);
        table.getColumns().add(col("Reasons", 320, r -> r.getReasons().isEmpty() ? "\u2014" : String.join("; ", r.getReasons())));

        table.getSortOrder().add(score); // show the highest risk first
    }

    private TableColumn<FraudResult, String> col(String title, double width, Function<FraudResult, String> f) {
        TableColumn<FraudResult, String> c = new TableColumn<>(title);
        c.setPrefWidth(width);
        c.setCellValueFactory(d -> new ReadOnlyStringWrapper(f.apply(d.getValue())));
        return c;
    }

    /** numeric sorting for columns whose values are displayed as text */
    private TableColumn<FraudResult, String> numCol(String title, double width, Function<FraudResult, String> f) {
        TableColumn<FraudResult, String> c = col(title, width, f);
        c.setComparator(Comparator.comparingDouble(s -> Double.parseDouble(s.replaceAll("[^0-9.\\-]", ""))));
        return c;
    }
}
