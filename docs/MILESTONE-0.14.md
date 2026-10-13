# 0.14 — Room soundtracks

Source: d7a54be86a7e22d258ec1845a87881191a652046
Successful build: https://github.com/CCASSA/Lilyly/actions/runs/38357501976

Both Debug and Garden Preview APKs compiled. All 44 JVM tests and 17 Android emulator journeys passed. The new journey reuses a journal playlist in a room, edits its caption, checks persistent storage with a fresh AppStore, confirms the original page remains unchanged, and removes the room attachment. Pure tests cover canonical URL deduplication, old/malformed settings, and non-destructive backup merging. The soundtrack screenshot was inspected for readable text and accessible controls.

Garden Preview APK SHA-256:
8cbfb2bf93904e0956d8cc3a957348ab6b0e6cf76d9a3320f2e7c2e547a73c0d

See MUSIC-SPACES.md for user flows and data behavior. Spotify account linking is still unconfigured and has not been tested against a live account. These room collections open links externally; they do not provide in-app playback or imply live Spotify authentication. The synthetic playlist used in tests is not represented as an actual music recommendation.

This build was requested deliberately through the build marker. This documentation commit does not request another build.
