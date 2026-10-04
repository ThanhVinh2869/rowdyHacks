package frontend;

import model.FraudResult;

import java.io.File;
import java.util.List;

/** callbacks from the view to the controller */
public interface ViewListener {
    /** called when the user chooses a valid csv file */
    void onFileChosen(File file);

    /** called when the user starts analysis */
    void onAnalyze(File file);

    /** called when the user cancels analysis */
    void onCancel();

    /** called to export flagged results */
    void onExport(List<FraudResult> flagged);

    /** called when the user starts another analysis */
    void onReset();
}
