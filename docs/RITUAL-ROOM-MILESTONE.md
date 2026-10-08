# Ritual Room — 0.11

Dedicated room accessible from More → Ritual room. Create a personal practice or use three original reflective prompts; browse/search/filter plans and practised records. Progressive editor separates intention/date, materials/steps and reflection/completion date. Native date pickers; optional approximate lunar phase recorded for practice date (no exact lunar timing claim). Dates do not schedule alarms.

RitualDetails is an additive optional JSON field on JournalEntry. Old spell pages remain reachable through Open all spell pages. Existing encrypted journal storage and backup preserve the metadata, body, artwork and handwriting together. Opening a ritual's grimoire page opens that same ID, with metadata displayed separately from editable personal prose. Journal deletion uses existing confirmation. No duplicate linked snapshot.

Repeat practice makes a new ID retaining the plan but clearing dates, reflections and personal art/prose; old record is untouched. Draft uses saveable JSON and explicit Keep ritual; back/close confirms discard. The new record is not saved until Keep ritual.

Scope: personal ritual planning and recording, not a sourced herb/crystal reference library or a claim of guaranteed supernatural outcomes. No alarms, step timers, ingredient safety database or reference catalog in this milestone.

## Validation

[Run 37783874901](https://github.com/CCASSA/Lilyly/actions/runs/37783874901) passed for `bfa06e8608a55842bb7135efd42cbfffb2986c54`: 34 JVM tests and 14 Android journeys. The first run's ritual test attempted an offscreen lazy-list save action; exact logs identified the missing node. The test now scrolls to editor/list actions, and the deliberately triggered retry passed. Saved ritual list screenshot visually reviewed. Physical Samsung testing remains with the user.

Preview APK SHA-256: `fb25b9792e1dff37dcf74d7d4576d2235189620e1f55b8507d20a79eb2a8ddbe`. Artifact: `Lilyly-Garden-Preview-safe-side-by-side`.
