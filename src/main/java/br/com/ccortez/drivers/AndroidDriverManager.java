package br.com.ccortez.drivers;

import br.com.ccortez.config.DriverTestConfig;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Optional;

/**
 * Creates and manages the AndroidDriver used by the UI tests.
 *
 * <p>The Appium server is expected to be running inside the Docker container
 * defined in {@code docker-compose.yml} (service {@code android-appium}). The
 * APK and keystore paths below are as seen from <strong>inside</strong> that
 * container, after the volume mounts declared in the compose file.</p>
 *
 * <p>The APK path sent to Appium is resolved by {@link br.com.ccortez.config.DriverTestConfig}
 * (system property {@link br.com.ccortez.config.DriverTestConfig#APPIUM_APP_KEY}, then
 * {@code APK_PATH_IN_CONTAINER}, then an in-container default). Other secrets and
 * device settings use environment variables with sensible defaults.</p>
 */
public class AndroidDriverManager {

    // ===== Appium server & device =====
    private static final String APPIUM_URL =
            envOrDefault("APPIUM_SERVER_URL", "http://localhost:4723");
    private static final String DEVICE_NAME =
            envOrDefault("ANDROID_DEVICE_NAME", "Samsung Galaxy S10");
    private static final String UDID =
            envOrDefault("ANDROID_UDID", "emulator-5554");
    private static final String PLATFORM_VERSION =
            envOrDefault("ANDROID_PLATFORM_VERSION", "9");

    // ===== Paths inside the Docker container =====
    // The docker-compose file mounts
    //   ./apks   -> /home/androidusr/apks
    //   ./config -> /home/androidusr/config
    // under the androidusr home because the Appium server inside the
    // container runs as that user and cannot read /root (mode 700).
    private static final String KEYSTORE_PATH_IN_CONTAINER =
            envOrDefault("KEYSTORE_PATH_IN_CONTAINER", "/home/androidusr/config/my-debug-keystore.jks");

    // ===== Keystore credentials (debug keystore only — override via env in real use) =====
    private static final String KEYSTORE_PASSWORD =
            envOrDefault("KEYSTORE_PASSWORD", "mypassword");
    private static final String KEY_ALIAS =
            envOrDefault("KEY_ALIAS", "my-debug-alias");
    private static final String KEY_PASSWORD =
            envOrDefault("KEY_PASSWORD", "mypassword");

    /**
     * Inline JSON for {@code appium:espressoBuildConfig}. Appium Espresso
     * accepts either a file path or the JSON content directly as a string,
     * so we embed it here to keep the suite fully self-contained.
     *
     * <p>The test-APK that Appium rebuilds must be signed with the same key
     * as the app under test, hence the {@code signingConfig} block. The
     * {@code additionalAndroidTestDependencies} list mirrors the Compose /
     * AndroidX versions used by {@code DesafioShopperAppTaxi} (1.7.5).</p>
     *
     * <p>{@code androidGradlePlugin} must match the app under test (see that
     * project's {@code gradle/libs.versions.toml} {@code agp}): mismatches cause
     * Gradle to fail resolving {@code com.android.application}.</p>
     */
    private static final String ESPRESSO_BUILD_CONFIG_JSON = ""
            + "{"
            + "\"toolsVersions\":{"
            +     "\"gradle\":\"8.7\","
            +     "\"androidGradlePlugin\":\"8.6.0\""
            + "},"
            + "\"signingConfig\":{"
            +     "\"storeFile\":\"" + KEYSTORE_PATH_IN_CONTAINER + "\","
            +     "\"storePassword\":\"" + KEYSTORE_PASSWORD + "\","
            +     "\"keyAlias\":\"" + KEY_ALIAS + "\","
            +     "\"keyPassword\":\"" + KEY_PASSWORD + "\""
            + "},"
            + "\"additionalAndroidTestDependencies\":["
            +     "\"androidx.activity:activity:1.9.0\","
            +     "\"androidx.activity:activity-compose:1.8.2\","
            +     "\"androidx.lifecycle:lifecycle-extensions:2.2.0\","
            +     "\"androidx.fragment:fragment:1.5.1\","
            +     "\"androidx.compose.ui:ui:1.7.5\","
            +     "\"androidx.compose.ui:ui-graphics:1.7.5\","
            +     "\"androidx.compose.ui:ui-tooling:1.7.5\","
            +     "\"androidx.compose.ui:ui-tooling-preview:1.7.5\","
            +     "\"androidx.compose.material:material:1.7.5\","
            +     "\"androidx.compose.material3:material3:1.3.1\","
            +     "\"androidx.compose.runtime:runtime:1.7.5\","
            +     "\"androidx.compose.runtime:runtime-android:1.7.5\","
            +     "\"androidx.navigation:navigation-compose:2.7.7\","
            +     "\"androidx.compose.ui:ui-test-junit4:1.7.5\","
            +     "\"androidx.compose.ui:ui-test-manifest:1.7.5\""
            + "]"
            + "}";

    private AndroidDriver driver;

    public void createAndroidDriver() throws MalformedURLException {
        UiAutomator2Options options = new UiAutomator2Options();

        options.setAutomationName("espresso");
        options.setPlatformName("Android");
        options.setDeviceName(DEVICE_NAME);
        options.setUdid(UDID);
        options.setPlatformVersion(PLATFORM_VERSION);
        options.setCapability("appium:deviceName", DEVICE_NAME);

        options.setApp(DriverTestConfig.appiumAppPath());
        options.setCapability("appium:forceAppiumRebuild", true);
        options.setCapability("appium:espressoBuildConfig", ESPRESSO_BUILD_CONFIG_JSON);

        options.setNewCommandTimeout(Duration.ofSeconds(300));

        URL appiumServerUrl = new URL(APPIUM_URL);
        driver = new AndroidDriver(appiumServerUrl, options);

        // Enable Jetpack Compose element handling in the Espresso driver.
        driver.setSetting("driver", "compose");
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
    }

    public void quitDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    public AndroidDriver getDriver() {
        return this.driver;
    }

    private static String envOrDefault(String name, String defaultValue) {
        return Optional.ofNullable(System.getenv(name))
                .filter(v -> !v.isBlank())
                .orElse(defaultValue);
    }
}
