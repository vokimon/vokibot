# VokiBot Developer Notes

## Project

- Kotlin Android app (F-Droid distribution)
- Modules: `app`, `puppet`, `shared` (git submodule)
- Stack: Jetpack Compose, Material3, serialization, coroutines
- SDK: compileSdk/targetSdk 36, minSdk 26
- 12 languages (an, ar, ca, de, en, es, eu, fr, gl, pt, ru, andaluh)

## Developer Commands

```bash
./gradlew test                      # unit tests
./gradlew assemble                 # build debug APKs
./gradlew installFlossDebug             # install to device (app)
./gradlew :puppet:installFlossDebug    # install Puppet companion
./gradlew connectedFlossDebugAndroidTest  # instrumented tests
./gradlew spotlessApply           # format code
./gradlew build                   # full build (lint + compile)
```

CI runs: build → test → assemble (see `.github/workflows/main.yaml`)

## Controlled Workflow

1. **Task**: User asks a task
2. **Propose**: Agent proposes code changes (few tens of lines), focused on a clear goal
3. **Refine**: User reviews, asks refinements in chat
4. **Apply**: Either User or Agent writes/edits the files
5. **Test**: User compiles, tests, and provides feedback in chat (this is important)
6. **Iterate**: Repeat until User commits or discards the proposal

Alternative: User can discard by checking out (reverting).

**Git references**:
- Last commit = reference for ongoing changes
- Stage = optional reference for refinements (only before asking for refinements in an ongoing proposal)

**Proposal categories** (do not mix in one proposal):
1. Style (formatting, naming)
2. Code moved among files
3. Code moved within a file
4. Other refactors
5. Added functionality
6. Build system and dependencies

**Rules**:
- Do NOT execute git commands that change repo state (commit, push, add, etc.)
- Read-only git commands are allowed (status, log, diff, etc.)
- Do NOT modify untracked files
- May create new files, but only edit them after User adds them to the stage
- Produce small, focused proposals (tens of lines)
- Split large changes into multiple commits, planning a sequence where each step keeps the codebase working. This requires strategic thinking: break the task into incremental steps that each compile and pass tests.

## TDD (Test-Driven Development)

When using TDD (Beck/Fowler methodology):
1. **Red**: Write a failing test. Only a failing assertion counts as RED (not compile errors or runtime crashes).
2. **Green**: Write the minimal implementation to make the test pass. Do not add extra behavior.
3. **Refactor**: Clean up code while keeping tests passing.

**When to use**: TDD applies to platform-independent code (e.g., business logic, data models, utilities). UI and Android-specific code does not use TDD.

**Test writing conventions**:
- Avoid multiple asserts in a single test
- When asserting multiple parts of a structure, build a helper that dumps the structure as string and assert against expected output using `net.canvoki.shared.test.assertEquals` (supports colored multiline diff)
- When testing multiple cases with the same logic, create a separate test method for each case; extract common code to a helper method with discriminant features as parameters
- For setup objects, encapsulate common setup in a helper with parameters for what varies between cases; this makes each test case show only what differs

**Rules**:
- Do not change behavior during RED phase
- Implement only what is needed to pass the test, no more
- Each step should compile and pass tests

## Style

- Prefer early exits
- Apply extract method to sectioning comments
- IDs and comments in English (regardless of prompt language)
- Avoid "conversational comments" — code comments that make sense only in this conversation, common in tutorials but awful in committed code
- Comments should help maintenance, not explain what you changed

## Exception Handling

- Avoid catch-all exception handling which may mask bugs
- Scope try's to the specific statements that may throw
- Expect the specific exception types you want to handle
- Catching an exception deserves at least a log
- If you don't know how to handle, let it raise

## Code Reuse

- Always consider extracting functions for repeated code
- Consider existing functions before adding new ones
- Domain-independent code is promoted to `net.canvoki.shared`, and eventually moved to the shared module/library

## Translation Files

- `meta/translations/<isoCode>.yaml` - format: id -> text
- English is reference; Andalusian auto-generated from Spanish
- Block scalars for multiline strings
- For AI translations in "id -> lang -> text" format, use the `distribute` subcommand of yaml-translations script
