package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.LoginPage;
import utils.ConfigReader;

public class LoginTest extends BaseTest {

    @Test(description = "Login page opens from the header")
    public void loginPageOpens() {
        LoginPage login = new HomePage(getPage()).clickSignupLogin();
        Assert.assertEquals(login.getHeadingText(), "Login to your account");
    }

    @Test(description = "Valid credentials log the user in")
    public void validLogin() {
        HomePage home = new HomePage(getPage());
        LoginPage login = home.clickSignupLogin();
        login.login(ConfigReader.get("validEmail"), ConfigReader.get("validPassword"));
        Assert.assertTrue(home.isLoggedIn(), "'Logged in as' was not shown after valid login");
    }

    @Test(description = "Wrong password shows an error")
    public void invalidPassword() {
        LoginPage login = new HomePage(getPage()).clickSignupLogin();
        login.login(ConfigReader.get("validEmail"), "wrongPassword123");
        Assert.assertTrue(login.isErrorDisplayed(), "Error message not displayed");
    }

    @Test(description = "Empty fields are blocked by the browser")
    public void emptyFields() {
        LoginPage login = new HomePage(getPage()).clickSignupLogin();
        login.clickLoginWithEmptyFields();
        Assert.assertTrue(login.isEmailRequiredErrorShown(), "Email should be flagged as required");
        Assert.assertTrue(login.getCurrentUrl().contains("/login"), "Should stay on the login page");
    }

    @Test(description = "Logout returns the user to the login page")
    public void logout() {
        HomePage home = new HomePage(getPage());
        LoginPage login = home.clickSignupLogin();
        login.login(ConfigReader.get("validEmail"), ConfigReader.get("validPassword"));
        Assert.assertTrue(home.isLoggedIn(), "Login failed, so logout can't be tested");

        LoginPage afterLogout = home.clickLogout();
        Assert.assertTrue(afterLogout.getCurrentUrl().contains("/login"));
        Assert.assertEquals(afterLogout.getHeadingText(), "Login to your account");
    }
}