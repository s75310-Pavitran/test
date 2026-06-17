package com.dobidemo.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

public class TS02_TC04_ModifySupplierProfile {

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
    @DisplayName("TS02-TC04: Modify valid field updates for an active supplier profile")
    public void testModifySupplierProfileDetails() {

        // PRE-CONDITION: Navigate to the overview dashboard first to find "CleanPro"
        driver.get("http://softwaretesting.umt.edu.my/dobidemo/index.php?page=suppliers");
        wait.until(ExpectedConditions.urlContains("page=suppliers"));

        // Locate "CleanPro" link row to open its detailed profile mode context
        WebElement supplierLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//table//a[contains(text(),'CleanPro')]")
        ));
        supplierLink.click();
        
        wait.until(ExpectedConditions.urlContains("page=view_supplier"));

        // Click the 'Edit' or modification button inside the detail panel layout
        // (Alternatively, routes often appending '&do=edit' to the current view URL string)
        WebElement editButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(),'Edit') or contains(@href, 'do=edit') or contains(@class, 'btn-warning')]")
        ));
        editButton.click();

        // TEST PROCEDURE 1: Select the phone field container, clear out old value, and insert new metric
        WebElement phoneField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.name("supplier_phone"))
        );
        phoneField.clear();
        phoneField.sendKeys("0127778899");

        // TEST PROCEDURE 2: Edit description details array field context values
        WebElement descField = driver.findElement(By.name("supplier_desc"));
        descField.clear();
        descField.sendKeys("Primary Supplier (New Office Address)");

        // TEST PROCEDURE 3: Click the blue [Update] / save confirmation action button
        WebElement updateButton = driver.findElement(By.cssSelector("form button[type='submit'], #submit, .btn-primary, .btn-info"));
        updateButton.click();

        // --- EXPECTED OUTPUT VERIFICATION (JUNIT 5 ASSERTIONS) ---
        
        // 1. Force navigation back to the primary suppliers list view layout
        driver.get("http://softwaretesting.umt.edu.my/dobidemo/index.php?page=suppliers");
        wait.until(ExpectedConditions.urlContains("page=suppliers"));

        // 2. Locate the row text block for "CleanPro" to evaluate update sync accuracy
        WebElement nameLink = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//table//a[contains(text(),'CleanPro')]")
        ));
        WebElement tableRow = nameLink.findElement(By.xpath("./ancestor::tr"));
        String rowText = tableRow.getText();

        // 3. Instantly verify that the updated value changes appear live in the datagrid
        assertTrue(rowText.contains("0127778899"), 
                "FAIL: The updated phone number string value '0127778899' was not successfully verified inside the data row layout. Found instead: " + rowText);
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}