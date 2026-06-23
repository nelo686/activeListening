# Active Listening

**English** | [Español](./README.md)

An educational Android app for musicians, drummers, and music students. It imports a local song and guides the user through active listening to locate transitions, challenge hypotheses, and build an editable structural map.

The app does not present an automatically generated song structure as definitive truth. Local proposals and AI-generated suggestions are starting points that users should listen to, review, confirm, or correct.

## User guide

If this is your first time using the app, start with the [user guide](./USER_GUIDE.md). It explains the complete workflow without technical detail: importing, playback, guided listening, section editing, saved songs, and export.

## Current features

- Local audio import through the Android document picker.
- Format, access, and duration validation before loading a song.
- Title, artist, and artwork extraction when file metadata is available.
- Media3 ExoPlayer playback with play, pause, and seeking.
- Guided listening with an initial structure proposal and educational prompts for each section.
- Optional remote guidance through an OpenAI-compatible API, with a local fallback when the key is missing or the request fails.
- Protection against late responses: an AI response is never applied to a different song or later session.
- Editable map with `Intro`, `Verse`, `Chorus`, `Bridge`, `Outro`, and `Other` labels.
- Custom names for `Other` sections, such as `Pre-chorus`, `Solo`, or `Interlude`.
- `Suggested`, `Confirmed`, and `Uncertain` section states.
- Horizontally scrollable timeline with playback cursor and timecodes.
- Direct boundary editing: dragging an edge updates both the left block's end and the right block's start.
- Precise start and end editing from the section details.
- Splitting at the current playback position and merging adjacent sections.
- Minimum section duration enforced when moving, splitting, or merging.
- Editable `Rhythm change` indicator in both section details and timeline blocks.
- Approximate bar-count and regularity estimates based on section duration.
- Explanations adapted to introductory, intermediate, advanced, and expert levels.
- Guided actions to confirm, mark uncertainty, replay eight seconds, or move to the next section.
- Normal and reduced guidance intensity.
- Restore the original proposal after editing.
- Saved sessions, original/edited structures, progress, and last playback position.
- Saved-song list with title, artist, duration, and `Analyzed` or `In progress` state.
- Swipe-to-delete with immediate undo.
- Structural map export to PDF through the Android destination picker.

## Educational approach

The workflow keeps the user actively involved:

- listen for changes in energy, rhythm, instrumentation, or feel;
- locate transitions with timecodes;
- compare the proposal with what is actually heard;
- label, split, merge, and move boundaries;
- mark decisions as confirmed or uncertain;
- interpret estimates and suggestions as editable approximations.

The AI does not receive or analyze the audio. It works with the song title, duration, and initial markers to propose labels, questions, and listening cues. Its responses are therefore educational assistance, not definitive analysis.

## Main workflow

1. Import a supported song or open a saved session.
2. Play it and complete an initial free listen.
3. Start guided listening to create and save the initial map.
4. Explore the timeline horizontally.
5. Drag boundaries or open a section to edit timing, label, state, and rhythmic contrast.
6. Split, merge, or restore the original proposal when needed.
7. Continue later from `Saved songs`.
8. Export the completed map to PDF.

## Timeline and editing

Each block shows its label, time range, and duration. `Suggested` and `Uncertain` appear as badges; confirmed sections do not add a badge. A section marked with a contrast displays `Rhythm change`.

Internal boundaries have a visible handle with a wider touch target. A provisional timecode appears while dragging. The change is persisted on release, keeps both blocks contiguous, and respects the minimum duration.

Tapping a block opens a fully expanded section sheet. Its content is scrollable and accounts for the software keyboard, keeping time and custom-name fields visible during editing.

## Saved songs

A song is added to the saved list when guided listening starts. Each card is limited to three lines:

1. song title;
2. artist and duration;
3. `Analyzed` or `In progress`.

`Analyzed` means every recorded section has been reviewed. Otherwise, the card displays `In progress`.

Opening a session restores its map and last position. Swiping a card to the left reveals deletion. Deleting removes the session, structure, and progress from Room, but never deletes the source audio file; the snackbar provides an immediate undo action.

## Persistence and export

Room, currently at schema version 5, stores:

- educational level and guidance intensity;
- original and edited structural maps;
- session, title, artist, media type, duration, and last position;
- local practice records and reviewed sections.

The song remains an external document. The app retains the read permission granted by Android. If the file is moved, deleted, or the permission is no longer valid, it must be selected again.

The PDF includes title, duration, sections, time ranges, states, bar estimates, approximation warnings, rhythm changes, and educational notes. Export is enabled only for a valid timeline.

## Technology

- Kotlin 2.4.0
- Jetpack Compose and Material 3 (Compose BOM 2026.06.00)
- MVVM with Android `ViewModel` and `StateFlow`
- Hilt 2.59.2
- Room 2.8.4 with KSP
- Media3 ExoPlayer 1.10.1
- Aallam `openai-kotlin` 4.1.0 and Ktor/OkHttp
- Gradle 9.5.1 and Android Gradle Plugin 9.2.1

## Architecture

- `ui`: Compose screens, immutable state, and coordination through `ActiveListeningViewModel`.
- `domain`: models, contracts, structure rules, use cases, and validators.
- `data`: Room, document import, ExoPlayer, remote guidance, and PDF generation.
- `di`: Hilt modules for the database, playback, repositories, and remote client.

The UI does not access DAOs, ExoPlayer, `PdfDocument`, or the OpenAI client directly. These dependencies are hidden behind domain contracts and repositories.

## Requirements

- Android Studio compatible with the current project stack.
- JDK 21 for Gradle.
- Android SDK with `compileSdk 37`.
- A device or emulator running Android 10 / API 29 or later.

Kotlin/Java bytecode targets Java 11 compatibility, while Gradle runs on JDK 21.

## Remote guidance configuration

The default configuration targets the DevExpert-compatible service. Add private values to `local.properties`, which must not be committed:

```properties
devexpert.apiKey=your_key
devexpert.guidanceModel=mimo-v2.5
devexpert.baseUrl=https://inference.devexpert.io/v1/
```

The `openai.apiKey` and `openai.guidanceModel` aliases and the `DEVEXPERT_API_KEY` environment variable are also supported. Without a key, the app continues to work with local guidance.

## Build and test

```bash
./gradlew compileDebugKotlin
./gradlew testDebugUnitTest
./gradlew compileDebugAndroidTestKotlin
./gradlew assembleDebug
```

The debug APK is generated under `app/build/outputs/apk/debug/`. Instrumented tests require a device or emulator.

To run the app from Android Studio, open the project folder, wait for Gradle sync to finish, and select a device or emulator running Android 10 / API 29 or later. If Gradle fails with a permissions error inside `~/.gradle`, check that Android Studio or the terminal can write to the user's Gradle cache and sync again.

Root and module-level `build/` directories are generated by Gradle. They should not be committed; if they appear as untracked files after a build, they can be removed with:

```bash
git clean -fd app/build data/build domain/build build
```

## Formats and limits

- Formats: MP3, WAV, M4A, and AAC.
- Maximum duration: 15 minutes.
- Export format: PDF.
- Minimum section duration: 5 seconds.
- Rhythm estimates are approximate.

## Current limitations

- The AI does not listen to or analyze the audio.
- Saved sessions cannot be manually renamed from the list.
- There is no waveform or automatic acoustic transition detection.
- PNG, JSON, CSV, MusicXML, and MIDI exports are not available yet.
