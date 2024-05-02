package brokenlinks;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class BrokenLinksCheck {

	static Properties prop;

	public static void loadPropertiesFile() throws IOException {

		prop = new Properties();
		File propFile = new File(System.getProperty("user.dir") + "\\src\\main\\java\\Config\\config.properties");
		try {
			FileInputStream fis = new FileInputStream(propFile);
			prop.load(fis);
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) throws InterruptedException, IOException {

		loadPropertiesFile();
		// Set the path to your ChromeDriver executable
		WebDriverManager.chromedriver().setup();

		ChromeOptions options = new ChromeOptions();
		options.addArguments("--disable-popup-blocking");

		// Initialize ChromeDriver
		WebDriver driver = new ChromeDriver(options);

		// Navigate to the webpage
		driver.get(prop.getProperty("url"));
		driver.manage().window().maximize();

		// Set a wait time for the driver to find elements
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

		// Get all links on the page
		List<WebElement> pageLinks = driver.findElements(By.tagName("a"));
		int linkcount = pageLinks.size();
		//System.out.println(linkcount);

		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
		String fileName = ".\\file\\BrokenLinksCheck_" + formatter.format(now) + ".txt";

		try {
			// Create a file output stream
			FileOutputStream fos = new FileOutputStream(new File(fileName));
			// Redirect standard output to the file
			PrintStream ps = new PrintStream(fos);
			System.setOut(ps);

			//System.out.println("BrokenLinksCheck");
			
			String[] links = new String[linkcount];
			
			
			// Check each link for a 404 error based on the title
			for (int i = 0; i < linkcount; i++) {
				links[i] = pageLinks.get(i).getAttribute("href");

				// Skip if link is null or empty
				if (links[i] == null || links[i].isEmpty()) {
					continue;
				}
				// Skip the sign-out link
				String logout = prop.getProperty("logouturl");

				if (logout.equals(links[i])) {
					continue;
				}
				// Open the link in a new tab
				((ChromeDriver) driver).executeScript("window.open(arguments[0]);", links[i]);

				// Switch to the newly opened tab
				String originalHandle = driver.getWindowHandle();

				for (String handle : driver.getWindowHandles()) {
					if (!handle.equals(originalHandle)) {
						driver.switchTo().window(handle);
					}
				}

				// Get the title of the page
				String pageTitle = driver.getTitle();
				String output;

				// Check if the title indicates a 404 error page
				if (pageTitle.contains("404")) {
					output = "404 Error: Page " + links[i] + " not found";

					WebDriverWait wait = new WebDriverWait(driver, 40);
					wait.until(ExpectedConditions.titleContains("Homepage"));

					System.out.println(output);
					System.out.println(driver.getTitle());

				} else {
					System.out.println(pageTitle);
					output = "Page " + links[i] + " is accessible";
					System.out.println(output);
				}

				// Close the tab
				driver.close();

				// Switch back to the original tab
				driver.switchTo().window(originalHandle);

			} // Close the file output stream
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			// Quit the WebDriver
			if (driver != null) {
				driver.quit();
			}
		}
	}
}
