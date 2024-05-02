package brokenlinks;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Ga4URL {
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

	public static void main(String[] args) throws IOException {
		loadPropertiesFile();

		// Provide the correct path to your Excel file
		String excelFilePath = "C:\\Users\\catal\\eclipse\\zrpl\\project\\TestData\\data.xlsx"; // Corrected file path

		// Set the path to your ChromeDriver executable
		WebDriverManager.chromedriver().setup();

		ChromeOptions options = new ChromeOptions();
		options.addArguments("--disable-popup-blocking");

		// Initialize ChromeDriver
		WebDriver driver = new ChromeDriver(options);

		try (FileInputStream fis = new FileInputStream(new String(excelFilePath.getBytes(), StandardCharsets.UTF_8));
				Workbook workbook = new XSSFWorkbook(fis)) { // Use XSSFWorkbook for .xlsx files
			Sheet sheet = workbook.getSheetAt(0); // Assuming URLs are in the first sheet

			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
			String fileName = ".\\file\\Ga4URL_" + formatter.format(now) + ".txt";

			try {
				// Create a file output stream
				FileOutputStream fos = new FileOutputStream(new File(fileName));
				// Redirect standard output to the file
				PrintStream ps = new PrintStream(fos);
				System.setOut(ps);

				for (Row row : sheet) {
					Cell cell = row.getCell(0); // Assuming URLs are in the first column
					if (cell != null && cell.getCellType() == CellType.STRING) {
						driver.get(prop.getProperty("url"));
						String Static = "https://zepp.zeppsandbox.com";
						String url1 = Static.concat(cell.getStringCellValue());
						// System.out.println("Opening URL: " + url1);
						driver.get(url1);
						driver.manage().window().maximize();
						driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

						String logout = prop.getProperty("logouturl");
						if (logout.equals(url1)) {
							continue;
						}
						String pageTitle = driver.getTitle();
						String output;

						// Check if the title indicates a 404 error page
						if (pageTitle.contains("404")) {
							output = "404 Error: Page " + url1 + " not found";
							WebDriverWait wait = new WebDriverWait(driver, 40);
							wait.until(ExpectedConditions.titleContains("Homepage"));

							System.out.println(output);
							System.out.println(driver.getTitle());

						} else {
							System.out.println(pageTitle);
							output = "Page " + url1 + " is accessible";
							System.out.println(output);
						}

					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				// Quit the WebDriver
				driver.quit();
			}
		}

	}
}
