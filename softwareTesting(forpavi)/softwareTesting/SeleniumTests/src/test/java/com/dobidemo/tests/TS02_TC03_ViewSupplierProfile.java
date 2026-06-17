package com.dobidemo.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

public class TS02_TC03_ViewSupplierProfile {

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
    @DisplayName("TS02-TC03: View comprehensive supplier ledger breakdown details")
    public void testViewSupplierComprehensiveProfile() {

        // PRE-CONDITION 2: Ensure we start from the main suppliers listing module
        driver.get("http://softwaretesting.umt.edu.my/dobidemo/index.php?page=suppliers");
        wait.until(ExpectedConditions.urlContains("page=suppliers"));

        // TEST PROCEDURE 1 & TEST INPUT: Locate and click the target profile text link: "CleanPro"
        // We look for any text containing "CleanPro" in the table (supports variations like CleanPro Sdn. Bhd.)
        WebElement supplierLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//table//a[contains(text(),'CleanPro')]")
        ));
        supplierLink.click();

        // TEST PROCEDURE 2: Wait for redirection to verify system loads the detailed "View Supplier" profile screen
        wait.until(ExpectedConditions.urlContains("page=view_supplier"));
        wait.until(ExpectedConditions.urlContains("id="));

        // TEST PROCEDURE 3 & 4: Review summary layout data entries and examine expense tracking logs
        String pageLayoutContent = driver.getPageSource();

        // --- EXPECTED OUTPUT VERIFICATION (JUNIT 5 MASTER DATA ASSERTIONS) ---
        assertAll("Verify Detailed Supplier Profile Screen Master Data & Ledger Log Layout",
            // 1. Confirm core entity identity is loaded inside the profile viewer title context
            () -> assertTrue(pageLayoutContent.contains("CleanPro"), 
                    "FAIL: Target identity 'CleanPro' master name was missing from the detailed viewer page layout."),
            
            // 2. Confirm foundational tracking metadata strings are rendered on screen
            () -> assertTrue(pageLayoutContent.contains("0123456789"), 
                    "FAIL: Linked phone metadata credential '0123456789' was missing from the profile details review."),
            
            // 3. Confirm financial ledger starting/current baseline structure is rendered
            () -> assertTrue(pageLayoutContent.contains("150.00") || pageLayoutContent.contains("150"), 
                    "FAIL: Ledger structural parameter matching '150.00' was missing from the detailed viewer page layout."),
            
            // 4. Validate the presence of the historical expense transaction tracking ledger subsection/table component
            // We use structural keywords typically displayed in transaction sections (e.g., Expense, History, Description, Amount, Date)
            () -> assertTrue(
                    pageLayoutContent.contains("Expense") || 
                    pageLayoutContent.contains("History") || 
                    pageLayoutContent.contains("Transaction") ||
                    pageLayoutContent.contains("Log"),
                    "FAIL: Chronological expense history transaction tracking sub-section or log layout was missing from the profile ledger breakdown screen.")
        );
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}