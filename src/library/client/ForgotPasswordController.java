package library.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.JSONObject;

public class ForgotPasswordController {

    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 8080;

    @FXML
    private TextField emailField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private void resetPassword(ActionEvent event) {
        String email = emailField.getText();
        String username = usernameField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (email.isEmpty() || username.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showErrorAlert("Input Error", "All fields are required.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showErrorAlert("Input Error", "New passwords do not match.");
            return;
        }

        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Create a JSON object with reset password data
            JSONObject resetData = new JSONObject();
            resetData.put("email", email);
            resetData.put("username", username);
            resetData.put("newPassword", newPassword);

            // Send a POST request with the JSON payload
            out.println("POST /reset-password HTTP/1.1");
            out.println("Content-Type: application/json");
            out.println("Content-Length: " + resetData.toString().length());
            out.println();
            out.println(resetData.toString());

            // Read the server's JSON response
            StringBuilder responseBuilder = new StringBuilder();
            String line;

            // Skip HTTP response headers
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                System.out.println(line);
            }

            // Read JSON response body
            while ((line = in.readLine()) != null) {
                responseBuilder.append(line);
            }

            // Parse the JSON response
            JSONObject jsonResponse = new JSONObject(responseBuilder.toString());
            String status = jsonResponse.getString("status");
            String message = jsonResponse.getString("message");

            if (status.equals("success")) {
                showInfoAlert("Success", "Password reset successfully.");
            } else {
                showErrorAlert("Password Reset Failed", message);
            }

        } catch (IOException e) {
            showErrorAlert("Connection Error", "Failed to connect to the server.");
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
    
    @FXML
	private void goToLogin(ActionEvent event) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
			Stage stage = (Stage) usernameField.getScene().getWindow();
			stage.setScene(new Scene(root));
			stage.setTitle("Login");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
