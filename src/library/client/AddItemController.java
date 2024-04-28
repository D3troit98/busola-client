package library.client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class AddItemController {

	@FXML
	private TextField titleField;

	@FXML
	private TextField authorField;

	@FXML
	private TextField typeField;

	@FXML
	private ComboBox<String> typeComboBox; // Field for cover image URL

	@FXML
	private Button addItemButton;

	@FXML
	private Button backButton;

	@FXML
	private Button fileUploadButton; // Declare fileUploadButton

	@FXML
	private Label fileNameLabel; // Declare fileNameLabel

	private AdminLibraryClient client;

	@FXML
	private void initialize() {
		client = new AdminLibraryClient();
		typeComboBox.setItems(FXCollections.observableArrayList("Audio books", "PDF", "DVDs" , "Games"));
	}

	@FXML
	private void handleAddItem(ActionEvent event) {
	    // Retrieve values from the form
	    String title = titleField.getText();
	    String author = authorField.getText();
	    String type = typeComboBox.getValue();
	    String absoluteFilePath = fileNameLabel.getText(); // Renamed variable for clarity

	    // Check if any of the required fields are empty
	    if (title.isEmpty() || author.isEmpty() || type == null || absoluteFilePath.isEmpty()) {
	        showErrorAlert("Incomplete Fields", "Please fill all the required fields (Title, Author, Type, and File).");
	        return;
	    }

	    // Check the size of the file
	    File file = new File(absoluteFilePath);
	    long fileSize = file.length();

	    // Define the size limit (200KB)
	    long sizeLimit = 5000 * 1024; // 5MB

	    if (fileSize > sizeLimit) {
	        showErrorAlert("File Size Error", "The file size should not exceed 5MB.");
	        return;
	    }

	    try {
	        // Determine the absolute path of the project directory
	        String projectPath = System.getProperty("user.dir");

	        // Define the resources folder path
	        String resourcesFolderPath = projectPath + "/resources";

	        // Create the resources folder if it doesn't exist
	        File resourcesFolder = new File(resourcesFolderPath);
	        if (!resourcesFolder.exists()) {
	            resourcesFolder.mkdirs(); // Create the folder and any necessary parent directories
	        }

	        // Define the target file path within the resources folder
	        String targetFilePath = resourcesFolderPath + "/" + file.getName();

	        // Check if the file already exists
	        File targetFile = new File(targetFilePath);
	        if (targetFile.exists()) {
	            showErrorAlert("File Exists", "The file already exists in the resources folder.");
	            return;
	        }

	        // Save the file to the resources folder
	        Files.copy(file.toPath(), Paths.get(targetFilePath));

	        // Create a CatalogItem instance with the file path
	        CatalogItem newItem = new CatalogItem(title, author, type, true, null,0,0);
	        newItem.setFilePath(targetFilePath); // Save the file path in the CatalogItem

	        // Save the rest of the catalog item to MongoDB
	        boolean success = client.addCatalogItem(newItem);

	        if (success) {
	            showSuccessAlert("Item Added", "The item was successfully added to the catalog.");
	            goBackToViewItems(event);
	        } else {
	            showErrorAlert("Add Item Failed", "Failed to add the item to the catalog.");
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	        showErrorAlert("File Upload Error", "An error occurred while processing the file.");
	    }
	}
	

	// Handle the file upload button action
	@FXML
	private void handleFileUpload(ActionEvent event) {
		// Create a FileChooser instance
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select a PDF File");

		// Set file extension filters for PDF and EPUB
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
				new FileChooser.ExtensionFilter("EPUB Files", "*.epub"));

		// Show the file chooser and get the selected file
		File selectedFile = fileChooser.showOpenDialog(null);

		if (selectedFile != null) {
			// Display the selected file's name in fileNameLabel
			fileNameLabel.setText(selectedFile.getAbsolutePath());

			// Further actions to handle the file (e.g., storing the file path or content)
			// ...
		} else {
			// Handle the case where no file is selected
			showErrorAlert("File Selection Error", "No file was selected.");
		}
	}

	@FXML
	private void goBackToViewItems(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("ViewItems.fxml"));
			Parent root = loader.load();

			// Set the scene and stage
			Stage stage = (Stage) backButton.getScene().getWindow();
			stage.setScene(new Scene(root));
			stage.setTitle("View Items");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void showSuccessAlert(String title, String message) {
		javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
				javafx.scene.control.Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	private void showErrorAlert(String title, String message) {
		javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
