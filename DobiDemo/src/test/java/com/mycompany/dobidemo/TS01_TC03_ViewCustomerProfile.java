package com.mycompany.dobidemo;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class TS01_TC03_ViewCustomerProfile {

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

        // PRE-CONDITION 2 & 3: User is currently on Customer page where target profile "Pavi" exists
        driver.get("http://softwaretesting.umt.edu.my/dobidemo/index.php?page=customers");
        wait.until(ExpectedConditions.urlContains("page=customers"));
    }

    @Test
    @DisplayName("TS01-TC03: View Customer Profile and Sales History - Target: 'Pavi'")
    public void testViewCustomerDetailsAndSalesHistory() {

        // TEST PROCEDURE 1: Locate the specific customer row from the main grid list
        // Locates either an active text cell or a clickable link matching 'Pavi'
        WebElement customerLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//table//a[contains(text(),'Pavi')] | //table//td[contains(text(),'Pavi')]/following-sibling::td/a")
        ));

        // TEST PROCEDURE 2: Click the user profile
        customerLink.click();

        // TEST PROCEDURE 3: (System) Navigate to the detailed customer summary page
        // Verifies the application appends the correct page action or customer id indicator inside the active URL parameters
        wait.until(ExpectedConditions.urlContains("page=view_customer"));

        // TEST PROCEDURE 4 & EXPECTED OUTPUT: Populate and display the complete profile summary layout data entries
        // Scrapes page source content to verify all expected detailed card fields match perfectly
        String detailedPageSource = driver.getPageSource();

        assertTrue(detailedPageSource.contains("Pavi"),
                "FAIL: Customer Detailed View page failed to render the target name text 'Pavi'.");

        // Validates structure items (Looks for common contextual properties or presence of telephone patterns)
        assertTrue(detailedPageSource.contains("0112345678") || detailedPageSource.contains("0198765432"),
                "FAIL: Expected phone number records were not found populated inside the customer info card context.");

        // TEST PROCEDURE 5 & EXPECTED OUTPUT: Automatically retrieve and display the associated sales tracking sub-section table
        // Verifies that a transaction table summary layout exists on the page
        WebElement salesTable = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//table[contains(@id,'sales') or contains(@class,'table')] | //div[contains(@class,'card')]//table")
        ));
        assertTrue(salesTable.isDisplayed(), "FAIL: Associated sales history sub-section table was missing or hidden on screen.");

        // Verifies that multiple transactional records are bound inside the sales table body rows
        List<WebElement> salesRows = salesTable.findElements(By.xpath(".//tbody/tr"));

        // Ensure there is at least 1 record or it doesn't just say 'No data available'
        assertFalse(salesRows.isEmpty(), "FAIL: Transactional sales log grid elements contain 0 index entries rows.");

        String firstRowText = salesRows.get(0).getText();
        assertFalse(firstRowText.toLowerCase().contains("no data") || firstRowText.toLowerCase().contains("no records"),
                "FAIL: Sales tracking sub-section table is displayed but contains empty log warnings.");
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
