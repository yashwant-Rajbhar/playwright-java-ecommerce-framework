package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.ProductDetailsPage;
import pages.ProductPage;

public class ProductTest extends BaseTest {

    @Test(description = "Search finds a known product")
    public void searchFindsProduct() {
        ProductPage products = new ProductPage(getPage()).open();
        products.search("Blue Top");
        Assert.assertTrue(products.getResultsTitle().equalsIgnoreCase("Searched Products"),
                "Unexpected title: " + products.getResultsTitle());
        Assert.assertTrue(products.getProductCount() > 0, "No results");
        Assert.assertTrue(products.hasProductNamed("Blue Top"), "'Blue Top' missing from results");
    }

    @Test(description = "Search with nonsense text returns nothing")
    public void searchNoResults() {
        ProductPage products = new ProductPage(getPage()).open();
        products.search("zzzxxxqqq");
        Assert.assertEquals(products.getProductCount(), 0);
    }

    @Test(description = "Category filter: Women > Dress")
    public void filterByCategory() {
        ProductPage products = new ProductPage(getPage()).open();
        products.filterByCategory("Women", 1);
        Assert.assertTrue(products.getProductCount() > 0, "No products in category");
        Assert.assertTrue(getPage().url().contains("/category_products/1"));
    }

    @Test(description = "Brand filter: Polo")
    public void filterByBrand() {
        ProductPage products = new ProductPage(getPage()).open();
        products.filterByBrand("Polo");
        Assert.assertTrue(products.getProductCount() > 0, "No products for brand");
        Assert.assertTrue(getPage().url().contains("/brand_products/Polo"));
    }

    @Test(description = "Product details page matches the listing")
    public void productDetails() {
        ProductPage products = new ProductPage(getPage()).open();
        String listName = products.getName(1);
        int listPrice = products.getPrice(1);

        ProductDetailsPage details = products.openDetails(1);
        Assert.assertEquals(details.getName(), listName);
        Assert.assertEquals(details.getPrice(), listPrice);
    }
}