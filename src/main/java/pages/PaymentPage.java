package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class PaymentPage {

    private final Page page;
    private final Locator nameOnCard;
    private final Locator cardNumber;
    private final Locator cvc;
    private final Locator expiryMonth;
    private final Locator expiryYear;
    private final Locator payButton;

    public PaymentPage(Page page) {
        this.page = page;
        // all assumed
        this.nameOnCard = page.locator("[data-qa='name-on-card']");
        this.cardNumber = page.locator("[data-qa='card-number']");
        this.cvc = page.locator("[data-qa='cvc']");
        this.expiryMonth = page.locator("[data-qa='expiry-month']");
        this.expiryYear = page.locator("[data-qa='expiry-year']");
        this.payButton = page.locator("[data-qa='pay-button']");
    }

    public void enterCardDetails(String name, String number, String cvcCode, String month, String year) {
        nameOnCard.fill(name);
        cardNumber.fill(number);
        cvc.fill(cvcCode);
        expiryMonth.fill(month);
        expiryYear.fill(year);
    }

    public OrderConfirmationPage payAndConfirm() {
        payButton.click();
        page.waitForURL("**/payment_done/**");
        return new OrderConfirmationPage(page);
    }

    public void clickPayWithoutDetails() {
        payButton.click();
    }

    /** The card inputs are "required", so the browser stops an empty submit. */
    public boolean isNameOnCardMissingFlagged() {
        return (Boolean) nameOnCard.evaluate("el => el.validity.valueMissing");
    }

    public String getCurrentUrl() {
        return page.url();
    }
}