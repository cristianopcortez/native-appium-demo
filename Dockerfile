FROM budtmo/docker-android:emulator_9.0

USER root

# The base image ships with OpenJDK 25, which Gradle 8.7 + Android Gradle
# Plugin 8.6.x (used by the Espresso driver's on-device test APK build)
# do not support. Install OpenJDK 17 and make it the default.
RUN apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
        openjdk-17-jdk-headless \
    && rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# The Espresso driver rebuild step must use an AGP version that matches the app
# under test (see espressoBuildConfig). Keep platforms;android-34 and
# build-tools;34.0.0 so the test server APK can be rebuilt. The base image only
# ships android-28 + build-tools 36, so
# install the missing pieces here and make /opt/android writable by androidusr
# (AGP's auto-installer may still want to touch the SDK dir at runtime).
ENV ANDROID_HOME=/opt/android
ENV ANDROID_SDK_ROOT=/opt/android
RUN yes | /opt/android/cmdline-tools/tools/bin/sdkmanager --sdk_root=/opt/android --licenses > /dev/null \
    && /opt/android/cmdline-tools/tools/bin/sdkmanager --sdk_root=/opt/android \
        "platforms;android-34" \
        "build-tools;34.0.0" \
    && chown -R androidusr:androidusr /opt/android \
    && chmod -R u+w /opt/android

# Patch emulator.py to allow running without KVM (software rendering via swiftshader_indirect)
COPY emulator.py /home/androidusr/docker-android/cli/src/device/emulator.py

USER androidusr
