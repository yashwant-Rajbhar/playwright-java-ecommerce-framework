package base;

import com.microsoft.playwright.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.ConfigReader;
import utils.ScreenshotUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class PlaywrightFactory {

    private static final Logger log = LogManager.getLogger(PlaywrightFactory.class);

    // ThreadLocal = every test thread gets its own browser, which makes parallel runs safe
    private static final ThreadLocal<Playwright> tlPlaywright = new ThreadLocal<>();
    private static final ThreadLocal<Browser> tlBrowser = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> tlContext = new ThreadLocal<>();
    private static final ThreadLocal<Page> tlPage = new ThreadLocal<>();
    private static final ThreadLocal<String> tlBrowserName = new ThreadLocal<>();

    private static final Pattern AD_URLS = Pattern.compile(
            ".*(googlesyndication|doubleclick|googleadservices|fundingchoices|adservice\\.google).*");

    /**
     * @param browserParam value from testng.xml (&lt;parameter name="browser"&gt;); null or blank
     *                     means "use the browser from config / -Dbrowser".
     */
    public static void initBrowser(String browserParam) {
        String browserName = (browserParam != null && !browserParam.isBlank()
                ? browserParam : ConfigReader.get("browser")).toLowerCase();
        boolean headless = Boolean.parseBoolean(ConfigReader.get("headless"));
        log.info("Launching {} (headless={}, env={})", browserName, headless, ConfigReader.getEnv());

        Playwright playwright = Playwright.create();
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().setHeadless(headless);

        Browser browser = switch (browserName) {
            case "chromium" -> playwright.chromium().launch(options);
            case "firefox" -> playwright.firefox().launch(options);
            case "webkit" -> playwright.webkit().launch(options);
            default -> throw new IllegalArgumentException("Unsupported browser: " + browserName);
        };

        // A fresh, isolated "browser profile" for every test, recording a video
        BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1366, 768)
                .setRecordVideoDir(Paths.get("reports", "videos", "tmp")));

        // Trace = DOM snapshots + network + screenshots + actions, for debugging failures
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));

        // Block ad scripts so they can't cover buttons and cause flaky clicks
        context.route(AD_URLS, route -> route.abort());

        Page page = context.newPage();

        tlPlaywright.set(playwright);
        tlBrowser.set(browser);
        tlContext.set(context);
        tlPage.set(page);
        tlBrowserName.set(browserName);
    }

    public static Page getPage() {
        return tlPage.get();
    }

    public static String getBrowserName() {
        String name = tlBrowserName.get();
        return name == null ? "unknown" : name;
    }

    /**
     * Closes everything. If the test failed, keeps a screenshot, trace and video.
     * If it passed, the trace and video are thrown away.
     */
    public static void closeBrowser(String evidenceName, boolean failed) {
        BrowserContext context = tlContext.get();
        Page page = tlPage.get();
        Video video = null;

        try {
            if (context != null && page != null) {
                video = page.video();

                if (failed) {
                    ScreenshotUtil.capture(page, evidenceName);
                    Path trace = Paths.get("reports", "traces", evidenceName + ".zip");
                    Files.createDirectories(trace.getParent());
                    context.tracing().stop(new Tracing.StopOptions().setPath(trace));
                    log.error("Evidence saved: reports/screenshots, reports/traces, reports/videos ({})", evidenceName);
                } else {
                    context.tracing().stop();   // no path = trace is discarded
                }
            }
        } catch (Exception e) {
            log.warn("Could not save evidence: {}", e.getMessage());
        } finally {
            if (context != null) context.close();   // the video file is finished on close
            if (video != null) {
                try {
                    if (failed) {
                        Path target = Paths.get("reports", "videos", evidenceName + ".webm");
                        Files.createDirectories(target.getParent());
                        video.saveAs(target);
                    }
                    video.delete();   // remove the temporary copy
                } catch (Exception e) {
                    log.warn("Could not handle video: {}", e.getMessage());
                }
            }
            if (tlBrowser.get() != null) tlBrowser.get().close();
            if (tlPlaywright.get() != null) tlPlaywright.get().close();
            tlPage.remove();
            tlContext.remove();
            tlBrowser.remove();
            tlPlaywright.remove();
            tlBrowserName.remove();
        }
    }
}