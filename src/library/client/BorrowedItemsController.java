package library.client;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.scene.control.ListView;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.stage.Stage;
import javafx.scene.control.TextInputDialog;
import org.icepdf.ri.common.MyAnnotationCallback;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;
import org.icepdf.ri.util.FontPropertiesManager;

public class BorrowedItemsController {
    private String userId; // Instance variable to store the user ID
    private LibraryClient client;

    @FXML
    private ListView<CatalogItem> borrowedItemsListView;

    public BorrowedItemsController(String userId) {
        this.userId = userId;
        client = new LibraryClient(userId);
    }

    @FXML
    public void initialize() {
        // Load the borrowed items
        loadBorrowedItems();
    }
    
    @FXML
    private void handleAddRating(ActionEvent event) {
        // Get the selected item from the borrowed items list view
        CatalogItem selectedItem = borrowedItemsListView.getSelectionModel().getSelectedItem();

        // Check if an item is selected
        if (selectedItem != null) {
            // Create a dialog to input the rating
            TextInputDialog ratingDialog = new TextInputDialog();
            ratingDialog.setTitle("Add Rating");
            ratingDialog.setHeaderText("Add a rating for " + selectedItem.getTitle());
            ratingDialog.setContentText("Enter your rating (1-5):");

            // Show the dialog and get the user's input
            ratingDialog.showAndWait().ifPresent(ratingInput -> {
                try {
                    // Parse the user's input as an integer
                    int rating = Integer.parseInt(ratingInput);
                    
                    // Check if the rating is within the valid range (1-5)
                    if (rating < 1 || rating > 5) {
                        showErrorAlert("Invalid Rating", "Please enter a rating between 1 and 5.");
                        return;
                    }

                    // Update the rating of the selected item
                    selectedItem.setDummyRating(rating);
                    
                    // Send the updated rating to the server
                    boolean success = client.updateRating(selectedItem);
                    
                    if (success) {
                        showInfoAlert("Rating Added", "Successfully added a rating of " + rating + " to " + selectedItem.getTitle());
                        // Optionally, refresh the list view
                        loadBorrowedItems();
                    } else {
                        showErrorAlert("Update Failed", "Unable to update the rating.");
                    }
                } catch (NumberFormatException e) {
                    showErrorAlert("Invalid Input", "Please enter a valid number.");
                }
            });
        } else {
            // Display an error message if no item is selected
            showErrorAlert("No Selection", "Please select an item to add a rating.");
        }
    }

    private void loadBorrowedItems() {
		borrowedItemsListView.setCellFactory(listView -> new CatalogItemListCell());
		if (client != null) {
			List<CatalogItem> items = client.getBorrowedItems();
			if (items != null) {
				System.out.println(items.getFirst().getTitle());
				Platform.runLater(() -> borrowedItemsListView.getItems().setAll(items));
			} else {
				Platform.runLater(() -> showErrorAlert("Loading Error", "Failed to load borrowed items"));
			}
		} else {
			Platform.runLater(() -> showErrorAlert("Client Not Initialized", "Library client is not initialized"));
		}
	}
    
    private class CatalogItemListCell extends ListCell<CatalogItem> {
	    @Override
	    protected void updateItem(CatalogItem item, boolean empty) {
	        super.updateItem(item, empty);

	        if (empty || item == null) {
	            setText(null);
	            setGraphic(null);
	        } else {
	            // Create a VBox to hold the item details
	            VBox itemDetails = new VBox(5);
	            itemDetails.setPadding(new Insets(5));

	            // Create labels for the title, author, and due date
	            Label titleLabel = new Label("Title: " + item.getTitle());
	            Label authorLabel = new Label("Author: " + item.getAuthor());
	            Label dueDateLabel = new Label("Due Date: " + item.getDueDate());

	            // Add the labels to the VBox
	            itemDetails.getChildren().addAll(titleLabel, authorLabel, dueDateLabel);

	            // Set the VBox as the graphic for this ListCell
	            setGraphic(itemDetails);
	        }
	    }
	}

