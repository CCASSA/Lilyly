# Lilyly 0.22 — Reading sessions (awaiting verification)

Implementation committed at 17f3374604902874ebb9586078ebc3d257c9c81b. Adds dated foreground-reader sessions, monotonic elapsed durations, book start/end positions, historical UTC offsets and return-to-place navigation. Existing lifetime totals remain as undated earlier time. Book metadata and backup formats receive an additive sessions field.

Deliberately requested build: https://github.com/CCASSA/Lilyly/actions/runs/39793952061
The GitHub Actions job remained queued without a runner during this work session. No compilation or test result is available for 0.22, and no 0.22 APK has been produced or delivered. The workspace has JDK 17 but lacks Gradle and the Android SDK, so local Android validation was unavailable. Do not present this milestone as verified.

Added six JVM tests covering legacy records, JSON persistence, malformed session data, duplicate intervals, background pauses, clock changes and short visits. Added an Android history journey plus session assertions in the imported-book organization journey. These are authored tests, not passing results until CI runs. The workflow will capture reading-history.png for visual review after the journey succeeds.

Next continuation: inspect this run first, fetch exact logs if it fails, fix and retry deliberately, inspect the screenshot and reports, then save and deliver the Garden Preview APK. Do not trigger a duplicate build simply because this one is queued. The last verified milestone remains 0.21 (75 JVM tests, 26 Android journeys).

See READING-SESSIONS.md for behavior and limits. The full dream roadmap remains intended work.
