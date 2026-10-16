package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import utils.PriceUtil;

public class CheckoutPage {

    private final Page page;
    private final Locator deliveryAddress;
    private final Locator billingAddress;
    private final Locator commentBox;
    private final Locator placeOrderButton;

    public CheckoutPage(Page page) {
        this.page = page;
        this.deliveryAddress = page.locator("#address_delivery");   // assumed
        this.billingAddress = page.locator("#address_invoice");     // assumed
        this.commentBox = page.locator("textarea[name='message']"); // assumed
        this.placeOrderButton = page.locator("a[href='/payment']"); // assumed
    }

    public String getDeliveryAddress() {
        return deliveryAddress.textContent().replaceAll("\\s+", " ").trim();
    }

    public String getBillingAddress() {
        return billingAddress.textContent().replaceAll("\\s+", " ").trim();
    }

    public boolean hasProduct(int id) {
        return page.locator("#product-" + id).count() > 0;
    }

    /** The last ".cart_total_price" on the page is the "Total Amount" row. */
    public int getTotalAmount() {
        return PriceUtil.parse(page.locator(".cart_total_price").last().textContent());
    }

    public void enterComment(String text) {
        commentBox.fill(text);
    }

    public PaymentPage placeOrder() {
        placeOrderButton.click();
        page.waitForURL("**/payment");
        return new PaymentPage(page);
    }
}