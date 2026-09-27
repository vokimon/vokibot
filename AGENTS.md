# VokiBot Developer Notes

## Project

- Kotlin Android app (F-Droid distribution)
- Modules: `app`, `puppet`, `shared` (git submodule)
- Stack: Jetpack Compose, Material3, serialization, coroutines
- SDK: compileSdk/targetSdk 36, minSdk 26

## Token economy

- **Default to the shortest answer that fully answers**
- Outlines over tables and prose; no preambles, no recap of what you just did, no re-explaining changes the diff already shows;
- Lead with the outcome. No narration, no filler praise, 
- Use full diff to propose changes, not full code snippets.
- Agents just analyze code in plan mode and edits it in build mode.
- Agents can propose build commands for the user to run, when they are not the usual ones, but agens NEVER run build commands.

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

CI runs: build -> test -> assemble (see `.github/workflows/main.yaml`)

To avoid build collisions and excessive token expending.
Those commands are for the User to run. Not for the agent to run,
with the only exception of `spotlessApply` after its editions.

## Controlled Workflow

1. **Task**: User asks a task
2. **Converge**: Agent asks the user one by one those aspects that are ambiguous in the task
2. **Propose**: Agent proposes code changes (few tens of lines), focused on a clear goal.
3. **Refine**: User reviews, asks refinements in chat
4. **Apply**: Either User or Agent writes/edits the files
5. **Build/Test**: User compiles, tests, and provides feedback in chat. The agent does not compile or test (this is important)
6. **Iterate**: Repeat until User commits or discards the proposal by reverting uncommited changes.


**Git references**:
- Last commit = reference for ongoing changes for the current proposal
- Stage = optional reference for refinements (usually before asking for refinements in an ongoing proposal)

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
- May create new files, but only change initial content after User adds them to the stage
- Produce small, focused proposals (tens of lines)
- Split large changes into multiple small changesets,
  planning a sequence where each step keeps the codebase working.
  This requires strategic thinking:
  break the task into incremental steps
  (each must compile; all tests must pass except the new test during TDD RED phase).
- Before entering to build mode and edit files, the user wants to see a full diff of the change

## TDD (Test-Driven Development)

When using TDD (Beck/Fowler methodology):
1. **Red**: Write a failing test.
   Only a failing assertion counts as RED --
   compilation errors, runtime crashes, or build failures do not qualify.
   The proposal must compile,
   and only the new test may fail;
   existing tests must still pass.
2. **Green**: Write the minimal implementation to make the test pass. Do not add extra behavior.
3. **Refactor**: Clean up code while keeping tests passing.

**When to use**: TDD applies to platform-independent code (e.g., business logic, data models, utilities). UI and Android-specific code does not use TDD.

**Test writing conventions**:
- Avoid multiple asserts in a single test
- When asserting multiple parts of a structure, build a helper that dumps the structure as string and assert against expected output using `net.canvoki.shared.test.assertEquals` (supports colored multiline diff)
- When testing multiple cases with the same logic, create a separate test method for each case; extract common code to a helper method with discriminant features as parameters
- Asserting large structures often became fragile. Concentrate inside a helper the setup of the irrelevant parts of the structure, and parametrize the relevant ones, to make updating those irrelevant parts easier. Do not expose parameters before they are needed.
- For setup objects, encapsulate common setup in a helper with parameters for what varies between cases; this makes each test case show only what differs
- Name the tests to include those parts: sut, case and optionally expectation, like in `summary with many errors display one each line`
- For literals, choose content that when shown in assertions, help to make faster diagnoses. Instead of naming two test objects 'a' and 'b', name them "previous", "wrongname"
- For aggregations consider testing 0, 1, N cases. Depending on the case, 0 or 1 first may make simpler fail the tests in order.

**Rules**:
- Do not change behavior during RED phase
- Implement only what is needed to pass the test, no more
- Each step must compile.
  RED proposals have only the new test failing by design;
  existing tests must still pass.
  GREEN and refactored steps must pass all tests.

**Workflow**: Agent proposes RED code
(test + deliberately wrong implementation that compiles but the new test fails).
User reviews the RED --
verifies the failure message is informative,
may adjust the test.
After approval, Agent adds the GREEN fix.
RED and GREEN are committed together in a single commit for the step.
Methodologically we separate RED and GREEN, but we do not commit REDs because they would break CI/CD.

### Long refactorings workflow (Duppe, Fill, Rely, Cleanup)

