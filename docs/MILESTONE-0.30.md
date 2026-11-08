# Lilyly 0.30 · Moon almanac

From Home’s moon or More → Magical calendar, browse a month, select a day and inspect the next four estimated principal phases. Open the selected date in Threads of a day and return to that date in the almanac. The wheel of the year uses the selected date and configured hemisphere.

Lilyly’s existing mean-cycle model now treats the reference moment as UTC and projects estimated phase changes into the device’s time zone. Calendar month boundaries use local midnight; the selected-date illustration represents local noon. The illustration is mirrored for the southern hemisphere and is schematic, not a prediction of the sky angle. No location permission or network call is required. This is not an astronomical ephemeris.

Begin a moon page opens an unsaved journal draft stamped with the selected date and estimated phase. Saving keeps the actual creation time and files the page under its reflection date in day threads. Existing pages default to their original creation date; backups retain the optional field.

Tests cover a phase that falls on different local dates, continuity across a daylight-saving transition, historical dates, leap-day navigation and opening the selected day.

CI previews use a distinct application ID derived from the public fingerprint of the actual debug signing certificate, verified against the packaged APK. A cache miss cannot produce an incompatible update with the same ID. This preview installs beside the original and older Garden Preview; existing records stay in their originating app. A matching certificate allows later previews with that ID to update in place. Long-term release signing is still an open release gate.

## Build evidence

Source: `8a0f2b18b5851cb021df15a7fa88bb6614ebfc07`. [Deliberate Android run](https://github.com/CCASSA/Lilyly/actions/runs/42532452938). The unit report recorded 105 tests with zero failures or skips. The final APK passed the signing certificate and application ID check (package `com.lilyly.app.preview.k3b9df640d57509308db5e673`).

APK: `Lilyly-Moon-0.30.0.apk`, 18,494,769 bytes, SHA-256 `e05e44837feb0d323d1f72c0b2de5a84d92d9b47e8527dcfa8ef16bc15b390a7`. See [testing on a Samsung](TEST-ON-SAMSUNG.md).

The delivered preview installed and launched on the Android API 35 emulator (Activity Manager reported Status: ok). The debug instrumentation suite passed all 35 tests, including calendar navigation, shared inspiration, notification privacy and ritual persistence. This is not a physical Samsung or release-signing certification.
