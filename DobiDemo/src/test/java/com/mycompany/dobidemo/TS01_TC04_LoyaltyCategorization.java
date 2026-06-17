package com.mycompany.dobidemo;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class TS01_TC04_LoyaltyCategorization {

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

        // PRE-CONDITION 2: Navigate to Customer summary list page
        driver.get("http://softwaretesting.umt.edu.my/dobidemo/index.php?page=customers");
        wait.until(ExpectedConditions.urlContains("page=customers"));
    }

    /**
     * Helper method to verify that a row with a specific number of sales
     * correctly displays the expected automated loyalty category status text
     * badge.
     */
    private void verifyCategoryBySalesCount(String salesCount, String expectedCategory) {
        // Dynamic XPath searching the customer table rows:
        // It locates a row containing the exact sales string, then verifies the Category cell.
        // Adjust column indexes if your table headers vary (e.g., td[4] for sales, td[5] for category)
        String xpathExpression = "//table//tbody/tr[td[contains(text(), '" + salesCount + "')]]";

        List<WebElement> matchingRows = driver.findElements(By.xpath(xpathExpression));

        // Assert that the test data/mock profiles are present for validation
        assertFalse(matchingRows.isEmpty(),
                "FAIL: No mock customer profile found matching a sales count of '" + salesCount + "'. Ensure mock data exists.");

        boolean validationPassed = false;
        String actualRowText = "";

        for (WebElement row : matchingRows) {
            actualRowText = row.getText();
            if (actualRowText.contains(expectedCategory)) {
                validationPassed = true;
                break;
            }
        }

        assertTrue(validationPassed,
                "FAIL: For Sales = " + salesCount + ", expected category '" + expectedCategory + "' but row text displayed: '" + actualRowText + "'");
    }

    @Test
    @DisplayName("TS01-TC04: Input 1 & 2 - Boundary Partition (Sales <= 3 -> 'New')")
    public void testLoyaltyPartitionNew() {
        // Input 1 (Inside Partition): Sales = 2 -> Expected Output: "New"
        verifyCategoryBySalesCount("2", "New");

        // Input 2 (Boundary Edge): Sales = 3 -> Expected Output: "New"
        verifyCategoryBySalesCount("3", "New");
    }

    @Test
    @DisplayName("TS01-TC04: Input 3, 4 & 5 - Boundary Partition (Sales 4 to 15 -> 'Regular')")
    public void testLoyaltyPartitionRegular() {
        // Input 3 (Boundary Edge): Sales = 4 -> Expected Output: "Regular"
        verifyCategoryBySalesCount("4", "Regular");

        // Input 4 (Inside Partition): Sales = 14 -> Expected Output: "Regular"
        verifyCategoryBySalesCount("14", "Regular");

        // Input 5 (Boundary Edge): Sales = 15 -> Expected Output: "Regular"
        verifyCategoryBySalesCount("15", "Regular");
    }

    @Test
    @DisplayName("TS01-TC04: Input 6 - Boundary Edge (Sales > 15 -> 'Loyal')")
    public void testLoyaltyPartitionLoyal() {
        // Input 6 (Boundary Edge): Sales = 16 -> Expected Output: "Loyal"
        verifyCategoryBySalesCount("16", "Loyal");
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

}
