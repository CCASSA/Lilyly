# Lilyly 0.30 · Moon almanac

From Home’s moon or More → Magical calendar, browse a month, select a day and inspect the next four estimated principal phases. Open the selected date in Threads of a day and return to that date in the almanac. The wheel of the year uses the selected date and configured hemisphere.

Lilyly’s existing mean-cycle model now treats the reference moment as UTC and projects estimated phase changes into the device’s time zone. Calendar month boundaries use local midnight; the selected-date illustration represents local noon. The illustration is mirrored for the southern hemisphere and is schematic, not a prediction of the sky angle. No location permission or network call is required. This is not an astronomical ephemeris.

Begin a moon page opens an unsaved journal draft stamped with the selected date and estimated phase. Saving keeps the actual creation time and files the page under its reflection date in day threads. Existing pages default to their original creation date; backups retain the optional field.

Tests cover a phase that falls on different local dates, continuity across a daylight-saving transition, historical dates, leap-day navigation and opening the selected day.
