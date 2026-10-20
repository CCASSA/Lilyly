# Library shelves — 0.20

The existing Reading Nook now supports one named shelf per book, an Unfiled view, reading-stage filters (Unopened, Reading, Finished), and ordering by recent reading, import date, title or author. Search includes title, author and shelf. Filters combine with favorites and offer a clear reset.

Reader Tools opens Book details & shelf: edit title/author, choose or type a shelf, and mark a book finished or undo that mark without resetting its position. Shelf names are limited to 60 characters. Existing journal copies retain their original source attribution; book notes, bookmarks, time and imported files retain their identities.

The additive collection JSON field defaults to empty for existing books. The existing encrypted book metadata and backup path carry it without a destructive migration or new permissions. Filters and ordering survive screen recreation; book details and shelf assignments survive app restarts and backup. Empty standalone shelves, multi-shelf membership, bulk organization, richer reader layout, inline EPUB highlighting and individual reading-session history remain planned.

Validation pending deliberate GitHub Actions run. The local execution workspace was unavailable; edits were prepared through the repository connector. Automated build and emulator results will be recorded explicitly; no local visual review is claimed.
