# Cycle Garden engineering notes

## Proven baseline
Successful Actions run: https://github.com/CCASSA/Lilyly/actions/runs/37238898878
Source baseline: 7615cc9 (manual workflows). The normalized `android/` tree was reconstructed using the successful one-shot workflow's exact unpacking and patches. AGP 8.13.2, Gradle 8.13, JDK 17, SDK 36 and pinned Compose/dependency versions are preserved. No dependency upgrade was used to redesign UI.

## Storage compatibility
Existing `lilyly_secure` encrypted preferences, key alias, application ID and all model fields remain. Cycle JSON adds `selections` and `ratingsRecorded`; settings adds `cyclePreferences`. Missing new fields default safely. Unrecorded numeric defaults do not feed the graph. Existing free-text observations remain editable under More. Upsert uses date, so editing history replaces that day's record rather than duplicating it. No records are deleted.

## Known limits / next work
- Period episodes are inferred by a >10-day gap between logged bleeding dates. Sparse logs remain ambiguous. Explicit period ranges/start correction should replace this heuristic in a later migration.
- Calendar and phase bands are estimates based on recorded starts; missing starts are not manufactured. Overdue countdown never rolls forward. Future dates are read-only for health logging.
- Pregnancy/TTC modes are initial context controls, not gestational/clinical functionality. No fabricated hormone graph.
- Daily integration currently displays date-linked records. Deep navigation, sleep records and user-controlled statistical correlations are still roadmap work.
- Icons are accessible vector symbols per category, not final unique illustrated artwork for every symptom. Large-font and Samsung visual/device testing still needed.
- Existing secure storage error recovery/export/restore needs dedicated work before release; this milestone does not change encryption or key handling.
- The old CI build did not preserve its debug signing key. New milestone CI caches its key for continuity (cache is not a permanent signing vault). If Android reports a conflicting signature, do NOT uninstall and lose local data. Recover the original signing key or implement/test encrypted export and restore first. Production signing needs durable private configuration.

## Build controls
`cycle-milestone.yml` builds `android/`, runs unit tests and uploads APK plus test reports. It runs manually or when `.github/build-request.txt` is deliberately changed on main. Ordinary commits do not trigger builds. The two legacy workflows remain manual and still reconstruct the untouched bundles, so they build the old prototype, not this milestone.

## Device acceptance checklist
Install as an update only if Android accepts the signature. Check existing journal/cycle/therapy/medication records; month navigation and historical edit; bleeding vs spotting; saved selections and notes after relaunch; close-with-unsaved-changes dialog; settings persistence; pregnancy pause and hormonal fertility suppression; no-history/overdue/irregular states; graph missing days; keyboard/rotation/large text; light and dark themes. Compilation is not visual/device acceptance.
