# Remembered words — 0.21

Reading Nook now opens a global Saved passages & notes space. Search includes quote text, thoughts, source title, author and shelf. Everything/Passages/Notes filters combine with search; records with both a quote and thoughts appear in both relevant filters. Newest records appear first.

Return to book opens the recorded section/page using the existing reader and saved position. Returning to the shelves from that reader brings the user back to the passage collection. Send to journal uses the existing independent commonplace-page copy and reopens it on subsequent taps.

Edit saved words persists changes to the existing note identity; Cancel asks before discarding edits. Remove requires confirmation and deletes only that saved passage/note, keeping the book, bookmarks and journal copies. No new data schema, permissions or network calls are needed. Missing source books retain searchable writing, with unavailable source navigation disabled.

Verified in GitHub Actions run 39675440969: 75 JVM tests and 26 Android 35 emulator journeys passed. The new journey checks source-location callbacks, saved edits after reload, journal-copy independence, cancel/confirm removal, and retained book position/bookmarks. The actual reader uses its existing tested opening flow; the new journey does not render a source book. No physical Samsung or new-screen screenshot review is claimed. This milestone does not add exact inline EPUB highlight locations, PDF text selection, annotations on the PDF itself, or reading-session history; these remain planned.
