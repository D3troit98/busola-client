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

public class ViewMembersController {

	private AdminLibraryClient client;

	@FXML
	private TableView<LibraryMember> membersTableView;

	@FXML
	private TableColumn<LibraryMember, String> memberIdColumn;

	@FXML
	private TableColumn<LibraryMember, String> usernameColumn;

	@FXML
	private TableColumn<LibraryMember, String> emailColumn;

	@FXML
	private TableColumn<LibraryMember, String> statusColumn;

	@FXML
	private Button suspendMemberButton;

	@FXML
	private Button deleteMemberButton;

	@FXML
	private Button backDashboardButton;

	public ViewMembersController() {

	}

	@FXML
	private void initialize() {
		client = new AdminLibraryClient();

		// Set cell value factories for each column
		memberIdColumn.setCellValueFactory(cellData -> cellData.getValue().memberIdProperty());
		usernameColumn.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());
		emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
		statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

		loadMembers();

	}

	private void loadMembers() {
		if (client != null) {
			List<LibraryMember> members = client.getAllMembers();
			System.out.println("Received members list: " + members);
			if (members != null) {
				Platform.runLater(() -> {
					System.out.println("Updating TableView with members list");
					membersTableView.getItems().setAll(members);
					System.out.println("TableView updated");
				});
			}
		}
	}

	@FXML
	private void suspendSelectedMember(ActionEvent event) {
		LibraryMember selectedMember = membersTableView.getSelectionModel().getSelectedItem();
		System.out.println("the seleted member: " + selectedMember);
		if (selectedMember != null && client != null) {
			boolean success = client.toggleMemberStatus(selectedMember);

			if (success) {
				// Refresh the list of members
				loadMembers();
			} else {
				// Show an error message
				showErrorAlert("Suspension Failed", "Unable to suspend the selected member.");
			}
		}
	}

	@FXML
	private void deleteSelectedMember(ActionEvent event) {
		LibraryMember selectedMember = membersTableView.getSelectionModel().getSelectedItem();

		if (selectedMember != null && client != null) {
			boolean success = client.deleteMember(selectedMember);

			if (success) {
				// Remove the member from the table view
				membersTableView.getItems().remove(selectedMember);
			} else {
				// Show an error message
				showErrorAlert("Deletion Failed", "Unable to delete the selected member.");
			}
		}
	}

	@FXML
	private void goBackToDashboard(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminDashboard.fxml"));

			loader.setController(new AdminDashboardController()); // set the controller with userId
			Parent root = loader.load();
			// Set the scene to the dashboard
			Stage currentStage = (Stage) backDashboardButton.getScene().getWindow();
			currentStage.setScene(new Scene(root));
			currentStage.setTitle("Admin Dashboard");
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
