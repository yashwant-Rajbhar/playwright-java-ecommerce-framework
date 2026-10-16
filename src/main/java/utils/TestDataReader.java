package utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class TestDataReader {

    private static JsonObject root;

    static {
        try (InputStream is = TestDataReader.class.getClassLoader().getResourceAsStream("testdata.json")) {
            if (is == null) {
                throw new RuntimeException("testdata.json not found on classpath");
            }
            try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                root = JsonParser.parseReader(reader).getAsJsonObject();
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read testdata.json", e);
        }
    }

    /** get("payment", "cardNumber") reads {"payment": {"cardNumber": "..."}} */
    public static String get(String section, String key) {
        return root.getAsJsonObject(section).get(key).getAsString();
    }
}