# Lilyly 0.28 — Spotify removed

The owner explicitly removed Spotify from Lilyly on 1 November 2026. This supersedes the earlier Spotify roadmap and setup requirements; other product goals remain intact.

Removed the Spotify Settings destination, sign-in/token/network implementation, exported callback activity and intent filter, developer build variables, journal attachment controls, saved-music picker, and Sanctuary/Reading Nook soundtrack controls. No Spotify account or callback domain is needed.

Existing journal music fields remain readable as plain saved notes, including captions, previously fetched title/artist and URL. Editing/saving a page preserves those records. Room soundtrack fields remain in local settings/backups for compatibility but have no active controls. Existing PDF exports can retain the old page references. This change does not silently delete personal memories.

Opening the updated app clears only its retired `lilyly_spotify` credential preferences. It does not remotely revoke Spotify account grants or touch the main encrypted personal-data namespace.

Two obsolete OAuth unit tests and the removed soundtrack interaction test are retired. The music journey now verifies existing page memories survive save/reload, active Spotify controls are absent in that page, and stored integration credentials are cleared. Legacy record/parser/backup coverage remains.

Build source: `776f587b9801d544efc2e3f4a0bb84fd041143f0`.
Deliberate build: https://github.com/CCASSA/Lilyly/actions/runs/41301413988

APK: `Lilyly-Preview-0.28.0.apk`, 18,445,401 bytes.
SHA-256: `c6496b5d6d9eed1cd536727e58dc3310a0bce6243e5a47418a7a8758cc979d19`.
Garden Preview remains a separate package from the original Lilyly app and retains its milestone signing continuity.

Validation: 95 JVM tests and 29 Android tests passed. Sanctuary screenshot reviewed: soundtrack control absent and remaining goal content readable. Physical Samsung testing remains for the owner.
