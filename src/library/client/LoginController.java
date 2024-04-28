package library.client;

import java.io.BufferedReader;
import javafx.scene.media.AudioClip;
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

public class LoginController {

	private static final String SERVER_IP = "127.0.0.1"; // Server IP
	private static final int SERVER_PORT = 8080; // Server port
	private LibraryClient client;
	// Load sound effects
	private AudioClip successSound;
	private AudioClip failureSound;

	@FXML
	private TextField usernameField;

	@FXML
	private PasswordField passwordField;

	@FXML
	private Button loginButton;

	@FXML
	public void initialize() {
		// Initialize the sound effects in the initialize method
		// This way, you can handle potential exceptions and log them
		try {
			successSound = new AudioClip(getClass().getResource("/sounds/success.mp3").toString());
			failureSound = new AudioClip(getClass().getResource("/sounds/failure.mp3").toString());
		} catch (NullPointerException e) {
			System.err.println("Failed to load sound effects. Please check the file paths.");
		}
	}

	@FXML
	private void login(ActionEvent event) {
		String username = usernameField.getText();
		String password = passwordField.getText();


		// Validate the input
		if (username.isEmpty() || password.isEmpty()) {

			if (failureSound != null) {
				failureSound.play(); // Play the success sound effect if loaded
			}
			showErrorAlert("Input Error", "Please provide both username and password.");


			return;
		} 
		// Show loading dialog on the JavaFX application thread
		ProgressDialog progressDialog = new ProgressDialog();
		Platform.runLater(progressDialog::show);

		// Create a Task to handle the background network operations
		Task<Void> loginTask = new Task<Void>() {
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
					out.println("POST /login HTTP/1.1");
					out.println("Content-Type: application/json");
					out.println("Content-Length: " + loginData.toString().length());
					out.println();
					out.println(loginData.toString());

					// Read the server's JSON response
					StringBuilder jsonResponseBuilder = new StringBuilder();
					String line;

					// Skip HTTP response headers
					while ((line = in.readLine()) != null && !line.isEmpty()) {
						System.out.println(line);
					}

					// Read JSON response body
					while ((line = in.readLine()) != null) {
						jsonResponseBuilder.append(line);
					}

					// Parse the JSON response
					JSONObject jsonResponse = new JSONObject(jsonResponseBuilder.toString());

					// Display the response message
					String status = jsonResponse.getString("status");
					String message = jsonResponse.getString("message");
					Platform.runLater(() -> {
						if (status.equals("success")) {
							// Play the success sound effect
							if (successSound != null) {
								successSound.play(); // Play the success sound effect if loaded
							}
							// Extract userId from response and store it in LibraryClient
							String userId = jsonResponse.getString("userId");
							client = new LibraryClient(userId);

							// Notify user of successful login and navigate to library dashboard
							Platform.runLater(() -> {
								showInfoAlert("Login Successful", message);
								navigateToLibraryDashboard(userId);
							});
						} else {
							// Notify user of failed login
							Platform.runLater(() -> {
								showErrorAlert("Login Failed", message);
							});
						}
					});

				} catch (IOException e) {
					// Handle IOExceptions and notify the user
					e.printStackTrace();
					
					Platform.runLater(() ->{ 
						if (failureSound != null) {
							failureSound.play(); // Play the success sound effect if loaded
						}
						showConnectionErrorAlert();
					});
				} catch (Exception e) {
					// Handle all other exceptions and notify the user
					e.printStackTrace();
					Platform.runLater(() -> showErrorAlert("Error", "An unexpected error occurred: " + e.getMessage()));
				} finally {
					// Close the progress dialog
					Platform.runLater(() -> progressDialog.close());
				}

				return null;
			}
		};

		Thread backgroundThread = new Thread(loginTask);
		backgroundThread.start();
	}

	@FXML
	private void goToRegister(ActionEvent event) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("Register.fxml"));
			Stage stage = (Stage) usernameField.getScene().getWindow();
			stage.setScene(new Scene(root));
			stage.setTitle("Register");
		} catch (IOException e) {
			e.printStackTrace();
			showErrorAlert("Navigation Error", "Unable to navigate to the Library Dashboard");
		}
	}

	@FXML
	private void goToForgotPassword(ActionEvent event) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("ForgotPassword.fxml"));
			Stage stage = (Stage) usernameField.getScene().getWindow();
			stage.setScene(new Scene(root));
			stage.setTitle("Forgot Password");
		} catch (IOException e) {
			e.printStackTrace();
			showErrorAlert("Navigation Error", "Unable to navigate to the Forgot Password page.");
		}
	}

	public void navigateToLibraryDashboard(String userId) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("LibraryDashboard.fxml"));

			loader.setController(new LibraryDashboardController(userId)); // set the controller with userId
			Parent root = loader.load();
			// Set the scene to the dashboard
			Stage currentStage = (Stage) loginButton.getScene().getWindow();
			currentStage.setScene(new Scene(root));
			currentStage.setTitle("Library Dashboard");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void goToAdminLogin(ActionEvent event) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("AdminLogin.fxml"));
			Stage stage = (Stage) usernameField.getScene().getWindow();
			stage.setScene(new Scene(root));
			stage.setTitle("Admin Login");
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
