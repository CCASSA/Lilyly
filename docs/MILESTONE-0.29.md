# Lilyly 0.29 — Gathered sparks

Android Share now accepts plain text, links, a single image or a batch of up to eight images. The receiving screen is behind the existing app lock and screenshot protection. Nothing enters the inspiration collection until Keep inspiration is tapped. The review can add a title, source credit and collection. Text remains intact; the first valid HTTPS link is suggested as an editable source without fetching it.

Image shares require content URIs. Local file paths, remote image URLs, unsupported MIME types, empty and oversized payloads are rejected. The existing bounded importer checks actual image bytes, normalizes orientation and dimensions and strips metadata. Each image becomes its own card with attribution; all cards commit together. Failed batches remove their temporary copies. Original shared files are never deleted. Saved cards use the existing encrypted journal records and image-backup path, and can be inserted into scrapbook pages with credits intact.

Review fields survive activity recreation. A retained import controller finishes a confirmed transaction across rotation without starting a duplicate job. A share not yet kept is not an inbox record: dismissal, task removal or unavailable sender grants may require sharing again. No Pinterest account/API integration, web scraping, automatic preview download or new media permission is introduced. Live behavior of third-party senders still requires physical-device checking.

The receiving transaction merges against persisted journal records, preserving newer records saved by another activity instance. Returning to the original app activity refreshes journal records.

Spotify remains removed by owner request. Other full-roadmap work remains intended.

Verified on [run 42531297502](https://github.com/CCASSA/Lilyly/actions/runs/42531297502), source `748ac280e6b09a222dfd8b8e896b0cca61345f9f`: 101 JVM tests and 34 Android emulator tests passed. The test harness uses Android MediaStore for temporary image fixtures, including a malformed input; production needs no new provider or permission. Physical Samsung sharing and existing preview signing compatibility are separate checks.
