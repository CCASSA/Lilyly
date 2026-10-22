# Lilyly 0.23 — Books first

The Reading Nook opening view now prioritizes the illustrated room and actual book covers. Search, favorites, shelves, reading stage and sorting live in Browse shelves. Room & keepsakes opens themes, soundtrack, saved passages and session history. Existing features, stored preferences and books remain intact. No data migrations, dependencies or permissions changed.

The imported EPUB/PDF Android journey now asserts the book is visible without scrolling, chooses a room through the new sheet, then reads, bookmarks and keeps a passage. Its book fixture is isolated and restored. Existing shelf organization and reader/history journeys remain in the full validation suite.

Verified source: 4015dc92ad7d15c0289c47d57a9db91d639edb4a.
Successful deliberate build: https://github.com/CCASSA/Lilyly/actions/runs/39845077587
81 JVM tests passed with zero failures; Android instrumentation reports OK (27 tests). Reviewed bookshelf.png: both imported fixture books are visible on entry, with the celestial room and two tool-sheet entry points above them. Physical Samsung testing remains with the user.

Delivered Garden Preview: Lilyly-Preview-0.23.0.apk, 18,396,525 bytes.
SHA-256: bb76d571ef5445fc48bb9531f82c12d4b5a85df8b5b260278a7d30fd1855340c.
Application ID com.lilyly.app.preview, version code 23. Uses the preserved preview signing key and installs alongside the original app. The run's original-app debug artifact retained an older label; only its label is normalized to Lilyly-debug for future runs. Build outputs and package version are unchanged by that workflow-label correction. This milestone includes the reading-session work from 0.22. Remaining work includes richer scenic assets, reading checkpoints, independent session management and the full product roadmap.
