# Gathered Inspiration — 0.16

## Use it

Open **More → Inspiration → Add inspiration**. Choose an image from Android's document picker, keep a source link, write a thought, or combine them. Add a title, creator/source credit, collection, tags and a favorite mark. Search matches those saved fields; collection and favorite filters work independently.

In a journal or grimoire page's **Scrapbook** mode, choose **+ Inspiration** and tap a saved card. Image cards become movable, resizable, rotatable photo elements. Cards without images become text elements. Each element keeps its source URL and creator credit. Select the element to see the credit and **Open source**. Save the page to persist the inserted element. Removing or editing the original inspiration card does not change an element already saved to another page.

## Local media and attribution

An import copies and normalizes a selected image into private app storage. It does not depend on ongoing access to the original document or gallery file. Imports accept supported decodable images up to 25 MB and 200 megapixels, sample the longest side down to at most 2048 pixels, normalize EXIF orientation where supported, and re-encode as PNG without retaining original EXIF metadata. Animated formats are treated as a still image. Imported pictures are app-private files, not individually encrypted image blobs; the existing exported backup encrypts the complete archive.

Source links must be full HTTPS URLs without embedded credentials. Links are not automatically fetched, scraped or previewed. Opening one is an explicit user action in another app/browser. Pinterest pages can be kept as source links; this does not claim Pinterest account linking, API access or permission to copy someone else's work. Use owned or permitted images and keep source credits.

## Storage and existing data

Cards reuse the existing JournalEntry record with section `Inspiration`, so search, record backups and existing photo packaging can preserve them. `sourceUrl` and `sourceCredit` are additive optional fields on journal records and canvas elements; old records load with empty values. Canvas image references are included by the existing backup media mapper. Backup/restore preserves source fields alongside both legacy and canvas photos.

Normal card deletion removes its record. The import helper removes an unreferenced file from its own `inspiration-media` directory, but preserves files still used by a saved journal page, template or tarot record. It never deletes the original gallery/document file. Existing restored-media files use the older backup ownership rules and are not removed by this helper. App-wide storage cleanup and deletion controls remain separate roadmap work.

## Remaining depth

Android share-sheet intake, richer boards/collages, caption placement options, dedicated media management, asset collections and any supported future platform integration remain planned. This milestone makes local image/source collection and reuse functional without inventing a third-party integration.
