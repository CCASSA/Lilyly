# Music throughout Lilyly — 0.14

## Reading Nook

Use **Add a soundtrack** above the bookshelf. While reading, open **Tools** to reach the same soundtrack. Paste a full Spotify song/playlist link or use **Choose saved music** to reuse music already attached to a journal page or another space. Tap **Open in Spotify** to listen in the Spotify app or browser, then return to Lilyly to read.

## Sanctuary

The soundtrack button beneath the Sanctuary tabs opens a separate collection. Adding/removing music and changing a caption saves immediately. These changes never modify the source journal page or Reading Nook collection.

## Journal and grimoire

The existing music panel now offers **Choose saved music** and **Edit caption**. Journal changes remain part of the page draft until the user saves the page. Each attachment is an independent copy; removing a room card does not remove it from a journal page.

The local picker searches saved titles, creators, and personal captions. It excludes reusable templates and hides tracks already attached to the destination. Canonical Spotify URLs prevent duplicate share links. Each destination holds up to 12 music cards.

## Storage and privacy

Room collections are an additive `settings.soundtracks` JSON object. Old settings load with no room collections. Settings already participate in encrypted local storage and encrypted backup/restore. Restore keeps existing destination choices on a conflict and imports room collections absent from the current settings. Authentication tokens remain in their separate, unexported preferences namespace.

No music auto-plays. This milestone does not add Spotify streaming, background playback control, search of Spotify's online catalog, or new network scopes. Live login/metadata verification still requires the configuration described in SPOTIFY-SETUP.md.
