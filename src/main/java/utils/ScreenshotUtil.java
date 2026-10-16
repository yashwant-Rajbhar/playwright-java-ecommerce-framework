package utils;

import com.microsoft.playwright.Page;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ScreenshotUtil {

    /** Saves a full-page screenshot to reports/screenshots/<name>.png and returns its path. */
    public static Path capture(Page page, String name) {
        try {
            Path path = Paths.get("reports", "screenshots", name + ".png");
            Files.createDirectories(path.getParent());
            page.screenshot(new Page.ScreenshotOptions().setPath(path).setFullPage(true));
            return path;
        } catch (Exception e) {
            // never let evidence-gathering hide the real test failure
            return null;
        }
    }
}