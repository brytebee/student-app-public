# 🛠️ Development Guide

Welcome to the Native Tutor AI development team! This guide will help you set up your environment and understand our development workflows.

## 💻 Environment Setup

- **Android Studio**: Ladybug or newer.
- **JDK**: Version 17+.
- **Gradle**: 8.x+.
- **Private Repository**: Ensure you have side-by-side access to `language-engine-private`.

### Gradle Configuration
The app links to the private engine via `settings.gradle.kts`. Ensure the path is correct for your local setup:
```kotlin
include(":language-engine-private")
project(":language-engine-private").projectDir = file("../language-engine-private")
```

## 🧪 Testing Workflows

### 1. Manual Verification
- **Microphone**: Test in a quiet environment to ensure STT accuracy.
- **Eco Mode**: You can simulate thermal warnings by manually passing high temperature values to `getSystemOptimization()` in the `MainActivity` initialization.

### 2. Unit Testing
Unit tests for the language logic reside in the `language-engine-private` module. 
- Run `./gradlew :language-engine-private:test` to verify engine logic.

## 🔋 Working with Eco Mode™

Eco Mode is critical for performance on mid-range devices. When implementing new UI components:
- Use `OptimizationLevel` to adjust animation complexity.
- Avoid heavy background processing when `ECO_MODE_ACTIVE` is triggered.

## 📝 Contribution Guidelines

1. **Feature Branches**: Create a branch from `main` for any new feature.
2. **Linguistic Data**: Do not hardcode phrases. Use the [Community Consensus Portal](https://github.com/brytebee/community-consensus) for adding or correcting linguistic data.
3. **PR Reviews**: Ensure all UI changes are tested on at least one physical Android device to verify thermal impact.

---
For any questions, reach out to the core team at `brytebee@gmail.com`.
