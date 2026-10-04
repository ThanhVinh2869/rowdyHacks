package frontend;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** application entry point */
public class FraudApp extends Application {

    @Override
    public void start(Stage stage) {
        FraudController controller = new FraudController(stage);
        MainView view = new MainView(controller);
        controller.bind(view);
        view.setRules(FraudController.RULE_DESCRIPTIONS);

        stage.setTitle("Fraud Analyzer");
        stage.setScene(new Scene(view, 1100, 720));
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
