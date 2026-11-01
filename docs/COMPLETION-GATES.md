> **Owner decision — 1 November 2026:** Spotify integration is removed from Lilyly in 0.28. Earlier Spotify plans/setup instructions below are historical and no longer required. Account linking, service lookups, attachment creation and room soundtrack controls are removed. Existing page music notes and backup fields are retained to avoid losing personal content. Other music ideas are not implemented by this removal.

# Lilyly completion gates

This is a delivery checklist, not a reduction of PRODUCT-ROADMAP.md. A milestone APK verifies only its stated scope. The full dream application is not complete at 0.27.

## Current gaps to close

| Area | Existing foundation | Work still needed before calling the dream complete |
| --- | --- | --- |
| Cycle | Wheel, calendar, historical logs, configurable estimates, phase comparisons | Full pregnancy/TTC experiences, temperature workflows, historical contraception context, deeper cycle history and uncertainty handling |
| Sanctuary | Check-ins, separate diagnosed/exploring profiles, support plan, therapy notes and connected goals | Richer practice management, appointment reminders, deeper longitudinal review and reviewed educational content |
| Medication | Scheduled/PRN records, reminders, manual refill tracking | Complex schedules, inventory depth, medication correlations and physical-device reminder reliability |
| Creative journal | Notebooks, covers, templates, movable elements, photos, ink, context/music cards and reading-copy PDFs | Richer decorations and composition tools, large-canvas performance, exact canvas export and further handwriting/stylus polish |
| Tarot and witchcraft | Visual deck, personal spreads, reading history, journal links, ritual records | Richer original/licensed card art, freeform/larger spreads, expanded reviewed references and personal correspondences |
| Reading Nook | Themed shelves, EPUB/PDF import, saved passages, sessions and daily threads | Richer EPUB rendering, exact inline highlights, larger-library/performance checks and reading insights |
| Moon | Approximate phase context across spaces and hemisphere-aware seasonal dates | Astronomical accuracy review, timezone edge cases, upcoming phases and fuller lunar calendar |
| Sleep and insights | Persistent sleep/dream entries, recurring tags, opt-in rest and cycle-phase comparisons | Broader symptom/medication/pain/energy analysis, coverage controls and expanded inspectable evidence |
| Music | Spotify PKCE architecture, saved link cards and room soundtracks | Developer configuration and live connect/deny/refresh/disconnect testing; supported richer metadata/discovery/playback where feasible |
| Inspiration | Local image imports, credit/source links and scrapbook insertion | Android share intake, deeper boards/collections and media management |
| Visual system | Atmospheric themes, room art and progressive disclosure in key spaces | Consistent polish across remaining forms, transitions, curated typography, decoration controls and accessibility |
| Privacy and release | Local encrypted records, lock, screenshot protection, encrypted backup/merge restore | Independent privacy review, migration/restore stress testing, signing continuity, physical Samsung checks and Play release preparation |

## Evidence required for a finished feature

- Its visible controls perform real actions, and empty/error/offline states are designed.
- Important records survive reopening; existing records and independent journal copies remain intact.
- Relevant cross-system links work in both directions where the experience calls for them.
- Export, restore and deletion have explicit semantics and meaningful tests.
- Recorded evidence is distinguished from estimates, correlations and traditional beliefs.
- Large text, screen-reader labels, keyboard behavior and narrow screens are checked.
- A deliberate build passes relevant tests and produces an installable APK. Physical-device behavior is verified where emulator checks are insufficient.

## External configuration gate

Spotify remains unverified live until the owner supplies the developer Client ID and an owned HTTPS callback host with Android App Links configured. No client secret should be put in the APK. Follow SPOTIFY-SETUP.md; a successful APK build does not prove authentication works.

## Release gate

Do not describe the application as finished solely because all current automated tests pass. Review every full-roadmap requirement against actual behavior, complete physical-device and accessibility testing, inspect privacy/permissions and backup recovery, and prepare release signing and store materials. Preserve remaining requirements until implemented or explicitly changed by the owner.
