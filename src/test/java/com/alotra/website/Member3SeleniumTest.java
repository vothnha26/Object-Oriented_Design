package com.alotra.website;

import com.alotra.entity.*;
import com.alotra.entity.enums.ProductStatus;
import com.alotra.entity.enums.ToppingStatus;
import com.alotra.repository.*;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Member 3: Selenium E2E Tests")
class Member3SeleniumTest {

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private WebDriverWait wait;

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductSizeRepository productSizeRepository;
    @Autowired
    private ProductVariantRepository variantRepository;
    @Autowired
    private ToppingRepository toppingRepository;

    private Customer testUser;
    private Product testProduct;
    private ProductSize sizeM;
    private ProductSize sizeL;
    private Topping topping1;
    private Topping topping2;
    private Topping topping3;

    @BeforeAll
    void setupClass() {
        WebDriverManager.chromedriver().setup();
        
        Category cat = new Category();
        cat.setName("Selenium Category " + System.currentTimeMillis());
        cat = categoryRepository.save(cat);

        testProduct = new Product();
        testProduct.setName("Selenium Matcha " + System.currentTimeMillis());
        testProduct.setCategory(cat);
        testProduct.setStatus(ProductStatus.ACTIVE);
        testProduct.setImageUrl("");
        testProduct = productRepository.save(testProduct);

        sizeM = new ProductSize();
        sizeM.setName("Medium " + System.currentTimeMillis());
        sizeM.setPriceAdjustment(BigDecimal.ZERO);
        sizeM = productSizeRepository.save(sizeM);

        sizeL = new ProductSize();
        sizeL.setName("Large " + System.currentTimeMillis());
        sizeL.setPriceAdjustment(new BigDecimal("10000"));
        sizeL = productSizeRepository.save(sizeL);

        ProductVariant vM = new ProductVariant();
        vM.setProduct(testProduct);
        vM.setSize(sizeM);
        vM.setPrice(new BigDecimal("40000"));
        vM.setStatus(ProductStatus.ACTIVE);
        variantRepository.save(vM);

        ProductVariant vL = new ProductVariant();
        vL.setProduct(testProduct);
        vL.setSize(sizeL);
        vL.setPrice(new BigDecimal("50000"));
        vL.setStatus(ProductStatus.ACTIVE);
        variantRepository.save(vL);

        topping1 = new Topping();
        topping1.setName("Black Pearl " + System.currentTimeMillis());
        topping1.setExtraPrice(new BigDecimal("5000"));
        topping1.setStatus(ToppingStatus.AVAILABLE);
        topping1 = toppingRepository.save(topping1);

        topping2 = new Topping();
        topping2.setName("Pudding " + System.currentTimeMillis());
        topping2.setExtraPrice(new BigDecimal("7000"));
        topping2.setStatus(ToppingStatus.AVAILABLE);
        topping2 = toppingRepository.save(topping2);

        topping3 = new Topping();
        topping3.setName("Aloe Vera " + System.currentTimeMillis());
        topping3.setExtraPrice(new BigDecimal("8000"));
        topping3.setStatus(ToppingStatus.AVAILABLE);
        topping3 = toppingRepository.save(topping3);

        testUser = new Customer();
        testUser.setUsername("sel_user_" + System.currentTimeMillis());
        testUser.setPasswordHash("{noop}password123");
        testUser.setEmail("sel_p_" + System.currentTimeMillis() + "@test.com");
        testUser.setFullName("Selenium Pricing Tester");
        testUser = customerRepository.save(testUser);
    }

