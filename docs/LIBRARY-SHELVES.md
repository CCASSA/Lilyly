# Library shelves — 0.20

The existing Reading Nook now supports one named shelf per book, an Unfiled view, reading-stage filters (Unopened, Reading, Finished), and ordering by recent reading, import date, title or author. Search includes title, author and shelf. Filters combine with favorites and offer a clear reset.

Reader Tools opens Book details & shelf: edit title/author, choose or type a shelf, and mark a book finished or undo that mark without resetting its position. Shelf names are limited to 60 characters. Existing journal copies retain their original source attribution; book notes, bookmarks, time and imported files retain their identities.

The additive collection JSON field defaults to empty for existing books. The existing encrypted book metadata and backup path carry it without a destructive migration or new permissions. Filters and ordering survive screen recreation; book details and shelf assignments survive app restarts and backup. Empty standalone shelves, multi-shelf membership, bulk organization, richer reader layout, inline EPUB highlighting and individual reading-session history remain planned.

Validated in successful GitHub Actions run 39457772132 at source commit af1ab7c3d842e12e31a1c3d40637b3c29c9d3c87: 72 JVM tests and 25 Android 35 emulator journeys passed. The new journey edits an imported PDF, assigns a shelf, marks completion, filters to that shelf and verifies reloaded bookmarks, reading position, time and notes. The shelf screenshot was reviewed after the local workspace became available again. Physical Samsung testing remains user validation.
