package tests;

import api.ApiClient;
import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.CheckoutPage;
import pages.HomePage;
import pages.ProductPage;

import java.util.UUID;

public class ApiUiTest extends BaseTest {

    private static final String PASSWORD = "Test@12345";

    @Test(description = "API creates the user, UI logs in and shows the API address, API deletes the user")
    public void apiCreatedUserCanLogInAndSeeAddress() {
        String email = "qa_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";

        try (ApiClient api = new ApiClient()) {
            // 1. create test data through the API
            ApiClient.ApiResult created = api.createAccount("QA User", email, PASSWORD);
            Assert.assertEquals(created.code(), 201, "API user creation failed: " + created.message());

            try {
                // 2. do the operation in the UI
                HomePage home = new HomePage(getPage());
                home.clickSignupLogin().login(email, PASSWORD);
                Assert.assertTrue(home.isLoggedIn(), "UI login failed for the API-created user");

                ProductPage products = new ProductPage(getPage()).open();
                products.addToCart(1);
                CheckoutPage checkout = products.viewCartFromModal().proceedToCheckout();

                // 3. the UI shows what the API stored
                String address = checkout.getDeliveryAddress();   // layout is assumed
                Assert.assertTrue(address.contains(ApiClient.ADDRESS1), "Address missing: " + address);
                Assert.assertTrue(address.contains(ApiClient.CITY), "City missing: " + address);
            } finally {
                api.deleteAccount(email, PASSWORD);   // always clean up
            }

            // 4. confirm through the API that the user is gone
            Assert.assertEquals(api.verifyLogin(email, PASSWORD).code(), 404, "User should be deleted");
        }
    }
}