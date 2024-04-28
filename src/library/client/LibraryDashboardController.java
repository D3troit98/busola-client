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
import javafx.scene.control.Alert.AlertType;

import javafx.stage.Stage;


public class LibraryDashboardController {

	private String userId; // Instance variable to store the user ID
	private LibraryClient client;

	@FXML
	private Label welcomeLabel;

	@FXML
	private TextField searchField;

	@FXML
	private TableView<CatalogItem> catalogTableView;


	// Add the @FXML annotations for the TableColumn fields
	@FXML
	private TableColumn<CatalogItem, String> titleColumn;
	@FXML
	private TableColumn<CatalogItem, String> authorColumn;
	@FXML
	private TableColumn<CatalogItem, String> typeColumn;
	@FXML
	private TableColumn<CatalogItem, Boolean> availableColumn;
	
	@FXML
	private TableColumn<CatalogItem, Integer> ratingColumn; 

	@FXML
	private TableColumn<CatalogItem, Integer> holdCountColumn; // Column for hold count

	/**
	 * Constructor that accepts a user ID and initializes the LibraryClient and
	 * welcome label.
	 *
	 * @param userId the user ID to initialize the client and fetch user information
	 */
	public LibraryDashboardController(String userId) {
		System.out.println("contructer id: " + userId);
		this.userId = userId; // Store the user ID
		// Initialize the LibraryClient with the provided user ID
		client = new LibraryClient(userId);
	}

	// Getter for userId if needed
	public String getUserId() {
		return userId;
	}

	@FXML
	public void initialize() {
		System.out.println("intialized id: " + userId);
		// Set cell value factories for each colum

		if (client != null) {
			String username = client.getUsername();
			if (username != null) {
				welcomeLabel.setText("Welcome, " + username);
			} else {
				welcomeLabel.setText("Welcome");
			}

			ProgressDialog progressDialog = new ProgressDialog();
			progressDialog.show();

			try {
				initializeCatalogTableView();
			
			} catch (Exception e) {
				e.printStackTrace();
				Platform.runLater(
						() -> showErrorAlert("Loading Items Failed", "An error occurred while fetching the items"));
			} finally {
				Platform.runLater(progressDialog::close);
			}

		} else {
			// Handle case where LibraryClient is not initialized
			Platform.runLater(() -> showErrorAlert("Client Not Initialized", "Library client is not initialized"));
		}
	}
	
	@FXML
	private void handleAddToHoldList(ActionEvent event) {
	    // Get the selected item from the table
	    CatalogItem selectedItem = catalogTableView.getSelectionModel().getSelectedItem();
	    if (selectedItem != null) {
	        if (client != null) {
	            // Call a method in the client to add the selected item to the hold list
	            if (client.addItemToHoldList(selectedItem)) {
	                // Show a success message
	                showInfoAlert("Hold Request", "Successfully requested to be added to the hold list for: " + selectedItem.getTitle());
	                // Refresh the catalog items to reflect the changes
	                loadCatalogItems();
	            } else {
	                // Show an error message if the request failed
	                showErrorAlert("Hold Request Failed", "Unable to request hold for: " + selectedItem.getTitle());
	            }
	        } else {
	            showErrorAlert("Client Not Initialized", "Library client is not initialized");
	        }
	    } else {
	        // Show an error message if no item is selected
	        showErrorAlert("No Selection", "Please select an item to request to be added to the hold list");
	    }
	}

	private void initializeCatalogTableView() {
		// Initialize catalog TableView columns
		// Set data properties for columns
		titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
		authorColumn.setCellValueFactory(cellData -> cellData.getValue().authorProperty());
		typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
		availableColumn.setCellValueFactory(cellData -> cellData.getValue().availableProperty());

		  // Set cell value factories for the new columns
		ratingColumn.setCellValueFactory(cellData -> cellData.getValue().ratingNumberProperty().asObject());
	    holdCountColumn.setCellValueFactory(cellData -> cellData.getValue().holdCountProperty().asObject());
		// Load catalog items
		loadCatalogItems();
	}
	
