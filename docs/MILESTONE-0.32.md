# Lilyly 0.32 · Play test preparation

The original application ID now builds a non-debuggable unsigned Android App Bundle. Milestone previews use the existing signer-scoped package ID. CI checks release metadata, and a separate deliberately triggered delivery check uses pinned Google bundletool to validate the AAB, generate APK splits, install them on API 35 and launch the production activity. Its temporary two-day signing key is for device verification only and is not an upload key.

The launch icon is now an original moon and lily drawable; its artwork source also generates the 512 × 512 store icon and 1024 × 500 feature graphic. Privacy & health information in Settings explains local records, private media, external providers, notification/authentication behavior, deletion and export. The app no longer requests Internet permission. A deliberate empty journal remains empty on reopen.

Optional waking temperature in Celsius is stored on each dated cycle log and displayed in daily threads. TTC mode exposes the field directly; other modes show it under detailed observations. The 30-day chart breaks lines across missing dates and has an expandable list of exact readings. Existing records default to unknown, and the fertility estimate is not changed by a temperature entry.

The publisher still controls identity, upload signing, privacy policy hosting, account declarations, testing and release. Refer to docs/play/PUBLISH.md and docs/COMPLETION-GATES.md. No Play upload or public release has been performed.

Production delivery evidence: [run 43372065069](https://github.com/CCASSA/Lilyly/actions/runs/43372065069), commit `04e72238063d45cb1f9ea27978d5f4b44c0bac6f`. Its 12,364,292-byte unsigned AAB has SHA-256 `2efb16952cb6dbf227732e71f1df0fe6525127221577e87a31c5efff75b4383c`. The delivered manifest was non-debuggable with no Internet permission; the base, English, x86_64 and xxhdpi splits installed together and the original activity cold-launched successfully. Both included 64-bit native libraries have 16 KiB aligned ELF LOAD segments; that static check is not a 16 KiB device test.

Preview: [run 43371542529](https://github.com/CCASSA/Lilyly/actions/runs/43371542529), app source `9a8be35e866403c29062a910aacab62e03bc337b`. Unit report: 110 passed, zero failures or skips. Preview SHA-256 `c537f0ccf61c6efdfc3bb449702259f7f25855184b8b7fd36e9b353c5d5ebc9f`, 18,531,131 bytes. The package and verified signing fingerprint match the delivered 0.31 build.
The final API 35 instrumentation report passed all 37 tests, including preservation of an intentionally empty journal and a stored temperature after restart.
