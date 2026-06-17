package com.dobidemo.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

public class TS02_TC01_ValidateStartingBalance {

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

        // PRE-CONDITION 1: Log into DobiDemo System
        driver.get("http://softwaretesting.umt.edu.my/dobidemo/login.php");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        driver.findElement(By.name("username")).sendKeys("team_k1_5");
        driver.findElement(By.name("password")).sendKeys("b4tuen4m");

        // Specific form login submission targeting
        driver.findElement(By.cssSelector("form button[type='submit']")).click();

        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("login.php")));
    }

    @Test
    @DisplayName("TS02-TC01: Validate Starting Balance rejects negative value (-0.01)")
    public void testNegativeStartingBalanceRejected() {

        // PROCEDURE 1: Navigate directly to the Add Supplier form route
        driver.get("http://softwaretesting.umt.edu.my/dobidemo/index.php?page=view_supplier&do=add");

        // Guard Assert: Verify user didn't get kicked back out to login
        assertFalse(driver.getCurrentUrl().contains("login.php"),
                "FAIL: Session lost — redirected back to login page.");

        // PROCEDURE 2: Wait for form elements to load and populate entries
        WebElement supplierNameField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.name("supplier_name"))
        );

        supplierNameField.sendKeys("BubbleWrap Co.");
        driver.findElement(By.name("supplier_phone")).sendKeys("0198887766");
        driver.findElement(By.name("supplier_desc")).sendKeys("Packaging Material Supplies");

        // Input the invalid negative boundary metric
        WebElement balanceField = driver.findElement(By.name("supplier_balance"));
        balanceField.clear();
        balanceField.sendKeys("-0.01");

        // PROCEDURE 3: Click the 'Save' submission button
        WebElement submitButton = driver.findElement(By.cssSelector("form button[type='submit'], #submit, .btn-primary"));
        submitButton.click();

        // --- STEP 7: MULTI-LAYER VALIDATION CHECK (JUNIT 5) ---
        
        // Check A: Handle if a JavaScript Alert popped up to block submission
        try {
            Alert alert = driver.switchTo().alert();
            String alertText = alert.getText();
            alert.accept(); 
            
            assertTrue(alertText.toLowerCase().contains("error") || alertText.toLowerCase().contains("invalid"),
                    "FAIL: Alert popped up but did not indicate a validation failure. Message: " + alertText);
            return; 
        } catch (NoAlertPresentException e) {
            // No alert pop-up active; moving to check page body behaviors
        }

        // Check B: Trap system bug where data is wrongly saved and redirected to a profile view page
        String currentUrl = driver.getCurrentUrl();
        if (currentUrl.contains("id=") && !currentUrl.contains("do=add")) {
            fail("FAIL: System accepted a negative balance (-0.01) and successfully saved the profile record at: " + currentUrl);
        }

        // Check C: Fallback to look for dynamic error text banners on-screen
        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//*[contains(text(),'Error') or contains(text(),'invalid') or contains(text(),'Invalid') or contains(text(),'cannot be negative')]")
                )
        );

        assertTrue(errorMsg.isDisplayed(), 
                "FAIL: Expected error text message for negative balance (-0.01) was not visible on the layout.");
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}