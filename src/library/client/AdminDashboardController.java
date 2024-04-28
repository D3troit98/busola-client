package library.client;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Label;

public class AdminDashboardController {

	@FXML
	private Label welcomeLabel;

	@FXML
	private void goToViewMembers(ActionEvent event) {
		try {
			// Load the FXML file for viewing members
			Parent root = FXMLLoader.load(getClass().getResource("ViewMembers.fxml"));

			// Set the scene and stage
			Stage stage = (Stage) welcomeLabel.getScene().getWindow();
			stage.setScene(new Scene(root));
			stage.setTitle("View All Members");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void goToViewItems(ActionEvent event) {
		try {
			// Load the FXML file for viewing items
			Parent root = FXMLLoader.load(getClass().getResource("ViewItems.fxml"));

			// Set the scene and stage
			Stage stage = (Stage) welcomeLabel.getScene().getWindow();
			stage.setScene(new Scene(root));
			stage.setTitle("View All Items");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void goToListBorrowedBooks(ActionEvent event) {
		try {
			// Load the FXML file for listing borrowed books
			Parent root = FXMLLoader.load(getClass().getResource("ListBorrowedBooks.fxml"));

			// Set the scene and stage
			Stage stage = (Stage) welcomeLabel.getScene().getWindow();
			stage.setScene(new Scene(root));
			stage.setTitle("List of Borrowed Books");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void logout(ActionEvent event) {
		try {
			// Load the login page FXML file
			Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));

			// Set the scene and stage
			Stage stage = (Stage) welcomeLabel.getScene().getWindow();
			stage.setScene(new Scene(root));
			stage.setTitle("Login");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
