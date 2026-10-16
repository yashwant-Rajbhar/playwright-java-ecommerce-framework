package tests;

import api.ApiClient;
import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import pages.*;
import utils.TestDataReader;

import java.util.UUID;

public class CheckoutTest extends BaseTest {

    private static final int BLUE_TOP = 1;
    private static final String PASSWORD = "Test@12345";

    // ThreadLocal so a parallel run never mixes up two tests' users
    private final ThreadLocal<String> createdEmail = new ThreadLocal<>();

    /** Creates a fresh user through the API, logs in through the UI, adds Blue Top, opens the cart. */
    private CartPage loginAndAddProduct() {
        String email = "qa_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";

        try (ApiClient api = new ApiClient()) {
            ApiClient.ApiResult created = api.createAccount("QA User", email, PASSWORD);
            Assert.assertEquals(created.code(), 201, "API user creation failed: " + created.message());
        }
        createdEmail.set(email);

        HomePage home = new HomePage(getPage());
        home.clickSignupLogin().login(email, PASSWORD);
        Assert.assertTrue(home.isLoggedIn(), "Login failed, so checkout can't be tested");

        ProductPage products = new ProductPage(getPage()).open();
        products.addToCart(BLUE_TOP);
        return products.viewCartFromModal();
    }

    @AfterMethod(alwaysRun = true)
    public void deleteTestUser() {
        String email = createdEmail.get();
        if (email != null) {
            try (ApiClient api = new ApiClient()) {
                api.deleteAccount(email, PASSWORD);
            }
            createdEmail.remove();
        }
    }

    @Test(description = "Logged-out user is asked to register or log in at checkout")
    public void loggedOutUserIsAskedToLogin() {
        ProductPage products = new ProductPage(getPage()).open();
        products.addToCart(BLUE_TOP);
        CartPage cart = products.viewCartFromModal();

        cart.clickProceedToCheckoutLoggedOut();

        Assert.assertTrue(cart.isLoginRequiredPopupShown(), "Login popup was not shown");
        Assert.assertFalse(getPage().url().contains("/checkout"), "Should not reach /checkout");
    }

    @Test(description = "Checkout page shows the product, total, and both addresses")
    public void reviewOrder() {
        CartPage cart = loginAndAddProduct();
        int cartTotal = cart.getTotal(BLUE_TOP);

        CheckoutPage checkout = cart.proceedToCheckout();

        Assert.assertTrue(checkout.hasProduct(BLUE_TOP), "Product missing on checkout page");
        Assert.assertEquals(checkout.getTotalAmount(), cartTotal, "Checkout total differs from cart total");
        Assert.assertFalse(checkout.getDeliveryAddress().isEmpty(), "Delivery address is empty");
        Assert.assertFalse(checkout.getBillingAddress().isEmpty(), "Billing address is empty");
    }

    @Test(description = "Full purchase: cart > checkout > payment > order confirmation")
    public void placeOrderEndToEnd() {
        CartPage cart = loginAndAddProduct();
        CheckoutPage checkout = cart.proceedToCheckout();
        checkout.enterComment(TestDataReader.get("order", "comment"));

        PaymentPage payment = checkout.placeOrder();
        payment.enterCardDetails(
                TestDataReader.get("payment", "nameOnCard"),
                TestDataReader.get("payment", "cardNumber"),
                TestDataReader.get("payment", "cvc"),
                TestDataReader.get("payment", "expiryMonth"),
                TestDataReader.get("payment", "expiryYear"));
        OrderConfirmationPage confirmation = payment.payAndConfirm();

        Assert.assertTrue(confirmation.getCurrentUrl().contains("/payment_done"));
        Assert.assertTrue(confirmation.getHeading().equalsIgnoreCase("Order Placed!"),
                "Unexpected heading: " + confirmation.getHeading());
        Assert.assertTrue(confirmation.isConfirmationMessageShown(), "Confirmation message not shown");
    }

    @Test(description = "Payment with empty card fields is blocked")
    public void paymentWithEmptyFields() {
        CartPage cart = loginAndAddProduct();
        PaymentPage payment = cart.proceedToCheckout().placeOrder();

        payment.clickPayWithoutDetails();

        Assert.assertTrue(payment.isNameOnCardMissingFlagged(), "Name on card should be flagged as required");
        Assert.assertTrue(payment.getCurrentUrl().contains("/payment"), "Should stay on the payment page");
        Assert.assertFalse(payment.getCurrentUrl().contains("payment_done"), "Order must not be placed");
    }
}