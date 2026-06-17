package com.dobidemo.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

public class TS02_TC04_BlankMandatoryFieldUpdate {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // Setup Step: Log into DobiDemo System
        driver.get("http://softwaretesting.umt.edu.my/dobidemo/login.php");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        driver.findElement(By.name("username")).sendKeys("team_k1_5");
        driver.findElement(By.name("password")).sendKeys("b4tuen4m");
        driver.findElement(By.cssSelector("form button[type='submit']")).click();

        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("login.php")));
    }

    @Test
    @DisplayName("TS02-TC04: Attempt to clear a mandatory text field during profile modification")
    public void testBlankMandatoryFieldRejected() throws InterruptedException {

        // PRE-CONDITION: Navigate to primary management module to locate "CleanPro"
        driver.get("http://softwaretesting.umt.edu.my/dobidemo/index.php?page=suppliers");
        wait.until(ExpectedConditions.urlContains("page=suppliers"));

        WebElement supplierLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//table//a[contains(text(),'CleanPro')]")
        ));
        supplierLink.click();
        
        wait.until(ExpectedConditions.urlContains("page=view_supplier"));

        // Trigger Edit configuration route panel
        WebElement editButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(),'Edit') or contains(@href, 'do=edit') or contains(@class, 'btn-warning')]")
        ));
        editButton.click();

        // TEST PROCEDURE 1: Select the contents of the Phone Number field and clear it out completely
        WebElement phoneField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.name("supplier_phone"))
        );
        phoneField.clear(); 
        phoneField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE); 

        // Update description details placeholder
        WebElement descField = driver.findElement(By.name("supplier_desc"));
        descField.clear();
        descField.sendKeys("Updating records without phone data.");

        // TEST PROCEDURE 2: Click the blue [Update] button
        WebElement updateButton = driver.findElement(By.cssSelector("form button[type='submit'], #submit, .btn-primary, .btn-info"));
        updateButton.click();

        // --- EXPECTED OUTPUT VERIFICATION ---
        Thread.sleep(1500); // Wait briefly for the browser reload behavior to complete

        // 1. Guard Check: Check if the application accepted the empty field and redirected to a saved page layout
        String currentUrl = driver.getCurrentUrl();
        if (currentUrl.contains("id=") && !currentUrl.contains("do=edit")) {
            // CRITICAL FAILURE: The application saved the update even though the field was blank!
            fail("FAIL: Security validation bug detected! The web application successfully processed the modification request and saved the record even though the mandatory Phone input field was left blank.");
        }

        // 2. Fallback check if it stayed on the page: Look at the page context to ensure an error message is visible
        String pageSource = driver.getPageSource();
        boolean hasErrorText = pageSource.contains("Error") || 
                               pageSource.contains("required") || 
                               pageSource.contains("Required") || 
                               pageSource.contains("Phone");

        // If it didn't redirect but also didn't display any message warning the user, fail the test
        assertTrue(hasErrorText, 
                "FAIL: The application blocked the redirect but failed to display any visible validation error message (e.g., 'Error: Phone required') on the layout screen.");
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}