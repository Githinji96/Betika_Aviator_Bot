package org.app.main;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.concurrent.atomic.AtomicBoolean;

import java.io.Serializable;
import java.time.Duration;
import java.util.*;

public class App implements Serializable {

    private int wins = 0;

    private static Elements elements;

    // Constants
    private final double LEFT_SIDE_ODDS = 2.50;  // Manually inputted in the Aviator game
    private final double RIGHT_SIDE_ODDS = 3.0; //Manually inputted in the Aviator game
    private final double ORIGINAL_TARGET = 200.0; //Starting profit target per round
    private final double CONSECUTIVE_WIN_RESET = 7; // Reset target after 7 consecutive wins
    private final double ERROR_RETRY_DELAY = 5; //Seconds to wait before retrying after an error
    private String URL = "https://www.betika.com/en-ke/aviator?next=%2Faviator";

    // Variables (Updated During Betting)
    private double profit_target = ORIGINAL_TARGET;
    private int consecutive_wins = 0;
    private int round_number = 0;

    private double currentScore = 0.0;

    // Selenium specific variables
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final JavascriptExecutor js;

    public App() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        js = (JavascriptExecutor) driver;
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        this.navigateToGameBoard();
    }

    private void navigateToGameBoard() {
        // Visit the aviator link from Betika
        driver.get(URL);

        // Click the demo play button
        Optional<WebElement> el1 = Optional.of(driver.findElement(elements.demoPlayBtn));
        el1.ifPresent(WebElement::click);

        WebElement iframe = driver.findElement(elements.gameIframe);  // Locate the iframe by its id
        driver.switchTo().frame(iframe);
    }

    public void run() throws InterruptedException { // Implement logic to navigate

        // Wait until betika aviator page load is displayed
        WebElement leftSideBetInput = wait.until(ExpectedConditions.visibilityOfElementLocated(elements.leftSideBetInput));

        if (hasFlownOrCanPlaceBet(leftSideBetInput)) { //place left side bet
            // Type in the odds
            js.executeScript("arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input'));", driver.findElement(elements.leftSideBetInput), String.format("%.02f", this.ORIGINAL_TARGET));

/*            // click auto bet tab
//            driver.findElement(elements.leftSideAutoBetSwitch).click();

            // Set the bet mode to auto cash out
//            driver.findElement(elements.leftSideCashOutCheckMark).click();

            // Key in the odds
            js.executeScript("arguments[0].setAttribute('value','');" +
                            "arguments[0].value = ''; arguments[0].dispatchEvent(new Event('input')); " + // Clear first
                            "arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input'));",
                    driver.findElement(elements.leftSideOddInput),
                    String.format("%.02f", this.LEFT_SIDE_ODDS)
            );*/
        }

       /* WebElement rightSideBetInput = wait.until(ExpectedConditions.visibilityOfElementLocated(elements.rightSideBetInput));
//        if (hasFlownOrCanPlaceBet(rightSideBetInput)) { //place left side bet
//            // Type in the stake
//            js.executeScript(
//                    "arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input'));",
//                    driver.findElement(elements.rightSideBetInput),
//                    String.format("%.02f", this.ORIGINAL_TARGET)
//            );
//
//            // click auto bet tab
//            driver.findElement(elements.rightSideAutoBetSwitch).click();
//
//            // Set the bet mode to auto cash out
//            driver.findElement(elements.rightSideCashOutCheckMark).click();
//
//            // Key in the odds
//            js.executeScript("arguments[0].setAttribute('value','');" +
//                            "arguments[0].value = ''; arguments[0].dispatchEvent(new Event('input')); " + // Clear first
//                            "arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input'));",
//                    driver.findElement(elements.rightSideOddInput),
//                    String.format("%.02f", this.RIGHT_SIDE_ODDS)
//            );
//
//            for (WebElement el : driver.findElements(elements.betBtns)) {
//                el.click();
//            }
//        }
//        while (this.inputTagHasNoValue(driver.findElement(elements.leftSideOddInput),
//                String.valueOf(this.LEFT_SIDE_ODDS)) ||
//                this.inputTagHasNoValue(driver.findElement(elements.rightSideOddInput),
//                        String.format("%.02f", this.RIGHT_SIDE_ODDS))
//        ) {
//            Thread.sleep(1000);
//        }*/

        // Left side bet stake input
        js.executeScript("let el = arguments[0];" +
                        "el.focus();" +
                        "el.value = arguments[1];" +
                        "el.dispatchEvent(new Event('input', { bubbles: true }));" +
                        "el.dispatchEvent(new Event('change', { bubbles: true }));" +
                        "el.dispatchEvent(new Event('blur', { bubbles: true }));",
                driver.findElement(elements.leftSideBetInput), String.format("%.02f", 200.00));

        // Right side bet stake input
        js.executeScript(
                "let el = arguments[0];" +
                        "el.focus();" +
                        "el.value = arguments[1];" +
                        "el.dispatchEvent(new Event('input', { bubbles: true }));" +
                        "el.dispatchEvent(new Event('change', { bubbles: true }));" +
                        "el.dispatchEvent(new Event('blur', { bubbles: true }));",
                    driver.findElement(elements.rightSideBetInput),
                    String.format("%.02f", this.ORIGINAL_TARGET));

        js.executeScript(
                "arguments[0].value = arguments[1];",
                driver.findElement(elements.rightSideBetInput),
                String.format("%.02f", this.ORIGINAL_TARGET)
        );

        // Confirm bets
        for (WebElement el : driver.findElements(elements.betBtns)) {
            js.executeScript("arguments[0].click()", el);
        }

        // While the plane is flying, check whether each button individually reaches the target
        while (planeIsFlying()) {
            List<WebElement> betButtons = driver.findElements(elements.activeBetBtns);

            if (!betButtons.isEmpty()) {
                for (WebElement button : betButtons) {
                    AtomicBoolean clicked = new AtomicBoolean(false); // individual flag for this button

                    Thread thread = new Thread(() -> {
                        try {
                            if (!clicked.get() && targetReached(button,
                                    this.ORIGINAL_TARGET,
                                    this.ORIGINAL_TARGET + 200.0)) {

                                this.currentScore += this.ORIGINAL_TARGET;

                                if (clicked.compareAndSet(false, true)) {
                                    js.executeScript("arguments[0].click()", button);
                                }
                            }
                        } catch (StaleElementReferenceException e) {
                            System.err.println("⚠️ Stale element skipped.");
                        }
                    });
                    thread.start();
                }
            }
        }
        // Check for win
        System.out.println(this.checkWinStreak());
        if (this.profit_target >= this.currentScore) this.run();
    }

    public boolean targetReached(WebElement cashOutBtn, double stake, double targetWin) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("-?\\d{1,3}(?:,\\d{3})*(?:\\.\\d+)?|-?\\d+(\\.\\d+)?");
        java.util.regex.Matcher matcher = pattern.matcher(cashOutBtn.getText());
        double currentReading = matcher.find() ? Double.parseDouble(matcher.group().replace(",", "")) : 0.0;

        System.out.printf("CS %.02f \nCR: %.02f ::: %s \nTW: %.02f \nStake: %.02f\n",
                this.currentScore,
                currentReading,
                matcher.find()?matcher.group() : "null value",
                targetWin,
                stake);

        // Check if target has been achieved
        if (currentReading + 1.0 >= targetWin) {
            return true;
        }
        return false;

    }

    private boolean hasFlownOrCanPlaceBet(By element) {
        return driver.findElement(element).isDisplayed();
    }

    private boolean hasFlownOrCanPlaceBet(WebElement element) {
        return element.isDisplayed();
    }

    private boolean inputTagHasNoValue(WebElement input, String value) {
        try {
            return !input.isDisplayed() || !Objects.equals(input.getDomProperty("value"), value);
        } catch (Exception e) {
            return true;
        }
    }

    private boolean planeIsFlying() { // bet bts = 0 or cashout btns.size > 0
        List<WebElement> betWaitBtns = driver.findElements(elements.betWaitBtns);
        List<WebElement> activeBetBtns = driver.findElements(elements.activeBetBtns);
        return !betWaitBtns.isEmpty() || !activeBetBtns.isEmpty();
    }

    private String checkWinStreak() {
        // Click the placed bets section toggle button('My Bets')
        js.executeScript("arguments[0].click();", driver.findElement(elements.placedBetsButton));

        // List the betting history
        List<WebElement> betRecords = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(elements.placedBets));

        if (betRecords.isEmpty()) {
            return "Not enough bet records to determine results." + betRecords.toString();
        }

        return "Streak = " + (this.verifyWin(betRecords.get(0)) ? "W" : "L") + (this.verifyWin(betRecords.get(1)) ? "W" : "L");

    }

    private boolean verifyWin(WebElement element) {
        try {
            boolean hasWon = element.findElement(By.className("celebrated")).isDisplayed();
            if (hasWon) this.consecutive_wins += 1;
            else this.consecutive_wins = 0;
            return hasWon;
        } catch (NoSuchElementException e) {
            this.consecutive_wins = 0;
            return false;
        }
    }
}
