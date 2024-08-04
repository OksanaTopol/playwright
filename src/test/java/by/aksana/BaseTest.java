package by.aksana;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import io.qameta.allure.Step;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class BaseTest {
    private final Playwright playwright = Playwright.create();
    private Browser browser;
    protected Page page;
    private BrowserContext context;
    protected APIRequest request;

    @Step("Make a screenshot with name: {name}")
    protected void screenshot(String name) {
        Path screenshotPath = Paths.get(name);
        page.screenshot(new Page.ScreenshotOptions().setPath(screenshotPath));
        try (InputStream is = Files.newInputStream(screenshotPath)) {
            Allure.attachment(name, is);
        } catch (IOException e) {
            throw new RuntimeException("Cannot attach screenshot", e);
        }
    }

    private void openByText(String text) {
        page.getByText(text).click();
    }

    @Step("Open link by text {linkText}")
    protected void openPage(String linkText) {
        openByText(linkText);
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName(linkText))).containsText(linkText);
    }

    @Step("Assert that element '{locator}' is visible")
    protected static void assertElementIsVisible(Locator locator) {
        assertThat(locator).isVisible();
    }

    @Attachment(type = "video/webm", fileExtension = "webm")
    private byte[] attachVideoRecord() throws IOException {
        return Files.readAllBytes(page.video().path());
    }

    @Step("Open page at the url: {url}")
    protected void navigateToPageUrl(String url) {
        page.navigate(url);
    }

    @BeforeEach
    public void beforeEach() {
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(Boolean.parseBoolean(System.getenv("HEADLESS"))));
        context = browser.newContext(new Browser.NewContextOptions().setRecordVideoDir(Paths.get("./target/videos/")));
        page = context.newPage();
        request = playwright.request();
    }

    @AfterEach
    public void afterEach() throws IOException {
        context.close();
        attachVideoRecord();
        browser.close();
        playwright.close();
    }
}
