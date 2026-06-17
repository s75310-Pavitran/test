package com.dobidemo.tests;

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

public class TS02_TC02_SortingAndSearchingSuppliers {

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
    @DisplayName("TS02-TC02: Sort the supplier listing table columns and test query search matches")
    public void testSortingAndSearching() throws InterruptedException {

        // PRE-CONDITION 2: Navigate to Supplier dashboard list page
        driver.get("http://softwaretesting.umt.edu.my/dobidemo/index.php?page=suppliers");
        wait.until(ExpectedConditions.urlContains("page=suppliers"));

        // --- TEST PROCEDURE 1: TOGGLE AND VERIFY SORTING ---
        
        // 1. Locate the 'Name' table header click element
        // Adjust the locator if it's a specific <th> tag or contains sorting classes
        WebElement nameHeader = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//th[contains(text(),'Name') or contains(text(),'Supplier') or .//text()[contains(.,'Name')]]")
        ));
        
        // 2. Click Name Header to trigger alphabetical sorting toggle
        nameHeader.click();
        Thread.sleep(1000); // Small pause to let frontend sorting scripts compute rows

        // 3. Extract the text of all name elements from the table to verify list layout order
        List<WebElement> rowNameElements = driver.findElements(By.xpath("//table//tbody/tr/td[1]"));
        List<String> actualNames = new ArrayList<>();
        for (WebElement element : rowNameElements) {
            if (!element.getText().trim().isEmpty()) {
                actualNames.add(element.getText().trim());
            }
        }

        // 4. Create a copy of the list and sort it programmatically to check against actual browser state
        List<String> sortedNamesCopy = new ArrayList<>(actualNames);
        Collections.sort(sortedNamesCopy); // Standard natural alphabetical sort

        // Check if rows are sorted (either ascending or descending depends on the initial state of the app)
        boolean isAscending = actualNames.equals(sortedNamesCopy);
        Collections.sort(sortedNamesCopy, Collections.reverseOrder());
        boolean isDescending = actualNames.equals(sortedNamesCopy);

        assertTrue(isAscending || isDescending, 
                "FAIL: Supplier data table rows did not reorganize dynamically in alphabetical order. Current order: " + actualNames);


        // --- TEST PROCEDURE 2 & 3: INPUT SEARCH QUERY AND EVALUATE RE-RENDER ---

        // 1. Locate the top table search/filter panel input component
        // Commonly structured as input[type='search'], .dataTables_filter input, or By.id('search')
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[type='search'], #search, input[placeholder*='Search'], .form-control-sm")
        ));

        // 2. Clear old input entries and type the explicit search string: "CleanPro"
        searchInput.clear();
        searchInput.sendKeys("CleanPro");
        Thread.sleep(1000); // Wait for live update text filter scripts to filter matching items

        // 3. Re-grab all visible row elements remaining in the table layout view
        List<WebElement> visibleRows = driver.findElements(By.xpath("//table//tbody/tr"));

        // 4. Assert that the table updates live, displaying only elements containing "CleanPro"
        for (WebElement row : visibleRows) {
            String rowText = row.getText().trim();
            
            // Skip checking empty placeholders or Datatables "No matching records found" rows
            if (rowText.isEmpty() || rowText.contains("No matching records")) {
                continue;
            }

            assertTrue(rowText.contains("CleanPro"), 
                    "FAIL: Search mechanism failed to filter out irrelevant rows. Found un-matched data row containing: " + rowText);
        }
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}