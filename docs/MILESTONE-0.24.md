# Lilyly 0.24 — Reading care (awaiting validation)

Reading sessions now save a checkpoint every 30 seconds while the reader is open. Each running interval retains one ID; updates add only newly elapsed time. Pause/exit saves the remainder. Repeated or stale checkpoints do not double-count totals, and background time remains excluded. If the process ends unexpectedly, already-saved checkpoints survive; time after the last checkpoint can still be lost. The scheduler is not a guarantee of exactly 30-second recovery under OS suspension.

History now offers confirmed removal of an individual session and its recorded duration. Cancellation changes nothing. Books, saved positions, bookmarks, notes, journal copies and earlier undated totals remain. Existing backups still carry the additive session field; no migration or new permission is required.

Four additional unit tests cover checkpoint updates/reload, stale/conflicting data, removal preservation and overflow. Android journeys now require a persisted checkpoint while the actual reader remains open, then verify one final interval; history checks cancellation, deletion and reload. Full build and screenshot review pending.

The full dream roadmap remains intended work. Date filters, richer reading insights and remaining product depth are not declared complete.
