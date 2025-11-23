# Repository Guidelines

## Project Snapshot
- Java 21 desktop + CLI tool that records farm sessions for Dofus Rétro players. `Main` switches between Picocli commands (arguments provided) and the Swing UI (no arguments).
- Persistence is local SQLite stored under `~/.dofus-rentabilizer/data.db` via `Database`. Always go through `SessionService` to touch data so validation and foreign keys stay enforced.
- UI assets live in `src/main/resources` and are consumed by `ThemePalette`, `RoundedPanel`, and `UiComponents`.
- `devlog.txt` chronicles progress; append a `[YYYY-MM-DD] <area>: <change>` entry for every change set.

## Quality Bar & Hygiene
- Keep changes small, cohesive, and reversible; delete dead code/assets and avoid TODOs or commented blocks.
- Maintain parity between Gradle and Maven definitions whenever dependencies or plugins change.
- Prefer explicit validation and friendly error messages; guard against nulls and malformed user input early.
- Log with `logger.info` for new workflows so batch CLI runs stay auditable; keep log noise low.
- Document assumptions, edge cases, and user-visible impacts in the devlog and PR notes.

## Frontend Craft (Dofus 1.29)
- Reuse `ThemePalette`, `UiComponents`, and `RoundedPanel`; do not hardcode colors, fonts, or shadows outside the shared theme.
- Build intentional layouts: consistent spacing, alignment, and sizing; no flat placeholders. Verify at least 1280x720 and 1920x1080 window sizes plus resize behavior.
- Cover interaction states (hover/focus/disabled) and keyboard navigation while keeping styling on-theme and legible.
- Keep backgrounds/assets under `src/main/resources` with sensible names; ensure gradients/textures tile or scale without distortion and remain readable behind content.
- When adding UI features, validate end-to-end flows (Main menu, Mode Farm, Infos zones) and keep CLI parity where it matters.

## Source Layout & Responsibilities
- `src/main/java/com/dofus/rentabilizer/Main.java` – entry point registering CLI subcommands and launching `MainWindow`.
- `cli/` – Picocli commands (`AddSessionCommand`, `StatsCommand`, `HistoryCommand`). Register any new command in `Main`.
- `db/Database.java` – filesystem-safe SQLite initialization (tables `zones` and `sessions`) plus helpers for upserts and FK-aware connections.
- `domain/` – immutable records (`SessionRecord`, `ZoneRecord`, `ZoneStatsRecord`). Add new DTOs as `record` types to keep serialization simple.
- `service/SessionService.java` – the single gateway for persistence (session creation, history, aggregated stats, latest zones). Extend this layer instead of embedding SQL in UI/CLI code.
- `ui/` – Swing components themed after Dofus 1.29. Reuse `ThemePalette`, `UiComponents`, and `RoundedPanel` when adding panels/dialogs so gradients, fonts, and spacing remain consistent.
- `build.gradle.kts` + `pom.xml` – mirrored dependency definitions (Picocli, SQLite JDBC, JUnit 5). Keep them in lockstep; bump versions in both files.

## Build, Run & Verification Commands
- `./gradlew run --args="add --zone 'Porcos' --minutes 45 --kamas 120000"` – execute CLI command through Gradle (preferred during development).
- `./gradlew run --args="stats"` / `./gradlew run --args="history -n 10"` – sanity-check stats queries and history formatting.
- `./gradlew shadowJar` then `java -jar build/libs/dofus-rentabilizer-all.jar` – produce the fat JAR and launch the Swing UI for manual testing.
- `mvn -q -DskipTests compile` – Maven fallback for IDE import/verification. Use `mvn exec:java -Dexec.mainClass="com.dofus.rentabilizer.Main" -Dexec.args="stats"` for CLI parity.
- `./gradlew test` or `mvn test` – run JUnit 5 suites. Prefer Gradle for CI alignment.

## Standard Workflow
1. **Analyze** – restate the problem, inspect the relevant package (`cli`, `service`, `ui`, etc.), confirm DB schema impact, and review theme assets if UI work is involved.
2. **Plan** – outline the path across layers (data/service/UI/tests). Document assumptions in the PR or devlog if requirements are ambiguous.
3. **Implement** – favor reusable helpers (`SessionService`, `UiComponents`, `ThemePalette`) and keep SQL inside the service package. For CLI changes, wire new commands through Picocli annotations and register them in `Main`.
4. **Verify** – compile with both toolchains when dependencies change, run the affected CLI commands, and open the Swing UI to visually validate theme consistency. Cover logic with JUnit tests under `src/test/java/...`.
5. **Document** – update README/AGENTS for workflow shifts, capture the change in `devlog.txt`, and include testing notes/screenshots in PRs.

## Coding Style & Patterns
- Java code uses 4-space indentation, `camelCase` for members, and `PascalCase` for classes. Favor `record` for immutable data holders.
- UI code must reuse `ThemePalette` colors, gradients, and fonts; avoid redefining hex codes. Shared widgets belong in `ui/` rather than ad-hoc anonymous classes.
- Keep domain logic and SQL inside `service`/`db` packages. CLI and UI layers should call service methods and render results only.
- Add lightweight logging (`logger.info`) when introducing new workflows so CLI batch runs remain auditable.
- No formatting hook exists; run IntelliJ “Reformat Code” before committing.

## Testing & QA
- Tests belong in `src/test/java/com/dofus/rentabilizer/...` and follow the `ClassNameTest` naming convention (e.g., `SessionServiceTest`).
- Focus unit tests on `SessionService`, database edge cases, and CLI command handlers (parameter validation, output formatting).
- Add integration-style tests that hit SQLite when adding schema changes; `Database.init()` is safe to call during tests because it writes to a user-specific folder.
- When altering UI behavior, include manual validation notes (screenshots/GIFs) in the PR plus any small assertions that can be automated.

## Commit & PR Expectations
- Use `<type>: <summary>` commit messages (`feat: add zone suggestion dropdown`, `fix: clamp session duration bounds`).
- Every PR description should cover purpose, testing evidence (`./gradlew run --args="stats"`, `mvn compile`, UI manual checks), and reference issues (`Closes #12`) when applicable.
- Keep Gradle and Maven definitions synchronized before requesting review, and mention schema/data migrations if the DB layout changes so reviewers can test upgrades safely.
