# Apothecary — 0.7

Continues existing medication and log records with additive JSON defaults. Existing records do not automatically enable notifications.

- Edit medications, prescribed-dose wording and notes; archive without deleting history.
- Multiple daily times, separate as-needed use, historical date selection and editable taken/late/skipped/PRN logs with notes. Scheduled slots are upserted rather than duplicated; manual legacy entries remain readable.
- Optional manually maintained doses-remaining count and refill threshold. Counts do not automatically change, avoiding inference about quantities from free-text prescribed doses.
- Real local AlarmManager reminders with explicit notification permission controls, channel settings and a test reminder. Notifications contain no medication name or dose and never mark a dose taken. Tapping opens the medication room behind the existing app lock.
- Reschedule after edits, logging, restore, launch, reboot, timezone/time changes and app updates. No external health-data transmission.

Reminders are inexact and can be delayed by Android, Doze or manufacturer battery settings. They are not precise alarms and must not be the sole mechanism for time-critical medication. Force-stopping prevents background delivery until relaunch. PRN and archived records do not schedule. Complex weekly/cyclic schedules and automatic inventory accounting remain future work. Refill count is a manual record, not a dosing instruction. Longitudinal medication correlations remain on the full roadmap.

Verification includes five schedule/migration tests and two Android journeys for corrected slot persistence, refill count and a real private notification without creating a dose log. Physical Samsung reboot/Doze timing remains unverified.

Platform basis: https://developer.android.com/develop/background-work/services/alarms

Verified [run 37597478391](https://github.com/CCASSA/Lilyly/actions/runs/37597478391), source `d09715f4b00771f103757ca8300864a439defc6a`: both APK variants built; 28 unit tests and ten Android API 35 journeys passed. Medication screen reviewed within the full app shell. Preview SHA-256: `3d6a03c59988f247f89e28433f256229a5fb1eda8efb35f7443348b8335e8560`.

The notification test invokes the receiver and verifies an actual generic Android notification and unchanged log count. It does not establish timed AlarmManager delivery under Samsung Doze/reboot conditions.
