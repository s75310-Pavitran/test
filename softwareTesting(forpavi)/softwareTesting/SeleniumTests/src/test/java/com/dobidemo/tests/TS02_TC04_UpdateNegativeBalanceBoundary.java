package com.dobidemo.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

public class TS02_TC04_UpdateNegativeBalanceBoundary {

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
    @DisplayName("TS02-TC04: Update the supplier account balance to an invalid boundary limit value (-0.01)")
    public void testUpdateNegativeBalanceBoundaryRejected() throws InterruptedException {

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

        // TEST PROCEDURE 1 & 2: Click into balance field, clear out numerical tracking characters and enter "-0.01"
        WebElement balanceField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.name("supplier_balance"))
        );
        balanceField.clear(); 
        balanceField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE); 
        balanceField.sendKeys("-0.01");

        // TEST PROCEDURE 3: Click the blue [Update] button
        WebElement updateButton = driver.findElement(By.cssSelector("form button[type='submit'], #submit, .btn-primary, .btn-info"));
        updateButton.click();

        // --- EXPECTED OUTPUT VERIFICATION ---
        Thread.sleep(1500); // Wait briefly for the browser reload behavior to finish processing

        // 1. Guard Check: Check if the application accepted the illegal negative value and saved it anyway
        String currentUrl = driver.getCurrentUrl();
        if (currentUrl.contains("id=") && !currentUrl.contains("do=edit")) {
            // CRITICAL DEFECT: The system committed a negative balance modification!
            fail("FAIL: Security validation bug! The web application successfully updated the record even though the Balance input field was changed to an illegal negative boundary value (-0.01).");
        }

        // 2. Fallback check: Look at the page context to confirm an error warning is visible
        String pageSource = driver.getPageSource();
        boolean hasErrorText = pageSource.contains("Error") || 
                               pageSource.contains("invalid") || 
                               pageSource.contains("Invalid") || 
                               pageSource.contains("balance");

        assertTrue(hasErrorText, 
                "FAIL: The application blocked the form redirect but failed to display any visible validation error dialog message (e.g., 'Error: Invalid balance') on screen.");
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}