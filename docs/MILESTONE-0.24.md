# Lilyly 0.24 — Reading care

Reading sessions now save a checkpoint every 30 seconds while the reader is open. Each running interval retains one ID; updates add only newly elapsed time. Pause/exit saves the remainder. Repeated or stale checkpoints do not double-count totals, and background time remains excluded. If the process ends unexpectedly, already-saved checkpoints survive; time after the last checkpoint can still be lost. The scheduler is not a guarantee of exactly 30-second recovery under OS suspension.

History now offers confirmed removal of an individual session and its recorded duration. Cancellation changes nothing. Books, saved positions, bookmarks, notes, journal copies and earlier undated totals remain. Existing backups still carry the additive session field; no migration or new permission is required.

Four additional unit tests cover checkpoint updates/reload, stale/conflicting data, removal preservation and overflow. Android journeys now require a persisted checkpoint while the actual reader remains open, then verify one final interval; history checks cancellation, deletion and reload. Compilation, unit tests and all Android journeys passed in deliberate run https://github.com/CCASSA/Lilyly/actions/runs/40691686952 at source 5da4050f54ac4ba41caacd50c6b0a1ef2619f623. The combined 0.25 milestone will be delivered after its own validation and screenshot review.

The full dream roadmap remains intended work. Date filters, richer reading insights and remaining product depth are not declared complete.
