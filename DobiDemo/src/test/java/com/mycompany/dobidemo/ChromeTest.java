package com.mycompany.dobidemo;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class ChromeTest {

    @Test
    public void openGoogle() {

        
//        DECLARE
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get("https://efadzli.com/dobidemo/login.php?gongbadak=ProUMTedumy_6096684100_KualaNerus_96782048");

        
//        LOGIN
        System.out.println("Title: " + driver.getTitle());
        driver.findElement(By.id("username")).sendKeys("public_umt");
        driver.findElement(By.id("password")).sendKeys("umt2023");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        
//        PRODUCTS
        driver.findElement(By.id("menuProducts")).click();
        String info = driver.findElement(By.id("product_info")).getText();
        System.out.println(info);

        
        
        
//        QUIT
        driver.quit();
    }
}