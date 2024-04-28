package library.client;

import java.io.Serializable;

public class RegistrationRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	private String username;
	private String password;
	private String confirmPassword;

	public RegistrationRequest(String username, String password, String confirmPassword) {
		this.username = username;
		this.password = password;
		this.confirmPassword = confirmPassword;
	}

	// Getters and setters (optional)
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}
}
