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
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;

public class LibraryClient {
	private static final String SERVER_IP = "127.0.0.1";
	private static final int SERVER_PORT = 8080;
	private String username;
	private String userId; // Add userId field
	private List<CatalogItem> originalCatalogItems;

	public LibraryClient(String userId) {
		// Initialize client and connect to the server
		this.userId = userId;

		// Fetch user information from the server
		fetchUserInformation();
	}

	
	public boolean updateRating(CatalogItem item) {
	    try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
	         PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
	         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
	         
	        // Create a JSON object to send the request to update the rating
	        JSONObject requestJson = new JSONObject();
	        requestJson.put("userId", userId); // Include the user ID in the request
	        requestJson.put("itemId", item.getItemId()); // Include the item ID in the request
	        requestJson.put("rating", item.getDummyRating()); // Include the new rating in the request
	        
	        // Send the POST request to the server to update the rating
	        out.println("POST /update-rating HTTP/1.1");
	        out.println("Host: " + SERVER_IP);
	        out.println("Content-Type: application/json");
	        out.println("Content-Length: " + requestJson.toString().length());
	        out.println();
	        out.println(requestJson.toString());
	        
	        // Read and parse the server's response
	        String line;
	        while ((line = in.readLine()) != null && !line.isEmpty()) {
	            System.out.println(line); // Debugging: print headers
	        }
	        
	        // Read the JSON response body
	        StringBuilder responseBuilder = new StringBuilder();
	        while ((line = in.readLine()) != null) {
	            responseBuilder.append(line);
	        }
	        JSONObject jsonResponse = new JSONObject(responseBuilder.toString());
	        
	        // Check the response status
	        String status = jsonResponse.optString("status", "error");
	        
	        // Return true if the update operation was successful, false otherwise
	        return "success".equals(status);
	        
	    } catch (IOException e) {
	        e.printStackTrace();
	        showErrorAlert("Connection Error", "Failed to connect to the server. Please try again later.");
	        return false;
	    } catch (JSONException e) {
	        e.printStackTrace();
	        showErrorAlert("Parsing Error", "Failed to parse server response.");
	        return false;
	    }
	}
	
	public List<String> fetchAllChats() {
	    List<String> chatMessages = new ArrayList<>();
	    
	    try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
	         PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
	         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
	         
	        // Send a GET request to fetch all chats
	        out.println("GET /chats HTTP/1.1");
	        out.println("Host: " + SERVER_IP);
	        out.println();

	        // Skip HTTP response headers
	        String line;
	        while ((line = in.readLine()) != null && !line.isEmpty()) {
	            System.out.println(line); // Debugging: print headers
	        }

	        // Read JSON body
	        StringBuilder jsonResponseBuilder = new StringBuilder();
	        while ((line = in.readLine()) != null) {
	            jsonResponseBuilder.append(line);
	        }

	        // Parse the JSON response
	        JSONObject jsonResponse = new JSONObject(jsonResponseBuilder.toString());
	        System.out.println("chats :" + jsonResponse);
	        // Check if the JSON response contains a "chats" array
	        if (jsonResponse.has("chats")) {
	            JSONArray chatsArray = jsonResponse.getJSONArray("chats");

	            // Parse each chat message from the JSON array
	            for (int i = 0; i < chatsArray.length(); i++) {
	                JSONObject chatJson = chatsArray.getJSONObject(i);

	                // Parse the username, message, and date sent from the JSON object
	                String username = chatJson.optString("username");
	                String message = chatJson.optString("message");
	                String dateSent = chatJson.optString("dateSent");

	                // Format the chat message with date, username, and message
	                String formattedMessage = dateSent + " | " + username + ": " + message;
	                
	                // Add the formatted chat message to the list
	                chatMessages.add(formattedMessage);
	            }
	        } else {
	            // Handle the case where "chats" array is missing
	            Platform.runLater(() -> showErrorAlert("Error", "Failed to load chats."));
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	        Platform.runLater(this::showConnectionErrorAlert);
	    } catch (JSONException e) {
	        e.printStackTrace();
	        Platform.runLater(() -> showErrorAlert("Parsing Error", "Failed to parse server response."));
	    }

	    return chatMessages;
	}
	
	

	public boolean sendChatMessage(String userId, String message) {
	    if (message == null || message.isEmpty()) {
	        System.out.println("Message is empty. Please enter a message before sending.");
	        return false;
	    }

	    try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
	         PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
	         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

	        // Create a JSON object for the chat message
	        JSONObject requestJson = new JSONObject();
	        requestJson.put("userId", userId);
	        requestJson.put("message", message);

	        // Send the HTTP request to the server
	        out.println("POST /send-chat-message HTTP/1.1");
	        out.println("Host: " + SERVER_IP);
	        out.println("Content-Type: application/json");
	        out.println("Content-Length: " + requestJson.toString().length());
	        out.println();
	        out.println(requestJson.toString());

	        // Read the server response and skip the status and headers
	        String line;
	        while ((line = in.readLine()) != null) {
	            // If we've found the start of the JSON response, break the loop
	            if (line.trim().startsWith("{") || line.trim().startsWith("[")) {
	                break;
	            }
	        }

	        // If line is null or empty, something went wrong, return false
	        if (line == null || line.isEmpty()) {
	            System.out.println("No valid response from server.");
	            return false;
	        }

	        // Parse the response JSON
	        JSONObject jsonResponse = new JSONObject(line);
	        
	        // Log the server response for debugging purposes
	        System.out.println("Server response: " + jsonResponse);

	        // Check the response status and return the appropriate boolean
	        return jsonResponse.optString("status", "").equals("success");

	    } catch (IOException e) {
	        e.printStackTrace();
	        System.out.println("IOException occurred while trying to send chat message.");
	        return false;
	    } catch (JSONException e) {
	        e.printStackTrace();
	        System.out.println("JSONException occurred while parsing server response.");
	        return false;
	    }
	}
	
	
	private void fetchUserInformation() {
		// Show loading dialog
		ProgressDialog progressDialog = new ProgressDialog();
		progressDialog.show();
		try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

			// Send a request to the server for user information
			out.println("GET /user/" + userId + " HTTP/1.1");
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

			// Parse the JSON response
			JSONObject jsonResponse = new JSONObject(jsonResponseBuilder.toString());
			System.out.println("jsonrespnse: " + jsonResponse);
			// Check if the JSON response contains user information
			if (jsonResponse.has("username")) {
				// Set the username field with the received value
				username = jsonResponse.getString("username");
				System.out.println(username);
			} else {
				Platform.runLater(() -> showErrorAlert("Error", "Failed to fetch user information."));
			}
		} catch (IOException e) {
			e.printStackTrace();
			Platform.runLater(this::showConnectionErrorAlert);
		} catch (JSONException e) {
			e.printStackTrace();
			Platform.runLater(() -> showErrorAlert("Parsing Error", "Failed to parse server response."));
		} finally {
			Platform.runLater(progressDialog::close);
		}
	}

	public String getUsername() {
		return username;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}

	public List<BorrowedItem> getBorrowedHistory() {
		List<BorrowedItem> borrowedHistory = new ArrayList<>();
		ProgressDialog progressDialog = new ProgressDialog();
		Platform.runLater(() ->progressDialog.show());

		try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

			// Send request to the server to get the borrowed history for the user
			out.println("GET /borrowed-history/" + userId + " HTTP/1.1");
			out.println("Host: " + SERVER_IP);
			out.println();

			// Skip HTTP response headers
			String line;
			while ((line = in.readLine()) != null && !line.isEmpty()) {
				System.out.println("line " + line); // Debugging: print headers
			}

			// Read the JSON body
			StringBuilder jsonResponseBuilder = new StringBuilder();
			while ((line = in.readLine()) != null) {
				jsonResponseBuilder.append(line);
			}

			// Parse the JSON response
			JSONObject jsonResponse = new JSONObject(jsonResponseBuilder.toString());
			System.out.println("the response: " + jsonResponse);
			// Check if the JSON response contains a "borrowedHistory" array
			if (jsonResponse.has("borrowedHistory")) {
				JSONArray borrowedHistoryArray = jsonResponse.getJSONArray("borrowedHistory");

				// Parse each borrowed item from the JSON array
				for (int i = 0; i < borrowedHistoryArray.length(); i++) {
					JSONObject borrowedItemJson = borrowedHistoryArray.getJSONObject(i);
					System.out.println("json item " +borrowedItemJson);
					// Check if the necessary keys exist in the JSON object
					if (borrowedItemJson.has("title") && borrowedItemJson.has("dateBorrowed")) {

						// Extract data from the JSON object
						String title = borrowedItemJson.getString("title");
						String author = borrowedItemJson.getString("author");
						String type = borrowedItemJson.getString("type");
						String borrower = borrowedItemJson.optString("borrower","You");
						String dueDate = borrowedItemJson.optString("dueDate","today");
						String dateBorrowed = borrowedItemJson.getString("dateBorrowed");
						ObjectId borrowerId = new ObjectId(userId);

						// Create a BorrowedItem instance

						String dateReturned = borrowedItemJson.optString("dateReturned","Not yet returned");

						// Create a BorrowedItem instance from the JSON object

						BorrowedItem borrowedItem = new BorrowedItem(title, author, type, borrower, dueDate,
								dateBorrowed, borrowerId , dateReturned);


						// Add the borrowed item to the borrowed history list
						borrowedHistory.add(borrowedItem);
					}
				} 
			} else {
				// Handle the case where "borrowedHistory" array is missing
				Platform.runLater(() -> showErrorAlert("Error", "Failed to load borrowed history."));
			}
		} catch (IOException e) {
			e.printStackTrace();
			Platform.runLater(this::showConnectionErrorAlert);
		} catch (JSONException e) {
			e.printStackTrace();
			Platform.runLater(() -> showErrorAlert("Parsing Error", "Failed to parse server response."));
		} finally {
			Platform.runLater(progressDialog::close);
		}

		return borrowedHistory;
	}

	public boolean returnItem(CatalogItem item) {
		try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

			  // Create a JSON object to send the borrowing request
	        JSONObject requestJson = new JSONObject();
	        requestJson.put("userId", userId); // Include the user ID in the request
	        requestJson.put("itemId", item.getItemId()); // Include the item ID in the request

			// Send the POST request to the server to return an item
			out.println("POST /return-item HTTP/1.1");
			out.println("Host: " + SERVER_IP);
			out.println("Content-Type: application/json");
			out.println("Content-Length: " + requestJson.toString().length());
			out.println();
			out.println(requestJson.toString());

			// Read and parse the server's response
			String line;
			while ((line = in.readLine()) != null && !line.isEmpty()) {
				System.out.println(line); // Debugging: print headers
			}

			// Read the JSON response body
			StringBuilder responseBuilder = new StringBuilder();
			while ((line = in.readLine()) != null) {
				responseBuilder.append(line);
			}
			JSONObject jsonResponse = new JSONObject(responseBuilder.toString());

			// Check the response status
			String status = jsonResponse.optString("status", "error");

			return "success".equals(status);

		} catch (IOException e) {
			e.printStackTrace();
			showErrorAlert("Connection Error", "Failed to connect to the server. Please try again later.");
			return false;
		} catch (JSONException e) {
			e.printStackTrace();
			showErrorAlert("Parsing Error", "Failed to parse server response.");
			return false;
		}
	}

	public List<CatalogItem> getCatalogItems() {
	    List<CatalogItem> catalogItems = new ArrayList<>();
	    ProgressDialog progressDialog = new ProgressDialog();
	    progressDialog.show();

	    try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
	         PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
	         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

	        // Send request to the server
	        out.println("GET /catalog HTTP/1.1");
	        out.println("Host: " + SERVER_IP);
	        out.println();

	        // Skip HTTP response headers
	        String line;
	        while ((line = in.readLine()) != null && !line.isEmpty()) {
	            System.out.println(line); // Debugging: print headers
	        }

	        // Read JSON body
	        StringBuilder jsonResponseBuilder = new StringBuilder();
	        while ((line = in.readLine()) != null) {
	            jsonResponseBuilder.append(line);
	        }

	        // Parse JSON response
	        JSONObject jsonResponse = new JSONObject(jsonResponseBuilder.toString());

	        // Check if the JSON response contains a "catalog" array
	        if (jsonResponse.has("catalog")) {
	            JSONArray catalogArray = jsonResponse.getJSONArray("catalog");

	            // Parse each item from the JSON array
	            for (int i = 0; i < catalogArray.length(); i++) {
	                JSONObject catalogItemJson = catalogArray.getJSONObject(i);

	                // Parse the fields using optString and optBoolean methods
	                String title = catalogItemJson.optString("title", null);
	                String author = catalogItemJson.optString("author", null);
	                String type = catalogItemJson.optString("type", null);
	                boolean available = catalogItemJson.optBoolean("available", false);

	                // Use optString with default values to handle missing keys
	                String itemId = catalogItemJson.optString("_id", null);
	                int holdList = catalogItemJson.optInt("holdList",0);
					int rating = catalogItemJson.optInt("rating",0);

	                // Create CatalogItem instance from the parsed data
	                CatalogItem catalogItem = new CatalogItem(title, author, type, available, itemId, rating,holdList);

	                // Add the CatalogItem to the list
	                catalogItems.add(catalogItem);
	            }

	            // Store the original catalog list for future searches
	            originalCatalogItems = new ArrayList<>(catalogItems);
	        } else {
	            // Handle the case where "catalog" array is missing
	            Platform.runLater(() -> showErrorAlert("Error", "Failed to load catalog items."));
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	        Platform.runLater(this::showConnectionErrorAlert);
	    } catch (JSONException e) {
	        e.printStackTrace();
	        Platform.runLater(() -> showErrorAlert("Parsing Error", "Failed to parse server response."));
	    } finally {
	        Platform.runLater(progressDialog::close);
	    }

	    return catalogItems;
	}
	
	public boolean addItemToHoldList(CatalogItem item) {
	    // Establish a connection to the server and set up input and output streams
	    try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
	         PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
	         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

	        // Create a JSON object to send the request to add the item to the hold list
	        JSONObject requestJson = new JSONObject();
	        requestJson.put("userId", userId); // Include the user ID in the request
	        requestJson.put("itemId", item.getItemId()); // Include the item ID in the request

	        // Send the POST request to the server to add the item to the hold list
	        out.println("POST /add-to-hold-list HTTP/1.1");
	        out.println("Host: " + SERVER_IP);
	        out.println("Content-Type: application/json");
	        out.println("Content-Length: " + requestJson.toString().length());
	        out.println();
	        out.println(requestJson.toString());

	        // Read and parse the server's response
	        String line;
	        while ((line = in.readLine()) != null && !line.isEmpty()) {
	            System.out.println(line); // Debugging: print headers
	        }

	        // Read the JSON response body
	        StringBuilder responseBuilder = new StringBuilder();
	        while ((line = in.readLine()) != null) {
	            responseBuilder.append(line);
	        }
	        JSONObject jsonResponse = new JSONObject(responseBuilder.toString());

	        // Check the response status
	        String status = jsonResponse.optString("status", "error");

	        // Return true if the operation was successful, false otherwise
	        return "success".equals(status);

	    } catch (IOException e) {
	        e.printStackTrace();
	        // Handle connection errors
	        showErrorAlert("Connection Error", "Failed to connect to the server. Please try again later.");
	        return false;
	    } catch (JSONException e) {
	        e.printStackTrace();
	        // Handle JSON parsing errors
	        showErrorAlert("Parsing Error", "Failed to parse server response.");
	        return false;
	    }
	}

	public List<CatalogItem> getBorrowedItems() {
		List<CatalogItem> borrowedItems = new ArrayList<>();
		ProgressDialog progressDialog = new ProgressDialog();
		progressDialog.show();

		try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

			// Send request to the server
			out.println("GET /borrowed/" + userId + " HTTP/1.1");
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

			// Check if the JSON response contains a "borrowed" array
			if (jsonResponse.has("borrowed")) {
				JSONArray borrowedArray = jsonResponse.getJSONArray("borrowed");
				System.out.println("json array: " + borrowedArray);
				// Parse each item from the JSON array
				for (int i = 0; i < borrowedArray.length(); i++) {
					JSONObject borrowedItemJson = borrowedArray.getJSONObject(i);

					// Check if the necessary keys exist in the JSON object
					if (borrowedItemJson.has("title") && borrowedItemJson.has("author") && borrowedItemJson.has("type")
							) {

						// Create a BorrowedItem instance from the JSON object
						String title = borrowedItemJson.getString("title");
						String author = borrowedItemJson.getString("author");
						String type = borrowedItemJson.getString("type");
						String borrower = borrowedItemJson.optString("borrower","You");
						String dueDate = borrowedItemJson.optString("due_date","No due date");
						String dateBorrowed = borrowedItemJson.optString("date_borrowed","No date available");
						ObjectId borrowerId = new ObjectId(borrowedItemJson.optString("borrowerId", userId));
						String itemId = borrowedItemJson.getString("_id");
						String filePath = borrowedItemJson.getString("file_path");
						int holdList = borrowedItemJson.optInt("holdList",0);
						int rating = borrowedItemJson.optInt("rating",0);
						

						// Create a BorrowedItem instance
						CatalogItem borrowedItem = new CatalogItem(title, author, type, false, itemId, rating, holdList);
						borrowedItem.setBorrower(borrower);
						borrowedItem.setDueDate(dueDate);
						borrowedItem.setDateBorrowed(dateBorrowed);
						borrowedItem.setBorrowerId(borrowerId);
						borrowedItem.setFilePath(filePath);

						// Add the borrowed item to the list
						borrowedItems.add(borrowedItem);
					}
				}
			} else {
				// Handle the case where "borrowed" array is missing
				Platform.runLater(() -> showErrorAlert("Error", "Failed to load borrowed items."));
			}
		} catch (IOException e) {
			e.printStackTrace();
			Platform.runLater(this::showConnectionErrorAlert);
		} catch (JSONException e) {
			e.printStackTrace();
			Platform.runLater(() -> showErrorAlert("Parsing Error", "Failed to parse server response."));
		} finally {
			Platform.runLater(progressDialog::close);
		}

		return borrowedItems;
	}

	public List<CatalogItem> searchCatalog(String query, TableView<CatalogItem> catalogTableView) {
	    // Ensure originalCatalogItems is not null
	    if (originalCatalogItems == null) {
	        return new ArrayList<>();
	    }

	    // Create a list to hold the filtered items
	    List<CatalogItem> filteredItems = new ArrayList<>();

	    // Iterate through all the catalog items and filter based on the query
	    for (CatalogItem item : originalCatalogItems) {
	        // Convert the item's title, author, and type to lowercase for case-insensitive
	        // search
	        String title = item.getTitle().toLowerCase();
	        String author = item.getAuthor().toLowerCase();
	        String type = item.getType().toLowerCase();

	        // Convert the query to lowercase for case-insensitive search
	        String lowerCaseQuery = query.toLowerCase();

	        // Check if the query is a substring of the item's title, author, or type
	        if (title.contains(lowerCaseQuery) || author.contains(lowerCaseQuery) || type.contains(lowerCaseQuery)) {
	            // If a match is found, add the item to the filtered list
	            filteredItems.add(item);
	        }
	    }

	    // Return the filtered list of items
	    return filteredItems;
	}

	public boolean borrowItem(CatalogItem item) {
		// Establish a connection to the server and set up input and output streams
		try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

			// Create a JSON object to send the borrowing request
			JSONObject requestJson = new JSONObject();
			requestJson.put("userId", userId); // Include the user ID in the request
			requestJson.put("itemId", item.getItemId()); // Include the item ID in the request

			// Send the POST request to the server to borrow an item
			out.println("POST /borrow-item HTTP/1.1");
			out.println("Host: " + SERVER_IP);
			out.println("Content-Type: application/json");
			out.println("Content-Length: " + requestJson.toString().length());
			out.println();
			out.println(requestJson.toString());

			// Read the server's response
			String line;
			while ((line = in.readLine()) != null && !line.isEmpty()) {
				System.out.println(line); // Debugging: print headers
			}

			// Parse the server's response
			StringBuilder response = new StringBuilder();
			while ((line = in.readLine()) != null) {
				response.append(line);
			}

			JSONObject jsonResponse = new JSONObject(response.toString());

			// Check the response status
			String status = jsonResponse.getString("status");

			// Return true if the borrowing operation was successful, otherwise false
			return status.equals("success");

		} catch (IOException e) {
			e.printStackTrace();
			// Handle connection errors
			showErrorAlert("Connection Error", "Failed to connect to the server. Please try again later.");
			return false;
		} catch (JSONException e) {
			e.printStackTrace();
			// Handle JSON parsing errors
			showErrorAlert("Parsing Error", "Failed to parse server response.");
			return false;
		}
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
