package library.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.JSONObject;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;

public class AdminLoginController {

	private static final String SERVER_IP = "127.0.0.1"; // Server IP
	private static final int SERVER_PORT = 8080; // Server port
	private LibraryClient client;

	@FXML
	private TextField adminUsernameField;

	@FXML
	private PasswordField adminPasswordField;

	@FXML
	private Button adminLoginButton;

	@FXML
	private void adminLogin(ActionEvent event) {
		// Get the provided admin username and password
		String username = adminUsernameField.getText();
		String password = adminPasswordField.getText();

		// Validate the input
		if (username.isEmpty() || password.isEmpty()) {
			showErrorAlert("Input Error", "Please provide both username and password.");
			return;
		}

		// Show loading dialog on the JavaFX application thread
		ProgressDialog progressDialog = new ProgressDialog();
		Platform.runLater(progressDialog::show);

		// Create a Task to handle the background network operations
		Task<Void> loginTask = new Task<>() {
			@Override
			protected Void call() throws Exception {
				try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
						PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
						BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

					// Create JSON object with login data
					JSONObject loginData = new JSONObject();
					loginData.put("username", username);
					loginData.put("password", password);

					// Send POST request with JSON payload
					out.println("POST /admin-login HTTP/1.1");
					out.println("Content-Type: application/json");
					out.println("Content-Length: " + loginData.toString().length());
					out.println();
					out.println(loginData.toString());

					// Read the server's JSON response
					StringBuilder jsonResponseBuilder = new StringBuilder();
					String line;

					// Skip the HTTP response headers
					while ((line = in.readLine()) != null && !line.isEmpty()) {
						// Skip lines while they're not empty, indicating headers
						System.out.println(line);
					}

					// Now read the JSON body
					while ((line = in.readLine()) != null) {
						jsonResponseBuilder.append(line);
					}

					// Parse the JSON response
					JSONObject jsonResponse = new JSONObject(jsonResponseBuilder.toString());

					// Process the response on the JavaFX application thread
					Platform.runLater(() -> {
						progressDialog.close(); // Close the loading dialog
						String status = jsonResponse.getString("status");
						String message = jsonResponse.getString("message");

						if (status.equals("success")) {
							// Extract userId from response and store it in LibraryClient
							String userId = jsonResponse.getString("userId");
							client = new LibraryClient(userId);

							showInfoAlert("Login Successful", message);
							// Navigate to the library dashboard
							navigateToAdminDashboard(client.getUserId());
						} else {
							showErrorAlert("Login Failed", message);
						}
					});
				} catch (IOException e) {
					e.printStackTrace();
					// Handle the exception on the JavaFX application thread
					Platform.runLater(() -> {
						progressDialog.close(); // Close loading dialog
						showConnectionErrorAlert();
					});
				}
				return null;
			}
		};

		// Run the Task in a background thread
		Thread backgroundThread = new Thread(loginTask);
		backgroundThread.start();
	}

	@FXML
	private void goToLogin(ActionEvent event) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
			Stage stage = (Stage) adminUsernameField.getScene().getWindow();
			stage.setScene(new Scene(root));
			stage.setTitle("Login");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void navigateToAdminDashboard(String userId) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminDashboard.fxml"));

			loader.setController(new AdminDashboardController()); // set the controller with userId
			Parent root = loader.load();
			// Set the scene to the dashboard
			Stage currentStage = (Stage) adminLoginButton.getScene().getWindow();
			currentStage.setScene(new Scene(root));
			currentStage.setTitle("Admin Dashboard");

		} catch (IOException e) {
			e.printStackTrace();
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

	private void showConnectionErrorAlert() {
		showErrorAlert("Connection Error", "Failed to connect to the server. Please try again later.");
	}
}
