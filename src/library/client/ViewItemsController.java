package library.client;

import java.io.IOException;
import java.util.List;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class ViewItemsController {

	private AdminLibraryClient client;

	@FXML
	private TableView<CatalogItem> itemsTableView;

	@FXML
	private TableColumn<CatalogItem, String> titleColumn;

	@FXML
	private TableColumn<CatalogItem, String> authorColumn;

	@FXML
	private TableColumn<CatalogItem, String> typeColumn;

	@FXML
	private TableColumn<CatalogItem, Boolean> availableColumn;

	@FXML
	private Button deleteItemButton;

	@FXML
	private Button backDashboardButton;

	@FXML
	private void initialize() {
		client = new AdminLibraryClient(); // Initialize client with AdminLibraryClient

		// Set cell value factories for each column
		titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
		authorColumn.setCellValueFactory(cellData -> cellData.getValue().authorProperty());
		typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
		availableColumn.setCellValueFactory(cellData -> cellData.getValue().availableProperty());
		loadItems();
	}

	private void loadItems() {

		if (client != null) {
			List<CatalogItem> items = client.getCatalogItems();
			if (items != null) {
				Platform.runLater(() -> {
					itemsTableView.getItems().setAll(items);
				});

			}
		}

	}

	@FXML
	private void deleteSelectedItem(ActionEvent event) {
		CatalogItem selectedItem = itemsTableView.getSelectionModel().getSelectedItem();

		if (selectedItem != null && client != null) {
			boolean success = client.deleteItem(selectedItem);

			if (success) {
				// Remove the item from the table view
				itemsTableView.getItems().remove(selectedItem);
			} else {
				// Show an error message
				showErrorAlert("Item Deletion Failed", "Unable to delete the selected item.");
			}
		}
	}

	@FXML
	private void goBackToDashboard(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminDashboard.fxml"));

			loader.setController(new AdminDashboardController()); // set the controller with userId
			Parent root = loader.load();

			// Set the scene and stage
			Stage stage = (Stage) itemsTableView.getScene().getWindow();
			stage.setScene(new Scene(root));
			stage.setTitle("Admin Dashboard");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void goToAddItem(ActionEvent event) {
		try {
			// Load the AddItem.fxml file
			FXMLLoader loader = new FXMLLoader(getClass().getResource("AddItem.fxml"));
			Parent root = loader.load();

			// Set the scene and stage
			Stage stage = (Stage) itemsTableView.getScene().getWindow();
			stage.setScene(new Scene(root));
			stage.setTitle("Add Item to Library");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void showErrorAlert(String title, String message) {
		javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
