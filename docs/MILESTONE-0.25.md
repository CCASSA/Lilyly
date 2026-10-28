# Lilyly 0.25 — Reading threads

Dated reading sessions now join Cycle, Sanctuary, Sleep, Medication, Pages, Therapy, Tarot and Rituals in Threads of a day. Each entry names its book, foreground-reader duration, recorded start time/offset and starting/ending place; Return to this reading place opens the book at the recorded end position.

Cross-midnight sessions are explicitly filed under their recorded local start date. Duration is not split into inferred daily totals or treated as attention, sleep or mood evidence. Earlier undated totals do not create invented entries. No correlation or health claim is made.

Existing saved thread choices remain unchanged, so existing users can enable Reading in Choose threads. New installs include Reading in the default set. The chooser scrolls to remain usable as categories grow. Hiding a thread preserves source records.

Includes the reading checkpoint and confirmed session-removal work from 0.24. New unit coverage checks date/offset, linked position, old undated totals and malformed input; an Android journey checks visibility, callback position and persisted hiding. Full build and screenshot validation completed; see results below.

First build 40692756030 compiled and passed 86 unit tests and the new daily-thread journey, but the foreground checkpoint journey timed out. The save loop now uses an Android main-thread Handler with callbacks removed on pause/disposal and scheduled on resume, avoiding the mismatch between Compose test coroutine scheduling and the monotonic elapsed clock. Retry passed; no failed-run APK delivered.

## Verified result

Successful deliberate retry: https://github.com/CCASSA/Lilyly/actions/runs/40693970440
Source: bc3d9fec385411b1274bf637556314eda63fdda5.
86 JVM tests, zero failures; Android report OK (28 tests), including the foreground checkpoint test. Daily reading screenshot and session-removal dialog reviewed. Physical Samsung testing and broader accessibility/device coverage remain.

Delivered Lilyly-Preview-0.25.0.apk: 18,412,921 bytes.
SHA-256: 242e03f658145570bd611d56294ed8a9f6ee0bd78fd53164d759313065baf3ff.
Application ID com.lilyly.app.preview, version code 25, preserved preview signing. Updates Garden Preview alongside the original app. Enable Reading under Threads of a day → Choose threads when upgrading with saved visibility choices.

This is a tested development milestone, not completion of the full dream app. Live Spotify configuration, deeper cycle/pregnancy/TTC experiences, richer canvas/tarot/reference content, lunar accuracy review, broader insights and release-quality device/accessibility/privacy review remain on the roadmap.