To keep larger refactors in small steps with stable commits,
Agent should split the code change proposals in committable stages following the methodology explained here.
Most refactors replace an old artifact (file, class, method, attribute, data source...) with a new one.
This methodology requires having different interface entry points for queries (getters) and updates (setters) of the state;
whenever any artifact mixes them, Agent should spot the case to the user and ask how to proceed.

The stages are:

1. Duppe (Duplicate):
Create the new artifact (file, class, method, attribute, data source)
that will hold the target code, without removing the existing one.
Both old and new coexist.

2. Fill (Keep in sync):
Ensure the new artifact mirrors the state of the old one by
updating the new artifact whenever the old one is modified (double setting)
This step can be split by each state update strategically thinking what needs to be done first.

3. Rely (Switch to new):
Once the new artifact faithfully represents the old one,
switch to using it in the code.
Replace remaining uses of the old artifact with use of the new one (replace getters).
This step can also be split by each state query.

4. Cleanup:
Remove the original artifact and any remaining state-setting code.

Since all the steps are stable, we could stop an ongoing refactor and focus on TDD some missing stuff in the new artifact.


## Style

- Prefer early exits
- Apply extract method to sectioning comments
- IDs and comments in English (regardless of prompt language)
- Avoid "conversational comments": code comments that make sense only in this conversation, common in tutorials but awful in committed code
- Comments should help maintenance, not explain what you changed
- Names should provide meaning and purpose, names should suffice to avoid comments most of the time
- Meaningful names use to be long, avoid including empty significants (Manager, Object...)
- Meaningful names use to be long, avoid repeating implied context, ie, `agentName` attribute in an `Agent` class.
- Suppressions and Opt-in clauses should be limited to the statements that require them. Do not apply to a function or class if just one statement needs it.

## Exception Handling

- Avoid catch-all exception handling to avoid masking bugs
- Scope try blocks to the specific statements expected to throw
- Expect the specific exception types you want to handle
- Catching an exception deserves at least a log
- If you don't know how to handle, let it raise

## Code Reuse

- Always consider extracting functions for repeated code
- Consider reuse existing functions before adding new ones
- Domain-independent code is promoted to `net.canvoki.shared`, and eventually moved to the shared module/library

## Translation Files

- 12 languages: an (aragonese), `and` (andaluh, hijacked ISO), ar (arabic), ca, de, en, es, eu, fr, gl, pt, ru
- English is reference; Andalusian auto-generated from Spanish
- `meta/translations/<isoCode>.yaml` - format: `id->text`, agents do not edit them directly.
- Agents propose new strings by generating a proposal.yaml which is `id->lang->text`.
- User reviews and applies `proposal.yaml` with `yaml-translations distribute`
- Use block scalars for multiline strings
- Avoid quotes if not needed
- Use interpolation `{varname}`, escaped `{{`. In code, they will be positional by their order in the reference language.

## Version control

- According to previous rules, agents should not commit, rebase or merge, but they could suggest humans commands for the human to execute or to solve conflicts by editing files.
- Commit messages use a limited set of gitmoji: 📝 doc, 🐛 fix, ♻️ refactor, 🎨 style, 🔧 ci...
- First word after the gitmoji should be context if needed (class, file, context of a wide change...)
- Commits are granular: you should be able to clearly identify in the diff what the change is
- Commits are topic: separate documentation, code style, refactorings and features or fixes. Each code style/refactor/fix/doc, in a different commit.
- Branches are short and merged with rebase so the merged history is a linear branch. Mergers are responsible to adapt their branch commits to already merged changes before merge.
- Agents should warn humans whenever uncomitted changes are about to accommulate so that a granular commit is becoming hard to make.

## Architecture

### Domain concepts

VokiBot automates actions on the device.
Three entities are linked in a graph:

- **Trigger**: represents an event that fires the automation (NFC, shortcut, bluetooth...).
  Each type has its **Dispatcher**: an Android component that receives system events,
  looks up the matching trigger and runs its automations.
- **Command**: *what action is executed* (`abstract suspend fun execute(context)`).
  Abstract entity concretable in multiple types.
- **Automation**: *the link* `triggerId -> commandIds`.
  When a trigger fires, its automations are looked up and their commands executed.

Flow: `event -> triggerId -> Automation.executeByTrigger() -> command.execute()`.