    @FXML
	private void handleRead(ActionEvent event) {
	    // Get the selected item from the borrowed items list view
	    CatalogItem selectedItem = borrowedItemsListView.getSelectionModel().getSelectedItem();

	    // Check if an item is selected
	    if (selectedItem != null) {
	        try {
	            // Retrieve the file path from the CatalogItem
	            String filePath = selectedItem.getFilePath();

	            // Check if file path is valid
	            if (filePath == null || filePath.isEmpty()) {
	                Platform.runLater(() -> showErrorAlert("Error Reading Book", "File path not found."));
	                return;
	            }

	            // Create a new stage for the e-reader window
	            Stage eReaderStage = new Stage();
	            eReaderStage.setTitle("E-Reader: " + selectedItem.getTitle());

	            if (selectedItem.getType().equals("PDF")) {
	                // Load the PDF file using IcePDF
	                File pdfFile = new File(filePath);
	                // Check if the file exists
	                if (!pdfFile.exists()) {
	                    Platform.runLater(() -> showErrorAlert("Error Reading Book", "File not found at the specified path."));
	                    return;
	                }

	                // Initiate font caching for faster startups
	                FontPropertiesManager.getInstance().loadOrReadSystemFonts();

	                // Create a SwingController and SwingViewBuilder
	                SwingController controller = new SwingController();
	                SwingViewBuilder factory = new SwingViewBuilder(controller);

	                // Build the viewer panel
	                JPanel viewerComponentPanel = factory.buildViewerPanel();
	                controller.getDocumentViewController().setAnnotationCallback(new MyAnnotationCallback(controller.getDocumentViewController()));

	                // Load the document using IcePDF
	                controller.openDocument(pdfFile.getAbsolutePath());

	                // Create a new JFrame to display the PDF viewer
	                JFrame viewerFrame = new JFrame("PDF Viewer - " + selectedItem.getTitle());
	                viewerFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	                viewerFrame.add(viewerComponentPanel);
	                viewerFrame.pack();
	                viewerFrame.setVisible(true);
	            } else if (selectedItem.getType().equals("EPUB")) {
	                // EPUB handling to be implemented later
	                // Placeholder comment
	            } else {
	                // Handle other invalid book types
	                showErrorAlert("Error Reading Book", "Not a valid book type.");
	                return; // Return early if book type is invalid
	            }

	            // Show the e-reader window (if needed)
	            // eReaderStage.show(); // Uncomment if you need to show an empty stage

	        } catch (Exception e) {
	            e.printStackTrace();
	            showErrorAlert("Error Reading Book", "An error occurred while reading the book: " + e.getMessage());
	        }
	    } else {
	        // Display an error message if no book is selected
	        showErrorAlert("No Selection", "Please select a book to read.");
	    }
	}
    
    @FXML
    private void handleReturn(ActionEvent event) {
        CatalogItem selectedItem = borrowedItemsListView.getSelectionModel().getSelectedItem();

        if (selectedItem != null) {
            if (client != null) {
                if (client.returnItem(selectedItem)) {
                    showInfoAlert("Item Returned", "Successfully returned: " + selectedItem.getTitle());
                    loadBorrowedItems();
                } else {
                    showErrorAlert("Returning Failed", "Unable to return: " + selectedItem.getTitle());
                }
            } else {
                showErrorAlert("Client Not Initialized", "Library client is not initialized");
            }
        } else {
            showErrorAlert("No Selection", "Please select an item to return");
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
			Stage stage = (Stage) borrowedItemsListView.getScene().getWindow();

			// Set the scene to the new root
			stage.setScene(new Scene(root));
			stage.setTitle("Library Dashboard");
		} catch (IOException e) {
			e.printStackTrace();
			showErrorAlert("Navigation Error", "Unable to navigate back to the Library Dashboard");
		}
	}

    @FXML
    private void handleViewBorrowedHistory(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("BorrowedHistory.fxml"));
            loader.setController(new BorrowedHistoryController(userId));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Borrowed History");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Unable to navigate to the Borrowed History page.");
        }
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
