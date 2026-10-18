# Ink Studio — 0.17

Open a journal or grimoire page and choose **Write / draw**. Pen, Highlighter and Erase stroke tools offer five colors and three widths. Finger and stylus input work; Stylus only ignores finger drawing. Pen pressure affects width when supplied by the device. Physical stylus behavior needs hardware testing; this is not universal palm rejection or handwriting recognition.

Dots and short marks are retained. Erasing removes a whole touched stroke. Undo/redo records completed gestures, including clearing, with up to 40 changes while the studio remains open. Clear asks for confirmation. Leaving the studio resets undo history, not drawing data.

**Place ink on scrapbook** creates an independent transparent ink element. Existing scrapbook controls move, resize, rotate, duplicate and reorder it. Save the page to persist changes. The original handwriting layer stays editable; placed copies support element transforms rather than stroke editing.

Version-2 ink stores reference dimensions, points, pressure, colors, widths and highlighter state. Uniform scaling preserves proportions. Highlighter strokes use one translucent path to avoid dark seams. Records, templates and encrypted backups retain this data through existing journal JSON.

Legacy arrays of pixel-coordinate strokes remain readable. They did not record canvas dimensions, so original proportions on a different device cannot be reconstructed exactly. Coordinates are preserved and current dimensions attached upon editing or placement. There is no bulk conversion or destructive migration.

Remaining scope: richer brushes, hardware QA, editing strokes within placed elements, a fullscreen workspace, long-session performance, optional handwriting recognition, export/printing and further polish. The full dream roadmap remains intended work.
