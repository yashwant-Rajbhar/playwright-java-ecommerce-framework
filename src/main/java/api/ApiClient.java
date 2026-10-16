package api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.RequestOptions;
import utils.ConfigReader;

public class ApiClient implements AutoCloseable {

    // address used for every API-created user, so UI tests can compare against it
    public static final String ADDRESS1 = "12 Test Street";
    public static final String CITY = "Mumbai";
    public static final String COUNTRY = "India";

    /** code = the responseCode from the JSON body (or the HTTP status if the body has none). */
    public record ApiResult(int httpStatus, int code, String message, JsonObject json) {}

    private final Playwright playwright;
    private final APIRequestContext request;

    public ApiClient() {
        playwright = Playwright.create();
        request = playwright.request().newContext(
                new APIRequest.NewContextOptions().setBaseURL(ConfigReader.get("baseUrl")));
    }

    private ApiResult toResult(APIResponse response) {
        JsonObject json;
        try {
            json = JsonParser.parseString(response.text()).getAsJsonObject();
        } catch (Exception e) {
            json = new JsonObject();   // body was not JSON
        }
        int code = json.has("responseCode") ? json.get("responseCode").getAsInt() : response.status();
        String message = json.has("message") ? json.get("message").getAsString() : "";
        return new ApiResult(response.status(), code, message, json);
    }

    // ---------- products and brands ----------
    public ApiResult getProducts() {
        return toResult(request.get("/api/productsList"));
    }

    public ApiResult postToProducts() {
        return toResult(request.post("/api/productsList"));
    }

    public ApiResult getBrands() {
        return toResult(request.get("/api/brandsList"));
    }

    public ApiResult searchProduct(String term) {
        return toResult(request.post("/api/searchProduct",
                RequestOptions.create().setForm(FormData.create().set("search_product", term))));
    }

    public ApiResult searchProductWithoutParameter() {
        return toResult(request.post("/api/searchProduct"));
    }

    // ---------- user account ----------
    public ApiResult createAccount(String name, String email, String password) {
        FormData form = FormData.create()
                .set("name", name)
                .set("email", email)
                .set("password", password)
                .set("title", "Mr")
                .set("birth_date", "10")
                .set("birth_month", "May")
                .set("birth_year", "1990")
                .set("firstname", "QA")
                .set("lastname", "User")
                .set("company", "Test Co")
                .set("address1", ADDRESS1)
                .set("address2", "Suite 4")
                .set("country", COUNTRY)
                .set("zipcode", "400001")
                .set("state", "Maharashtra")
                .set("city", CITY)
                .set("mobile_number", "9876543210");
        return toResult(request.post("/api/createAccount", RequestOptions.create().setForm(form)));
    }

    public ApiResult verifyLogin(String email, String password) {
        return toResult(request.post("/api/verifyLogin", RequestOptions.create()
                .setForm(FormData.create().set("email", email).set("password", password))));
    }

    public ApiResult getUserByEmail(String email) {
        return toResult(request.get("/api/getUserDetailByEmail",
                RequestOptions.create().setQueryParam("email", email)));
    }

    public ApiResult deleteAccount(String email, String password) {
        return toResult(request.delete("/api/deleteAccount", RequestOptions.create()
                .setForm(FormData.create().set("email", email).set("password", password))));
    }

    @Override
    public void close() {
        request.dispose();
        playwright.close();
    }
}