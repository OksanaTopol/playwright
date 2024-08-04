package by.aksana;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.LoadState;
import io.qameta.allure.Step;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

public class TestPlaywrightSetup extends BaseTest {
    private static final String PLAYWRIGHT_PAGE_TITLE = "Playwright";


    @Test
    public void testPlaywrightGoogle() {
        navigateToPageUrl("https://google.com");
        acceptCookies();
        searchForPlaywrightDocumentation();
        openFirstResult();
        checkPageTitle();
    }

    @Step("Accept cookies")
    private void acceptCookies() {
        Locator element = page.getByText(Pattern.compile("^Zaakceptuj"));
        if (element.isVisible()) {
            element.click();
        }
    }

    @Step("Search for playwright documentation")
    private void searchForPlaywrightDocumentation() {
        page.locator("//textarea[@name='q']").fill("open playwright documentation");
        page.keyboard().press("Enter");
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    @Step("Open first result")
    private void openFirstResult() {
        Locator locator = page.locator("//h3").first();
        screenshot("google.png");
        Assertions.assertTrue(locator.count() > 0, "Check that search results are visible");
        locator.click();
        page.waitForLoadState();
    }

    @Step("Check page title")
    private void checkPageTitle() {
        String title = page.title();
        screenshot("playwright_dev_site.png");
        Assertions.assertTrue(title.contains(PLAYWRIGHT_PAGE_TITLE), "Check page title '%s' contains '%s'".formatted(title, PLAYWRIGHT_PAGE_TITLE));
    }


}
