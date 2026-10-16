package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {

    private static final Properties props = new Properties();
    private static final String ENV = System.getProperty("env", "qa");

    static {
        load("config.properties");                 // shared defaults
        load("config-" + ENV + ".properties");     // environment overrides
        loadOptional("config-local.properties");   // your private values, never committed
    }

    private static void load(String fileName) {
        try (InputStream is = ConfigReader.class.getClassLoader().getResourceAsStream(fileName)) {
            if (is == null) {
                throw new RuntimeException(fileName + " not found on classpath"
                        + " (environment was '" + ENV + "', valid values: dev, qa, stage)");
            }
            props.load(is);   // later files override earlier ones
        } catch (IOException e) {
            throw new RuntimeException("Could not load " + fileName, e);
        }
    }

    private static void loadOptional(String fileName) {
        try (InputStream is = ConfigReader.class.getClassLoader().getResourceAsStream(fileName)) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not load " + fileName, e);
        }
    }

    /** A -Dkey=value on the command line overrides every file. */
    public static String get(String key) {
        String override = System.getProperty(key);
        return override != null ? override : props.getProperty(key);
    }

    public static String getEnv() {
        return ENV;
    }
}