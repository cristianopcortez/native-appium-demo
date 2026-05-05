# native-appium-demo

End-to-end test suite that drives a native Android app (Jetpack Compose) and a
companion web page from a single JVM process, using **Appium 2 + Espresso**
running inside a fully containerised Android emulator.

Designed as a reference / portfolio project for SDET work: the whole test
environment comes up with a single `docker compose` command and the suite has
no machine-specific paths hard-coded.

---

## Tech stack

| Layer | Tool |
|---|---|
| Language | Java 17 |
| Build | Maven 3.9+ |
| Mobile automation | Appium 2 (java-client 9.x) + Espresso driver |
| Web automation | Selenium 4 + WebDriverManager |
| Test runner | TestNG 7 |
| Assertions | AssertJ / TestNG asserts |
| Android runtime | `budtmo/docker-android:emulator_9.0` (patched for software rendering) |

---

## Repository layout

```
.
├── Dockerfile                 # Extends budtmo/docker-android + patches emulator.py
├── docker-compose.yml         # One-shot environment for the emulator + Appium
├── emulator.py                # Patched emulator launcher (graceful no-KVM fallback)
├── pom.xml                    # Java 17 / Appium / Selenium / TestNG deps
├── testng.xml                 # TestNG suite definition
├── apks/                      # (gitignored) drop app-debug.apk here
├── config/                    # (gitignored) drop my-debug-keystore.jks here
│   └── espresso-build-config.example.json
├── src/
│   ├── main/java/br/com/ccortez/
│   │   ├── config/DriverTestConfig.java
│   │   └── drivers/AndroidDriverManager.java
│   └── test/java/br/com/ccortez/
│       ├── pages/             # Page Objects
│       └── tests/             # Test classes
└── .github/workflows/ci.yml   # Compile + unit tests on push / PR
```

---

## Prerequisites

- **JDK 17+** (Temurin recommended). Set `JAVA_HOME` accordingly.
- **Maven 3.9+** (or use the wrapper `mvnw` if present).
- **Docker Desktop 4.30+** (Windows/macOS) or **Docker Engine 24+** (Linux).
- **Google Chrome on the host** — only required for Mode A (Chrome on host).
  Mode B ships Chrome inside a container and has no host browser requirement.
