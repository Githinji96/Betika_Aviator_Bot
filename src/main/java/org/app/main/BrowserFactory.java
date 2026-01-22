package org.app.main;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;

class BrowserFactory {

    public static WebDriver getDriver(String browserName) {
        switch (browserName.toLowerCase()) {
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                return new org.openqa.selenium.firefox.FirefoxDriver();
            case "edge":
                WebDriverManager.edgedriver().setup();
                return new org.openqa.selenium.edge.EdgeDriver();
            case "chrome":
            default:
                WebDriverManager.chromedriver().setup();
                return new org.openqa.selenium.chrome.ChromeDriver();
        }
    }
}