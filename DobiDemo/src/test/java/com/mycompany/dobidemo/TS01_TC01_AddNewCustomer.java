package com.mycompany.dobidemo;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

public class TS01_TC01_AddNewCustomer {

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
        driver.get("https://efadzli.com/dobidemo/login.php?gongbadak=ProUMTedumy_6096684100_KualaNerus_96782048");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        driver.findElement(By.id("username")).sendKeys("public_umt");
        driver.findElement(By.id("password")).sendKeys("umt2023");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("login.php")));

        // PRE-CONDITION 2: Navigate to Customer Page
        // Assuming your side menu/navigation matches standard menu IDs (like By.id("menuCustomers") or URL parameters)
        try {
            driver.findElement(By.id("menuCustomers")).click();
        } catch (Exception e) {
            driver.get("https://efadzli.com/dobidemo/index.php?page=customers");
        }
        wait.until(ExpectedConditions.urlContains("page=customers"));
    }

    @Test
    @DisplayName("TS01-TC01: Input 1 (Valid - All Fields)")
    public void testAddValidCustomer() {
        WebElement newButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(),'+ New') or contains(text(),'New') or contains(@href, 'do=add')]")
        ));
        newButton.click();

        WebElement nameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("customer_name")));
        nameField.sendKeys("Pavi");
        driver.findElement(By.name("customer_phone")).sendKeys("0112345678");
        driver.findElement(By.name("customer_desc")).sendKeys("Regular customer");
        
        WebElement balanceField = driver.findElement(By.name("customer_balance"));
        balanceField.clear();
        balanceField.sendKeys("50.00");

        driver.findElement(By.cssSelector("form button[type='submit'], #submit, .btn-primary")).click();

        if (!driver.getCurrentUrl().contains("page=customers")) {
            driver.get("https://efadzli.com/dobidemo/index.php?page=customers");
        }
        wait.until(ExpectedConditions.urlContains("page=customers"));

        WebElement nameLink = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//table//td[contains(text(),'Pavi')] | //table//a[contains(text(),'Pavi')]")
        ));
        assertTrue(nameLink.isDisplayed(), "FAIL: Customer name 'Pavi' was not found in the table list.");

        WebElement tableRow = nameLink.findElement(By.xpath("./ancestor::tr"));
        String rowText = tableRow.getText();

        assertTrue(rowText.contains("0112345678"), "FAIL: Expected phone number missing from row.");
        assertTrue(rowText.contains("New"), "FAIL: Expected automated category status label 'New' was not found.");
    }

    @Test
    @DisplayName("TS01-TC01: Input 2 (Invalid - Blank Name)")
    public void testAddCustomerBlankName() {
        WebElement newButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(),'+ New') or contains(text(),'New') or contains(@href, 'do=add')]")
        ));
        newButton.click();

        WebElement nameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("customer_name")));
        nameField.clear(); 
        
        driver.findElement(By.name("customer_phone")).sendKeys("0112345678");
        driver.findElement(By.name("customer_desc")).sendKeys("Missing name validation");
        
        WebElement balanceField = driver.findElement(By.name("customer_balance"));
        balanceField.clear();
        balanceField.sendKeys("0.00");

        driver.findElement(By.cssSelector("form button[type='submit'], #submit, .btn-primary")).click();

        String alertText = "";
        try {
            Alert alert = driver.switchTo().alert();
            alertText = alert.getText();
            alert.accept();
        } catch (NoAlertPresentException e) {
            WebElement errorBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(text(),'required') or contains(text(),'Name field is required') or contains(text(),'blank')]")
            ));
            alertText = errorBox.getText();
        }
        assertTrue(alertText.contains("required") || alertText.contains("blank"), "FAIL: Expected validation error message for blank name was missing.");
    }

    @Test
    @DisplayName("TS01-TC01: Input 3 (Invalid - Negative Balance)")
    public void testAddCustomerNegativeBalance() {
        WebElement newButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(),'+ New') or contains(text(),'New') or contains(@href, 'do=add')]")
        ));
        newButton.click();

        WebElement nameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("customer_name")));
        nameField.sendKeys("Pavi");
        driver.findElement(By.name("customer_phone")).sendKeys("0198765432");
        driver.findElement(By.name("customer_desc")).sendKeys("Negative balance test");
        
        WebElement balanceField = driver.findElement(By.name("customer_balance"));
        balanceField.clear();
        balanceField.sendKeys("-15.00");

        driver.findElement(By.cssSelector("form button[type='submit'], #submit, .btn-primary")).click();

        String errorText = "";
        try {
            Alert alert = driver.switchTo().alert();
            errorText = alert.getText();
            alert.accept();
        } catch (NoAlertPresentException e) {
            WebElement errorBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(text(),'negative') or contains(text(),'Invalid balance')]")
            ));
            errorText = errorBox.getText();
        }
        assertTrue(errorText.contains("negative") || errorText.contains("Invalid"), "FAIL: System didn't present error restriction for negative balance.");
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}