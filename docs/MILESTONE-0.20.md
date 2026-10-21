# Lilyly 0.20 — Library shelves

Completed named shelf assignments, combined reading-stage/favorite/search filters, title/author/recent sorting, editable book metadata, reversible completion status and dialog-local keyboard Done behavior. Existing books default to Unfiled; reading positions, bookmarks, notes and imported files retain their identities. The full dream roadmap remains intact.

Verified source: af1ab7c3d842e12e31a1c3d40637b3c29c9d3c87.
Successful run: https://github.com/CCASSA/Lilyly/actions/runs/39457772132
72 JVM tests and 25 Android 35 emulator journeys passed. Debug and Garden Preview APKs built successfully. Shelf screenshot reviewed. No physical Samsung validation is claimed.

Earlier deliberate attempts caught a PDF test-fixture Closeable mismatch and a dialog keyboard-dismissal timeout. Fixed the fixture using finally/close, added a dialog-local IME Done action and exercised that action in the Android test; the complete suite then passed.

Preview APK SHA-256: 2beb84997c65fdcf2edd40030416847bf095766dc85673b265877d9f41b4c22b

Use Reading Nook → open a book → Tools → Book details & shelf to organize it; use Browse shelves on the library screen to filter and sort. Each book currently belongs to one named shelf. Empty standalone shelves, bulk organization, richer EPUB layout and reading-session history remain planned. See LIBRARY-SHELVES.md.
