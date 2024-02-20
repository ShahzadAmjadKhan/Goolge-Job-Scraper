package org.example;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class Main {
    public static void main(String[] args) {
        JobScrapper scrapper = new JobScrapper();
        scrapper.start(args[0]);
    }
}