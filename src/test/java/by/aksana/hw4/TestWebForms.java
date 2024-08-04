package by.aksana.hw4;

import by.aksana.BaseTest;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureId;
import io.qameta.allure.Step;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class TestWebForms extends BaseTest {

    protected AtomicReference<Request> apiRequest = new AtomicReference<>();
    protected AtomicReference<Response> apiResponse = new AtomicReference<>();
    private static final String SUBMITTED_FORM_URL_MARKER = "submitted-form";


    @BeforeEach
    public void openWebFormPage() {
        navigateToPageUrl("https://bonigarcia.dev/selenium-webdriver-java/");
        openPage("Web form");
        page.onRequest(request -> {
            System.out.println(request.url());
            if (request.url().contains(SUBMITTED_FORM_URL_MARKER)) {
                System.out.println(request.url());
                System.out.printf("%s\n\n%s%n", request, request.response());
                apiRequest.set(request);
            }
        });

        page.onResponse(response -> {
            if (response.url().contains(SUBMITTED_FORM_URL_MARKER)) {
                Allure.step("Verify response");
                apiResponse.set(response);
                assertTrue(response.ok(), "Verify that response status is OK");
            }
        });
        assertThat(page).hasURL(Pattern.compile(".*/web-form\\..*"));
    }

    @AllureId("Verify correct text and password")
    @Test
    public void testTextAndPassword() {
        String textValue = "0123456789";
        String passwordValue = "password";
        fillText(textValue, "#my-text-id");
        fillText(passwordValue, "[name='my-password']");
        clickSubmit();
        Allure.step("Verify request");
        String requestUrl = apiRequest.get().url();
        verifyingRequest(()-> assertTrue(requestUrl.contains("my-password=%s".formatted(passwordValue)),
                        "Assert that there is a password in request"),
                ()-> assertTrue(requestUrl.contains("my-text=%s".formatted(textValue)), "Assert that " +
                        "there is a text in request"));
    }

    @AllureId("Verify that colors and date are changeable")
    @Test
    public void testColorsAndDate() {
        String expectedColor = "#666666";
        fillColor(expectedColor);
        String expectedDate = selectRandomDate();
        clickSubmit();
        Allure.step("Verify request");
        String requestUrl = apiRequest.get().url();
        System.out.println(requestUrl);
        verifyingRequest(()-> assertTrue(requestUrl.contains("my-colors=%s".formatted(expectedColor.replace("#", "%23"))),
                        "Assert that there is a color in request"),
                ()-> assertTrue(requestUrl.contains("my-date=%s".formatted(expectedDate.replace("/", "%2F"))), "Assert that " +
                        "there is a date in request"));
    }

    @AllureId("Verify that element is disabled and has status readonly")
    @Test
    public void testDisabledAndReadonly() {
        assertElementIsDisabled("[name='my-disabled']");
        assertElementIsReadOnly("[name='my-readonly']");
    }

    @AllureId("Verify that element has correct options and ")
    @Test
    public void testSelect() {
        Locator mySelect = page.locator("[name='my-select']");
        Map<String, String> expectedOptions = new HashMap<>();
        expectedOptions.put("1", "One");
        expectedOptions.put("2", "Two");
        expectedOptions.put("3", "Three");
        assertElementIsVisible(mySelect);
        assertSelectedOption(mySelect,"Open this select menu");
        assertOptionsCount(mySelect, 4);
        verifyOptionsValue(mySelect, expectedOptions);
        verifyOrder(mySelect);
    }

    @AllureId("Verify selected option")
    @ParameterizedTest
    @ValueSource(strings = {"One", "Two", "Three"})
    public void testSelectOption(String option) {
        Locator mySelect = page.locator("[name='my-select']");
        selectOption(option, mySelect);
        clickSubmit();
    }

    @AllureId("Verify range")
    @Test
    public void testRange() {
        Locator myRange = page.locator("[name='my-range']");
        assertElementIsVisible(myRange);
        assertAttributeValue(myRange,"min","0");
        assertAttributeValue(myRange,"max","10");
        assertAttributeValue(myRange,"step","1");
        setValue(myRange, "25");
        clickSubmit();
        Allure.step("Verify request");
        String requestUrl = apiRequest.get().url();
        verifyingRequest(()-> assertTrue(requestUrl.contains("my-range=%s".formatted("10")),
                "Assert that there is a range in request"));
    }

    @Step("Click 'Submit' button")
    private void clickSubmit() {
        page.waitForResponse(response -> response.url().contains(SUBMITTED_FORM_URL_MARKER), () -> page.locator("button").click());
    }

    @Step("Fill text '{textValue}' into field '{fieldSelector}'")
    private void fillText(String textValue, String fieldSelector) {
        page.locator(fieldSelector).fill(textValue);
    }

    @Step("Select random date")
    private String selectRandomDate() {
        Locator myDate = page.locator("[name='my-date']");
        myDate.click();
        List<Locator> days = page.locator(".day:not(.old, .new)").all();
        int dayIndex = new Random().nextInt(days.size());
        Locator day = days.get(dayIndex);
        int dayValue = Integer.parseInt(day.textContent());
        day.click();
        String selectedDate = LocalDate.now().withDayOfMonth(dayValue).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        Allure.attachment("Selected date", selectedDate);
        return selectedDate;
    }

    @Step("Fill color '{expectedColor}'")
    private void fillColor(String expectedColor) {
        page.locator("[name='my-colors']").evaluate("object => object.value='%s'".formatted(expectedColor));
    }

    @Step("Verify element '{selector}' is read only")
    private void assertElementIsReadOnly(String selector) {
        assertThat(page.locator(selector)).hasAttribute("readonly", "");
    }

    @Step("Verify element '{selector}' is disabled")
    private void assertElementIsDisabled(String selector) {
        assertThat(page.locator(selector)).isDisabled();
    }

    @Step("Assert that selected option has a correct name")
    private static void assertSelectedOption(Locator mySelect, String selectedOption) {
        assertThat(mySelect.locator("[selected]")).containsText(selectedOption);
    }

    @Step("Assert that there is right amount of options")
    private static void assertOptionsCount(Locator mySelect, int expectedCount) {
        assertEquals(expectedCount, mySelect.locator("option").count(),
                "Select should contain %s options".formatted(expectedCount));
    }

    @Step("Verify that the options are correct")
    private static void verifyOptionsValue(Locator mySelect, Map<String, String> expectedOptions) {
        List<Locator> options = mySelect.locator("option[value]").all();
        Map<String, String> actualOptions = new HashMap<>();
        for (Locator option : options) {
            actualOptions.put(option.getAttribute("value"), option.textContent());
        }
        assertEquals(expectedOptions, actualOptions, "Options should be the same as value");
    }

    @Step("Verify that the order is correct")
    private static void verifyOrder(Locator mySelect) {
        List<Locator> allOptions = mySelect.locator("option").all();
        List<String> expectedOptionsOrder = List.of("Open this select menu", "One", "Two", "Three");
        List<String> actualOptionsOrder = allOptions.stream().map(option -> option.textContent()).toList();
        assertEquals(expectedOptionsOrder, actualOptionsOrder, "Options should be in correct order");
    }

    @Step("Select option '{option}'")
    private static void selectOption(String option, Locator mySelect) {
        mySelect.selectOption(option);
    }

    @Step("Set value '{value}' to element '{locator}'")
    private void setValue(Locator locator, String value) {
        page.evaluate("object => object.value='%s'".formatted(value), locator.elementHandle());
    }

    @Step("Assert attribute '{attribute}' has value '{expectedValue}'")
    private static void assertAttributeValue(Locator locator, String attribute, String expectedValue) {
        assertEquals(expectedValue, locator.getAttribute(attribute),
                "'%s' value should be %s".formatted(attribute, expectedValue));
    }

    private void verifyingRequest(Executable... verifications) {
        page.onRequestFinished((request) -> {
            if (request.url().contains(SUBMITTED_FORM_URL_MARKER)) {
                String requestUrl = request.url();
                Allure.addAttachment("request url", requestUrl);
                assertAll(verifications);
            }
        });
    }
}
