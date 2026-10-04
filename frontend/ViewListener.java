package frontend;

import model.FraudResult;

import java.io.File;
import java.util.List;

/**
 * controller implements this interface
 */
public interface ViewListener {
    /** valid csv file was selected */
    void onFileChosen(File file);

    /** analyze file was pressed */
    void onAnalyze(File file);

    /** cancel was pressed while processing */
    void onCancel();

    /** export flagged was pressed */
    void onExport(List<FraudResult> flagged);

    /** analyze another file was pressed */
    void onReset();
}
