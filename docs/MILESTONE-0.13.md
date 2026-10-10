# 0.13 — Music pages

Built from c54f60bb19fc031697f0c65379c635658ee96e8b.
GitHub Actions run: https://github.com/CCASSA/Lilyly/actions/runs/38083695707

- Debug and Garden Preview APKs compiled successfully.
- 41 JVM tests passed, zero failures/skips.
- 16 Android emulator journeys passed, including adding a Spotify link, saving a journal page, reopening it from persistent storage, and removing its attachment.
- Inspected the music-page emulator screenshot: title, personal caption, Open in Spotify and Remove controls are readable and fit the page.
- Garden Preview APK SHA-256: e2ac99fb4567fba865f6b82ed9fa652df0d5f7d08b13b0e3225fc389719a1f60.

This is the first Spotify milestone, not completed playback integration. Full links and captions work now. Secure PKCE login, token exchange/refresh, explicit metadata retrieval and disconnect are implemented, but live account authentication and catalog calls remain unverified until developer configuration is provided. See SPOTIFY-SETUP.md. No Client ID or callback domain was invented; Connect remains disabled in this unconfigured APK. Automated tests use a synthetic music ID and do not claim that it resolves to a real track.

Future music work includes live authentication verification, API artwork with branding compliance, account-library/search features as permitted, soundtracks in Reading Nook/Sanctuary, and supported playback controls. These remain on the product roadmap.

Ordinary source/documentation commits do not trigger this milestone workflow. The final run was requested deliberately after adding the device journey; the earlier successful compilation run was superseded by this final verification run.
