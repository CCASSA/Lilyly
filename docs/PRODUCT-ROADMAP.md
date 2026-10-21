# Lilyly — full product direction

Lilyly is an Android-first private magical world: grimoire, journal, scrapbook, cycle tracker, mental-health sanctuary, wellness companion, reading space and personal pattern system. This is a continuation of CCASSA/Lilyly, not a replacement app or reduced MVP. Major systems must connect through the user's own daily context. Complexity underneath; simplicity on the surface.

## Visual and interaction direction
Dark botanical, celestial, feminine, tactile, intimate, sophisticated. Ink, plum, wine, ivory, antique gold, sage; original botanical and celestial illustrations, paper, candlelight, restrained movement and readable expressive type. Avoid clinical forms, repeated rectangular cards and Halloween kitsch. Stardust references inform the wheel, calendar and quick categorized selection interactions only. Do not reproduce its artwork, branding or exact layouts.

## Current milestone: Cycle Garden 0.2
- Normalized Android source derived from the successful bundled build and its exact compatibility patches. Legacy bundles/workflows retained as recovery paths.
- Botanical ring, countdown, calendar with historical editing, daily bottom sheet, selected symptom tiles, optional numeric detail, custom tags and persistent settings.
- Recorded-data trend graph and date-linked summaries of cycle, journal, Sanctuary and medication records.
- Pregnancy mode pauses predictions; TTC mode uses estimated fertility; hormonal/irregular context suppresses fertility markers. These are foundations, not complete pregnancy or fertility products.
- Same application ID, encrypted preferences and existing JSON fields. Additive fields have safe defaults. No deletion or destructive migration.

## Sanctuary & Pages 0.3 continuation
Sanctuary now has quick feeling selections, optional detailed ratings, separate diagnosed/exploring fields and opt-in support reflections. Journal pages now have persistent movable text/photo/decorative elements, sizing/rotation, paper styles, context stamps, favorites and notebook-name filtering. This is a foundation for the full canvas, not completion of the journal roadmap. See SANCTUARY-PAGES-MILESTONE.md for concrete limits and validation.

## Full intended roadmap — none of these are discarded
1. **Foundation and Home:** cohesive reusable design system, original art/textures, atmospheric customizable daily home, greeting/date/moon/cycle/check-in/prompt/medication/continue-reading/insights/season/ritual/favorites, navigation, transitions, accessibility and performance.
2. **Cycle:** comprehensive bleeding/body/mood/energy/focus/cravings/mucus/gut/sex/activity/disruptor/contraception/test/medication/supplement/endometriosis/custom/notes tracking; cycle history, confirmed starts and ends, irregular cycles and uncertain estimates, PMS, fertility/ovulation awareness, pregnancy and TTC experiences, temperature, calendar and lunar overlays, configurable assumptions and cross-system graphs. No medical certainty or contraception claims.
3. **Sanctuary:** gentle expressive quick check-ins with optional detail; mood/anxiety/energy/irritability/overwhelm/sensory overload/dissociation/emptiness/intensity/urges/stress/focus/social battery; diagnosed conditions separate from exploring conditions; evidence-based education without diagnosis. Medication schedules/reminders/PRN/taken/late/skipped/refills/adherence/notes/correlations without dose recommendations. Therapy notes/topics/goals/homework/insights/questions/progress. Humane private support plan and difficult-moment reflection.
4. **Journal, scrapbook and grimoire canvas:** notebooks/grimoires/covers, paper backgrounds, typed movable blocks, handwriting/drawing/highlighting, photos with move/resize/rotate, stickers/washi/pressed flowers/herbs/botanical/celestial decorations, quotes/cards/moon stamps/spells/ritual elements/links/songs/playlists, cycle and mood context, reusable templates, search/tags/favorites/collections/recent/linked pages.
5. **Tarot:** visual deck browsing/search, upright/reversed meanings, symbolism/correspondences, notes/favorites, visual spreads/placement/shuffle/draw, history/recurring cards, journal a reading and linked cards. Reflective/divinatory practice, never guaranteed factual prediction. Licensed or original art.
6. **Witchcraft:** searchable herbs/crystals/moons/candles/colors/planets/elements/correspondences, ritual planning/records, spells/personal spellbook, sigils/altar notes/seasonal calendar/personal correspondences, links to pages. Distinguish historical traditions, modern beliefs and scientific evidence.
7. **Reading Nook:** meaningful Cottage/Gothic/Celestial/Botanical room themes, actual cover books on shelves, EPUB/PDF imports, organization/progress/bookmarks/highlights/notes/quotes/search/favorites/sessions, soundtrack and quotes sent to journal pages.
8. **Moon:** current/upcoming phases, lunar calendar, prompts and context in daily views/journal/tarot/ritual/cycle comparison; correct date/timezone, location and hemisphere handling. Current approximate phase helper needs astronomical accuracy review.
9. **Sleep and Dreams:** bedtime/waking/duration/quality/interruptions/nightmares/energy/trends; magical dream entries/tags/symbols/themes/lucidity/mood/people/places/search/recurrence, linked to sleep/cycle/mood/medication/journal.
10. **Longitudinal insights:** individual correlations across anxiety/cycle, sleep/period, symptoms/sleep, mood/phase, medication adherence, dream recurrence, pain and energy; minimum sample/coverage controls, explicit uncertainty and user control over contributing data. No causation or diagnosis claims.
11. **Music and inspiration:** real Spotify supported API/auth integration, configuration supplied externally; song and playlist cards, ritual/focus/sleep/reading/Sanctuary soundtrack and permitted playing context. Add Inspiration via supported imports/share/API paths with permissions/copyright respected. No fake integration controls.
12. **Themes:** Dark Botanical/Celestial/Gothic/Cottage Witch/Antique Grimoire/Botanical, accents/backgrounds/page styles/covers/nook rooms/decorative intensity/curated readable fonts.
13. **Privacy:** local-first, app lock/biometrics, permission controls, export/backup/restore/deletion, resilient storage and migrations. Never send intimate records to integrations unnecessarily.
14. **Product readiness:** maintainable persistent architecture, relevant tests, robust Android builds, signing continuity, device/large-text/accessibility testing, Google Play readiness, polish and useful downloadable debug milestones.

