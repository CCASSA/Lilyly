# Reading sessions — 0.22

Reading Nook now offers dated reading-session history with book title, foreground reader time, starting/ending page or section, recorded UTC offset and navigation back to the ending place. Returning from the book returns to history. Sessions are added when leaving the reader or when Android pauses it; background time is excluded. Tools/dialog time is included, so this is reader-open time rather than measured attention or reading speed.

Duration uses a monotonic clock. Wall-clock changes do not alter elapsed seconds. Repeated resume/pause/disposal events do not duplicate a running interval. Visits shorter than a second produce no session. Sub-minute durations remain visible in seconds.

Sessions are additive fields in existing encrypted book metadata and travel through the existing backup format. Older readingSeconds totals are preserved and shown separately as undated time; no historical sessions are invented. Malformed session rows are skipped while retaining the book. Deleting a book removes its associated sessions as part of the already-confirmed deletion.

From 0.24, a foreground interval is checkpointed every 30 seconds with a stable ID. Later checkpoints and pause/exit update that interval and add only newly elapsed seconds. Unexpected process termination can lose time since the most recent checkpoint; Android scheduling/suspension can extend that window. Independent session removal asks for confirmation and subtracts only the selected recorded duration. Cancellation leaves it untouched.

From 0.25, sessions can appear alongside other records in Threads of a day. They use their recorded local start date and show the original offset; cross-midnight intervals are labelled rather than split into inferred daily totals. Existing thread visibility preferences are preserved; enable Reading in Choose threads. Date filters, goals and broader reading insights remain planned. No external service, permissions or dependency changes were introduced. Checkpoint, removal and daily-thread behavior validated by the 0.25 retry; see MILESTONE-0.25.md.
