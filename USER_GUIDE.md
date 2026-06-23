# Active Listening user guide

**English** | [Guía en español](./GUIA_DE_USO.md)

This guide explains, without technical detail, what each screen is for and what you can do during an active-listening session.

## What the app does

Active Listening helps you discover a song's structure yourself. Instead of giving you a closed answer, it proposes listening points so you can:

- recognize intros, verses, choruses, bridges, outros, and other parts;
- locate changes in rhythm, energy, instrumentation, or feel;
- mark and review transitions;
- build, save, and export a structural map.

Suggestions are editable hypotheses. The final decision always depends on your listening.

## Before you start

You need a local MP3, WAV, M4A, or AAC file up to 15 minutes long. The app only retains access to the selected document: it does not copy or delete the original audio.

## Start screen

The start screen contains:

- the button for importing a song;
- access to Settings;
- saved songs, if you have previously started any sessions.

Each saved song displays exactly three lines: title; artist and duration; and `Analyzed` or `In progress`.

## 1. Import a song

Tap `Import song` and choose a file from the Android picker. The app checks its format, whether it can be read, and whether it exceeds the duration limit.

When metadata is available, the title, artist, and artwork are displayed. If the artist is missing, the app shows `Unknown artist`.

Importing alone does not create a saved session. The session is saved when you start guided listening.

## 2. Complete a free first listen

Use the controls to play, pause, or seek through the song. Before editing, listen to a representative part and pay attention to:

- changes in energy;
- changes in rhythm or pulse;
- instruments entering or leaving;
- repetition and contrast;
- moments that sound like the beginning of a new idea.

You do not need to be right immediately. The goal is to form hypotheses.

## 3. Start guided listening

Tap `Start guided listening`. The app creates an initial proposal, starts saving the session, and shows a prompt for the current section.

Guided actions let you:

- `Confirm` a section;
- mark it as `Uncertain`;
- replay the previous eight seconds;
- skip to the next section.

Guidance can be local or remote. If no key is configured or the request fails, the workflow continues with local prompts. The AI does not listen to the audio: it only receives the title, duration, and initial markers.

## 4. Explore the timeline

The structural map scrolls horizontally. Swipe to explore songs that do not fit on screen. The red cursor shows the playback position.

Each block displays:

- section name;
- start and end time;
- duration;
- a `Suggested` or `Uncertain` badge when applicable;
- a `Rhythm change` indicator when marked.

Confirmed sections omit the state badge to keep the map cleaner.

## 5. Drag boundaries

The edges between blocks have vertical handles. Drag one to change a transition:

- the left block's end and the right block's start move together;
- a provisional timecode appears during the gesture;
- the change is saved when you release the handle;
- the app prevents sections shorter than five seconds.

The message under the map summarizes the interaction: `Swipe to explore. Use the edges to adjust.`

## 6. Edit section details

Tap a block to open its sheet. The sheet expands fully, scrolls, and keeps the active field above the software keyboard.

From the details sheet you can:

- change the musical label;
- select `Other` and enter a name such as `Pre-chorus`, `Solo`, or `Interlude`;
- cycle through `Suggested`, `Confirmed`, and `Uncertain`;
- enable or disable `Rhythm change`;
- enter start and end times;
- replay the section from its beginning;
- split at the playback position;
- merge with the previous or next section.

Time fields accept `mm:ss`. If you close the sheet after editing, the pending value is saved without reopening the sheet.

## 7. Rhythm changes and estimates

`Rhythm change` identifies a possible contrast in pulse, regularity, or feel. It may come from a suggestion or be marked manually. Enabling it in the details also displays it in the timeline block.

It is not a certainty. Check whether the contrast is truly rhythmic or related to feel, rather than only instrumentation, energy, or melody.

The bar estimate is calculated from section duration. It is an approximate listening aid and may be shown as low-confidence or irregular.

## 8. Split, merge, and restore

- `Split` creates two sections at the current position when both can retain the minimum duration.
- `Merge previous` and `Merge next` remove a shared boundary.
- `Restore original proposal` discards structural edits and returns to the initial map.

These actions are useful, but listen to the transition again after applying them.

## 9. Educational settings

Settings lets you select a level and guidance intensity.

Levels:

- `Introductory`
- `Intermediate`
- `Advanced`
- `Expert`

Section explanations change with the selected level. `Normal` intensity provides more context; `Reduced` simplifies the guidance and leaves more space for independent listening.

## 10. Saved songs

When you return to the start screen, you will find saved sessions. Each card shows:

1. a prominent title;
2. `artist · duration`;
3. `Analyzed` when every section has been reviewed, or `In progress` otherwise.

Tap the card or its arrow to continue. The map and last playback position are restored.

To delete a session, swipe its card to the left and tap the trash icon. This removes its session, map, and local progress, but not the audio file. Tap `Undo` in the snackbar to restore the deleted data.

## 11. Export to PDF

Once the timeline is valid, tap `Export map PDF` and choose a destination.

The PDF includes:

- song title and duration;
- sections and time ranges;
- each section's state;
- bar estimates and warnings;
- rhythm changes;
- educational notes adapted to the selected level.

## Labels and states

### Musical labels

- `Intro`: the beginning of the song.
- `Verse`: development or narrative material.
- `Chorus`: the main or most recognizable idea.
- `Bridge`: a contrasting or connecting section.
- `Outro`: the ending.
- `Other`: any custom name that better matches your analysis.

### States

- `Suggested`: an initial proposal waiting for review.
- `Confirmed`: you listened to it and it fits.
- `Uncertain`: it needs another listen or a later decision.

## Troubleshooting

- **The song cannot be imported:** check its format, duration, and file access.
- **Remote guidance does not appear:** the app can continue with local guidance.
- **A saved session does not open:** the file may have moved or been deleted; select it again.
- **A section cannot be split:** the position would leave a section shorter than five seconds.
- **The map cannot be exported:** check that the sections form a valid timeline.
- **The project does not start from Android Studio:** wait for Gradle sync to finish, select a compatible emulator or device, and check that Android Studio can write to `~/.gradle`.

## Recommended first workflow

1. Import a song.
2. Listen once without editing.
3. Start guided listening.
4. Confirm sections or mark uncertainty.
5. Drag one boundary and listen again.
6. Edit names or split only when needed.
7. Return later through saved songs.
8. Export the map when it represents what you hear.

## Key idea

Active Listening is not designed to give you a closed answer. Its value lies in listening, comparing, correcting, and trying again.
