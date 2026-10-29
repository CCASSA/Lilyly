# Lilyly 0.26 — Personal tarot spreads

Tarot now supports creating, naming, editing, selecting and confirming removal of personal spreads with one to six named positions. Six-card draws use rows of three with numbered labels and individual reveal/detail interactions. The three existing spreads remain available. All cards must be revealed before saving.

Templates are stored under stable IDs in encrypted settings and use the existing backup merge format. Older settings load without custom templates; malformed rows cannot replace built-ins. Existing local template fields win merge conflicts; independent templates are retained. As with existing merge restores, an older backup can reintroduce a removed template. Readings snapshot their spread name, cards and position wording, so editing/removing a template never edits prior readings or independent grimoire pages.

Editor validation supports one to six nonblank positions, name up to 80 characters and each position up to 120. Unsaved changes require deliberate discard. Position order follows editor line order. Arbitrary drag placement, larger spreads and richer card artwork remain planned; this is not completion of the full tarot or product roadmap.

Five JVM tests cover persistence/legacy settings, malformed input, unique six-card draws, backup merging and historical labels. A new Android journey creates and edits a template, reveals six cards, saves a reading, creates its journal copy and verifies confirmed template removal preserves both. Verified source: afa50bdb83a2b965442c6f13a30e9c73f4e5e121.
Successful deliberate build: https://github.com/CCASSA/Lilyly/actions/runs/40897683530
91 JVM tests passed with zero failures; Android instrumentation reports OK (29 tests). Reviewed personal-spread.png: six revealed cards in two rows with readable numbered positions, names and reversal indicators. Samsung/large-font device testing remains with the user.

Delivered Lilyly-Preview-0.26.0.apk: 18,445,681 bytes.
SHA-256: 6bd6dfa27a594ef11ff5a1ccc785c0dee202719295f017b79879f0ef97069f6b.
Application ID com.lilyly.app.preview, version code 26, preserved preview signing key. Updates Garden Preview alongside the original app.

To try it: Tarot → Draw → My spreads → Create a spread. Name it, enter one position per line, save, then Use the spread and Shuffle & lay the cards. This is a verified development milestone; the full dream app remains in progress.
