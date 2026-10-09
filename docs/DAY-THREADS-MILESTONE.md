# Day Threads — 0.12

Home opens a date-based read-only gathering of Cycle, Sanctuary, Sleep, Medication, Pages, Therapy, Tarot and Rituals. Native date selection, previous/next/today navigation, and the 60 most recent recorded-date shortcuts. Category visibility is stored in encrypted settings and included in backup; hiding does not delete records.

Uses recorded local dates, not inferred time zones. Sleep uses wake date; medication uses log date with scheduled slot shown separately; pages use creation date, excluding templates. Ritual plans and actual practice dates are separate labeled events. No missing ratings filled in; no adherence or health claims inferred. Approximate moon label is inherited from the existing lunar model.

Pages open their actual editor IDs. Other links visit the corresponding room; Cycle additionally selects the viewed historical date. Therapy, sleep and tarot link an existing journal page if one exists. Original records remain authoritative, with no summary copies or new sensitive-data transmission.

Limits: grouped daily records, not a chronological timestamp timeline or a correlation engine. Other room links do not yet focus the exact historical record. Filtering is only for this view, not global privacy policy. Computation currently scans local record lists; pagination/indexing remains a scale improvement.

## Validation

[Run 37938123959](https://github.com/CCASSA/Lilyly/actions/runs/37938123959) passed for `8eb3221b50308b1e73b4c572e8d9fa7a7da28afa`: 36 JVM tests and 15 Android journeys. Initial compiler failures (extra parenthesis and generated setter clash) were fixed from exact logs. Stale-checkout integration was corrected to preserve all Ritual Room routes and tests. The daily test's overnight fixture was cleaned up after it correctly triggered the later sleep journey's overlap validation. Final suite passes with product overlap protection retained. Daily-view screenshot reviewed. Physical Samsung validation remains with the user.

Preview APK SHA-256: `3b4e7cd4510db0b509bd397016126b07676154b96154cc4fbea3a5a17d5609d9`. Artifact: `Lilyly-Garden-Preview-safe-side-by-side`.
