package library.client;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class GlobalChatsController {

    private String userId; // Instance variable to store the user ID
    private LibraryClient client;
    private Timer refreshTimer;
    private int refreshCountdown = 30; // Refresh every 30 seconds

    @FXML
    private ListView<String> chatListView;

    @FXML
    private TextField chatInputField;

    @FXML
    private Button sendButton;
    
    @FXML
    private Label refreshCountdownLabel; // Label to display the refresh countdown

    public GlobalChatsController(String userId) {
        this.userId = userId;
        client = new LibraryClient(userId);
    }

    @FXML
    public void initialize() {
        fetchChats();
        startRefreshTimer();
    }

    private void fetchChats() {
        // Fetch the chat messages from the server
        List<String> chats = client.fetchAllChats();
        
        // Update the chatListView with the fetched chats
        Platform.runLater(() -> {
            chatListView.getItems().setAll(chats);
            refreshCountdown = 30; // Reset the refresh countdown
        });
    }

    private void startRefreshTimer() {
        // Create a timer to refresh the chats every 30 seconds
        refreshTimer = new Timer(true);
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Decrement the refresh countdown and update the label
                Platform.runLater(() -> {
                    refreshCountdown--;
                    refreshCountdownLabel.setText("Refresh in: " + refreshCountdown + "s");
                    if (refreshCountdown == 0) {
                        fetchChats(); // Refresh chats
                    }
                });
            }
        }, 1000, 1000); // Run every second
    }

    @FXML
    private void handleSend(ActionEvent event) {
        // Get the text from the chat input field
        String message = chatInputField.getText().trim(); // Use trim() to remove leading and trailing whitespace

        // Check if the message is empty
        if (message.isEmpty()) {
            // Use Platform.runLater to ensure the alert is displayed on the JavaFX Application Thread
            Platform.runLater(() -> {
                showErrorAlert("Empty Message", "Please enter a message before sending.");
            });
            return; // Exit the method to prevent sending an empty message
        }

        // Send the message to the server
        boolean sent = client.sendChatMessage(userId, message);

        if (sent) {
            // Refresh the chats after sending a message
            fetchChats();
            chatInputField.clear(); // Clear the chat input field
        } else {
            // Use Platform.runLater to ensure the alert is displayed on the JavaFX Application Thread
            Platform.runLater(() -> {
                showErrorAlert("Message Sending Failed", "Failed to send the message. Please try again.");
            });
        }
    }

    @FXML
    private void handleGoBack(ActionEvent event) {
        try {
            // Load the LibraryDashboard.fxml and navigate back to the library dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("LibraryDashboard.fxml"));
            loader.setController(new LibraryDashboardController(userId));
            Parent root = loader.load();
            Stage stage = (Stage) chatListView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Library Dashboard");
            
            // Stop the refresh timer
            if (refreshTimer != null) {
                refreshTimer.cancel();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Unable to navigate back to the Library Dashboard");
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
