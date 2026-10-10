# Spotify music pages (0.13)

This milestone supports persistent song/playlist attachments on journal, grimoire and ritual pages. Paste a Spotify Share link, optionally give it a personal caption, then save the page. Opening a card launches Spotify or a browser. No audio is downloaded or streamed by Lilyly.

Account linking uses authorization code + PKCE (S256), random state, a ten-minute pending request, encrypted credentials, token refresh and local disconnect. No client secret belongs in the app. Tokens are stored in a separate encrypted preferences namespace and excluded from Lilyly exports. Only explicit music lookups contact the Spotify catalog; journal text and health data are not transmitted.

## Configuration required before live login

1. Create a Spotify developer application and obtain its **Client ID**, not its secret. Check Spotify's current development-mode account eligibility and user access requirements in the developer dashboard.
2. Provide an HTTPS domain you control. Register these exact redirect URIs with Spotify, replacing HOST:
   - `https://HOST/spotify/callback` (original app)
   - `https://HOST/spotify/preview/callback` (Garden Preview)
3. Serve `https://HOST/.well-known/assetlinks.json` without redirects. Declare `delegate_permission/common.handle_all_urls` for each Android package: `com.lilyly.app` and `com.lilyly.app.preview`, with the SHA-256 signing certificate fingerprints for the builds installed on the phone. Obtain fingerprints using `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android` for the milestone signing key. Production signing will require its own fingerprints. Never share the keystore/private key.
4. Configure GitHub Actions repository variables `SPOTIFY_CLIENT_ID` and `SPOTIFY_REDIRECT_HOST` (host only, no scheme/path). For local Gradle builds use `-PspotifyClientId=... -PspotifyRedirectHost=...`. Trigger the manual milestone workflow after configuration.
5. Verify Android App Links on the actual installed build before testing sign-in (`adb shell pm get-app-links com.lilyly.app.preview`). Callback fallback pages should contain no analytics or third-party scripts and must not retain authorization query strings in server logs.
6. Test connect, deny, expired callback, refresh, disconnect, and track/playlist lookups with an eligible Spotify account. A configured build alone does not prove live authentication works.

Without configuration, Connect is disabled with an explanation; link attachments remain functional. Current scope is public item metadata, not account-library access, private playlists, playback controls, background soundtracks, or automatic music recommendations. No additional scopes are requested. A saved personal caption is never presented as a Spotify-supplied title. Album artwork is not fetched in this milestone.

Official references:
- https://developer.spotify.com/documentation/web-api/tutorials/code-pkce-flow
- https://developer.spotify.com/documentation/web-api/concepts/redirect_uri
- https://developer.spotify.com/documentation/web-api/concepts/quota-modes
