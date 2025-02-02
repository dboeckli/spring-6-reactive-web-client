package guru.springframework.spring6reactivewebclient.web.ui;

import guru.springframework.spring6reactivewebclient.client.BeerClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import static guru.springframework.spring6reactivewebclient.test.docker.MvcServerTestUtil.checkDatabaseInitDone;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("it")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BeerListPageIT {

    @LocalServerPort
    private int port;

    private WebDriver webDriver;

    @BeforeAll
    static void setUp(@Autowired BeerClient beerClient) {
        checkDatabaseInitDone(beerClient);
    }

    @BeforeEach
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");  // Run in headless mode
        webDriver = new ChromeDriver(options);
    }

    @AfterEach
    public void tearDown() {
        if (webDriver != null) {
            webDriver.quit();
        }
    }

    @Test
    @Order(0)
    void testBeerListPageLoads() {
        webDriver.get("http://localhost:" + port + "/beers");
        waitForPageLoad();
        assertEquals("Beer List", webDriver.getTitle());
    }

    @Test
    @Order(1)
     void testBeerListContainsItems() {
        webDriver.get("http://localhost:" + port + "/beers");
        waitForPageLoad();
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));

        List<WebElement> beerRows = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("#beerTable tbody tr")));

        log.info("### Found {} beer rows", beerRows.size());

        assertFalse(beerRows.isEmpty(), "Beer list should contain items");
        assertEquals(3, beerRows.size());
    }

    @Test
    @Order(3)
    void testViewButtonWorks() {
        webDriver.get("http://localhost:" + port + "/beers");
        waitForPageLoad();
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));

        // Wait for the first "View" button to be present
        WebElement firstViewButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("viewButton_0")));
        String href = firstViewButton.getAttribute("href");

        assertNotNull(href, "View button should have a href attribute");
        assertTrue(href.contains("/beer/"), "View button should link to a specific beer");

        // Extract the beer ID from the href
        String expectedBeerId = href.substring(href.lastIndexOf("/") + 1);

        // Click the "View" button
        firstViewButton.click();

        // Wait for the new page to load
        wait.until(ExpectedConditions.titleIs("Beer Details"));

        // Find the beer ID element
        WebElement beerIdElement = webDriver.findElement(By.id("beerId"));

        assertAll("Beer Details Page Assertions",
            () -> assertEquals("http://localhost:" + port + "/beer/" + expectedBeerId, webDriver.getCurrentUrl(),
                "Should navigate to the specific beer page"),
            () -> assertEquals("Beer Details", webDriver.findElement(By.id("pageTitle")).getText(),
                "Page title should be 'Beer Details'"),
            () -> assertEquals(expectedBeerId, beerIdElement.getText(),
                "Displayed beer ID should match the expected ID")
        );
   }

    private void waitForPageLoad() {
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(30));
        wait.until((ExpectedCondition<Boolean>) wd ->
            Objects.equals(((JavascriptExecutor) wd).executeScript("return document.readyState"), "complete"));
    }
}
