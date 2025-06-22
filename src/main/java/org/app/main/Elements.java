package org.app.main;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.*;
public class Elements {
    public By demoPlayBtn = By.className("purple");


    public By gameIframe = By.id("aviator-iframe");

    public By betWaitBtns =  By.xpath("//button[contains(@class, 'bet') and contains(@class, 'btn-danger')]");

    public By betBtns = By.xpath("//button[contains(@class, 'bet') and contains(@class, 'btn-success')]");

    public By activeBetBtns = By.xpath("//button[contains(@class, 'cashout') and contains(@class, 'btn-warning')]");

    public By leftSideBetInput = By.xpath("(//div[contains(@class, 'big')]//input)[1]");


    public By rightSideBetInput = By.xpath("(//div[contains(@class, 'big')]//input)[2]");


}
