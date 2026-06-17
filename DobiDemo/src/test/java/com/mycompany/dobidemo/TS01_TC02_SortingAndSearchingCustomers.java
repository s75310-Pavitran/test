package com.mycompany.dobidemo;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class TS01_TC02_SortingAndSearchingCustomers {

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

        // PRE-CONDITION 2 & 3: User is on Customer page with existing multiple profiles
        driver.get("http://softwaretesting.umt.edu.my/dobidemo/index.php?page=customers");
        wait.until(ExpectedConditions.urlContains("page=customers"));
    }

    @Test
    @DisplayName("TS01-TC02: Input 1 (Sorting - Name A-Z)")
    public void testCustomerSortByName() {
        // TEST PROCEDURE 1: View the main customer table layout grid
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//table")));

        // TEST PROCEDURE 2: Click on the "Name" column header to trigger ascending sorting
        // Locates table column headers containing the text 'Name'
        WebElement nameHeader = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//table//th[contains(text(),'Name')]")
        ));
        nameHeader.click();

        // Brief sleep to allow JavaScript rendering or page refresh to settle sorting animation
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // EXPECTED OUTPUT 1: Table responds instantly and displays rows sorted alphabetically (A-Z)
        // Scrapes column names to verify sorting order logic
        List<WebElement> nameElements = driver.findElements(By.xpath("//table//tbody/tr/td[2]"));
        List<String> actualNames = new ArrayList<>();

        for (WebElement element : nameElements) {
            String txt = element.getText().trim();
            if (!txt.isEmpty()) {
                actualNames.add(txt.toLowerCase());
            }
        }

        List<String> sortedNames = new ArrayList<>(actualNames);
        Collections.sort(sortedNames); // Expected ascending order configuration

        assertEquals(sortedNames, actualNames, "FAIL: Customer rows are not successfully sorted alphabetically (A-Z).");
    }

    @Test
    @DisplayName("TS01-TC02: Input 2 (Sorting - Balance Descending)")
    public void testCustomerSortByBalance() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//table")));

        // TEST PROCEDURE 3: Click on the "Account Balance" or "Balance" header to trigger descending sorting
        WebElement balanceHeader = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//table//th[contains(text(),'Balance') or contains(text(),'Account Balance')]")
        ));

        // Often web tables require clicking twice to toggle from default Ascending to Descending
        balanceHeader.click();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        balanceHeader.click();
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // EXPECTED OUTPUT 2: Table sorts immediately from highest balance down to RM0.00
        List<WebElement> balanceElements = driver.findElements(By.xpath("//table//tbody/tr/td[contains(text(),'.') or contains(text(),'00')]"));
        List<Double> actualBalances = new ArrayList<>();

        for (WebElement element : balanceElements) {
            try {
                // Strips non-numeric characters like 'RM', currency spacing, commas
                String cleanText = element.getText().replaceAll("[^0-9.]", "").trim();
                if (!cleanText.isEmpty()) {
                    actualBalances.add(Double.parseDouble(cleanText));
                }
            } catch (NumberFormatException e) {
                // Skips structural parsing anomalies safely
            }
        }

        List<Double> expectedSortedBalances = new ArrayList<>(actualBalances);
        Collections.sort(expectedSortedBalances, Collections.reverseOrder()); // Highest down to 0.00

        assertEquals(expectedSortedBalances, actualBalances, "FAIL: Table rows are not sorted by highest account balance descending.");
    }

    @Test
    @DisplayName("TS01-TC02: Input 3 (Valid Search - 'Pavi')")
    public void testValidCustomerSearch() {
        // TEST PROCEDURE 4: Enter search query into the search input field
        // Matches input types containing search labels, placeholding attributes, or data tables filter extensions
        WebElement searchField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[type='search'], input[placeholder*='Search'], #search_input")
        ));

        searchField.clear();
        searchField.sendKeys("Pavi");
        searchField.sendKeys(Keys.ENTER); // Submits via Enter key trigger block

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // EXPECTED OUTPUT 3: Filters down immediately, showing only the row matching "Pavi"
        List<WebElement> rows = driver.findElements(By.xpath("//table//tbody/tr"));

        for (WebElement row : rows) {
            String rowText = row.getText();
            // Validates that active, visible rows contain the queried criteria 
            if (row.isDisplayed() && !rowText.isEmpty() && !rowText.contains("No matching")) {
                assertTrue(rowText.contains("Pavi"),
                        "FAIL: Row displaying '" + rowText + "' does not match search query criteria 'Pavi'.");
            }
        }
    }

    @Test
    @DisplayName("TS01-TC02: Input 4 (Invalid Search - Non-existent)")
    public void testInvalidCustomerSearch() {
        WebElement searchField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[type='search'], input[placeholder*='Search'], #search_input")
        ));

        searchField.clear();
        searchField.sendKeys("Zack");
        searchField.sendKeys(Keys.ENTER);

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // EXPECTED OUTPUT 4: Clears out rows and displays message: "No matching records found."
        // Checks both inner grid summary records status text or empty parent elements row containers
        WebElement emptyMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'No matching') or contains(text(),'No records found') or contains(text(),'empty')]")
        ));

        String actualMessageText = emptyMessage.getText();
        assertTrue(actualMessageText.contains("No matching records found") || actualMessageText.toLowerCase().contains("no matching"),
                "FAIL: Expected 'No matching records found.' empty string block verification message layout was missing.");
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
