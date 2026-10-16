package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;

public class LoginPage {

    private final Page page;
    private final Locator heading;
    private final Locator emailInput;
    private final Locator passwordInput;
    private final Locator loginButton;
    private final Locator errorMessage;

    public LoginPage(Page page) {
        this.page = page;
        this.heading = page.locator(".login-form h2");
        this.emailInput = page.locator("[data-qa='login-email']");
        this.passwordInput = page.locator("[data-qa='login-password']");
        this.loginButton = page.locator("[data-qa='login-button']");
        this.errorMessage = page.getByText("Your email or password is incorrect!");
    }

    public void login(String email, String password) {
        emailInput.fill(email);
        passwordInput.fill(password);
        loginButton.click();
    }

    public void clickLoginWithEmptyFields() {
        loginButton.click();
    }

    public String getHeadingText() {
        return heading.textContent().trim();
    }

    public boolean isErrorDisplayed() {
        try {
            errorMessage.waitFor(new Locator.WaitForOptions().setTimeout(5000));
            return true;
        } catch (PlaywrightException e) {
            return false;
        }
    }

    /** Both inputs have the HTML "required" attribute, so the browser blocks an empty submit. */
    public boolean isEmailRequiredErrorShown() {
        return (Boolean) emailInput.evaluate("el => el.validity.valueMissing");
    }

    public boolean isPasswordRequiredErrorShown() {
        return (Boolean) passwordInput.evaluate("el => el.validity.valueMissing");
    }

    public String getCurrentUrl() {
        return page.url();
    }
}