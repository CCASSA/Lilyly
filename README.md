# Lilyly

An Android-first private world for body, mind, magic and memory.

Active editable source: **android/**. This continues the existing application and preserves its encrypted local records. The original chunked source and manual legacy workflows remain available for recovery.

- [Full product roadmap](docs/PRODUCT-ROADMAP.md)
- [Cycle Garden milestone and compatibility notes](docs/CYCLE-MILESTONE.md)
- [Deliberate milestone builds](../../actions/workflows/cycle-milestone.yml)

Build with JDK 17, Gradle 8.13, Android SDK 36:

```sh
gradle -p android :app:testDebugUnitTest :app:assembleDebug
```

CI is manual or explicitly requested via `.github/build-request.txt`. Ordinary source commits do not build. APK milestones are tests of progress, not claims that the complete product is finished.
