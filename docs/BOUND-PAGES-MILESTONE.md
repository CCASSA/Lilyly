# Bound Pages — 0.9

Existing journal records, free-text notebook names, handwriting, photographs and canvas elements are preserved. Named notebooks now have persistent Botanical/Celestial/Wine/Parchment covers, including empty notebooks. Legacy notebook names automatically appear on the shelf. Cover metadata lives in existing encrypted settings and backup merging.

Four original starter templates and user-kept templates create independent pages with fresh page/element IDs. Personal templates live in a dedicated Templates section in the existing encrypted journal collection, so the existing attachment-aware backup includes their photographs and handwriting. Home's recent page and Cycle daily page count exclude templates. Templates are editable through the picker; deletion uses the existing editor's confirmed deletion flow. New pages inherit the selected notebook.

Scrapbook additions: bounded undo/redo for canvas changes during the editing session; duplicate elements; bring-to-front/send-to-back; resizing keeps the horizontal origin within page bounds. Undo history itself is transient, while saved page content persists. Drag history currently records movement increments rather than grouped gestures.

Limits: notebook identity preserves the existing section/name relationship; renaming/moving whole notebooks and custom photograph covers remain future work. Themed covers use original vector ornaments, not rich raster cover art. Templates deliberately retain their photos/writing when copied; they are private personal templates, not a sharing service. Photo references remain the app's existing document-permission model. Full multi-page canvas, rich text, gesture grouping and deeper drawing tools remain on the full roadmap.

## Verified build

GitHub Actions run [37616050718](https://github.com/CCASSA/Lilyly/actions/runs/37616050718) succeeded for source commit `39a81199175efb1ec91a49c5e0ce814690b9a6d5`. Downloaded reports confirm 32 JVM tests and 12 Android journeys passed. The notebook journey creates a notebook, verifies encrypted cover persistence, creates a page from a template, and checks independently saved personal template data. The captured notebook shelf was visually reviewed. Physical Samsung testing remains with the user.

Download artifact: `Lilyly-Garden-Preview-safe-side-by-side`. Preview APK SHA-256: `eaa52949f79adeb2ef9b406278ef6013b37a7557c3b5c4afc418fece5d5ad234`. This is the separate preview application ID and does not replace the original Lilyly installation.
