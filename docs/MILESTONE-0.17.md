# 0.17 — Ink Studio

Validated source: 99bec14d76d4634b0042cb05f3523524b56469d7
Successful run: https://github.com/CCASSA/Lilyly/actions/runs/39085441049

Both Debug and Garden Preview APKs compiled. All 58 JVM tests and 21 Android emulator journeys passed. Ink coverage checks legacy decoding, versioned pressure/color/dimension round trips, segment-aware erasing, invalid data and independent scrapbook layers. The device journey draws, undoes/redoes, selects erasing and highlighting tools, places ink into a scrapbook, saves and checks reopened storage for matching strokes and highlighter state.

Earlier runs failed because the test tapped toolbar controls behind the top bar while the oversized studio item was scrolled. Exact job logs showed an unselected Erase stroke node at y=-7..77 pixels. The corrected journey positions the studio item, checks displayed controls and selected tools, then draws. No app data or existing tests were removed to pass the suite.

The captured screen was inspected for readable controls and visible pen geometry. The capture did not show the final highlighter stroke; its saved state is asserted by the device journey. Physical stylus pressure/palm behavior remains unverified on actual hardware. See INK-STUDIO.md for full scope and limitations.

Garden Preview APK SHA-256:
4a8a7fce4ad7fb12ad5d0e86a2f79a919fc3d5a93a5eaf27903a376699bfef7a

This completes a journal milestone, not the full dream product. Ordinary documentation commits do not trigger builds. The complete intended product remains in PRODUCT-ROADMAP.md.
