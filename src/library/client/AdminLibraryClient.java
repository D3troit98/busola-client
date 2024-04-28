package library.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class AdminLibraryClient {
	private static final String SERVER_IP = "127.0.0.1";
	private static final int SERVER_PORT = 8080;

	public AdminLibraryClient() {
		// Initialize any necessary fields here
	}

	public List<CatalogItem> getCatalogItems() {
		List<CatalogItem> catalogItems = new ArrayList<>();

		try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

			// Send GET request to the server
			out.println("GET /catalog HTTP/1.1");
			out.println("Host: " + SERVER_IP);
			out.println();

			// Skip the HTTP response headers
			String line;
			while ((line = in.readLine()) != null && !line.isEmpty()) {
				// Skip headers
			}

			// Read the JSON body response
			StringBuilder jsonResponseBuilder = new StringBuilder();
			while ((line = in.readLine()) != null) {
				jsonResponseBuilder.append(line);
			}

			// Parse the JSON response
			JSONObject jsonResponse = new JSONObject(jsonResponseBuilder.toString());

			// Check if the JSON response contains a "catalog" array
			if (jsonResponse.has("catalog")) {
				JSONArray catalogArray = jsonResponse.getJSONArray("catalog");

				// Parse each catalog item and add to the list
				for (int i = 0; i < catalogArray.length(); i++) {
					JSONObject catalogItemJson = catalogArray.getJSONObject(i);

					// Parse catalog item details
					String title = catalogItemJson.getString("title");
					String author = catalogItemJson.getString("author");
					String type = catalogItemJson.getString("type");
					boolean available = catalogItemJson.getBoolean("available");
					// Parse the `itemId`
					String itemId = catalogItemJson.getString("_id");
					int holdList = catalogItemJson.optInt("holdList",0);
					int rating = catalogItemJson.optInt("rating",0);
					

					// Create a `CatalogItem` instance with the `itemId` and other details
					CatalogItem catalogItem = new CatalogItem(title, author, type, available, itemId,rating, holdList);

					// Add the catalog item to the list
					catalogItems.add(catalogItem);
				}
			} else {
				Platform.runLater(() -> showErrorAlert("Error", "Failed to load catalog items."));
			}
		} catch (IOException | JSONException e) {
			e.printStackTrace();
			Platform.runLater(() -> showErrorAlert("Error", "Failed to load catalog items."));
		}

		return catalogItems;
	}

	public boolean deleteItem(CatalogItem item) {
		// Send request to server to delete the item
		try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

			// Construct JSON request
			JSONObject requestJson = new JSONObject();
			requestJson.put("title", item.getTitle());
			requestJson.put("author", item.getAuthor());

			// Send request
			out.println("DELETE /catalog-item HTTP/1.1");
			out.println("Content-Type: application/json");
			out.println("Content-Length: " + requestJson.toString().length());
			out.println();
			out.println(requestJson.toString());

			// Process server response
			String response = in.readLine();
			if (response.equals("HTTP/1.1 200 OK")) {
				return true; // Item deleted successfully
			} else {
				return false; // Item deletion failed
			}
		} catch (IOException e) {
			// Handle exception
			e.printStackTrace();
			return false;
		}
	}

	public boolean addCatalogItem(CatalogItem newItem) {
		// Send request to server to add the new catalog item
		try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

			// Create JSON object for the new catalog item
			JSONObject requestJson = new JSONObject();
			requestJson.put("title", newItem.getTitle());
			requestJson.put("author", newItem.getAuthor());
			requestJson.put("type", newItem.getType());
			requestJson.put("available", newItem.isAvailable());
			// Include base64 file content in the request
			requestJson.put("file_path", newItem.getFilePath());

			// Send POST request to the server
			out.println("POST /add-catalog-item HTTP/1.1");
			out.println("Content-Type: application/json");
			out.println("Content-Length: " + requestJson.toString().length());
			out.println();
			out.println(requestJson.toString());

			// Read the server's response
			String response = in.readLine();
			if (response.equals("HTTP/1.1 200 OK")) {
				return true; // Catalog item added successfully
			} else {
				return false; // Failed to add catalog item
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean deleteMember(LibraryMember member) {
		// Send request to server to delete the member
		try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

			// Construct JSON request
			JSONObject requestJson = new JSONObject();
			requestJson.put("memberId", member.getMemberId());

			// Send request
			out.println("DELETE /library-member HTTP/1.1");
			out.println("Content-Type: application/json");
			out.println("Content-Length: " + requestJson.toString().length());
			out.println();
			out.println(requestJson.toString());

			// Process server response
			String response = in.readLine();
			if (response.equals("HTTP/1.1 200 OK")) {
				return true; // Member deleted successfully
			} else {
				return false; // Member deletion failed
			}
		} catch (IOException e) {
			// Handle exception
			e.printStackTrace();
			return false;
		}
	}

	public List<LibraryMember> getAllMembers() {
		List<LibraryMember> members = new ArrayList<>();
		// Send request to server to fetch all members
		try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

			// Send request
			out.println("GET /library-members HTTP/1.1");
			out.println("Host: " + SERVER_IP);
			out.println();

			// Skip HTTP headers
			while (in.readLine() != null && !in.readLine().isEmpty()) {
				// Skip HTTP headers
			}

			// Read JSON response body
			StringBuilder jsonResponse = new StringBuilder();
			String line;
			while ((line = in.readLine()) != null) {
				jsonResponse.append(line);
			}

			// Parse JSON response
			JSONObject responseJson = new JSONObject(jsonResponse.toString());
			JSONArray membersArray = responseJson.getJSONArray("members");

			// Parse each member and add to the list
			for (int i = 0; i < membersArray.length(); i++) {
				JSONObject memberJson = membersArray.getJSONObject(i);
				String memberId = memberJson.getString("memberId");
				String username = memberJson.getString("username");
				String email = memberJson.getString("email");
				// Use optString to handle missing status field
				String status = memberJson.optString("status", "no status");

				// Use the four-parameter constructor for LibraryMember
				LibraryMember member = new LibraryMember(memberId, username, email, status);
				members.add(member);
			}

		} catch (IOException | JSONException e) {
			// Handle exceptions
			e.printStackTrace();
		}

		return members;
	}

	public boolean toggleMemberStatus(LibraryMember member) {
		// Send request to server to toggle the member's status
		try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

			// Construct JSON request
			JSONObject requestJson = new JSONObject();
			requestJson.put("memberId", member.getMemberId());

			// Send request
			out.println("POST /toggle-member-status HTTP/1.1");
			out.println("Content-Type: application/json");
			out.println("Content-Length: " + requestJson.toString().length());
			out.println();
			out.println(requestJson.toString());

			// Process server response
			String response = in.readLine();
			if (response.equals("HTTP/1.1 200 OK")) {
				return true; // Member status toggled successfully
			} else {
				return false; // Member status toggle failed
			}
		} catch (IOException e) {
			// Handle exception
			e.printStackTrace();
			return false;
		}
	}

	public List<BorrowedItem> getBorrowedItems() {
		List<BorrowedItem> borrowedItems = new ArrayList<>();
		// Send request to server to fetch borrowed items
		try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

			// Send GET request to the server
			out.println("GET /borrowed HTTP/1.1");
			out.println("Host: " + SERVER_IP);
			out.println();

			// Skip the HTTP response headers
			String line;
			while ((line = in.readLine()) != null && !line.isEmpty()) {
				System.out.println(line); // Debugging: print headers
			}

			// Now read the JSON body
			StringBuilder jsonResponseBuilder = new StringBuilder();
			while ((line = in.readLine()) != null) {
				jsonResponseBuilder.append(line);
			}

			// Parse JSON response
			JSONObject jsonResponse = new JSONObject(jsonResponseBuilder.toString());

			if (jsonResponse.has("borrowed")) {
				JSONArray borrowedArray = jsonResponse.getJSONArray("borrowed");

				// Parse each item from the JSON array
				for (int i = 0; i < borrowedArray.length(); i++) {
					JSONObject borrowedItemJson = borrowedArray.getJSONObject(i);

					// Check if the necessary keys exist in the JSON object
					if (borrowedItemJson.has("title") && borrowedItemJson.has("author") && borrowedItemJson.has("type")
							&& borrowedItemJson.has("available")) {

						// Create a BorrowedItem instance from the JSON object
						String title = borrowedItemJson.getString("title");
						String author = borrowedItemJson.getString("author");
						String type = borrowedItemJson.getString("type");
						String borrower = borrowedItemJson.getString("borrower");
						String dueDate = borrowedItemJson.getString("due_date");
						String dateBorrowed = borrowedItemJson.getString("date_borrowed");
						ObjectId borrowerId = new ObjectId(borrowedItemJson.getString("borrowerId"));
						String dateReturned = borrowedItemJson.getString("date_returned");

						// Create a BorrowedItem instance
						BorrowedItem borrowedItem = new BorrowedItem(title, author, type, borrower, dueDate,
								dateBorrowed, borrowerId, dateReturned);
						// Add the borrowed item to the list
						borrowedItems.add(borrowedItem);
					}
				}
			} else {
				// Handle the case where "borrowed" array is missing
				Platform.runLater(() -> showErrorAlert("Error", "Failed to load borrowed items."));
			}
		} catch (IOException | JSONException e) {
			e.printStackTrace();
			Platform.runLater(() -> showErrorAlert("Error", "An error occurred while loading borrowed items."));
		}

		// Return the list of borrowed items
		return borrowedItems;
	}

	private void showErrorAlert(String title, String message) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
