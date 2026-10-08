# Atmospheres — 0.10

Six global curated palettes: Lilyly, Celestial, Gothic, Cottage Witch, Antique Grimoire and Botanical. Settings presents real preview swatches with immediate selection. Storybook, Clear and Letters typography choices use Android font families. Header theme button opens the picker rather than overwriting the chosen family.

Selection persists in existing encrypted settings, included in backup. Missing/unknown theme keys fall back to the legacy dark/light preference. Existing journal pages and independently chosen Nook rooms retain their styles. Global Material surfaces, containers and navigation use coordinated theme roles rather than default purple containers.

Scope: global colors and typography. Existing specialist canvas art and cycle phase colors remain intentionally independent. This does not yet add custom color editing, texture packs, decorative intensity, downloadable fonts or custom background images.

## Validation

[Run 37751368009](https://github.com/CCASSA/Lilyly/actions/runs/37751368009) passed for source `95e915be1bb07c192a491611b01b9297fe5f84f9`: 32 JVM tests and 13 Android journeys. New Android journey covers theme selection, typography, persistent reload, light mode and unchanged page count. Captured Celestial/Letters settings screen was visually reviewed. Palette body and primary/secondary/tertiary colors against surface all pass a 4.5:1 computed contrast check; this is not a full accessibility audit.

Preview APK SHA-256: `14d9a72ac9c91778f2c822c40d584c019a565da1ef3ac6480feace122cff1875`. Artifact: `Lilyly-Garden-Preview-safe-side-by-side`. Physical Samsung testing remains with the user.
