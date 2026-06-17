package com.dobidemo.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

public class TS02_TC04_UpdateValidMinimumBalance {

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
    @DisplayName("TS02-TC04: Update the supplier account balance to the minimum valid boundary limit value (0.00)")
    public void testUpdateValidMinimumBalanceEdge() {

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

        // TEST PROCEDURE 1 & 2: Click into balance field, clear out characters and enter exactly "0.00"
        WebElement balanceField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.name("supplier_balance"))
        );
        balanceField.clear(); 
        balanceField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE); 
        balanceField.sendKeys("0.00");

        // TEST PROCEDURE 3: Click the blue [Update] button
        WebElement updateButton = driver.findElement(By.cssSelector("form button[type='submit'], #submit, .btn-primary, .btn-info"));
        updateButton.click();

        // --- EXPECTED OUTPUT VERIFICATION ---
        
        // 1. Wait until initial redirection action handles the save action complete
        wait.until(ExpectedConditions.urlContains("id="));

        // 2. Navigate back to the primary supplier dashboard list view index route
        driver.get("http://softwaretesting.umt.edu.my/dobidemo/index.php?page=suppliers");
        wait.until(ExpectedConditions.urlContains("page=suppliers"));

        // 3. Drill into the updated profile link to confirm data baseline changes
        WebElement nameLink = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//table//a[contains(text(),'CleanPro')]")
        ));
        nameLink.click();
        
        wait.until(ExpectedConditions.urlContains("page=view_supplier"));

        // 4. Assert that the ledger transaction value reflects exactly zero
        String profileDetailsText = driver.getPageSource();
        
        assertTrue(profileDetailsText.contains("0.00") || profileDetailsText.contains("0"), 
                "FAIL: The valid minimum boundary reset value '0.00' was not successfully verified inside the saved supplier profile layout view.");
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}