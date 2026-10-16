package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;

public class HomePage {

    private final Page page;
    private final Locator signupLoginLink;
    private final Locator logoutLink;
    private final Locator loggedInAs;

    public HomePage(Page page) {
        this.page = page;
        this.signupLoginLink = page.locator(".shop-menu a[href='/login']");
        this.logoutLink = page.locator(".shop-menu a[href='/logout']");
        this.loggedInAs = page.locator(".shop-menu a:has-text('Logged in as')");
    }

    public LoginPage clickSignupLogin() {
        signupLoginLink.click();
        return new LoginPage(page);
    }

    public LoginPage clickLogout() {
        logoutLink.click();
        return new LoginPage(page);
    }

    /** Waits up to 15 seconds for the "Logged in as ..." header link. */
    public boolean isLoggedIn() {
        try {
            loggedInAs.waitFor(new Locator.WaitForOptions().setTimeout(15000));
            return true;
        } catch (PlaywrightException e) {
            return false;
        }
    }
}