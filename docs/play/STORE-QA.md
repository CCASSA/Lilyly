# Release decision record

The build checks are technical evidence only. Review the final bundle on a Play testing track before making it public.

| Gate | What to verify | Evidence still required |
|---|---|---|
| Existing install | Upgrade a test install signed by the Play app signing key. Open a saved notebook, cycle log, medication schedule and imported book after updating. | Play-delivered installation on physical hardware; a debug keystore cannot substitute. |
| Privacy | Lock while a journal page is open, return from Recents, rotate and send a share intent while locked. Test both biometric authentication and the configured PIN fallback. | Real Samsung device, including notification denied and lock enabled. |
| Accessibility | TalkBack can name the Cycle calendar dates, scrapbook tools and Reading Nook navigation. At maximum supported text size the user can reach Save with the keyboard open. | Physical-device review and fixes for any blockers. |
| Recovery | Create a synthetic journal with media and a book, export an encrypted backup, and verify wrong-password rejection and successful restoration to a separate installation. | Keep the original install until media and record counts have been checked. |
| Content | Audit listing claims, privacy disclosures, licenses, dependencies and packed release assets. Do not advertise planned features as implemented. | Public publisher contact, rights review and matching live privacy policy. |
| Store review | Check the uploaded AAB’s app signing certificate, release version code, device coverage, native-library compatibility, content rating and Play pre-launch report. | Publisher’s authenticated Play Console. |

## Full product scope that remains

The release preflight does not retire the larger roadmap. Dated contraception history and full pregnancy/TTC experiences, richer medication schedules and correlations, precise canvas export, advanced EPUB highlighting, broader reference content and accessibility polish are still open. See ../COMPLETION-GATES.md for the complete working list.