## Engineering rules
Work in coherent vertical slices and preserve functioning features. Inspect exact failed build logs, repair actual errors, deliberately retry. Ordinary source commits must not build automatically. Build through workflow_dispatch or an explicit build-request file commit. Do not expose inert controls as working features. Report implemented, verified, unverified and remaining work separately. Never describe background progress unless work is actually executing.

## Tarot & Nook 0.4 continuation
Visual 78-card deck, saved spreads, personal card notes/favorites, recurring appearances and linked grimoire cards; real EPUB/PDF import, themed shelves, reading progress/bookmarks/notes/search and commonplace-page links. See TAROT-NOOK-MILESTONE.md for supported formats, validation and remaining depth. The full product direction above remains intact.

Night Garden adds persistent sleep/dream records, symbol recurrence, minimum-sample rest summaries and opt-in Sanctuary comparisons, with journal and cycle context links. Full statistical insight controls and deeper sleep analysis remain planned.

## Private World 0.5 continuation
Optional native biometric/device-lock access, screenshot protection, authenticated password-encrypted backup of records/books/photos and a merge restore flow. Details and limits: PRIVACY-MILESTONE.md. This does not complete the remaining product roadmap.

## Daily World 0.6 continuation
Atmospheric connected Home, direct saved-book/page links, daily record summaries and persistent Home personalization. See DAILY-WORLD-MILESTONE.md. All remaining roadmap items above remain intended work.

## Apothecary 0.7 continuation
Editable multiple daily medication times, PRN use, corrected scheduled logs, archive/history, manual refill counts and private local Android reminders. Complex schedules, automated inventory and longitudinal medication correlations remain planned. See APOTHECARY-MILESTONE.md.

## Therapy room 0.8 continuation
Searchable editable sessions with Before/After/Practice sections, questions/goals, practice completion and independent linked journal pages. Cross-session goal management, appointment reminders and deeper pattern review remain planned.

## Bound Pages 0.9 continuation
Persistent notebook covers, empty notebooks, starter/personal templates and canvas undo/redo, duplication and layer ordering. Existing notebook names/pages preserved. See BOUND-PAGES-MILESTONE.md for limits; remaining journal features are retained in the full roadmap.

## Music 0.13–0.14 continuation
Persistent Spotify song/playlist cards, encrypted PKCE credential storage and configurable account linking, saved-music reuse and separate Reading Nook/Sanctuary soundtracks are implemented. Live account authentication/metadata verification still needs developer configuration. In-app playback, richer music discovery and inspiration import remain planned. See SPOTIFY-SETUP.md and MUSIC-SPACES.md.

## Personal insights 0.15 continuation
Threads over time adds opt-in sources, date windows, matched-date descriptive rest/rating comparisons, recurring dream tags and inspectable recorded-day evidence. See PERSONAL-INSIGHTS.md. Remaining phase-based, medication, symptom and broader longitudinal analysis remains intended work. No part of the full product roadmap is removed or considered complete solely because an APK builds.

## Gathered Inspiration 0.16 continuation
Local image imports, source-link/creator-credit cards, collection/favorite/search tools and credited scrapbook insertion are implemented. Existing image backups preserve the new fields. This is a real local inspiration workflow, not Pinterest account integration; share-sheet intake, deeper boards, media management and richer asset collections remain planned. See GATHERED-INSPIRATION.md.

## Ink Studio 0.17 continuation
Pen/highlighter colors and widths, stylus-only input, pressure-aware strokes, whole-stroke erasing, gesture undo/redo, clear confirmation and independent scrapbook ink placement extend the journal. New reference dimensions preserve drawing proportions; legacy handwriting remains readable. See INK-STUDIO.md for remaining depth and hardware limits.

## Reading copies 0.18 continuation
Selected journal entries and filtered notebook collections can be exported as paginated printable PDFs with text, local images, ink, source credits and music references. Scope and unencrypted-output privacy are explicit. This reading layout is separate from exact scrapbook reproduction; richer book layout, exact canvas publishing and direct printing remain planned. See READING-COPIES.md.

## Cycle rhythms 0.19 continuation
Opt-in estimated cycle-phase comparisons now connect recorded Sanctuary mood/anxiety/energy, Cycle pain and Sleep time in bed. Evidence links, distinct-date and completed-cycle thresholds, source controls and conservative exclusions are implemented. Calendar estimates remain uncertain; historical contraception context, measured ovulation, medication/symptom correlations and broader analysis remain planned. See CYCLE-RHYTHMS.md.

## Library shelves 0.20 continuation
Named shelf assignments, reading-stage filters, library sorting, editable imported titles/authors and reversible completion status extend the Reading Nook. Existing book identities, reading positions, bookmarks and notes are preserved. See LIBRARY-SHELVES.md and MILESTONE-0.20.md for verified scope; richer EPUB presentation, individual session history and remaining library depth remain planned.
