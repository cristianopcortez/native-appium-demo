FROM budtmo/docker-android:emulator_9.0

USER root

# The base image ships with OpenJDK 25. Espresso driver 8.x builds the
# bundled espresso-server with Gradle 9 + AGP 9.x (driver defaults); use
# OpenJDK 17 as a stable LTS toolchain for that Gradle build.
RUN apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
        openjdk-17-jdk-headless \
    && rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Espresso server compilation targets SDK 34; install API 34 + build-tools so
# the driver rebuild step succeeds regardless of the app's own AGP version.
# The base image only ships android-28 + build-tools 36; install the missing
# pieces here and make /opt/android writable by androidusr (AGP may touch the
# SDK dir at runtime).
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
