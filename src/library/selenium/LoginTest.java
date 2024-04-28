package library.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class LoginTest {
	  private WebDriver driver;

	  @BeforeTest
	    public void setUp() {
	        // Set the path to your WebDriver executable (e.g., GeckoDriver for Firefox)
	        System.setProperty("webdriver.gecko.driver", "C:\\Users\\durua\\Downloads\\geckodriver-v0.34.0-win64\\geckodriver.exe");

	        // Initialize the WebDriver
	        driver = new FirefoxDriver();
	    }
	  
	  @Test
	    public void testLogin() {
	        // Navigate to the login page
	        driver.get("http://localhost:8080/login"); // Replace with your actual URL

	        // Find the username and password fields
	        WebElement usernameField = driver.findElement(By.id("usernameField"));
	        WebElement passwordField = driver.findElement(By.id("passwordField"));

	        // Enter valid credentials and attempt to login
	        usernameField.sendKeys("testuser");
	        passwordField.sendKeys("testpassword");
	        WebElement loginButton = driver.findElement(By.id("loginButton"));
	        loginButton.click();

	        // Verify that the login was successful by checking the presence of a specific element
	        // in the library dashboard or the expected URL change
	        boolean loginSuccess = driver.getCurrentUrl().contains("/library-dashboard");
	        Assert.assertTrue(loginSuccess, "Login should be successful");

	        // If you want to test an unsuccessful login, provide invalid credentials
	        usernameField.clear();
	        passwordField.clear();
	        usernameField.sendKeys("invaliduser");
	        passwordField.sendKeys("invalidpassword");
	        loginButton.click();

	        // Verify that the login failed and an appropriate error message is displayed
	        WebElement errorAlert = driver.findElement(By.xpath("//div[@class='alert alert-danger']")); // Adjust the selector as per your alert structure
	        Assert.assertTrue(errorAlert.isDisplayed(), "An error alert should be displayed for invalid credentials");
	    }
	  
	  @AfterTest
	    public void tearDown() {
	        // Close the browser
	        if (driver != null) {
	            driver.quit();
	        }
	    }

}