	@FXML
	private void handleRefresh(ActionEvent event) {
	    // Refresh the catalog items by reloading them
	    loadCatalogItems();
	    // Optionally, show a message to the user indicating that the refresh was successful
	    Platform.runLater(() ->   showInfoAlert("Refresh", "Catalog items refreshed successfully."));
	}
	
	
	@FXML
	private void goToGlobalChats(ActionEvent event) {
	    try {
	        // Load the GlobalChats.fxml file
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("GlobalChats.fxml"));
	        // Load the new scene for global chats
	       
	        loader.setController(new GlobalChatsController(userId)); // set the controller with userId
	        Parent root = loader.load();
	        
	        Stage currentStage = (Stage) catalogTableView.getScene().getWindow();
	        // Set the new scene
	        currentStage.setScene(new Scene(root));
	        // Set the title of the new scene
	        currentStage.setTitle("Global Chats");
	    } catch (IOException e) {
	        e.printStackTrace();
	        showErrorAlert("Navigation Error", "Unable to navigate to the Global Chats page.");
	    }
	}

	private void loadCatalogItems() {
		System.out.println("the client: " + client);
		if (client != null) {
			List<CatalogItem> items = client.getCatalogItems();
			System.out.println("the items: " + items);
			if (items != null) {
				Platform.runLater(() -> catalogTableView.getItems().setAll(items));
			} else {
				Platform.runLater(() -> showErrorAlert("Loading Error", "Failed to load catalog items"));
			}
		} else {
			System.out.println("the client is null");
			Platform.runLater(() -> showErrorAlert("Client Not Initialized", "Library client is not initialized"));
		}
	}
	

	@FXML
	private void handleSearch(ActionEvent event) {
		// Get the search query from the search field
		String query = searchField.getText();

		// Call the searchCatalog method from the client to filter items based on the
		// query
		List<CatalogItem> filteredItems = client.searchCatalog(query, catalogTableView);

		// Update the catalogTableView with the filtered items
		catalogTableView.getItems().setAll(filteredItems);
	}

	@FXML
	private void handleBorrow(ActionEvent event) {
		CatalogItem selectedItem = catalogTableView.getSelectionModel().getSelectedItem();
		if (selectedItem != null) {
			if (client != null) {
				if (client.borrowItem(selectedItem)) {
					showInfoAlert("Item Borrowed", "Successfully borrowed: " + selectedItem.getTitle());
					loadCatalogItems();
				} else {
					showErrorAlert("Borrowing Failed", "Unable to borrow: " + selectedItem.getTitle());
				}
			} else {
				showErrorAlert("Client Not Initialized", "Library client is not initialized");
			}
		} else {
			showErrorAlert("No Selection", "Please select an item to borrow");
		}
	}

	

	

	@FXML
	private void handleViewBorrowedItems(ActionEvent event) {
	    try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("BorrowedItems.fxml"));
	        loader.setController(new BorrowedItemsController(userId));
	        Parent root = loader.load();
	        Stage currentStage = (Stage) catalogTableView.getScene().getWindow();
			currentStage.setScene(new Scene(root));
			currentStage.setTitle("Borrowed Items");
	        
	     
	    } catch (IOException e) {
	        e.printStackTrace();
	        showErrorAlert("Navigation Error", "Unable to navigate to the Borrowed Items page.");
	    }
	}

	@FXML
	private void handleViewBorrowedHistory(ActionEvent event) {
		try {

			FXMLLoader loader = new FXMLLoader(getClass().getResource("BorrowedHistory.fxml"));
			loader.setController(new BorrowedHistoryController(userId)); 
			Parent root = loader.load();
			Stage currentStage = (Stage) catalogTableView.getScene().getWindow();
			currentStage.setScene(new Scene(root));
			currentStage.setTitle("Borrowed History");
		} catch (IOException e) {
			e.printStackTrace();
			showErrorAlert("Navigation Error", "Unable to navigate to the Borrowed History page.");
		}
	}
	
	

	@FXML
	private void handleLogout(ActionEvent event) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
			Stage currentStage = (Stage) welcomeLabel.getScene().getWindow();
			currentStage.setScene(new Scene(root));
			currentStage.setTitle("Login");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void handleQuit(ActionEvent event) {
		// Handle quitting the application
		Platform.exit();
	}

	private void showInfoAlert(String title, String message) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	private void showErrorAlert(String title, String message) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