    @BeforeEach
    void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--window-size=1920,1080");
        
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        
        // Clear history to isolate tests
        driver.get("http://localhost:" + port + "/alotra/cart/clear-history");
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("E2E: Random Topping Removal and Price Update")
    void testRandomToppingRemoval() throws InterruptedException {
        String baseUrl = "http://localhost:" + port + "/alotra";
        login(testUser.getUsername(), "password123");

        driver.get(baseUrl + "/products/" + testProduct.getId());
        Thread.sleep(2000);

        // Add 3 Toppings (Black Pearl, Pudding, Aloe Vera)
        addToppingInDetail(topping1.getName());
        addToppingInDetail(topping2.getName());
        addToppingInDetail(topping3.getName());
        Thread.sleep(1000);

        WebElement totalText = driver.findElement(By.id("totalText"));
        assertTrue(totalText.getText().contains("60"), "Total with 3 toppings should be 60,000");

        // Remove Topping 2 (Pudding)
        WebElement p2Minus = driver.findElement(By.xpath("//div[contains(@class, 'topping-row')][.//div[text()='" + topping2.getName() + "']]//button[contains(@class, 'btn-top-minus')]"));
        jsClick(p2Minus);
        Thread.sleep(2000);
        
        assertTrue(totalText.getText().contains("53"), "Total after removal should be 53,000");

        jsClick(driver.findElement(By.cssSelector("#addToCartForm button[type='submit']")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-success")));

        driver.get(baseUrl + "/cart");
        Thread.sleep(2000);
        WebElement cartTotal = driver.findElement(By.className("line-total"));
        assertTrue(cartTotal.getText().contains("53"), "Cart total should be 53,000");
    }

    @Test
    @DisplayName("E2E: Detailed Pricing Logic Verification (Task 1)")
    void testPricingLogicDetailed() throws InterruptedException {
        String baseUrl = "http://localhost:" + port + "/alotra";
        login(testUser.getUsername(), "password123");

        driver.get(baseUrl + "/products/" + testProduct.getId());
        Thread.sleep(2000);

        // Scenario 1: Base Product (Size M)
        WebElement totalText = driver.findElement(By.id("totalText"));
        assertTrue(totalText.getText().contains("40"), "Base price should be 40k");

        // Scenario 2: Size Upgrade (Size L)
        WebElement sizeLBtn = driver.findElement(By.xpath("//span[contains(text(), '" + sizeL.getName() + "')]/.."));
        jsClick(sizeLBtn);
        Thread.sleep(1500);
        assertTrue(totalText.getText().contains("50"), "Size L price should be 50k");

        // Scenario 3: Adding Toppings (Pearl + Pudding)
        addToppingInDetail(topping1.getName());
        addToppingInDetail(topping2.getName());
        Thread.sleep(1000);
        // 50k + 5k + 7k = 62k
        assertTrue(totalText.getText().contains("62"), "Total with toppings should be 62k");

        // Scenario 4: Multiple Quantities (x2)
        jsClick(driver.findElement(By.id("qtyPlus")));
        Thread.sleep(1500);
        // 62k * 2 = 124k
        assertTrue(totalText.getText().contains("124"), "Total x2 should be 124k");

        jsClick(driver.findElement(By.cssSelector("#addToCartForm button[type='submit']")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-success")));

        driver.get(baseUrl + "/cart");
        Thread.sleep(2000);
        WebElement cartTotal = driver.findElement(By.className("line-total"));
        assertTrue(cartTotal.getText().contains("124"), "Cart total should be 124k");
    }

    @Test
    @DisplayName("E2E #0: Basic Order Flow")
    void testBasicFlow() {
        String baseUrl = "http://localhost:" + port + "/alotra";

        // 1. Go to Login
        driver.get(baseUrl + "/login");
        
        // 2. Fill login form
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")))
            .sendKeys(testUser.getUsername());
        driver.findElement(By.name("password")).sendKeys("password123");
        driver.findElement(By.cssSelector("button.btn-alotra")).click();

        // 3. Verify Login Success
        wait.until(ExpectedConditions.urlContains("/alotra"));
        
        // 4. Go to Product Detail
        driver.get(baseUrl + "/products/" + testProduct.getId());
        
        // 5. Add to cart
        WebElement addButton = driver.findElement(By.cssSelector("#addToCartForm button[type='submit']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", addButton);

        // 6. Verify success message
        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-success")));
        assertTrue(alert.getText().contains("giỏ hàng"));
    }
    @Test
    @DisplayName("E2E: Undo Topping Update in Cart")
    void testUndoToppingUpdate() throws InterruptedException {
        String baseUrl = "http://localhost:" + port + "/alotra";

        // 1. Login
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys(testUser.getUsername());
        driver.findElement(By.name("password")).sendKeys("password123");
        driver.findElement(By.cssSelector("button.btn-alotra")).click();
        Thread.sleep(2000);

        // 2. Add Product with 1 Topping (Black Pearl - 5000)
        driver.get(baseUrl + "/products/" + testProduct.getId());
        Thread.sleep(2000);
        String p1Name = topping1.getName();
        WebElement p1Plus = driver.findElement(By.xpath("//div[contains(@class, 'topping-row')][.//div[text()='" + p1Name + "']]//button[contains(@class, 'btn-top-plus')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", p1Plus);
        Thread.sleep(1000);
        driver.findElement(By.cssSelector("#addToCartForm button[type='submit']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-success")));

        // 3. Go to Cart - Initial Price: 40,000 + 5,000 = 45,000
        driver.get(baseUrl + "/cart");
        Thread.sleep(2000);
        WebElement lineTotal = driver.findElement(By.className("line-total"));
        String p1Price = lineTotal.getText().replaceAll("[^0-9]", "");
        System.out.println("Initial Price in Cart: " + p1Price);
        assertTrue(p1Price.contains("45000"));

        // 4. Update Toppings in Cart (Add Pudding - 7000)
        // Toggle topping row (it's hidden by default in this template usually)
        // Let's check how to show it. In cart.html it's d-none. 
        // We'll just force it or click the chevron if exists.
        ((JavascriptExecutor) driver).executeScript("document.querySelectorAll('.bg-light.d-none').forEach(el => el.classList.remove('d-none'));");
        Thread.sleep(1000);
        
        String p2Name = topping2.getName();
        WebElement p2Plus = driver.findElement(By.xpath("//div[contains(@class, 'cart-top-item')][.//div[text()='" + p2Name + "']]//button[text()='+']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", p2Plus);
        Thread.sleep(1000);
        
        // Click "Cập nhật topping"
        WebElement updateBtn = driver.findElement(By.xpath("//button[text()='Cập nhật topping']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", updateBtn);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-success")));
        
        // 5. Verify New Price: 45,000 + 7,000 = 52,000
        lineTotal = driver.findElement(By.className("line-total"));
        String p2Price = lineTotal.getText().replaceAll("[^0-9]", "");
        System.out.println("Price after update: " + p2Price);
        assertTrue(p2Price.contains("52000"));

        // 6. Click UNDO
        System.out.println("Clicking UNDO...");
        WebElement undoLink = driver.findElement(By.partialLinkText("Hoàn tác"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", undoLink);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-success")));
        
        // 7. Verify Price Reverted to 45,000
        lineTotal = driver.findElement(By.className("line-total"));
        String finalPrice = lineTotal.getText().replaceAll("[^0-9]", "");
        System.out.println("Price after UNDO: " + finalPrice);
        assertTrue(finalPrice.contains("45000"), "Price should revert to 45,000. Found: " + finalPrice);
        
        System.out.println("E2E UNDO Passed!");
        Thread.sleep(3000);
    }

    @Test
    @DisplayName("E2E #1: Multi-Item Total Verification")
    void testMultiItemTotal() throws InterruptedException {
        String baseUrl = "http://localhost:" + port + "/alotra";
        login(testUser.getUsername(), "password123");

        // Add Product 1 (40k + 5k = 45k)
        addProductToCart(testProduct.getId(), topping1.getName()); 
        // Add Product 2 (Base only - 40k)
        driver.get(baseUrl + "/products/" + testProduct.getId());
        Thread.sleep(1000);
        jsClick(driver.findElement(By.cssSelector("#addToCartForm button[type='submit']")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-success")));

        driver.get(baseUrl + "/cart");
        Thread.sleep(2000);
        
        // CHECK ALL to update total
        jsClick(driver.findElement(By.id("checkAll")));
        Thread.sleep(1000);
        
        WebElement totalElem = driver.findElement(By.id("selectedTotal"));
        String totalStr = totalElem.getText().replaceAll("[^0-9]", "");
        // 45,000 + 40,000 = 85,000
        assertTrue(totalStr.contains("85000"), "Grand total should be 85,000. Found: " + totalStr);
        System.out.println("E2E #1 Passed: Multi-item total matches 85,000");
    }

    @Test
    @DisplayName("E2E #2: Size Swap Re-decorator in Cart")
    void testSizeSwapDecorator() throws InterruptedException {
        String baseUrl = "http://localhost:" + port + "/alotra";
        login(testUser.getUsername(), "password123");

        // Add Size M + Black Pearl (40k + 5k = 45k)
        addProductToCart(testProduct.getId(), topping1.getName());
        
        driver.get(baseUrl + "/cart");
        Thread.sleep(2000);
        
        // Change Size to L (40k -> 50k, total 45k -> 55k)
        WebElement variantSelect = driver.findElement(By.name("variantId"));
        new Select(variantSelect).selectByVisibleText(sizeL.getName()); // FIX: Use dynamic name
        Thread.sleep(3000);
        
        WebElement lineTotal = driver.findElement(By.className("line-total"));
        String finalPrice = lineTotal.getText().replaceAll("[^0-9]", "");
        assertTrue(finalPrice.contains("55000"), "Price after sizing to L should be 55,000. Found: " + finalPrice);
        System.out.println("E2E #2 Passed: Size swap correctly kept toppings price.");
    }

    @Test
    @DisplayName("E2E #3: Maximum Toppings UI Verification")
    void testMaxToppingsPrice() throws InterruptedException {
        String baseUrl = "http://localhost:" + port + "/alotra";
        login(testUser.getUsername(), "password123");

        driver.get(baseUrl + "/products/" + testProduct.getId());
        Thread.sleep(2000);
        
        addToppingInDetail(topping1.getName());
        addToppingInDetail(topping2.getName());
        addToppingInDetail(topping3.getName());
        
        // 40k + 5k + 7k + 8k = 60k
        WebElement totalText = driver.findElement(By.id("totalText"));
        assertTrue(totalText.getText().contains("60"), "Total with all toppings should be 60,000");
        
        jsClick(driver.findElement(By.cssSelector("#addToCartForm button[type='submit']")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-success")));
    }

    @Test
    @DisplayName("E2E #4: High Quantity Multiplier (x10)")
    void testHighQuantityMultiplier() throws InterruptedException {
        String baseUrl = "http://localhost:" + port + "/alotra";
        login(testUser.getUsername(), "password123");

        driver.get(baseUrl + "/products/" + testProduct.getId());
        Thread.sleep(2000);
        
        addToppingInDetail(topping1.getName()); // 45k
        
        WebElement qtyInput = driver.findElement(By.id("qty"));
        // Force update via JS to trigger event listeners
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = '10'; arguments[0].dispatchEvent(new Event('change'));", qtyInput);
        Thread.sleep(2000);
        
        // Total should be 450,000
        WebElement totalText = driver.findElement(By.id("totalText"));
        String totalVal = totalText.getText().replaceAll("[^0-9]", "");
        assertTrue(totalVal.contains("450000"), "Total x10 should be 450,000. Found: " + totalVal);
    }

    @Test
    @DisplayName("E2E #5: Multi-step Undo (Stack History)")
    void testMultiStepUndo() throws InterruptedException {
        String baseUrl = "http://localhost:" + port + "/alotra";
        login(testUser.getUsername(), "password123");
        addProductToCart(testProduct.getId(), null); // Base 40k
        
        driver.get(baseUrl + "/cart");
        Thread.sleep(1000);
        
        updateToppingInCart(topping1.getName(), 1); // 45k
        updateToppingInCart(topping2.getName(), 1); // 52k
        updateToppingInCart(topping1.getName(), 0); // 47k (remove pearl)
        
        WebElement priceElem = driver.findElement(By.className("line-total"));
        assertTrue(priceElem.getText().contains("47"), "Price after 3 actions should be 47k");
        
        clickUndo(); // Back to 52k
        assertTrue(driver.findElement(By.className("line-total")).getText().contains("52"), "Undo 1 failed");
        clickUndo(); // Back to 45k
        assertTrue(driver.findElement(By.className("line-total")).getText().contains("45"), "Undo 2 failed");
        clickUndo(); // Back to 40k
        assertTrue(driver.findElement(By.className("line-total")).getText().contains("40"), "Undo 3 failed");
    }

    @Test
    @DisplayName("E2E #6: Redo Functionality")
    void testRedoFunctionality() throws InterruptedException {
        String baseUrl = "http://localhost:" + port + "/alotra";
        login(testUser.getUsername(), "password123");
        addProductToCart(testProduct.getId(), null); // Base 40k
        
        driver.get(baseUrl + "/cart");
        Thread.sleep(1000);
        
        updateToppingInCart(topping1.getName(), 1); // 45k
        clickUndo(); // 40k
        assertTrue(driver.findElement(By.className("line-total")).getText().contains("40"));
        
        clickRedo(); // Back to 45k
        assertTrue(driver.findElement(By.className("line-total")).getText().contains("45"), "Redo to 45k failed");
    }

    @Test
    @DisplayName("E2E #7: Zero-Quantity Removal Verification")
    void testZeroQuantityRemoval() throws InterruptedException {
        String baseUrl = "http://localhost:" + port + "/alotra";
        login(testUser.getUsername(), "password123");
        addProductToCart(testProduct.getId(), topping1.getName()); // 45k
        
        driver.get(baseUrl + "/cart");
        Thread.sleep(1000);
        
        updateToppingInCart(topping1.getName(), 0);
        
        WebElement priceElem = driver.findElement(By.className("line-total"));
        assertTrue(priceElem.getText().contains("40"));
    }

    private void login(String user, String pass) throws InterruptedException {
        driver.get("http://localhost:" + port + "/alotra/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys(user);
        driver.findElement(By.name("password")).sendKeys(pass);
        jsClick(driver.findElement(By.cssSelector("button.btn-alotra")));
        Thread.sleep(1500);
    }

    private void addProductToCart(Integer pid, String toppingName) throws InterruptedException {
        driver.get("http://localhost:" + port + "/alotra/products/" + pid);
        Thread.sleep(1500);
        if (toppingName != null) {
            addToppingInDetail(toppingName);
        }
        jsClick(driver.findElement(By.cssSelector("#addToCartForm button[type='submit']")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-success")));
    }

    private void addToppingInDetail(String name) throws InterruptedException {
        WebElement plus = driver.findElement(By.xpath("//div[contains(@class, 'topping-row')][.//div[text()='" + name + "']]//button[contains(@class, 'btn-top-plus')]"));
        jsClick(plus);
        Thread.sleep(500);
    }

    private void updateToppingInCart(String name, int qty) throws InterruptedException {
        ((JavascriptExecutor) driver).executeScript("document.querySelectorAll('.bg-light.d-none').forEach(el => el.classList.remove('d-none'));");
        Thread.sleep(500);
        WebElement input = driver.findElement(By.xpath("//div[contains(@class, 'cart-top-item')][.//div[text()='" + name + "']]//input[@type='number']"));
        input.clear();
        input.sendKeys(String.valueOf(qty));
        Thread.sleep(500);
        jsClick(driver.findElement(By.xpath("//button[text()='Cập nhật topping']")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-success")));
        Thread.sleep(1000);
    }

    private void clickUndo() throws InterruptedException {
        WebElement undo = driver.findElement(By.partialLinkText("Hoàn tác"));
        jsClick(undo);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-success")));
        Thread.sleep(1000);
    }

    private void clickRedo() throws InterruptedException {
        WebElement redo = driver.findElement(By.partialLinkText("Làm lại"));
        jsClick(redo);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-success")));
        Thread.sleep(1000);
    }
    
    private void jsClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }
}
