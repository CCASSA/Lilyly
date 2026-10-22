# Reading sessions — 0.22

Reading Nook now offers dated reading-session history with book title, foreground reader time, starting/ending page or section, recorded UTC offset and navigation back to the ending place. Returning from the book returns to history. Sessions are added when leaving the reader or when Android pauses it; background time is excluded. Tools/dialog time is included, so this is reader-open time rather than measured attention or reading speed.

Duration uses a monotonic clock. Wall-clock changes do not alter elapsed seconds. Repeated resume/pause/disposal events do not duplicate a running interval. Visits shorter than a second produce no session. Sub-minute durations remain visible in seconds.

Sessions are additive fields in existing encrypted book metadata and travel through the existing backup format. Older readingSeconds totals are preserved and shown separately as undated time; no historical sessions are invented. Malformed session rows are skipped while retaining the book. Deleting a book removes its associated sessions as part of the already-confirmed deletion.

Unexpected process termination before pause/disposal can lose the active unsaved interval. Session checkpoints, history deletion independent of book removal, date filters, goals and cross-system reading insights remain planned. No external service, permissions or dependency changes were introduced. Validated by the 0.22 retry; see MILESTONE-0.22.md.
