# Therapy room — 0.8

The existing therapy records remain intact. Additive fields preserve questions, goals, homework completion and a linked journal ID. Old appointment wording is preserved, even when it is not a date.

Session library with search across session text; unfinished-practice filter; editable historical or planned session dates; progressive Before / After / Practice editor; questions, session reflections, goals and homework; revisitable practice completion; optional appointment date note; linked journal pages in a Sanctuary notebook.

Saving an existing session updates its ID instead of appending duplicates. Journal linking is idempotent and creates an independent snapshot. Later therapy edits do not overwrite changes made on the journal page. Therapy records remain in the existing encrypted store and existing backup/restore flow. No external transmission or diagnostic interpretation.

Limits: goals and practice are per-session text/status, not a full cross-session goal management system. Appointment dates are notes, not alarms or external calendar appointments. Broader pattern review remains on the full roadmap.

Tests cover old/new JSON and an Android journey that creates and edits a session, verifies saved practice state, links to the journal and protects independent journal edits. Build result recorded after execution.

Verified [run 37609567423](https://github.com/CCASSA/Lilyly/actions/runs/37609567423), source `6884a65e8b94e0b46fe1b2d608bd4f14c614dac5`: both APK variants assembled, 30 JVM tests and eleven Android API 35 journeys passed. Therapy screenshot reviewed. Preview SHA-256: `0df9be8eebf79557f7c8efda30c1eb263b5ae8a0ac49548181a55f65bdf2915e`.
