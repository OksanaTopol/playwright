package by.aksana.hw4;

import by.aksana.BaseTest;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.qameta.allure.AllureId;
import io.qameta.allure.Step;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.options.WaitForSelectorState.HIDDEN;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestImageLoadingAndMouseOver extends BaseTest {

    @BeforeEach
    public void openWebFormPage() {
        navigateToPageUrl("https://bonigarcia.dev/selenium-webdriver-java/");
    }

    @AllureId("Verify loading images")
    @Test
    public void testLoadingImages(){
        openPage("Loading images");
        Locator spinner = page.locator("#spinner");
        waitForElementIsNotVisible(spinner);
        assertElementContainsText(page.locator("#text"),"Done!");
        assertElementIsVisible(page.locator("#compass"));
        assertElementIsVisible(page.locator("#calendar"));
        assertElementIsVisible(page.locator("#award"));
        assertElementIsVisible(page.locator("#landscape"));
    }

    @AllureId("Verify images names")
    @Test
    public void testImagesNames(){
        openPage("Mouse over");
        assertImageAndCaption("[src*='compass']", "Compass");
        assertImageAndCaption("[src*='calendar']", "Calendar");
        assertImageAndCaption("[src*='award']", "Award");
        assertImageAndCaption("[src*='landscape']", "Landscape");
        List<String> expectedImages = List.of("Compass","Calendar","Award","Landscape");
        assertImagesOrder(expectedImages);
    }

    @Step("Assert that element '{locator}' contains '{text}'")
    private void assertElementContainsText(Locator locator, String text) {
        assertThat(locator).containsText(text);
    }

    @Step("Wait for the element '{locator}' is hidden")
    private void waitForElementIsNotVisible(Locator locator) {
        locator.waitFor(new Locator.WaitForOptions().setState(HIDDEN));
    }

    @Step("Assert that images have order: '{expectedImages}'")
    private void assertImagesOrder(List<String> expectedImages) {
        List<Locator> actualImageElements = page.locator(".caption").all();
        List<String> actualImageCaptions = actualImageElements.stream().map(image -> image.textContent().strip()).toList();
        assertEquals(expectedImages, actualImageCaptions, "Images should be in correct order");
    }

    @Step("Assert that element '{selector}' is visible and has caption '{caption}'")
    private void assertImageAndCaption(String selector, String caption) {
        assertThat(page.locator(selector)).isVisible();
        page.locator(selector).hover();
        assertThat(page.locator(".caption", new Page.LocatorOptions().setHasText(caption))).isVisible();
    }
}
