package library.client;

import java.io.IOException;
import java.util.List;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class BorrowedHistoryController {

	private String userId;
	private LibraryClient client; // Client instance for fetching data

	@FXML
	private TableView<BorrowedItem> borrowedHistoryTableView;

	@FXML
	private TableColumn<BorrowedItem, String> titleColumn;

	@FXML
	private TableColumn<BorrowedItem, String> dateBorrowedColumn;

	@FXML
	private TableColumn<BorrowedItem, String> dateReturnedColumn;

	@FXML
	private Button backButton;

	public BorrowedHistoryController(String userId) {
		this.userId = userId;

		client = new LibraryClient(userId);
	}

	// Getter for userId if needed
	public String getUserId() {
		return userId;
	}

	@FXML
	private void initialize() {
		// Initialize the LibraryClient (you can pass the user ID here if needed)

		// Set cell value factories for each column in the TableView
		titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
		dateBorrowedColumn.setCellValueFactory(cellData -> cellData.getValue().dateBorrowedProperty());
		dateReturnedColumn.setCellValueFactory(cellData -> cellData.getValue().dateReturnedProperty());
 
		// Load the borrowed history items
		loadBorrowedHistory();
	}

	private void loadBorrowedHistory() {
		if (client != null) {
			// Fetch the list of borrowed items history from the client
			List<BorrowedItem> borrowedHistory = client.getBorrowedHistory();
			System.out.println("the history: " + borrowedHistory);
			// Update the TableView with the borrowed history
			if (borrowedHistory != null) {
				Platform.runLater(() -> borrowedHistoryTableView.getItems().setAll(borrowedHistory));
			} else {
				Platform.runLater(() -> showErrorAlert("Loading Error", "Failed to load borrowed history"));
			}
		} else {
			Platform.runLater(() -> showErrorAlert("Client Not Initialized", "Library client is not initialized"));
		}
	}

	@FXML
	private void handleBack(ActionEvent event) {
		// Handle the back button action
		try {
			// Load the previous FXML file (e.g., LibraryDashboard.fxml) and set the scene
			FXMLLoader loader = new FXMLLoader(getClass().getResource("LibraryDashboard.fxml"));
			loader.setController(new LibraryDashboardController(userId)); // set the controller with userId
			Parent root = loader.load();

			// Get the current stage
			Stage stage = (Stage) backButton.getScene().getWindow();

			// Set the scene to the new root
			stage.setScene(new Scene(root));
			stage.setTitle("Library Dashboard");
		} catch (IOException e) {
			e.printStackTrace();
			showErrorAlert("Navigation Error", "Unable to navigate back to the Library Dashboard");
		}
	}

	private void showErrorAlert(String title, String message) {
		// Show an error alert dialog
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
