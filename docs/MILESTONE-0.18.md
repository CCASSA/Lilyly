# 0.18 — Reading copies

Validated source: 1764ebe74fe0ac1ce07672ee5fd0e55588dba1f2
Successful device/build run: https://github.com/CCASSA/Lilyly/actions/runs/39115731495

Debug and Garden Preview APKs compiled. All 61 JVM tests and 23 Android journeys passed. New tests cover frozen selection/order, safe filenames, tarot references, PDF pagination/rendering, unavailable-image notices and the export privacy/cancel confirmation. Existing app journeys remain green.

The six-sheet Android-generated test PDF was reopened with PdfRenderer, rendered with Poppler and visually inspected. Text extraction confirmed the final long-text paragraph, second entry, source attribution/URL, missing-image notice and music reference. Review caught an orphaned handwriting heading; the final renderer reserves space for the heading and artwork together. No clipping or overlap appeared in the final sample. Test content is synthetic and is not shipped as user records. Android save-picker/provider variations still need physical-device testing; exact scrapbook layout is explicitly outside this reading-copy format.

The first run passed the new export tests but hit a keyboard/scroll race in the existing ritual journey. A follow-up failed to compile that test because Espresso was not an available dependency. Exact logs were inspected; the final test uses existing AndroidX window-insets APIs to close and await the keyboard before scrolling. The full final suite passed without disabling tests or changing the working build dependencies.

Garden Preview APK SHA-256:
fa8f0b0b5ef2b77f1a2d561bf20532b05511173809c0fd191fe7aa6f9c775353

See READING-COPIES.md for export scope, privacy, temporary files, limits and remaining publishing work. This is a completed milestone, not completion of the entire Lilyly dream app. Ordinary documentation commits do not start another build.
