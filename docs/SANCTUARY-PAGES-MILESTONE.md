# Sanctuary & Pages 0.3

This is a tested-progress milestone of the existing app, not the finished product.

## Implementation
- Sanctuary quick feeling chips, optional deeper numeric check-ins, structured diagnosed/exploring profile fields, and a gentler support plan with opt-in difficult-moment reflections/history.
- Older free-text mental profile is retained in the context/previous notes field. Older check-ins keep their ratings; new quick check-ins do not display invented numeric observations.
- Journal scrapbook canvas with typed blocks, imported photographs, botanical/celestial text decorations, normalized movement, width and rotation controls, four paper choices, and current moon/cycle/feeling context stamps.
- Existing body text, image URI and ink are preserved. The original writing and drawing editors remain accessible. Page search, favorites and notebook-name filtering now work locally.
- Page fields added: canvasJson, paper, favorite, notebook. Mental fields added: feelings, detailedRatings. No encryption keys, existing fields or record identities are changed.
- A separate preview build type remains available because the original installed APK's signing key is not available. Preview records are separate. Do not uninstall the original app for this preview.

## Limits to address next
The journal remains a fixed-height single canvas; rich text pagination, true layered ink, multi-page notebooks/covers, undo/redo, richer illustrated assets and reusable templates need further implementation. Selection controls are functional but not final art direction. Custom photographs depend on retained document-provider access. Sanctuary medication schedules/reminders/refills and deeper therapy workflows remain unfinished. Reading Nook and Tarot have not received their planned full redesign. Spotify/Pinterest integrations are not implemented. Export/restore, biometric lock, comprehensive sleep tracking and cross-system statistical insights remain outstanding. The full product scope is preserved in PRODUCT-ROADMAP.md.

## Validation
Unit tests cover cycle boundaries and legacy/new JSON round trips. Android emulator journeys cover symptom persistence and Sanctuary-to-scrapbook persistence. Build reports, exact run link and screenshot review are recorded after the build completes. Physical Samsung and large-text testing remain necessary.
