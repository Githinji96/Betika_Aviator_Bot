package org.app.main;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.managers.ChromeDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class Aviator2 {

    // === CONFIG VARIABLES ===
    static double leftOdds = 2.0;
    static double rightOdds = 3.0;
    static double leftProfitTarget = 100.0;
    static double rightProfitTarget = 100.0;
    static int leftConsecutiveLosses = 0;
    static int rightConsecutiveLosses = 0;
    static final int STOP_LOSS_THRESHOLD = 7;
    static final int STAKE_MINIMUM = 10;
    static final int RESULT_WAIT_TIME = 10;

    static WebDriver driver;

    public static void main(String[] args) {

        // === Start Firefox WebDriver ===
        try {
            WebDriverManager.chromedriver().setup();
            driver = new ChromeDriver();
            driver.get("https://www.betika.com/en-ke/aviator");
            driver.manage().window().maximize();

            // === Wait for page to load ===
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

            switchToIframe();

            System.out.println("Waiting 2 minutes for manual login...");
            Thread.sleep(120_000); // 2 minutes wait

            while (true) {
                double leftStake = Math.max(STAKE_MINIMUM, leftProfitTarget / 10);
                double rightStake = Math.max(STAKE_MINIMUM, rightProfitTarget / 10);
                double stakeToPlace = Math.max(leftStake, rightStake);

                System.err.println("\nPlacing bets with stake: " + stakeToPlace);

                placeBets(stakeToPlace);  // This will place both bets
                checkResultsAndUpdate();  // Check and update profit logic
            }

        } catch (Exception e) {
            System.out.println("Error initializing: " + e.getMessage());
        }
    }

    // === Switch to Aviator iframe ===
    static void switchToIframe() {
        List<WebElement> iframes = driver.findElements(By.tagName("iframe"));
        if (!iframes.isEmpty()) {
            driver.switchTo().frame(iframes.get(0));
            System.out.println("Switched to Aviator iframe");
        } else {
            System.out.println("No iframe found");
        }
    }

    // === Place Left and Right Bets ===
    static void placeBets(double stakeAmount) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            WebElement stakeField = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("input.font-weight-bold[type='text']")
            ));
            stakeField.clear();
            stakeField.sendKeys(String.valueOf((int) stakeAmount));
            System.out.println("Entered stake: " + stakeAmount);

            // TODO: Place both LEFT and RIGHT bets
            // Find buttons and click them using button.click();
            // You can distinguish by position: button[0] = Left, button[1] = Right

        } catch (Exception e) {
            System.out.println("Error placing bets: " + e.getMessage());
        }
    }

    // === Check Results and Update Profit Targets ===
    static void checkResultsAndUpdate() {
        try {
            Thread.sleep(RESULT_WAIT_TIME * 1000); // Wait for result time

            driver.switchTo().defaultContent();

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement myBetsBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(), 'My Bets')]")
            ));
            myBetsBtn.click();

            // === Fetch all bet results ===
            List<WebElement> betItems = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.cssSelector("app-bet-item")
            ));

            boolean leftWon = false;
            boolean rightWon = false;

            // TODO: Update this section to analyze class attribute
            // Example:
            // String classAttr = betItems.get(0).getAttribute("class");
            // if (classAttr.contains("celebrated")) leftWon = true;

            // Use similar logic for rightWon (index 1)

            // === Update Profit Targets ===
            if (leftWon && rightWon) {
                System.out.println("Both sides WON (WW)");
                leftProfitTarget = Math.max(100, leftProfitTarget - (leftProfitTarget / 7));
                rightProfitTarget = Math.max(100, rightProfitTarget - (rightProfitTarget / 7));
                leftConsecutiveLosses = 0;
                rightConsecutiveLosses = 0;

            } else if (leftWon && !rightWon) {
                System.out.println("Left WON, Right LOST (WL)");
                rightProfitTarget += (STAKE_MINIMUM * rightOdds) / 2 / 5;
                rightConsecutiveLosses++;
                leftConsecutiveLosses = 0;

            } else if (!leftWon && rightWon) {
                System.out.println("Left LOST, Right WON (LW)");
                leftProfitTarget += (STAKE_MINIMUM * leftOdds) / 2 / 5;
                leftConsecutiveLosses++;
                rightConsecutiveLosses = 0;

            } else {
                System.out.println("Both LOST (LL)");
                leftProfitTarget += (STAKE_MINIMUM * leftOdds) / 5;
                rightProfitTarget += (STAKE_MINIMUM * rightOdds) / 5;
                leftConsecutiveLosses++;
                rightConsecutiveLosses++;
            }

            // === Handle STOP LOSS ===
            if (leftConsecutiveLosses >= STOP_LOSS_THRESHOLD) {
                System.out.println("LEFT side STOP LOSS triggered!");
                // You can implement a 24-hour timer lock here if needed
            }

            if (rightConsecutiveLosses >= STOP_LOSS_THRESHOLD) {
                System.out.println("RIGHT side STOP LOSS triggered!");
            }

        } catch (Exception e) {
            System.out.println("Error checking results: " + e.getMessage());
        }
    }
}
