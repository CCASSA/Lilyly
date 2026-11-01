# Lilyly 0.27 — My growing things (awaiting verification)

Sanctuary Therapy gains independent goals with editable intentions, Growing/Resting/Reached states, dated reflections, state filters and linked sessions. Existing freeform session goals remain intact. Goals appear in a gentle dedicated space without progress scores or clinical assessments.

Each reflection records the device local date/time and offset. Reflections appear under Therapy in Threads of a day using their recorded date and existing visibility controls. Goal journal pages are independent snapshots; later edits or deletion never overwrite them. Goal detail links back to linked sessions. Sessions can select multiple existing goals through progressive disclosure.

Removing a reflection requires confirmation and recomputes state from the latest remaining reflection (Growing if none). Removing a goal requires confirmation and atomically removes its session links while retaining session notes and journal copies. Unsaved editor changes require deliberate discard.

An additive therapyGoals record array participates in encrypted storage, backup validation and current-record-wins merge restore. Older records without goalIds/therapyGoals load unchanged. New goal-containing backups require this version or later; older apps reject unknown record types rather than silently discarding them. Restoring an older backup can restore a previously removed goal, consistent with merge behavior.

Six JVM tests cover persistence, duplicate reflections, state restoration, legacy session text, backup merge/validation and daily threads. A new Android journey creates a goal, reflects, links a session, creates an independently edited journal copy and verifies deletion/cancellation preserve the correct records. Full build, tests and visual review pending.

Remaining therapy work includes appointments/reminders, richer practice/task management and deeper longitudinal review. The complete product roadmap remains intended work.
