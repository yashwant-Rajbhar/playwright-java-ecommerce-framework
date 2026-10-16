package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import utils.ConfigReader;
import utils.PageLoad;
import utils.PriceUtil;

public class ProductPage {

    private final Page page;
    private final Locator searchBox;
    private final Locator searchButton;
    private final Locator cards;
    private final Locator resultsTitle;
    private final Locator cartModal;
    private final Locator continueShoppingButton;
    private final Locator viewCartLink;

    public ProductPage(Page page) {
        this.page = page;
        this.searchBox = page.locator("#search_product");
        this.searchButton = page.locator("#submit_search");
        this.cards = page.locator(".features_items .product-image-wrapper");
        this.resultsTitle = page.locator(".features_items h2.title");
        this.cartModal = page.locator("#cartModal");
        this.continueShoppingButton = page.locator("#cartModal .close-modal");
        this.viewCartLink = page.locator("#cartModal a[href='/view_cart']");
    }

    public ProductPage open() {
        page.navigate(ConfigReader.get("baseUrl") + "/products",
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        PageLoad.waitForContent(page, cards.first(), "Products page");
        return this;
    }

    // ---------- search ----------
    public void search(String text) {
        searchBox.fill(text);
        searchButton.click();
        page.waitForURL(url -> url.contains("search="));
    }

    public String getResultsTitle() {
        return resultsTitle.textContent().trim();
    }

    public int getProductCount() {
        return cards.count();
    }

    public boolean hasProductNamed(String name) {
        return cards.locator(".productinfo p")
                .getByText(name, new Locator.GetByTextOptions().setExact(true)).count() > 0;
    }

    // ---------- filters ----------
    public void filterByCategory(String parent, int categoryId) {
        // the category panels are collapsed until the parent is clicked
        page.locator("a[href='#" + parent + "']").click();
        page.locator(".category-products a[href='/category_products/" + categoryId + "']").click();
        page.waitForURL("**/category_products/" + categoryId);
    }

    public void filterByBrand(String brand) {
        page.locator(".brands-name a[href='/brand_products/" + brand + "']").click();
        page.waitForURL("**/brand_products/**");
    }

    // ---------- a single product card ----------
    private Locator cardById(int id) {
        return cards.filter(new Locator.FilterOptions()
                .setHas(page.locator("a.add-to-cart[data-product-id='" + id + "']")));
    }

    public int getPrice(int id) {
        return PriceUtil.parse(cardById(id).locator(".productinfo h2").textContent());
    }

    public String getName(int id) {
        return cardById(id).locator(".productinfo p").textContent().trim();
    }

    public void addToCart(int id) {
        Locator card = cardById(id);
        card.hover();
        card.locator(".overlay-content a.add-to-cart").click();
        cartModal.waitFor();   // waits for the "Added!" popup
    }

    public void continueShopping() {
        continueShoppingButton.click();
    }

    public CartPage viewCartFromModal() {
        viewCartLink.click();
        return new CartPage(page);
    }

    public ProductDetailsPage openDetails(int id) {
        cardById(id).locator("a[href='/product_details/" + id + "']").click();
        page.waitForURL("**/product_details/" + id);
        return new ProductDetailsPage(page);
    }
}