All of the above implement **StorableEntity**: a persistable entity that
serializes itself and exposes its references to export the graph.
Each one is stored as a single JSON file and registered in `EntityRegistry`
with an `EntityMetadata` that provides the UI editor, strings and icon.

Two recurring roles around every entity type:

- **Editor**: Form UI provided by each concrete entity type to create/edit an instance.
- **Item representation**: Methods and properties used to present instances in lists and pickers:
  `getTitle(context)` + `description` + `iconRes`/`loadIcon()`.

### Code structure

**Modules** (`settings.gradle`):

- `:app`: all domain and domain coupled UI.
- `:shared`: reusable classes shared with other apps: generic UI `StackNavigator`/`StackedScreen`, settings, crash, storage.
- `:puppet`: dummy app for instrumented and user tests.

**Root package** `app/src/main/kotlin/net/canvoki/vokibot/`:

- Domain:
    - `StorableEntity.kt`: The abstract class for all entities
    - `EntityRegistry.kt`: Central registry for serializable types
    - `Trigger.kt`/`Command.kt`/`Automation.kt`: Clases for those kind of entities (abstract for triggers and commands)
    - `FileDataRepository.kt`: Abstract storage for entities
    - `DataSet.kt`: Repository projection on one kind of entity (Repository implementation detail)
    - `ExportedBundle.kt`: Storable bundle of entities
    - `DataChangeBus.kt`: Notification/Subscription system for domain entity changes
- UI:
    - `MainActivity.kt`, 
    - `TriggerList.kt`, `TriggerTypePicker.kt`,
    - `CommandList.kt`, `CommandTypePicker.kt`,
    - `AutomationList.kt`, `AutomationEditor.kt`
    - reusable widgets in `common/`.
- Per-topic subpackages holding the full pattern (entity + editor + dispatcher + related utilities):
  `nfc/`, `shortcut/`, `bluetooth/`, `apps/`, `setting/`, `settingspage/`.

**Entity system** (the key pattern to add new types):

1. `interface StorableEntity`: `id`, `type`, `getTitle`, `iconRes`, `toJson`, `references`.
2. `abstract class Trigger : StorableEntity`: same pattern for `Command`;
   `Automation` is a plain data class with the same identity fields.
3. `interface EntityMetadata`:
   `typeKey`, `labelRes`, `iconRes`, `editorFactory`, `deserializer`, `helpRes`.
   Each entity implements it in its `companion object`.
4. Static registration in `EntityBootstrap` (inside `StorableEntity.kt`):
   `NfcTrigger.register()`, `ShortcutTrigger.register()`, `BluetoothDeviceTrigger.register()`...
   **single place**; the type picker and the list are generated from it.
5. Hand-rolled polymorphism (no SerializersModule): `EntityRegistry.fromJson`
   reads the `"type"` discriminator (`JsonConfig`), looks up the metadata and decodes.

**Persistence**: one JSON file per entity in `context.filesDir/repodata/`
through `FileDataRepository.kt` -> `DataSet.kt`, file `{prefix}{sanitizedId}.json`.
`DataSet.save/remove` emit `DataChangeBus` -> UI refreshes via `rememberDataVersion()`.
Export/import in `ExportedBundle.kt`. No DB/Room and no domain DataStore.

**Trigger dispatch**: triggers fire by identity (`triggerId`), with no predicates.
Dispatchers are per type: `NfcDispatchActivity`, `ShortcutDispatchActivity`
(foreground, via `TriggerDispatcher` composable) and `BluetoothTriggerReceiver`
(background, runs commands on `Dispatchers.IO`).
Generic delete in `TriggerList.kt` does not cancel side-effects
nor clean up `Automation.triggerId`.

**Navigation/UI**: screens are `@Serializable data class/object : StackedScreen<R>`
with constructor data;
editors follow the pattern `EditorHeader` + `rememberSaveable` + `rememberDiscardableState`
+ `LaunchedEffect(editingId)` with a `hasLoaded` flag
+ `repository.trigger.save(...)` -> `nav.pop()`.

**Tests** (`app/src/test/kotlin/net/canvoki/vokibot/`):
template to replicate `BluetoothConnectCommandTest.kt`
(`fromJson`, polymorphic roundtrip `assertIs`,
`registered with correct entityClass`,
`editor returns XEditor` with/without id);
also `NfcTriggerTest.kt`, `EntityRegistryTest.kt`, `FileDataRepositoryTest.kt`.
Helpers: `net/canvoki/shared/test/` (`assertJsonEqual`, multiline `assertEquals`).





