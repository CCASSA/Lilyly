# Lilyly 0.27 — My growing things

Sanctuary Therapy gains independent goals with editable intentions, Growing/Resting/Reached states, dated reflections, state filters and linked sessions. Existing freeform session goals remain intact. Goals appear in a gentle dedicated space without progress scores or clinical assessments.

Each reflection records the device local date/time and offset. Reflections appear under Therapy in Threads of a day using their recorded date and existing visibility controls. Goal journal pages are independent snapshots; later edits or deletion never overwrite them. Goal detail links back to linked sessions. Sessions can select multiple existing goals through progressive disclosure.

Removing a reflection requires confirmation and recomputes state from the latest remaining reflection (Growing if none). Removing a goal requires confirmation and atomically removes its session links while retaining session notes and journal copies. Unsaved editor changes require deliberate discard.

An additive therapyGoals record array participates in encrypted storage, backup validation and current-record-wins merge restore. Older records without goalIds/therapyGoals load unchanged. New goal-containing backups require this version or later; older apps reject unknown record types rather than silently discarding them. Restoring an older backup can restore a previously removed goal, consistent with merge behavior.

Six JVM tests cover persistence, duplicate reflections, state restoration, legacy session text, backup merge/validation and daily threads. A new Android journey creates a goal, reflects, links a session, creates an independently edited journal copy and verifies deletion/cancellation preserve the correct records. Validation complete: 97 JVM tests and 30 Android instrumented tests passed. The corrected journey exercises the actual Sanctuary app shell; its screenshot was inspected for readable text, spacing and theme integration. Physical Samsung testing remains for the owner.

Remaining therapy work includes appointments/reminders, richer practice/task management and deeper longitudinal review. The complete product roadmap remains intended work.

## Verified build

- Source: `49a2d72fd8c8c607b7ca4f68f9fbdad99f37c0c5`.
- Successful deliberate run: https://github.com/CCASSA/Lilyly/actions/runs/41288083560
- Garden Preview: `Lilyly-Preview-0.27.0.apk`, 18,494,837 bytes.
- SHA-256: `6e1ea875ef8eb9022a0c679660af2a4c062daf0ad2f8751a6fb400db7dc6958c`.
- Preview package remains `com.lilyly.app.preview`, using the existing milestone signing cache. It updates Garden Preview separately from the original app.
- The initial successful run used an isolated screen test. A second deliberate run corrected that verification gap; no production code workaround was needed.
- Ordinary documentation/source commits do not trigger the milestone workflow.

This completes the connected-goals slice, not the entire dream application. See COMPLETION-GATES.md and PRODUCT-ROADMAP.md.
