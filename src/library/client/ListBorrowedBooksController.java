package library.client;

import java.io.IOException;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class ListBorrowedBooksController {

	private AdminLibraryClient client;

	@FXML
	private TableView<BorrowedItem> borrowedBooksTableView;

	@FXML
	private TableColumn<BorrowedItem, String> titleColumn;

	@FXML
	private TableColumn<BorrowedItem, String> authorColumn;

	@FXML
	private TableColumn<BorrowedItem, String> typeColumn;

	@FXML
	private TableColumn<BorrowedItem, String> borrowerColumn;

	@FXML
	private TableColumn<BorrowedItem, String> dueDateColumn;

	@FXML
	private TableColumn<BorrowedItem, String> dateBorrowedColumn;

	@FXML
	private Button backDashboardButton;

	public ListBorrowedBooksController() {

	}

	@FXML
	private void initialize() {

		client = new AdminLibraryClient();

		// Set cell value factories for each column
		titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
		authorColumn.setCellValueFactory(cellData -> cellData.getValue().authorProperty());
		typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
		dueDateColumn.setCellValueFactory(cellData -> cellData.getValue().dueDateProperty());
		dateBorrowedColumn.setCellValueFactory(cellData -> cellData.getValue().dateBorrowedProperty());
		borrowerColumn.setCellValueFactory(cellData -> cellData.getValue().borrowerProperty());

		loadBorrowedBooks();
	}

	private void loadBorrowedBooks() {
		if (client != null) {
			List<BorrowedItem> borrowedBooks = client.getBorrowedItems();

			if (borrowedBooks != null) {
				borrowedBooksTableView.getItems().setAll(borrowedBooks);
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
			Stage stage = (Stage) borrowedBooksTableView.getScene().getWindow();
			stage.setScene(new Scene(root));
			stage.setTitle("Admin Dashboard");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
