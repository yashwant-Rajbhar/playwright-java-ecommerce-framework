package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;

public class OrderConfirmationPage {

    private final Page page;
    private final Locator heading;
    private final Locator confirmationMessage;

    public OrderConfirmationPage(Page page) {
        this.page = page;
        this.heading = page.locator("[data-qa='order-placed']");   // assumed
        this.confirmationMessage = page.getByText("Congratulations! Your order has been confirmed!"); // assumed
    }

    public String getHeading() {
        return heading.textContent().trim();
    }

    public boolean isConfirmationMessageShown() {
        try {
            confirmationMessage.waitFor(new Locator.WaitForOptions().setTimeout(5000));
            return true;
        } catch (PlaywrightException e) {
            return false;
        }
    }

    public String getCurrentUrl() {
        return page.url();
    }
}