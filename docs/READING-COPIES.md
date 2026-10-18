# Reading copies — 0.18

Use **Export reading copy** in a journal/grimoire list or page editor. The list exports the currently filtered entries, in displayed order, so a notebook, favorites and search can narrow the selection. The page editor exports the current draft without silently saving it to the app. The confirmation states the entry count and previews titles. Choose a book title, then an Android PDF destination. Canceling keeps records unchanged.

The printable A4 reading layout includes a cover, entry titles/dates/notebooks, paginated body text, a page photo, ritual summary, handwriting, scrapbook elements in layer order, source credits/URLs, music titles/captions/URLs and tags. Scrapbook text is reflowed; positions, rotations and paper styles are not reproduced. Tarot elements are readable card/position references, not deck artwork. Music/source references are printed text, not live metadata fetches or playback. Exact scrapbook-layout publishing, richer book design, table of contents and dedicated print controls remain planned. The saved PDF can be printed through a PDF viewer.

Each entry begins on a new sheet. Long text flows across numbered sheets. Photos are normalized for EXIF orientation and decoded at a bounded size. Unavailable or unsupported images produce a visible notice plus an export warning. No source URLs are fetched by Lilyly. Handwriting uses its recorded reference dimensions; legacy ink with no dimensions uses its point extents.

## Privacy and reliability

This is a plain, unencrypted PDF outside Lilyly's app lock, as the confirmation explicitly states. Only selected entry data is exported: the exporter does not fetch other cycle, therapy, medication or journal records, nor Spotify credentials. Context already written into a selected entry is included. Android's selected destination may be local or a cloud provider chosen by the user.

The selection is frozen before the save picker using a private temporary cache snapshot. Completed/canceled exports remove the snapshot; abandoned selection files older than 24 hours are cleaned when a new export begins. PDF generation finishes in private cache before copying to the chosen destination. Temporary PDF and decoded-image files are removed after use. A destination write failure can leave a partial file at the chosen destination; the UI reports failure and the user can remove it or retry. The existing password-encrypted backup is unchanged and remains the restore format; reading copies cannot restore app data.

Limits are 200 selected entries and 500 output sheets per export. Images are limited to 25 MB and 200 megapixels before downsampling to at most 2048 pixels on the longest side. Large selections should be split by notebook or search. Persistent journal models and existing records require no migration.
