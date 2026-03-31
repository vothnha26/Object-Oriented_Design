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
@DisplayName("Member 1: Selenium E2E Tests (Customer Order Flow)")
class Member1SeleniumE2ETest {

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
    @DisplayName("E2E Member 1 - Customer Checkout (Builder Pattern)")
    void testCustomerOrderFlow() throws InterruptedException {
        String baseUrl = "http://localhost:" + port + "/alotra";

        // 1. Login
        System.out.println("Logging in as Customer 'tai'...");
        login("tai", "123456");

        // 2. Add product from Home
        System.out.println("Adding product to cart...");
        driver.get(baseUrl + "/");
        WebElement firstProduct = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".card-body a.btn-outline-primary")));
        jsClick(firstProduct);
        
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#addToCartForm button")));
        jsClick(addBtn);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-success")));

        // 3. Cart & Checkout
        System.out.println("Proceeding to Checkout...");
        driver.get(baseUrl + "/cart");
        jsClick(wait.until(ExpectedConditions.presenceOfElementLocated(By.id("checkAll"))));
        Thread.sleep(1000);
        
        // Find checkout link
        WebElement checkoutBtn = driver.findElement(By.xpath("//a[contains(@href, 'confirm')]"));
        jsClick(checkoutBtn);

        // 4. Fill Shipping Info (Builder Pattern in Backend)
        System.out.println("Filling Shipping Info...");
        wait.until(ExpectedConditions.urlContains("/checkout/confirm"));
        
        jsClick(driver.findElement(By.id("methodShip")));
        Thread.sleep(500);
        
        driver.findElement(By.id("shipName")).sendKeys("Nguyen Van E2E");
        driver.findElement(By.id("shipPhone")).sendKeys("0912345678");
        driver.findElement(By.id("shipAddress")).sendKeys("IT Campus, Linh Trung");
        jsClick(driver.findElement(By.id("payCash")));

        // 5. Confirm Order
        System.out.println("Confirming Order...");
        jsClick(driver.findElement(By.id("btnPlaceOrder")));

        // 6. Verify Success
        wait.until(ExpectedConditions.urlContains("/account/orders"));
        assertTrue(driver.getPageSource().contains("Mã đơn: "), "Order placement failed");
        
        System.out.println("Member 1 Customer Flow E2E Success!");
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
