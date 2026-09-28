# Multi-Stage Dockerfile for TV Theater (Android TV App)
# Stage 1: Android Build Environment
FROM eclipse-temurin:17-jdk-jammy AS builder

LABEL maintainer="Hiếu Nguyễn <khongdung165@gmail.com>"
LABEL description="TV Theater Android TV Headless Build Container"

ENV ANDROID_HOME=/opt/android-sdk
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV PATH=${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools

# 1. Install prerequisites
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    unzip \
    git \
    && rm -rf /var/lib/apt/lists/*

# 2. Download and set up Android Command Line Tools
ARG CMDLINE_TOOLS_VERSION=11076708
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools && \
    curl -fsSL https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_TOOLS_VERSION}_latest.zip -o /tmp/cmdline-tools.zip && \
    unzip -q /tmp/cmdline-tools.zip -d ${ANDROID_HOME}/cmdline-tools && \
    mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest && \
    rm /tmp/cmdline-tools.zip

# 3. Accept licenses and install Android SDK components (API 34, Build-tools 34.0.0)
RUN yes | sdkmanager --licenses && \
    sdkmanager --install \
        "platform-tools" \
        "platforms;android-34" \
        "build-tools;34.0.0"

# 4. Set working directory and non-root user
WORKDIR /workspace

# Copy Gradle wrapper & configuration first for layer caching
COPY gradle/ /workspace/gradle/
COPY gradlew /workspace/
COPY gradle.properties /workspace/
COPY settings.gradle.kts /workspace/
COPY build.gradle.kts /workspace/
COPY app/build.gradle.kts /workspace/app/

# Download gradle dependencies
RUN ./gradlew --no-daemon dependencies || true

# Copy application source code
COPY app/ /workspace/app/

# Run unit tests and assemble debug APK
RUN ./gradlew --no-daemon testDebugUnitTest assembleDebug

# Stage 2: Minimal Distributable Artifact Container
FROM alpine:3.19 AS exporter

WORKDIR /dist

# Copy APK from builder stage
COPY --from=builder /workspace/app/build/outputs/apk/debug/app-debug.apk /dist/tv-theater-debug.apk

CMD ["cp", "/dist/tv-theater-debug.apk", "/output/tv-theater-debug.apk"]
