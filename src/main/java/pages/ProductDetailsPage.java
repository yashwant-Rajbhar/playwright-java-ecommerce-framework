package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import utils.ConfigReader;
import utils.PageLoad;
import utils.PriceUtil;

public class ProductDetailsPage {

    private final Page page;
    private final Locator info;
    private final Locator name;
    private final Locator price;
    private final Locator quantityInput;
    private final Locator addToCartButton;
    private final Locator cartModal;
    private final Locator viewCartLink;

    public ProductDetailsPage(Page page) {
        this.page = page;
        this.info = page.locator(".product-information");
        this.name = page.locator(".product-information h2");
        this.price = page.locator(".product-information span span");
        this.quantityInput = page.locator("#quantity");
        this.addToCartButton = page.locator("button.cart");
        this.cartModal = page.locator("#cartModal");
        this.viewCartLink = page.locator("#cartModal a[href='/view_cart']");
    }

    public ProductDetailsPage open(int productId) {
        page.navigate(ConfigReader.get("baseUrl") + "/product_details/" + productId,
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        PageLoad.waitForContent(page, info, "Product details page");
        return this;
    }

    public String getName() {
        return name.textContent().trim();
    }

    public int getPrice() {
        return PriceUtil.parse(price.first().textContent());
    }

    public void setQuantity(int qty) {
        quantityInput.fill(String.valueOf(qty));
    }

    public void addToCart() {
        addToCartButton.click();
        cartModal.waitFor();
    }

    public CartPage viewCartFromModal() {
        viewCartLink.click();
        return new CartPage(page);
    }
}