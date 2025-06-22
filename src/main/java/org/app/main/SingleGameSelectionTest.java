package org.app.main;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;
import java.util.Random;

public class SingleGameSelectionTest {
    public static void main(String[] args) {
        // Set up WebDriver
        WebDriverManager.chromedriver().setup();
        ChromeDriver driver = new ChromeDriver();
        driver.manage().window().maximize();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            driver.get("https://ke.betika.com/en-ke");

            // Locate and click the initial button
            By clickBtnLocator = By.xpath("//body//div[@id='__nuxt']//div[@class='pull-to-refresh-content relative w-full']//div//div//div//div//div//div[1]//div[1]//div[1]//div[2]//div[2]//a[1]");
            WebElement clickBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(clickBtnLocator));
            clickBtn.click();

            // Wait for outcomes container to load
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".pt-0.mt-0.pb-3")));

            // Get all outcome buttons
            List<WebElement> allButtons = driver.findElements(By.cssSelector("div.mt-0.gap-2.grid.grid-cols-2 button"));

            if (allButtons.isEmpty()) {
                throw new RuntimeException("No betting markets buttons found");
            }

            // Select random button
            Random rand = new Random();
            WebElement selectedButton = allButtons.get(rand.nextInt(allButtons.size()));

            // Click the selected button and print its text

            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", selectedButton);
            System.out.println("Selected: " + selectedButton.getText());


        } catch (Exception e) {
            e.printStackTrace();

        }
       // driver.quit();
    }

}
