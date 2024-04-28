package library.client;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ProgressDialog extends Stage {
    private Label headerLabel;

    public ProgressDialog() {
        initStyle(StageStyle.UNDECORATED);
        initModality(Modality.APPLICATION_MODAL);

        headerLabel = new Label("Please wait...");
        ProgressIndicator progressIndicator = new ProgressIndicator();

        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: white; -fx-padding: 20;");
        root.getChildren().addAll(headerLabel, progressIndicator);

        Scene scene = new Scene(root);
        setScene(scene);
    }

    /**
     * Sets the header text of the progress dialog.
     *
     * @param text the text to set as the header
     */
    public void setHeaderText(String text) {
        headerLabel.setText(text);
    }
}
