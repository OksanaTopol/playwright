package by.aksana.hw5;

import by.aksana.BaseTest;
import com.jayway.jsonpath.JsonPath;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.RequestOptions;
import io.qameta.allure.AllureId;
import io.qameta.allure.Step;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.options.WaitForSelectorState.HIDDEN;
import static com.microsoft.playwright.options.WaitForSelectorState.VISIBLE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestAPI extends BaseTest {

    public static final String API_BASE_URL = "https://practice.expandtesting.com/notes/api/";
    public static final String REGISTERED_USER = "users/register";
    public static final String DELETE_USER = "users/delete-account";
    public static final String LOGIN_USER = "users/login";
    public static final String NOTES = "notes";
    public static final String UI_URL = "https://practice.expandtesting.com/notes/app";
    private String userName = generateRandomString(8);
    private String password = generateRandomString(8);
    private String email = generateRandomString(8) + "@email.com";
    private APIRequestContext apiRequestContext;
    private String token;
    private static Random random = new Random();

    @BeforeEach
    public void createUser() {
        navigateToPageUrl(UI_URL);
        apiRequestContext = request.newContext(new APIRequest.NewContextOptions().setBaseURL(API_BASE_URL));
        FormData userFormData = FormData.create().set("name", userName).set("email", email).set("password", password);
        APIResponse apiResponse = apiRequestContext.post(REGISTERED_USER, RequestOptions.create().setForm(userFormData));
        System.out.println(apiResponse.text());
        assertThat(apiResponse).isOK();
    }

    @AfterEach
    public void cleanUpUser(TestInfo testInfo) {
        if (!testInfo.getTestMethod().get().getName().equals("testRegisterUser")) {
            deleteUser();
        }
    }

    @AllureId("Register user via API")
    @Test
    public void testRegisterUser() {
        loginUser();
        loginAsAUserUI();
        assertLogoutButtonIsVisible();
        logoutViaUI();
        deleteUser();
        loginAsAUserUI();
        assertAlertMessage();
    }

    @AllureId("Test that note can be completed")
    @Test
    public void testCompletedNote() {
        loginUser();
        createNote("Note", "Description", "Home");
        loginAsAUserUI();
        page.getByTestId("category-all").waitFor(new Locator.WaitForOptions().setState(VISIBLE));
        assertNoteStatusMessage("You have 0/1 notes completed in the all categories");
        completeNote();
        waitForElementHidden("spinner");
        assertNoteStatusMessage("You have completed all notes");
    }

    @AllureId("Test that large amount of notes can be deleted")
    @Test
    public void testNotesDeletion() {
        loginUser();
        createRandomNotes(20);
        loginAsAUserUI();
        page.getByTestId("note-delete").last().waitFor();
        deleteNotes();
        waitForElementHidden("note-delete-dialog");
        assertNoNotesMessage("You don't have any notes in all categories");
    }

    private void waitForElementHidden(String elementTestId) {
        page.getByTestId(elementTestId).waitFor(new Locator.WaitForOptions().setState(HIDDEN));
    }

    @Step("Create new note")
    private void createNote(String title, String description, String category) {
        FormData noteFormData = FormData.create().set("title", title).set("description", description).set("category", category);
        APIResponse apiResponse = apiRequestContext.post(NOTES, RequestOptions.create().setForm(noteFormData));
        System.out.println(apiResponse.text());
        assertThat(apiResponse).isOK();
    }

    @Step("Complete note")
    private void completeNote() {
        page.getByTestId("toggle-note-switch").check();
    }

    @Step("Assert notes status message")
    private void assertNoteStatusMessage(String expectedStatusText) {
        Locator locator = page.getByTestId("progress-info");
        assertThat(locator).isVisible();
        assertEquals(expectedStatusText, locator.textContent(), "Text is the same");
    }

    @Step("Assert no notes message")
    private void assertNoNotesMessage(String expectedStatusText) {
        Locator locator = page.getByTestId("no-notes-message");
        assertThat(locator).isVisible();
        assertEquals(expectedStatusText, locator.textContent(), "Text is the same");
    }

    @Step("Delete notes")
    private void deleteNotes() {
        int noteCount = page.getByTestId("note-delete").count();
        page.getByTestId("note-delete").all();
        for (int i = 0; i < noteCount; i++) {
            page.getByTestId("note-delete").first().click();
            page.getByTestId("note-delete-confirm").click();
        }
    }

    @Step("Create {amount} notes")
    private void createRandomNotes(int amount) {
        for (int i = 1; i <= amount; i++) {
            String randomTitle = generateRandomString(10);
            String randomContent = generateRandomString(50);
            createNote(randomTitle, randomContent, getRandomCategory());
        }
    }

    @Step("Delete user via API")
    public void deleteUser() {
        APIResponse apiResponse = apiRequestContext.delete(DELETE_USER);
        System.out.println(apiResponse.text());
        assertThat(apiResponse).isOK();
    }

    @Step("Login via API")
    public void loginUser() {
        FormData userFormData = FormData.create().set("email", email).set("password", password);
        APIResponse apiResponse = apiRequestContext.post(LOGIN_USER, RequestOptions.create().setForm(userFormData));
        System.out.println(apiResponse.text());
        assertThat(apiResponse).isOK();
        token = JsonPath.parse(apiResponse.text()).read("$.data.token");
        System.out.println(token);
        apiRequestContext = request.newContext(new APIRequest.NewContextOptions().setBaseURL(API_BASE_URL)
                .setExtraHTTPHeaders(Map.of("x-auth-token", token)));
    }

    @Step("Assert that alert message is visible")
    private void assertAlertMessage() {
        assertThat(page.getByTestId("alert-message")).isVisible();
    }

    @Step("Logout via UI")
    private void logoutViaUI() {
        page.getByTestId("logout").click();
    }

    @Step("Assert that 'Logout' button is visible")
    private void assertLogoutButtonIsVisible() {
        assertThat(page.getByTestId("logout")).isVisible();
    }

    @Step("Login as User via UI")
    private void loginAsAUserUI() {
        //screenshot("loginScreenshot");
        page.getByTestId("open-login-view").waitFor(new Locator.WaitForOptions().setState(VISIBLE));
        page.getByText("Login").click();
        page.getByTestId("login-email").fill(email);
        page.getByTestId("login-password").fill(password);
        page.getByTestId("login-submit").click();
    }

    public static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder stringBuilder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            stringBuilder.append(characters.charAt(index));
        }
        return stringBuilder.toString();
    }

    public static String getRandomCategory() {
        List<String> categories = List.of("Home", "Work", "Personal");
        int index = random.nextInt(categories.size());
        return categories.get(index);
    }
}
