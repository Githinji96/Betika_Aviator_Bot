package org.app.main;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.*;
public interface Elements {
    By demoPlayBtn = By.className("purple");

    By placedBetsButton = By.xpath("//button[contains(@class, 'tab') and contains(text(), 'My Bets')]");

    By placedBets = By.xpath("//app-bet-item[contains(@class, 'ng-star-inserted')]");

    By placedBetWon = By.xpath("//app-bet-item[(div[contains(@class, 'celebrated')])]");
    By placedBetLost = By.xpath("//app-bet-item[not(div[contains(@class, 'celebrated')])]");

    By gameIframe = By.id("aviator-iframe");

    By betWaitBtns =  By.xpath("//button[contains(@class, 'bet') and contains(@class, 'btn-danger')]");

    By betBtns = By.xpath("//button[contains(@class, 'bet') and contains(@class, 'btn-success')]");

    By activeBetBtns = By.xpath("//button[contains(@class, 'cashout') and contains(@class, 'btn-warning')]");

    By leftSideBetInput = By.xpath("(//div[contains(@class, 'big')]//input)[1]");
//    By leftActiveBetBtn = By.className("button_warning");
    By leftSideOddInput = By.xpath("(//div[contains(@class, 'small')]//input)[1]");
    By leftSideAutoBetSwitch = By.xpath("(//button[contains(@class, 'tab') and contains(text(), 'Auto')])[1]");
    By leftSideCashOutCheckMark = By.xpath("(//div[contains(@class,'cash-out-switcher')]//app-ui-switcher//div[contains(@class, 'input-switch')])[1]");

//    By rightNormalBetBtn = By.xpath("(//button[contains(@class, 'bet') and contains(@class, 'btn-success')])[2]");
    By rightSideBetInput = By.xpath("(//div[contains(@class, 'big')]//input)[2]");
//    By rightActiveBetBtn = By.className("buttonWarning");
    By rightSideOddInput = By.xpath("(//div[contains(@class, 'small')]//input)[2]");
    By rightSideAutoBetSwitch = By.xpath("(//button[contains(@class, 'tab') and contains(text(), 'Auto')])[2]");
    By rightSideCashOutCheckMark =By.xpath("(//div[contains(@class,'cash-out-switcher')]//app-ui-switcher//div[contains(@class, 'input-switch')])[2]");

}
