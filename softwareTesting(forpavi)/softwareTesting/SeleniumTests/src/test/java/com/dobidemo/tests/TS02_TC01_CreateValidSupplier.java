package com.dobidemo.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

public class TS02_TC01_CreateValidSupplier {

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

        // PRE-CONDITION 1: User is already logged into DobiDemo System
        driver.get("http://softwaretesting.umt.edu.my/dobidemo/login.php");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        driver.findElement(By.name("username")).sendKeys("team_k1_5");
        driver.findElement(By.name("password")).sendKeys("b4tuen4m");
        driver.findElement(By.cssSelector("form button[type='submit']")).click();

        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("login.php")));
    }

    @Test
    @DisplayName("TS02-TC01: Create a new supplier record with valid registration info")
    public void testCreateValidSupplierProfile() {

        // PRE-CONDITION 2 & PROCEDURE 1: Direct navigation to the Supplier Registration Entry Form
        driver.get("http://softwaretesting.umt.edu.my/dobidemo/index.php?page=view_supplier&do=add");

        // TEST PROCEDURE 2: Fill in the Supplier Name, Phone Number, Description, and a valid Starting Balance
        WebElement supplierNameField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.name("supplier_name"))
        );
        
        // Applying Test Inputs
        supplierNameField.sendKeys("CleanPro Sdn. Bhd.");
        driver.findElement(By.name("supplier_phone")).sendKeys("0123456789");
        driver.findElement(By.name("supplier_desc")).sendKeys("Chemical and Soap Vendor");
        
        WebElement balanceField = driver.findElement(By.name("supplier_balance"));
        balanceField.clear();
        balanceField.sendKeys("150.00");

        // TEST PROCEDURE 3: Click the 'Save' submission button
        WebElement saveButton = driver.findElement(By.cssSelector("form button[type='submit'], #submit, .btn-primary"));
        saveButton.click();

        // Wait until the initial redirection action completes
        wait.until(ExpectedConditions.urlContains("id="));

        // --- EXTENDED PROCEDURAL STEP: NAVIGATE BACK TO TABLE SUMMARY LIST ---
        driver.get("http://softwaretesting.umt.edu.my/dobidemo/index.php?page=suppliers");
        wait.until(ExpectedConditions.urlContains("page=suppliers"));

        // --- EXPECTED OUTPUT VERIFICATION (JUNIT 5 ASSERTIONS) ---
        
        // 1. Verify that "CleanPro Sdn. Bhd." exists as an active hyperlinked name row inside the data table
        WebElement nameLink = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//table//a[contains(text(),'CleanPro Sdn. Bhd.')]")
        ));
        assertTrue(nameLink.isDisplayed(), "FAIL: Supplier name hyperlink was not displayed in the summary table view.");

        // 2. Locate parent table row to verify adjacent phone column string
        WebElement tableRow = nameLink.findElement(By.xpath("./ancestor::tr"));
        String rowText = tableRow.getText();

        // 3. Verify phone string is listed on the row
        assertTrue(rowText.contains("0123456789"), 
                "FAIL: Expected phone number '0123456789' was missing from the summary table row.");

        // 4. To fully confirm initial balance registration, click into the active hyperlink profile details
        nameLink.click();
        wait.until(ExpectedConditions.urlContains("page=view_supplier"));
        
        String profileDetailsText = driver.getPageSource();
        assertTrue(profileDetailsText.contains("150.00") || profileDetailsText.contains("150"), 
                "FAIL: Registered starting balance '150.00' was not found inside the supplier profile view details page.");
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}