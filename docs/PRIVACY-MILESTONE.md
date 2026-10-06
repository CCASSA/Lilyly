# Private world — 0.5

## Implemented
- Optional Android-native biometric app access on API 28+, with the phone's PIN/pattern/password as fallback. API 26–27 uses the screen lock. Enabling or disabling requires device authentication. No custom password database and no biometric data collection.
- Locked cold starts and background returns display a separate protected screen. Secure-window flags hide screenshots and Android recent-app previews when app lock is enabled, with an independent screenshot setting when it is disabled.
- Password-encrypted backups with a versioned AES-256-GCM authenticated envelope, random salt/nonce and PBKDF2-HMAC-SHA256 (600,000 iterations). Passwords are not persisted. Minimum 12 characters. User-selected document destinations may be local or a user-selected cloud provider; Lilyly does not upload automatically.
- Records, imported books, covers and journal/tarot photos included. Backup fails rather than claiming a complete export when a linked local photo or book is inaccessible.
- Restore first authenticates the entire encrypted file, validates the archive and record data, then asks for explicit merge confirmation. Existing IDs and cycle dates keep their current version. No current records are deleted. App-lock settings are never imported. New media is copied into app-private storage.
- Archive paths, expansion size, schema and source references are checked. Authentication failures never apply records. Staging files are removed after success/failure/cancellation.

## Limits and operation
App lock is a UI access barrier using Android authentication; it does not bind every database decryption to a biometric cryptographic operation. It is not protection against a rooted or compromised OS. Records retain the existing Android Keystore encryption. Imported books and restored media use app-private files rather than separate at-rest encryption.

Lock closes dialogs. Journal/cycle and newer screens retain saveable drafts, but older forms using transient state may reset; the UI says to save before leaving. Hardware biometric success, device-PIN fallback and external document-picker interactions need Samsung validation. Password recovery is intentionally unavailable. Large backups are capped at 750 MB of expanded content / 1 GB encrypted input. Interrupted exports may leave an unusable encrypted destination file; the app reports failure. Restore merges, rather than rolling the entire app back to an earlier point in time.

## Verification gates
Unit tests cover encryption round-trip, unique random envelopes, wrong passwords, tampering/truncation, record collision policy and lock exclusion. Android tests restore a photo into an isolated store after removing its original file, retain current records, and verify protected cold-start UI and secure-window flags. Full CI outcomes are recorded after the deliberate build.