- The `app-debug.apk` of the app under test (this project targets
  [`Taxi`](https://github.com/cristianopcortez/Taxi) — a Jetpack Compose sample).
  Clone that repository and run `./gradlew :app:assembleDebug` to generate the APK,
  then drop `app-debug.apk` into the `apks/` folder of this project.
- The debug keystore used to sign that APK (`my-debug-keystore.jks`).

---

## First-time setup

1. **Clone and open the project** in IntelliJ IDEA. Make sure the Project SDK
   is set to JDK 17 (see _File → Project Structure → Project_).

2. **Drop the APK** you want to test into `./apks/`:

   ```
   apks/
   └── app-debug.apk
   ```

3. **Drop the matching keystore** into `./config/`:

   ```
   config/
   └── my-debug-keystore.jks
   ```

   If your keystore uses different credentials than the defaults, export them
   before running the tests (see [Configuration](#configuration) below).

4. **Start the emulator + Appium container**:

   ```powershell
   docker compose up -d
   ```

   First boot takes 1–3 minutes. You can watch the emulator in your browser
   at **<http://localhost:6080>** (noVNC — no password).

   To tail the container logs:

   ```powershell
   docker compose logs -f android-appium
   ```

   > This starts **Mode A** (Chrome on the host). If you would rather run
   > Chrome inside a container as well, use
   > `docker compose --profile web up -d` and see
   > [Web execution modes](#web-execution-modes).

5. **Run the tests** once the emulator is on the home screen:

   ```powershell
   mvn test
   ```

6. **Shut everything down** when you are done:

   ```powershell
   docker compose down
   ```

   `docker compose down` stops _every_ container started by this project,
   including the optional `selenium-chrome` service from Mode B — you do not
   need to repeat `--profile web` to tear it down.

---

## Configuration

[`DriverTestConfig`](src/main/java/br/com/ccortez/config/DriverTestConfig.java)
centralises the JVM system properties / environment variables shared by the
drivers:

| Key | Source order | Purpose |
|---|---|---|
| `appium.app` | system property, then `APK_PATH_IN_CONTAINER` env, then default | APK path as seen by the Appium process (in-container path when using Docker). If you omit `-Dappium.app`, behaviour matches the previous default chain (env + `/home/androidusr/apks/app-debug.apk`). |
| `SELENIUM_REMOTE_URL` | system property, then env | When set (e.g. `http://localhost:4444/wd/hub`), `WebDriverFactory` uses `RemoteWebDriver`. When unset / blank, local `ChromeDriver` via WebDriverManager. |

The Maven profile `dockerized-web` sets both via Surefire `systemPropertyVariables`
(see `pom.xml`).

`AndroidDriverManager` still reads these environment variables for Appium device
and signing settings:

| Variable | Default | Purpose |
|---|---|---|
| `APPIUM_SERVER_URL` | `http://localhost:4723` | Appium endpoint |
| `ANDROID_DEVICE_NAME` | `Samsung Galaxy S10` | Emulator profile |
| `ANDROID_UDID` | `emulator-5554` | ADB identifier |
| `ANDROID_PLATFORM_VERSION` | `9` | Android version inside the container |
| `APK_PATH_IN_CONTAINER` | `/home/androidusr/apks/app-debug.apk` (if unset) | Second choice for the APK path when the `appium.app` system property is not set. |
| `KEYSTORE_PATH_IN_CONTAINER` | `/home/androidusr/config/my-debug-keystore.jks` | Keystore path as seen by Appium |
| `KEYSTORE_PASSWORD` | `mypassword` | Override in real use |
| `KEY_ALIAS` | `my-debug-alias` | Override in real use |
| `KEY_PASSWORD` | `mypassword` | Override in real use |

Example — temporarily override the keystore password for a single run:

```powershell
$env:KEYSTORE_PASSWORD="my-real-password"; mvn test
```

The Espresso build configuration (Compose dependency versions, Gradle / AGP
versions, signing block) is embedded directly in
`AndroidDriverManager.java` and passed to Appium as the inline
`appium:espressoBuildConfig` capability — so no JSON file needs to exist
inside the container. A human-readable reference lives at
`config/espresso-build-config.example.json`.

---

## How the pieces fit together

```
+---------------------------+                 +-------------------------------+
|  Test JVM (host, JDK 17)  |  HTTP 4723      |  Docker container             |
|                           |  ─────────────► |   budtmo/docker-android 9.0   |
|  TestNG → AndroidDriver   |                 |   + patched emulator.py       |
|  Selenium → WebDriver ──┐ |                 |   + Appium 2 server           |
+-------------------------│-+                 |   + Android 9 emulator        |
                          │                   |                               |
        Mode A: host Chrome│   volume mounts: | /home/androidusr/apks/        |
        Mode B: HTTP 4444 ─┘   ./apks ────────┼── ▶     app-debug.apk         |
           to selenium-chrome  ./config ──────┼── ▶ /home/androidusr/config/  |
           (2nd container)                    |         …keystore.jks         |
                                              +-------------------------------+
```

The same JVM process drives both the web (Selenium) and the native Android app
(Appium → emulator inside Docker), which is useful for flows that hop between
a web checkout page and a companion mobile app. The Selenium side can target
Chrome either on the host _or_ inside a second container — see
[Web execution modes](#web-execution-modes).

---

## Web execution modes

The Selenium suite supports two interchangeable modes, selected at runtime by
the `SELENIUM_REMOTE_URL` env var / system property (see
[`DriverTestConfig`](src/main/java/br/com/ccortez/config/DriverTestConfig.java)
and [`WebDriverFactory`](src/test/java/br/com/ccortez/drivers/WebDriverFactory.java)):

### Mode A — Chrome on the host _(default, fast dev loop)_

```powershell
docker compose up -d        # only the Android stack
mvn test
```

`WebDriverManager` resolves the ChromeDriver binary automatically and drives
the browser already installed on the developer machine. Lowest friction for
iterating on test code.

### Mode B — Chrome in a container _(reproducible, CI-friendly)_

```powershell
docker compose --profile web up -d    # Android + selenium/standalone-chrome
```

Wait for `selenium-chrome` to be ready before firing the suite — the
container publishes a readiness endpoint on `/status`:

```powershell
docker compose ps                                    # both should show "Up"
curl http://localhost:4444/status                    # "ready": true
```

Then run the tests against the grid:

```powershell
mvn test -Pdockerized-web
```

The Maven profile exports `SELENIUM_REMOTE_URL` and `appium.app` (same default
APK path as local Docker). `WebDriverFactory` switches to `RemoteWebDriver`.
The browser runs inside
the `selenium/standalone-chrome` container so its version is pinned and
decoupled from whatever Chrome is installed on the host.

**Watching the web tests (Mode B):** while `mvn test -Pdockerized-web` runs,
open **<http://localhost:7900/>** in a normal browser — that is the
container’s **noVNC** viewer (same idea as the Android emulator at port 6080).
Log in with password `secret` to see Chrome open, navigate, and tear down as
each test runs.

### Running from IntelliJ

Right-clicking `testng.xml` → **Run '…\testng.xml'** uses IntelliJ's built-in
TestNG runner and **bypasses Maven entirely** — that means Maven profiles
are _not_ applied, so `SELENIUM_REMOTE_URL` is never set and the suite
always executes in **Mode A** (host Chrome), regardless of which compose
profile is up.

| How you run | Goes through Maven? | Result |
|---|---|---|
| `mvn test` | yes | Mode A |
| `mvn test -Pdockerized-web` | yes | Mode B |
| IntelliJ ▶ on `testng.xml` (default) | **no** | Mode A (always) |
| IntelliJ Maven tool window → `test`, `dockerized-web` unchecked | yes | Mode A |
| IntelliJ Maven tool window → `test`, `dockerized-web` checked | yes | Mode B |

To execute Mode B from the IDE, pick one of these — each is a one-time
setup that survives restarts:

1. **Use the Maven tool window.** _View → Tool Windows → Maven_, then:

   - In the **Profiles** section, the `dockerized-web` checkbox is the Mode
     A / Mode B switch:
     - **Unchecked** → running `test` from _Lifecycle_ executes **Mode A**
       (host Chrome).
     - **Checked** → running `test` executes **Mode B** (dockerized Chrome,
       equivalent to `mvn test -Pdockerized-web` on the command line).
   - Double-click `Lifecycle → test` to run. IntelliJ still renders the
     per-method TestNG tree view, so you get the pretty output _and_ profile
     handling in the same place.

2. **VM option on the Run Configuration.** Useful if you prefer the
   right-click ▶ flow on `testng.xml`. Run the suite once so IntelliJ
   creates the config, then open _Run → Edit Configurations…_, select it,
   and add to **VM options**:

   ```
   -DSELENIUM_REMOTE_URL=http://localhost:4444/wd/hub
   ```

   Duplicate the config to keep one "Mode A" and one "Mode B" entry, each
   with its own name — you get a dropdown in the top-right toolbar to pick
   between them per run.

3. **Environment variable on the Run Configuration.** Same dialog, field
   **Environment variables**:

   ```
   SELENIUM_REMOTE_URL=http://localhost:4444/wd/hub
   ```

> **Pitfall:** having the `dockerized-web` profile checked but forgetting
> to start the container (`docker compose --profile web up -d`) will make
> the suite fail with `Connection refused` on port 4444. If you usually
> run Mode A, keep the checkbox unticked by default and only turn it on
> when the container is up.

### Design note

The Android emulator is always containerised because its environment is
heavy and specific (Android SDK, AVD, Gradle/AGP, `/dev/kvm`). Chrome, in
contrast, is cheap to run on the host and that is closer to what a real user
experiences — so containerising it is offered as an _opt-in_ rather than the
default. The test JVM stays the sole orchestrator and talks to both targets
over HTTP, which is the same client/server shape as a Selenium Grid or
Appium Grid deployment.

---

## Troubleshooting

**`The application at '/home/androidusr/C:\...\app-debug.apk' does not exist`**
The Java client sent a Windows path to an Appium server running inside Linux.
Make sure you are using the path constants in `AndroidDriverManager`
(`/home/androidusr/apks/app-debug.apk`) and that the APK is present in
`./apks/` on the host so the volume mount exposes it in the container.

**`Cannot compile module … fallback SDK version 8 does not support the required jvm target 17`**
Your IDE is using JDK 8 as the project SDK. In IntelliJ go to
_File → Project Structure → Project_ and select a JDK 17 installation.

**`Unable to find CDP implementation matching <n>`**
Harmless Selenium warning — your installed Chrome is newer than the bundled
DevTools client. It does not break test execution. If it bothers you, add
`selenium-devtools-v<your-chrome-major>` to `pom.xml`.

**Emulator stuck on boot**
Open <http://localhost:6080> to see what the emulator is doing. On Windows
without KVM the first boot is slow; allow up to 3 minutes. Remove stale state
with `docker compose down -v && docker compose up -d`.

**Mode B: `Connection refused` / `WebDriverException: ... 4444`**
The `selenium-chrome` container is not running. Either you skipped
`--profile web` when starting the stack, or the container crashed. Check
with `docker compose ps` — if `selenium-chrome` is missing, start it with
`docker compose --profile web up -d`; if it's in a restart loop, inspect
logs via `docker compose logs selenium-chrome`.

**Mode B: `mvn test` runs but uses the host Chrome anyway**
You forgot the `-Pdockerized-web` profile, so `SELENIUM_REMOTE_URL` was
never set and `WebDriverFactory` fell back to the local driver. Re-run
with `mvn test -Pdockerized-web` (or export
`SELENIUM_REMOTE_URL=http://localhost:4444/wd/hub` in the shell before
calling `mvn test`).

---

## CI

A GitHub Actions workflow at `.github/workflows/ci.yml` runs a compile-only
build on every push / PR using JDK 17. Full UI tests are not executed on CI
(they require an Android emulator with hardware acceleration) and are
intended to be run locally or on a self-hosted runner with Docker + KVM.

### Maven commands (reference)

| Context | Command | Notes |
|---|---|---|
| Local UI suite | `mvn test` | After `docker compose up -d`. Host Chrome + Appium in Docker. |
| Local, Chrome in Docker | `mvn test -Pdockerized-web` | After `docker compose --profile web up -d`. Sets `SELENIUM_REMOTE_URL` and `appium.app`. |
| Override APK from CLI | `mvn test -Dappium.app=/home/androidusr/apks/other.apk` | Optional; omit `-Dappium.app` to keep env / default resolution. |
| CI (workflow) | `mvn -B -ntp clean test-compile` | Compiles main + test sources, no UI run. |
| CI (workflow) | `mvn -B -ntp -DskipTests package` | Produces the jar; skips TestNG. |

Self-hosted runners that replicate the full stack can reuse the same local
commands, for example `mvn -B -ntp test -Pdockerized-web` once the compose
services are healthy.
