package listeners;

import base.PlaywrightFactory;
import com.microsoft.playwright.Page;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;

public class TestListener implements ITestListener {

    private static final Logger log = LogManager.getLogger(TestListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        log.info("START  {}.{}", result.getTestClass().getName(), result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("PASS   {}", result.getMethod().getMethodName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        log.error("FAIL   {} -> {}", result.getMethod().getMethodName(),
                result.getThrowable() == null ? "" : result.getThrowable().getMessage());

        Page page = PlaywrightFactory.getPage();
        if (page != null) {
            try {
                byte[] png = page.screenshot(new Page.ScreenshotOptions()
                        .setFullPage(true)
                        .setTimeout(5000));
                Allure.addAttachment("Screenshot on failure", "image/png", new ByteArrayInputStream(png), "png");
            } catch (Exception e) {
                log.warn("Could not attach screenshot to Allure: {}", e.getMessage());
            }
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        // a retried failure is reported as SKIP; the cause is in the throwable
        log.warn("SKIP   {} (retry or skipped) -> {}", result.getMethod().getMethodName(),
                result.getThrowable() == null ? "" : result.getThrowable().getMessage());
    }
}