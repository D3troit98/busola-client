package library.client;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class LibraryMember {

	private final StringProperty memberId;
	private final StringProperty username;
	private final StringProperty email;
	private final StringProperty status;

	public LibraryMember(String memberId, String username, String email, String status) {
		this.memberId = new SimpleStringProperty(memberId);
		this.username = new SimpleStringProperty(username);
		this.email = new SimpleStringProperty(email);
		this.status = new SimpleStringProperty(status);

	}

	// Getters and setters for the properties
	public StringProperty memberIdProperty() {
		return memberId;
	}

	public String getMemberId() {
		return memberId.get();
	}

	public void setMemberId(String memberId) {
		this.memberId.set(memberId);
	}

	public StringProperty usernameProperty() {
		return username;
	}

	public StringProperty statusProperty() {
		return status;
	}

	public String getUsername() {
		return username.get();
	}

	public void setUsername(String username) {
		this.username.set(username);
	}

	public StringProperty emailProperty() {
		return email;
	}

	public String getEmail() {
		return email.get();
	}

	public void setEmail(String email) {
		this.email.set(email);
	}

	public String getStatus() {
		return status.get();
	}

	public void setStatus(String status) {
		this.status.set(status);
	}

}
