package utils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.WaitUntilState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PageLoad {

    private static final Logger log = LogManager.getLogger(PageLoad.class);

    /**
     * Waits for a marker element. If the page opened without its content (it happens on a slow
     * public site), reloads once and waits again. Fails with the URL and title if still missing.
     */
    public static void waitForContent(Page page, Locator marker, String description) {
        try {
            marker.waitFor(new Locator.WaitForOptions().setTimeout(15000));
        } catch (PlaywrightException first) {
            log.warn("{} did not show its content; reloading once. URL={}", description, page.url());
            page.reload(new Page.ReloadOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
            try {
                marker.waitFor(new Locator.WaitForOptions().setTimeout(30000));
            } catch (PlaywrightException second) {
                throw new RuntimeException(description + " did not load. URL=" + page.url()
                        + ", title='" + page.title() + "'", second);
            }
        }
    }
}