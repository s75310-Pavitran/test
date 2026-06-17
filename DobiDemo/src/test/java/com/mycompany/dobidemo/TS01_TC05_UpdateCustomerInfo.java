package com.mycompany.dobidemo;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

public class TS01_TC05_UpdateCustomerInfo {

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

        // PRE-CONDITION 2: User is currently on Customer page
        driver.get("http://softwaretesting.umt.edu.my/dobidemo/index.php?page=customers");
        wait.until(ExpectedConditions.urlContains("page=customers"));
    }

    @Test
    @DisplayName("TS01-TC05: Input 1 (Valid Partition - Update Phone and Description)")
    public void testUpdateCustomerValid() {
        // TEST PROCEDURE 1: Search for or locate the customer's row to be updated ("Pavi")
        // TEST PROCEDURE 2: Click on the customer name link to load the profile/edit screen
        WebElement customerLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//table//a[contains(text(),'Pavi')] | //table//td[contains(text(),'Pavi')]/following-sibling::td/a")
        ));
        customerLink.click();

        // If the system requires clicking an additional "Edit" button once on the profile view page:
        try {
            WebElement editButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(@href, 'do=edit') or contains(text(),'Edit')]")
            ));
            editButton.click();
        } catch (TimeoutException e) {
            // Skips if the page directly exposes editable inputs upon clicking the row link
        }

        // TEST PROCEDURE 3: Modify the data fields inside the inputs
        WebElement phoneField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("customer_phone")));
        phoneField.clear();
        phoneField.sendKeys("0179998888");

        WebElement descField = driver.findElement(By.name("customer_desc"));
        descField.clear();
        descField.sendKeys("Moved to Kuala Nerus");

        // TEST PROCEDURE 4: Click the "Update" or "Save Changes" button
        WebElement updateButton = driver.findElement(By.cssSelector("form button[type='submit'], #submit, .btn-primary"));
        updateButton.click();

        // TEST PROCEDURE 5 & EXPECTED OUTPUT 1: Verify if fields display updated details in listing layout grid
        // Redirect back to the summary listing grid if the system doesn't auto-redirect
        if (!driver.getCurrentUrl().contains("page=customers")) {
            driver.get("http://softwaretesting.umt.edu.my/dobidemo/index.php?page=customers");
        }
        wait.until(ExpectedConditions.urlContains("page=customers"));

        // Locate row to confirm changes are reflected live
        WebElement updatedNameLink = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//table//td[contains(text(),'Pavi')] | //table//a[contains(text(),'Pavi')]")
        ));
        WebElement tableRow = updatedNameLink.findElement(By.xpath("./ancestor::tr"));
        String rowText = tableRow.getText();

        // Verify updated phone string is actively showing in the table row
        assertTrue(rowText.contains("0179998888"),
                "FAIL: The summary list table did not display the updated phone number '0179998888'.");

        // Optional structural confirmation: drill down into profile to verify the longer description update field
        updatedNameLink.click();
        wait.until(ExpectedConditions.urlContains("view_customer"));
        String profileSource = driver.getPageSource();
        assertTrue(profileSource.contains("Moved to Kuala Nerus"),
                "FAIL: The updated customer description 'Moved to Kuala Nerus' was not persisted inside the customer profile view.");
    }

    @Test
    @DisplayName("TS01-TC05: Input 2 (Invalid Partition - Blank Name Validation)")
    public void testUpdateCustomerBlankName() {
        // TEST PROCEDURE 1 & 2: Locate and open target customer record "Pavi"
        WebElement customerLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//table//a[contains(text(),'Pavi')] | //table//td[contains(text(),'Pavi')]/following-sibling::td/a")
        ));
        customerLink.click();

        try {
            WebElement editButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(@href, 'do=edit') or contains(text(),'Edit')]")
            ));
            editButton.click();
        } catch (TimeoutException e) {
            // Skips if inputs are already editable
        }

        // TEST PROCEDURE 3: Modify the data fields inside the inputs - Clearing the Name field to make it [Blank]
        WebElement nameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("customer_name")));
        nameField.clear(); // Leaves name field completely empty

        WebElement phoneField = driver.findElement(By.name("customer_phone"));
        phoneField.clear();
        phoneField.sendKeys("0179998888");

        WebElement descField = driver.findElement(By.name("customer_desc"));
        descField.clear();
        descField.sendKeys("Testing blank validation");

        // TEST PROCEDURE 4: Click the "Update" button
        WebElement updateButton = driver.findElement(By.cssSelector("form button[type='submit'], #submit, .btn-primary"));
        updateButton.click();

        // EXPECTED OUTPUT 2: System rejects the request and presents the warning message: "Customer Name cannot be blank."
        String validationMessage = "";
        try {
            // Check if application fires a native web browser pop-up Alert dialog block
            Alert alert = driver.switchTo().alert();
            validationMessage = alert.getText();
            alert.accept();
        } catch (NoAlertPresentException e) {
            // Fallback: Locate visible HTML error container element banners rendered inside the viewport page source
            WebElement errorNotification = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(text(),'cannot be blank') or contains(text(),'required') or contains(@class,'alert-danger')]")
            ));
            validationMessage = errorNotification.getText();
        }

        // Verify the presence of the validation failure message
        assertTrue(validationMessage.contains("cannot be blank") || validationMessage.toLowerCase().contains("required"),
                "FAIL: The system accepted a blank name update or failed to show the required exception error message.");
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

}
