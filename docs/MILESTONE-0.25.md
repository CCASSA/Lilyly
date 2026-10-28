# Lilyly 0.25 — Reading threads (awaiting validation)

Dated reading sessions now join Cycle, Sanctuary, Sleep, Medication, Pages, Therapy, Tarot and Rituals in Threads of a day. Each entry names its book, foreground-reader duration, recorded start time/offset and starting/ending place; Return to this reading place opens the book at the recorded end position.

Cross-midnight sessions are explicitly filed under their recorded local start date. Duration is not split into inferred daily totals or treated as attention, sleep or mood evidence. Earlier undated totals do not create invented entries. No correlation or health claim is made.

Existing saved thread choices remain unchanged, so existing users can enable Reading in Choose threads. New installs include Reading in the default set. The chooser scrolls to remain usable as categories grow. Hiding a thread preserves source records.

Includes the reading checkpoint and confirmed session-removal work from 0.24. New unit coverage checks date/offset, linked position, old undated totals and malformed input; an Android journey checks visibility, callback position and persisted hiding. Full build and screenshot validation pending.

First build 40692756030 compiled and passed 86 unit tests and the new daily-thread journey, but the foreground checkpoint journey timed out. The save loop now uses an Android main-thread Handler with callbacks removed on pause/disposal and scheduled on resume, avoiding the mismatch between Compose test coroutine scheduling and the monotonic elapsed clock. Retry pending; no failed-run APK delivered.
