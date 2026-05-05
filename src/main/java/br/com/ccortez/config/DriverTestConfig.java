package br.com.ccortez.config;

import java.util.Optional;

/**
 * Central place for driver-related settings passed as JVM system properties
 * (often from Maven {@code systemPropertyVariables}) or environment variables.
 *
 * <p>When {@link #APPIUM_APP_KEY} is not set, {@link #appiumAppPath()} falls back to
 * {@code APK_PATH_IN_CONTAINER} and then the same default path as before, so local
 * runs without {@code -Dappium.app} behave unchanged.</p>
 */
public final class DriverTestConfig {

    public static final String SELENIUM_REMOTE_URL_KEY = "SELENIUM_REMOTE_URL";
    public static final String APPIUM_APP_KEY = "appium.app";

    private static final String DEFAULT_APP_PATH_IN_CONTAINER =
            "/home/androidusr/apks/app-debug.apk";

    private DriverTestConfig() {
    }

    /**
     * Selenium Grid / standalone URL, or {@code null} when unset / blank so callers
     * can choose local {@code ChromeDriver}.
     */
    public static String seleniumRemoteUrl() {
        String fromProp = System.getProperty(SELENIUM_REMOTE_URL_KEY);
        if (isNonBlank(fromProp)) {
            return fromProp;
        }
        String fromEnv = System.getenv(SELENIUM_REMOTE_URL_KEY);
        if (isNonBlank(fromEnv)) {
            return fromEnv;
        }
        return null;
    }

    /**
     * APK path as seen by the Appium server (e.g. inside the Docker mount namespace).
     * Resolution order: {@value #APPIUM_APP_KEY} system property, {@code APK_PATH_IN_CONTAINER}
     * environment variable, then the default in-container path.
     */
    public static String appiumAppPath() {
        String fromProp = System.getProperty(APPIUM_APP_KEY);
        if (isNonBlank(fromProp)) {
            return fromProp;
        }
        return Optional.ofNullable(System.getenv("APK_PATH_IN_CONTAINER"))
                .filter(DriverTestConfig::isNonBlank)
                .orElse(DEFAULT_APP_PATH_IN_CONTAINER);
    }

    private static boolean isNonBlank(String s) {
        return s != null && !s.isBlank();
    }
}
