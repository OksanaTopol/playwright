package by.aksana.hw4;

import by.aksana.BaseTest;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.qameta.allure.AllureId;
import io.qameta.allure.Step;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestNavigation extends BaseTest {

    public static final String FIRST_PAGE_TEXT = "Lorem ipsum dolor sit amet, consectetur" +
            " adipiscing elit, sed do eiusmod tempor incididunt ut\n" +
            "            labore et dolore magna aliqua.";
    public static final String SECOND_PAGE_TEXT = "Ut enim ad minim veniam, quis nostrud " +
            "exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure " +
            "dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.";
    private static final String THIRD_PAGE_TEXT = "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui " +
            "officia deserunt mollit anim id est laborum.";
    public static final String FIRST_PAGE_URL = "navigation1.html";
    private static final String SECOND_PAGE_URL = "navigation2.html";
    private static final String THIRD_PAGE_URL = "navigation3.html";

    private Locator previous;
    private Locator firstButton;
    private Locator secondButton;
    private Locator thirdButton;
    private Locator nextButton;

    @BeforeEach
    public void openWebFormPage() {
        navigateToPageUrl("https://bonigarcia.dev/selenium-webdriver-java/");
        openPage("Navigation");
        previous = getPaginationLocatorByText("Previous");
        firstButton = getPaginationLocatorByText("1");
        secondButton = getPaginationLocatorByText("2");
        thirdButton = getPaginationLocatorByText("3");
        nextButton = getPaginationLocatorByText("Next");
    }

    @AllureId("Verify that elements are visible")
    @Test
    public void testStartPagination(){
        assertElementIsVisible(page.locator(".pagination"));
        assertFirstPage();
    }

    @AllureId("Verify Previous and Next buttons")
    @Test
    public void testNextAndPreviousButtons(){
        clickButton(nextButton);
        assertSecondPage();
        clickButton(nextButton);
        assertThirdPage();
        clickButton(previous);
        assertSecondPage();
        clickButton(previous);
        assertFirstPage();
    }

    @Step("Click button '{button}'")
    private void clickButton(Locator button) {
        button.click();
    }

    @AllureId("Verify state transitions")
    @Test
    public void testStateTransitions(){
        clickButton(firstButton);
        assertFirstPage();
        clickButton(secondButton);
        assertSecondPage();
        clickButton(thirdButton);
        assertThirdPage();
    }

    @Step("Assert first page")
    private void assertFirstPage() {
        verifyPaginationElements(getVerificationsForFirstPage());
        assertPageURL(FIRST_PAGE_URL);
        assertCurrentPageText(FIRST_PAGE_TEXT);
    }

    private Map<Locator, List<String>> getVerificationsForFirstPage() {
        Map<Locator,List <String>> paginationElements = new HashMap<>();
        paginationElements.put(previous, List.of("Disabled", "Visible"));
        paginationElements.put(firstButton, List.of("Enabled","Highlighted", "Visible"));
        paginationElements.put(secondButton, List.of("Enabled", "Visible"));
        paginationElements.put(thirdButton, List.of("Enabled", "Visible"));
        paginationElements.put(nextButton, List.of("Enabled", "Visible"));
        return paginationElements;
    }

    @Step("Assert second page")
    private void assertSecondPage() {
        verifyPaginationElements(getVerificationsForSecondPage());
        assertPageURL(SECOND_PAGE_URL);
        assertCurrentPageText(SECOND_PAGE_TEXT);
    }

    private Map<Locator, List<String>> getVerificationsForSecondPage() {
        Map<Locator,List <String>> paginationElements = new HashMap<>();
        paginationElements.put(previous, List.of("Enabled", "Visible"));
        paginationElements.put(firstButton, List.of("Enabled", "Visible"));
        paginationElements.put(secondButton, List.of("Enabled", "Highlighted", "Visible"));
        paginationElements.put(thirdButton, List.of("Enabled", "Visible"));
        paginationElements.put(nextButton, List.of("Enabled", "Visible"));
        return paginationElements;
    }

    @Step("Assert third page")
    private void assertThirdPage() {
        verifyPaginationElements(getVerificationsForThirdPage());
        assertPageURL(THIRD_PAGE_URL);
        assertCurrentPageText(THIRD_PAGE_TEXT);
    }

    private Map<Locator, List<String>> getVerificationsForThirdPage() {
        Map<Locator,List <String>> paginationElements = new HashMap<>();
        paginationElements.put(previous, List.of("Enabled", "Visible"));
        paginationElements.put(firstButton, List.of("Enabled", "Visible"));
        paginationElements.put(secondButton, List.of("Enabled", "Visible"));
        paginationElements.put(thirdButton, List.of("Enabled", "Highlighted", "Visible"));
        paginationElements.put(nextButton, List.of("Disabled", "Visible"));
        return paginationElements;
    }

    private static void verifyPaginationElements(Map<Locator, List<String>> paginationElements) {
        for (Map.Entry<Locator, List<String>> entry : paginationElements.entrySet()){
            Locator locator = entry.getKey();
            List<String> verifications = entry.getValue();
            for (String verification: verifications){
                switch (verification){
                    case "Disabled":
                        assertElementIsDisabled(locator);
                        break;
                    case "Visible":
                        assertElementIsVisible(locator);
                        break;
                    case "Enabled":
                        assertElementIsEnabled(locator);
                        break;
                    case "Highlighted":
                        assertElementIsActive(locator);
                        break;
                }
            }
        }
    }

    @Step("Assert that page contains text '{expectedText}'")
    private void assertCurrentPageText(String expectedText) {
        assertThat(page.locator(".lead")).containsText(expectedText);
    }

    @Step("Assert that URL ends with '{expectedURL}'")
    private void assertPageURL(String expectedURL) {
        assertTrue(page.url().endsWith(expectedURL), "URL should ends with %s".formatted(expectedURL));
    }

    @Step("Assert that element '{locator}' is active")
    private static void assertElementIsActive(Locator locator) {
        assertTrue(locator.getAttribute("class").contains("active"), "Element should be active");
    }

    @Step("Assert that element '{locator}' is enabled")
    private static void assertElementIsEnabled(Locator locator) {
        assertThat(locator).isEnabled();
    }

    private Locator getPaginationLocatorByText(String text) {
        return page.locator("li", new Page.LocatorOptions().setHasText(text));
    }

    @Step("Verify element '{locator}' is disabled")
    private static void assertElementIsDisabled(Locator locator) {
        assertTrue(locator.getAttribute("class").contains("disabled"), "Element should be disabled");
    }
}
