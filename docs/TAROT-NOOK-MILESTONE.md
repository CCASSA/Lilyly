# Tarot & Reading Nook — 0.4

Continuation of the existing app; no replacement project or destructive migration.

## Implemented
- A complete 78-card visual deck, original botanical geometric card faces, search/suit/favorite filters, upright/reversed reflections, suit correspondences, personal notes and previous appearances.
- Random draws without replacement, optional reversals, one-card and two three-card reflective spreads, tap-to-reveal, kept readings and linked grimoire pages with movable visual cards. Existing free-text readings remain readable.
- Real document-picker EPUB/PDF import into app-private storage, cover extraction or intentional typography covers, four persistent shelf atmospheres, search and favorites.
- EPUB package/container/spine parsing, text reading sections, chapter navigation, text search and size adjustment. PDF page rendering and pinch zoom. Persistent reading position, bookmarks, foreground reading time and completion state.
- Passages and reading notes, with source/position attribution and idempotent links into the journal/commonplace book.
- Resource and archive size limits, no network resource fetching or book scripts, rejection of encrypted EPUBs, import failure cleanup, deletion confirmation, preservation of original selected documents.

## Honest limits
The original generated card faces use symbols and botanical geometry, not full scenic illustrations. Minor-card meanings are original rank/suit reflections, not an exhaustive historical tarot encyclopedia. Physical-deck manual visual selection and larger/custom spreads remain future work.

EPUB is a text-focused reader, not full EPUB conformance: fixed layout, embedded illustrations, original CSS, footnote navigation, audio and DRM are unsupported. A declared encryption.xml is rejected even if only fonts are obfuscated. PDF search/text selection is not implemented; PDF notes can be entered manually. EPUB passages are kept as annotations, not colored inline text highlights. Reader sections are capped at 6,000 characters; progress is a section/page location, not a claimed precise percentage of words read.

Imported books and cached text/cover files are app-private files, not separately encrypted. Sensitive reading notes and library metadata use the existing encrypted store. Neither is sent to an external service. Backups and a separate app lock are still outstanding. Reading sessions currently accumulate foreground time, without a session-by-session history.

## Validation
Unit coverage checks 78 unique cards, no-replacement draws, reversals and JSON migration, book/note persistence, spine order and rejection of encrypted/escaping/remote book resources. Emulator journeys import generated local EPUB/PDF fixtures, render PDF pages, reopen stored progress and annotations, and link both reading and tarot material into the journal. Build/test results are recorded after deliberate Actions execution.

API references: https://developer.android.com/reference/android/graphics/pdf/PdfRenderer and https://www.w3.org/TR/epub-33/ .

## Night Garden continuation
Persistent editable sleep/dream records, local bedtime/wake time, recorded time in bed, rest quality, waking energy, interruptions, dream mood/people/places/symbols, lucidity/nightmare flags, historical search and linked dream journal pages. Existing freeform dream pages remain accessible. Cycle daily summaries include rest records, and night history shows same-date Sanctuary feelings/cycle context.

Patterns require minimum sample sizes, keep missing numerical ratings out, and require opt-in before combining Sanctuary anxiety with sleep. Recurring symbols are literal user-tag frequencies, not dream interpretation or diagnosis. Manual local times do not infer actual time asleep, timezone changes or DST. The remainder of the full insight roadmap remains open.

During validation on 2026-10-05, GitHub reported an Actions runner-assignment incident. Deliberate newer milestone builds now cancel obsolete queued/running milestone builds in the same concurrency group; ordinary source commits still never trigger builds.
