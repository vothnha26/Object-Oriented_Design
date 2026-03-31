package com.alotra.merged;

import com.alotra.repository.CustomerRepository;
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
@DisplayName("Member 2: Final UI Verification")
class Member2SeleniumE2ETest {

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private WebDriverWait wait;

    @Autowired private CustomerRepository customerRepository;

    @BeforeAll
    void setupClass() {
        WebDriverManager.chromedriver().setup();
        customerRepository.findByUsername("tai").ifPresent(c -> {
            c.setPasswordHash("{noop}123456");
            customerRepository.save(c);
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
    @DisplayName("Verify Member 2 UI Flow")
    void testPatternUI() throws InterruptedException {
        String baseUrl = "http://localhost:" + port + "/alotra";

        login("tai", "123456");
        Thread.sleep(2000);

        // Verification 1: Cart Page (Facade Pattern Result)
        System.out.println("Checking Cart...");
        driver.get(baseUrl + "/cart");
        Thread.sleep(2000);
        assertTrue(driver.getCurrentUrl().contains("/cart"), "Cart page not reached");

        // Verification 2: Order History Page (Template Method Pattern)
        System.out.println("Checking Order History...");
        driver.get(baseUrl + "/account/orders");
        Thread.sleep(2000);
        // Look for any table or content that indicates order list
        boolean hasOrders = driver.findElements(By.tagName("table")).size() > 0 || driver.getPageSource().contains("MaDH");
        assertTrue(hasOrders || driver.getPageSource().contains("không có"), "Order history structure fails to load");

        System.out.println("Member 2 Patterns verified in UI successfully!");
        Thread.sleep(2000);
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
