# 0.15 — Personal insights

Validated source: 91d35242c19859ed0c038e78308bce7b46361c8e
Successful run: https://github.com/CCASSA/Lilyly/actions/runs/38460915935

Both Debug and Garden Preview APKs compiled. All 49 JVM tests and 18 Android emulator journeys passed. The new device journey selects insight sources, inspects a comparison, opens its recorded date through the callback, and verifies persisted source preferences. The captured insights screen was inspected for readable labels, group counts, values and recorded-day controls. Existing journeys for cycle, journal, therapy, medication, privacy, tarot, reading and music remained green.

Garden Preview APK SHA-256:
d94377d4fa2998f184c5c4539e6df398e98ba6667c50b11d204ae9916add764b

The preceding device-check attempt was blocked before tests started by a corrupt Android SDK system-image download (`Error on ZipFile unknown archive`). Its APK compilation and 49 unit tests passed. After inspecting the exact job log, a deliberate marker-only retry on a fresh runner succeeded; the known-working build dependencies were preserved.

See PERSONAL-INSIGHTS.md for methodology, limitations and opt-in behavior. This is a completed milestone, not completion of the entire Lilyly product. The full roadmap remains in PRODUCT-ROADMAP.md. No sample health data is shipped into the user's records; screenshot data comes from an isolated emulator test fixture.
