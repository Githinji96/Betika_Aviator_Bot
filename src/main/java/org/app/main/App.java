package org.app.main;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.Serializable;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class App implements Serializable {


    private static final double ORIGINAL_TARGET = 200.0;

    private static final String URL = "https://www.betika.com/en-ke/aviator?next=%2Faviator";

    private double profitTarget = ORIGINAL_TARGET;

    private double currentScore = 0.0;

    private static Elements elements;

    private  WebDriver driver;
    private final WebDriverWait wait;
    private final JavascriptExecutor js;

    public App() {
        String browser = "chrome"; // or chrome
        this.driver = BrowserFactory.getDriver(browser);
        this.js = (JavascriptExecutor) driver;

        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        driver.manage().window().setSize(new Dimension(1920, 1080));

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

    public void run() {
        WebElement leftSideBetInput = wait.until(ExpectedConditions.visibilityOfElementLocated(elements.leftSideBetInput));

        if (canPlaceBet(leftSideBetInput)) {
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
                        if (!clicked.get() && targetReached(button, ORIGINAL_TARGET, ORIGINAL_TARGET + 30.0)) {
                            currentScore += ORIGINAL_TARGET;
                            if (clicked.compareAndSet(false, true)) {
                                js.executeScript("arguments[0].click()", button);
                            }
                        }
                    } catch (StaleElementReferenceException e) {
                        System.err.println("️ Stale element skipped.");
                    }
                }).start();
            }
        }

       // System.out.println(checkWinStreak());

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

    public boolean targetReached(WebElement cashOutBtn, double stake, double targetWin) {
        String text = cashOutBtn.getText();
        double currentReading = extractDoubleFromText(text);
        System.out.printf("CS %.02f \nCR: %.02f ::: %s \nTW: %.02f \nStake: %.02f\n",
                currentScore, currentReading, text, targetWin, stake);
        return currentReading + 1.0 >= targetWin;
    }

    private double extractDoubleFromText(String text) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("-?\\d{1,3}(?:,\\d{3})*(?:\\.\\d+)?|-?\\d+(\\.\\d+)?")
                .matcher(text);
        return matcher.find() ? Double.parseDouble(matcher.group().replace(",", "")) : 0.0;
    }

    private boolean canPlaceBet(WebElement element) {
        return element.isDisplayed();
    }

    private boolean planeIsFlying() {
        return !driver.findElements(elements.betWaitBtns).isEmpty() ||
                !driver.findElements(elements.activeBetBtns).isEmpty();
    }

}
