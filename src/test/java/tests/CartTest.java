package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.CartPage;
import pages.ProductDetailsPage;
import pages.ProductPage;

public class CartTest extends BaseTest {

    private static final int BLUE_TOP = 1;     // Rs. 500
    private static final int MEN_TSHIRT = 2;   // Rs. 400

    @Test(description = "Add one product and verify name, price, quantity, total")
    public void addSingleProduct() {
        ProductPage products = new ProductPage(getPage()).open();
        String name = products.getName(BLUE_TOP);
        int price = products.getPrice(BLUE_TOP);

        products.addToCart(BLUE_TOP);
        CartPage cart = products.viewCartFromModal();

        Assert.assertTrue(cart.hasProduct(BLUE_TOP), "Product not in cart");
        Assert.assertEquals(cart.getName(BLUE_TOP), name);
        Assert.assertEquals(cart.getPrice(BLUE_TOP), price);
        Assert.assertEquals(cart.getQuantity(BLUE_TOP), 1);
        Assert.assertEquals(cart.getTotal(BLUE_TOP), price);
    }

    @Test(description = "Add two products and verify each row total")
    public void addTwoProducts() {
        ProductPage products = new ProductPage(getPage()).open();
        int price1 = products.getPrice(BLUE_TOP);
        int price2 = products.getPrice(MEN_TSHIRT);

        products.addToCart(BLUE_TOP);
        products.continueShopping();
        products.addToCart(MEN_TSHIRT);
        CartPage cart = products.viewCartFromModal();

        Assert.assertEquals(cart.getRowCount(), 2);
        Assert.assertEquals(cart.getTotal(BLUE_TOP), price1);
        Assert.assertEquals(cart.getTotal(MEN_TSHIRT), price2);
        Assert.assertEquals(cart.getTotal(BLUE_TOP) + cart.getTotal(MEN_TSHIRT), price1 + price2);
    }

    @Test(description = "Quantity set on the details page: total = price x quantity")
    public void quantityFromDetailsPage() {
        ProductDetailsPage details = new ProductDetailsPage(getPage()).open(BLUE_TOP);
        int price = details.getPrice();

        details.setQuantity(3);
        details.addToCart();
        CartPage cart = details.viewCartFromModal();

        Assert.assertEquals(cart.getQuantity(BLUE_TOP), 3);
        Assert.assertEquals(cart.getTotal(BLUE_TOP), price * 3);
    }

    @Test(description = "Remove a product and the cart becomes empty")
    public void removeProduct() {
        ProductPage products = new ProductPage(getPage()).open();
        products.addToCart(BLUE_TOP);
        CartPage cart = products.viewCartFromModal();

        cart.remove(BLUE_TOP);

        Assert.assertTrue(cart.isEmpty(), "Cart should show the empty message");
    }

    @Test(description = "A new session starts with an empty cart")
    public void emptyCart() {
        CartPage cart = new CartPage(getPage()).open();
        Assert.assertTrue(cart.isEmpty());
        Assert.assertEquals(cart.getRowCount(), 0);
    }
}