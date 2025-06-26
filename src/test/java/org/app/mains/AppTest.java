package org.app.mains;

import org.app.main.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AppTest {
    String USERNAME = "oauth-bonfacegithinji64-e7117";
    String ACCESS_KEY = "6a142b5e-2a7e-440c-83bf-1e5bcc7a51da";
    String SAUCE_URL = "https://" + USERNAME + ":" + ACCESS_KEY + "@ondemand.eu-central-1.saucelabs.com:443/wd/hub";
    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;
    private static final double ORIGINAL_TARGET = 200.0;
    private static final String URL = "https://www.betika.com/en-ke/aviator?next=%2Faviator";
    public static Elements elements;
    private double currentScore = 0.0;

    @Parameters("browser")
    @BeforeClass
    public void setUp(@Optional("chrome")String browserName) {

        MutableCapabilities sauceOptions = new MutableCapabilities();
        sauceOptions.setCapability("build", "selenium-build-71ZG9");
        sauceOptions.setCapability("name", " Tests in multiple environments");

        sauceOptions.setCapability("username", USERNAME);

        sauceOptions.setCapability("access-key",ACCESS_KEY);
        sauceOptions.setCapability("seleniumVersion", "4.30.0");
        sauceOptions.setCapability("tags","w3c-chrome-tests");

        MutableCapabilities options;
        if (browserName.equalsIgnoreCase("firefox")) {
            FirefoxOptions firefoxOptions = new FirefoxOptions();
            firefoxOptions.setCapability("platformName", "Windows 11");
            firefoxOptions.setCapability("browserVersion", "latest");
            firefoxOptions.setCapability("sauce:options", sauceOptions);
            options = firefoxOptions;
        } else if (browserName.equalsIgnoreCase("edge")) {
            EdgeOptions edgeOptions = new EdgeOptions();
            edgeOptions.setCapability("platformName", "macOS 13");
            edgeOptions.setCapability("browserVersion", "latest");
            edgeOptions.setCapability("sauce:options", sauceOptions);
            options = edgeOptions;
        } else {
            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.setCapability("platformName", "Linux");
            chromeOptions.setCapability("browserVersion", "latest");
            chromeOptions.setCapability("sauce:options", sauceOptions);
            options = chromeOptions;
        }

        try {
            driver = new RemoteWebDriver(new URL(SAUCE_URL), options);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }



        //  driver = BrowserFactory.getDriver(browserName);
        js = (JavascriptExecutor) driver;
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        elements = new Elements();

        navigateToGameBoard();
    }

    private void navigateToGameBoard() {
        driver.get(URL);
        driver.findElement(elements.demoPlayBtn).click();
        WebElement iframe = driver.findElement(elements.gameIframe);
        driver.switchTo().frame(iframe);
    }

    @Test
    public void testAviatorBettingFlow() {
        WebElement leftSideBetInput = wait.until(ExpectedConditions.visibilityOfElementLocated(elements.leftSideBetInput));

        if (leftSideBetInput.isDisplayed()) {
            inputBetAmount(elements.leftSideBetInput, ORIGINAL_TARGET);
        }

        inputBetAmount(elements.leftSideBetInput, 400.00);
        inputBetAmount(elements.rightSideBetInput, ORIGINAL_TARGET);

        for (WebElement el : driver.findElements(elements.betBtns)) {
            js.executeScript("arguments[0].click()", el);
        }

        while (planeIsFlying()) {
            List<WebElement> betButtons = driver.findElements(elements.activeBetBtns);
            for (WebElement button : betButtons) {
                AtomicBoolean clicked = new AtomicBoolean(false);
                new Thread(() -> {
                    try {
                        if (!clicked.get() && targetReached(button, ORIGINAL_TARGET + 20.0)) {
                            currentScore += ORIGINAL_TARGET;
                            if (clicked.compareAndSet(false, true)) {
                                js.executeScript("arguments[0].click()", button);
                            }
                        }
                    } catch (StaleElementReferenceException ignored) {

                    }
                }).start();
            }
        }
    }

    private void inputBetAmount(By locator, double amount) {
        WebElement input = driver.findElement(locator);
        js.executeScript("let el = arguments[0];" +
                        "el.focus();" +
                        "el.value = arguments[1];" +
                        "el.dispatchEvent(new Event('input', { bubbles: true }));" +
                        "el.dispatchEvent(new Event('change', { bubbles: true }));" +
                        "el.dispatchEvent(new Event('blur', { bubbles: true }));",
                input, String.format("%.02f", amount));
    }

    private boolean targetReached(WebElement cashOutBtn, double targetWin) {
        String text = cashOutBtn.getText();
        double currentReading = extractDoubleFromText(text);
        return currentReading + 1.0 >= targetWin;
    }

    private double extractDoubleFromText(String text) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("-?\\d{1,3}(?:,\\d{3})*(?:\\.\\d+)?|-?\\d+(\\.\\d+)?")
                .matcher(text);
        return matcher.find() ? Double.parseDouble(matcher.group().replace(",", "")) : 0.0;
    }

    private boolean planeIsFlying() {
        return !driver.findElements(elements.betWaitBtns).isEmpty() ||
                !driver.findElements(elements.activeBetBtns).isEmpty();
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
