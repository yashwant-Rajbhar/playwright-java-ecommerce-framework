package api;

import com.google.gson.JsonArray;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.UUID;

public class ProductApiTest {

    private static String uniqueEmail() {
        return "api_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }

    @Test(description = "GET /api/productsList returns products")
    public void getProductsList() {
        try (ApiClient api = new ApiClient()) {
            ApiClient.ApiResult r = api.getProducts();
            Assert.assertEquals(r.code(), 200);
            Assert.assertTrue(r.json().has("products"), "No 'products' in the response");
            JsonArray products = r.json().getAsJsonArray("products");
            Assert.assertTrue(products.size() > 0, "Product list is empty");
        }
    }

    @Test(description = "POST to productsList is not supported")
    public void postToProductsListIsNotSupported() {
        try (ApiClient api = new ApiClient()) {
            ApiClient.ApiResult r = api.postToProducts();
            Assert.assertEquals(r.code(), 405);
            Assert.assertTrue(r.message().contains("not supported"), "Message: " + r.message());
        }
    }

    @Test(description = "GET /api/brandsList returns brands")
    public void getBrandsList() {
        try (ApiClient api = new ApiClient()) {
            ApiClient.ApiResult r = api.getBrands();
            Assert.assertEquals(r.code(), 200);
            Assert.assertTrue(r.json().has("brands"), "No 'brands' in the response");
        }
    }

    @Test(description = "Search product by keyword")
    public void searchProduct() {
        try (ApiClient api = new ApiClient()) {
            ApiClient.ApiResult r = api.searchProduct("top");
            Assert.assertEquals(r.code(), 200);
            Assert.assertTrue(r.json().getAsJsonArray("products").size() > 0, "No search results");
        }
    }

    @Test(description = "Search without the search_product parameter is a bad request")
    public void searchWithoutParameter() {
        try (ApiClient api = new ApiClient()) {
            ApiClient.ApiResult r = api.searchProductWithoutParameter();
            Assert.assertEquals(r.code(), 400);
            Assert.assertTrue(r.message().contains("search_product"), "Message: " + r.message());
        }
    }

    @Test(description = "Login with an unknown user is rejected")
    public void verifyLoginWithInvalidUser() {
        try (ApiClient api = new ApiClient()) {
            ApiClient.ApiResult r = api.verifyLogin(uniqueEmail(), "doesNotMatter1");
            Assert.assertEquals(r.code(), 404);
            Assert.assertEquals(r.message(), "User not found!");
        }
    }

    @Test(description = "Account lifecycle: create, verify, read, delete, verify gone")
    public void accountLifecycle() {
        String email = uniqueEmail();
        String password = "Test@12345";

        try (ApiClient api = new ApiClient()) {
            try {
                ApiClient.ApiResult created = api.createAccount("API User", email, password);
                Assert.assertEquals(created.code(), 201, "Create failed: " + created.message());
                Assert.assertEquals(created.message(), "User created!");

                ApiClient.ApiResult login = api.verifyLogin(email, password);
                Assert.assertEquals(login.code(), 200);
                Assert.assertEquals(login.message(), "User exists!");

                ApiClient.ApiResult details = api.getUserByEmail(email);   // 'user' key is assumed
                Assert.assertEquals(details.code(), 200);
                Assert.assertTrue(details.json().has("user"), "No 'user' in the response");
                Assert.assertEquals(details.json().getAsJsonObject("user").get("email").getAsString(), email);
            } finally {
                api.deleteAccount(email, password);   // always clean up
            }

            Assert.assertEquals(api.verifyLogin(email, password).code(), 404, "Deleted user should not exist");
        }
    }
}