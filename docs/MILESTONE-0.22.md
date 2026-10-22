# Lilyly 0.22 — Reading sessions

Adds dated foreground-reader sessions, monotonic elapsed durations, book start/end positions, historical UTC offsets and return-to-place navigation. Existing lifetime totals remain as undated earlier time. Book metadata and backup formats receive an additive sessions field.

The first run (39793952061) compiled and passed the new session journeys but failed an older shelf test that clicked an off-screen lazy-list book. Exact logs identified the missing book test tag; the test now scrolls the shelf before tapping it.

Deliberate retry: https://github.com/CCASSA/Lilyly/actions/runs/39844186989
Source: dae34a533dd9c32f02c46af10a35e5e20341e8b0.
The unit report confirms 81 tests with zero failures; the Android instrumentation report confirms OK (27 tests). Session-history screenshot reviewed for readable dates, preserved earlier time, duration, positions and return action. The books-first 0.23 milestone builds on this work and is the delivered APK, with all tests passing.

See READING-SESSIONS.md for behavior and limits. The full dream roadmap remains intended work.
