package com.alotra.merged;

import com.alotra.repository.EmployeeRepository;
import com.alotra.repository.ProductRepository;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Member 3: Selenium E2E Tests (Command Pattern Focus)")
class Member3SeleniumE2ETest {

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private WebDriverWait wait;

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private ProductRepository productRepository;

    @BeforeAll
    void setupClass() {
        WebDriverManager.chromedriver().setup();
        employeeRepository.findByUsername("admin").ifPresent(e -> {
            e.setPasswordHash("{noop}123456");
            employeeRepository.save(e);
        });
    }

    @BeforeEach
    void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-gpu", "--no-sandbox", "--window-size=1920,1080");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    @DisplayName("E2E Member 3 - Product Management (Command Pattern + Undo)")
    void testAdminProductCommand() throws InterruptedException {
        String baseUrl = "http://localhost:" + port + "/alotra";

        // 1. Login as Admin
        System.out.println("Logging in as Admin...");
        login("admin", "123456");

        // 2. Go to Products
        System.out.println("Navigating to Product Management...");
        driver.get(baseUrl + "/admin/products");
        Thread.sleep(2000);

        // 3. Delete a product (Command Pattern)
        System.out.println("Deleting a product...");
        WebElement deleteBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href, '/delete/')]")));
        String deleteUrl = deleteBtn.getAttribute("href");
        String productId = deleteUrl.substring(deleteUrl.lastIndexOf("/") + 1);
        System.out.println("Deleting Product ID: " + productId);
        
        jsClick(deleteBtn);

        // Handle confirmation alert
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();

        // 4. Verify Delete success message
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-success")));
        assertTrue(driver.getPageSource().contains("thùng rác"), "Delete command failed");

        // 5. Undo (Command Pattern Undo)
        System.out.println("Undoing last action...");
        WebElement undoBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//form[@action[contains(.,'undo')]]/button")));
        jsClick(undoBtn);

        // 6. Verify Undo success
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-success")));
        assertTrue(driver.getPageSource().contains("Hoàn tác") || driver.getPageSource().contains("thành công"), "Undo command failed");

        System.out.println("Member 3 Command Pattern E2E Success!");
        Thread.sleep(3000);
    }

    private void login(String user, String pass) throws InterruptedException {
        driver.get("http://localhost:" + port + "/alotra/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys(user);
        driver.findElement(By.name("password")).sendKeys(pass);
        jsClick(driver.findElement(By.cssSelector("button.btn-alotra")));
        Thread.sleep(2000);
    }

    private void jsClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }
}
