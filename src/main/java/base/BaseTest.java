package base;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import utils.ConfigReader;
import utils.PageLoad;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BaseTest {

    /** "browser" comes from testng.xml when present; otherwise config.properties / -Dbrowser is used. */
    @BeforeMethod
    @Parameters("browser")
    public void setUp(@Optional String browser) {
        PlaywrightFactory.initBrowser(browser);

        Page page = getPage();
        page.setDefaultNavigationTimeout(60000);   // slow public site + 3 parallel browsers
        page.navigate(ConfigReader.get("baseUrl"),
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

        // If the home page arrives without its header, reload once before the test starts
        PageLoad.waitForContent(page, page.locator(".shop-menu"), "Home page");
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        // FAILURE, or SKIP caused by a retry: keep the evidence in both cases
        boolean failed = result.getStatus() != ITestResult.SUCCESS;
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String evidenceName = PlaywrightFactory.getBrowserName() + "_"
                + result.getMethod().getMethodName() + "_" + time;
        PlaywrightFactory.closeBrowser(evidenceName, failed);
    }

    protected Page getPage() {
        return PlaywrightFactory.getPage();
    }
}