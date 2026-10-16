package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import utils.ConfigReader;
import utils.PriceUtil;

public class CartPage {

    private final Page page;
    private final Locator emptyCartMessage;
    private final Locator rows;
    private final Locator checkoutButton;
    private final Locator loginRequiredText;

    public CartPage(Page page) {
        this.page = page;
        this.emptyCartMessage = page.locator("#empty_cart");
        this.rows = page.locator("tr[id^='product-']");
        this.checkoutButton = page.locator(".check_out");
        this.loginRequiredText = page.locator("#checkoutModal")
                .getByText("Register / Login account to proceed on checkout.");
    }

    public CartPage open() {
        page.navigate(ConfigReader.get("baseUrl") + "/view_cart");
        return this;
    }

    public boolean isEmpty() {
        return emptyCartMessage.isVisible();
    }

    public int getRowCount() {
        return rows.count();
    }

    private Locator row(int id) {
        return page.locator("#product-" + id);
    }

    public boolean hasProduct(int id) {
        return row(id).count() > 0;
    }

    public String getName(int id) {
        return row(id).locator(".cart_description h4 a").textContent().trim();
    }

    public int getPrice(int id) {
        return PriceUtil.parse(row(id).locator(".cart_price p").textContent());
    }

    public int getQuantity(int id) {
        return Integer.parseInt(row(id).locator(".cart_quantity button").textContent().trim());
    }

    public int getTotal(int id) {
        return PriceUtil.parse(row(id).locator(".cart_total_price").textContent());
    }

    public void remove(int id) {
        page.waitForLoadState(LoadState.LOAD);   // make sure cart.js has attached its click handler

        // click and wait for the request the site sends: GET /delete_cart/<id>
        page.waitForResponse(
                response -> response.url().contains("/delete_cart/" + id),
                () -> row(id).locator(".cart_quantity_delete").click());

        row(id).waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.DETACHED)
                .setTimeout(10000));
    }

    // ---------- checkout ----------

    /** Logged-in user: the button goes straight to /checkout. */
    public CheckoutPage proceedToCheckout() {
        page.waitForLoadState(LoadState.LOAD);
        checkoutButton.click();
        page.waitForURL("**/checkout");
        return new CheckoutPage(page);
    }

    /** Logged-out user: the button opens a "Register / Login" popup instead. */
    public void clickProceedToCheckoutLoggedOut() {
        page.waitForLoadState(LoadState.LOAD);
        checkoutButton.click();
    }

    public boolean isLoginRequiredPopupShown() {
        try {
            loginRequiredText.waitFor(new Locator.WaitForOptions().setTimeout(5000));
            return true;
        } catch (PlaywrightException e) {
            return false;
        }
    }
}