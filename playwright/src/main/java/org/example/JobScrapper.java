package org.example;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import java.util.ArrayList;
import java.util.List;

public class JobScrapper {

    private final Playwright playwright;
    private final Browser browser;
    private Page page;

    public JobScrapper() {
        this.playwright = Playwright.create();
        this.browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        this.page = browser.newPage();
    }

    public void start(String text) {

        try {
            System.out.println("Browsing to Google.com");
            browseToGoogle();

            System.out.println("Searching text: " + text + "");
            searchText(text);

            System.out.println("Going to jobs");
            //page.onResponse(response -> {System.out.println("<<" + response.status() + " " + response.url());});
            if (browseToJobs(text)) {
                List<String> jobs = scrapeJobs();
                jobs.stream().forEach(j -> {
                    System.out.println(j);
                    System.out.println("-------------------");
                });
                System.out.println("Total Jobs: " + jobs.size()) ;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            page.close();
            browser.close();
            playwright.close();
        }
    }

    private void browseToGoogle() {
        page.navigate("https://www.google.com");
        PlaywrightAssertions.assertThat(page).hasTitle("Google");
    }

    private void searchText(String text) {
        Locator search = page.getByTitle("Search");
        search.fill(text);

        Locator searchButton =
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Google Search")).first();

        searchButton.click();

        PlaywrightAssertions.assertThat(page).hasTitle(text + " - Google Search");
        page.waitForLoadState(LoadState.NETWORKIDLE);

    }

    private boolean browseToJobs(String text) throws Exception{
        Locator jobs = page.locator("#fMGJ3e").getByRole(AriaRole.LINK).first();
        jobs.click();
        PlaywrightAssertions.assertThat(page).hasTitle(text);
        page.waitForLoadState();
        boolean jobsFound = false;
        page.getByRole(AriaRole.LIST).last().isVisible();
        int startCount = page.getByRole(AriaRole.LISTITEM).count();

        synchronized (page) {
            int endCount = -1;
            if (startCount > 0) {
                while (startCount != endCount) {
                    Locator job = page.getByRole(AriaRole.LISTITEM).last();
                    job.scrollIntoViewIfNeeded();
                    endCount = startCount;
                    page.getByRole(AriaRole.LIST).last().isVisible();
                    page.waitForLoadState(LoadState.NETWORKIDLE);
                    page.wait(1000);
                    startCount = page.getByRole(AriaRole.LISTITEM).count();
                }
                jobsFound = true;
            }
        }
        return jobsFound;
    }



    private List<String> scrapeJobs() {

        ArrayList<String> list = new ArrayList<>();
        Locator jobs = page.getByRole(AriaRole.LISTITEM);
        jobs.all().stream().forEach( j -> list.add(j.innerText()));
        return list;
    }
}
