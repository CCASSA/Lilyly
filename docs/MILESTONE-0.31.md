# Cycle history

Cycle → Cycle history shows the intervals between recorded period starts, newest first. Each chapter lists how many days were logged and how many were marked as period. Expand one and tap a date to edit it in the existing daily log. History uses the same grouping rule as the wheel and never fills in missing logs or invents a subsequent start. The latest interval stays open. Logs before the first recorded start remain accessible through the calendar. No database changes or migrations.

The Moon 0.30 and Cycle 0.31 previews have the same signer-scoped application ID, `com.lilyly.app.preview.k3b9df640d57509308db5e673`. The new preview version code is 31. It is compatible with an in-place update of that Moon build; older separate apps keep their own storage.

Build: [run 42563632696](https://github.com/CCASSA/Lilyly/actions/runs/42563632696), source `5263c7c3562ae77380a77439fe160f22abb11030`. The report counted 108 unit tests with no failures or skips. APK SHA-256: `76f6e1cd2286fd788b47c1a4b04d0afbff08b7f3ab8b359656b11128656c6bc3`.

The API 35 emulator installed and launched the signed preview. All 36 instrumentation tests passed, including editing a historical cycle log and retaining its original note. Physical Samsung validation remains separate.
