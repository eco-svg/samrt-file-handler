# Smart File Organizer and Duplicate Finder

A menu-driven console program written in core Java for the
*Introduction to Programming* course. It scans a folder, sorts the files
into category folders (Images, Documents, Music, ...) and finds files
whose content is identical even when their names differ.

It uses only what the course covers: no collections, no inheritance, no
external libraries. Lists are plain arrays and every array operation is
written by hand.

## Run

Requires JDK 8 or newer.

```sh
./run.sh
```

or manually:

```sh
javac -d out src/*.java
java -cp out Main
```

## Quick demo

1. Choose `10` to create the `demo_messy` folder (16 sample files, some identical).
2. Choose `1`, press Enter to accept `demo_messy`, answer `y` to include sub-folders.
3. `2` file list, `3` statistics, `9` folder tree.
4. `6` finds 3 duplicate groups; answer `y` to move the extra copies to `_Duplicates`.
5. `7` previews and then organizes the files by type.
6. `8` puts everything from the last move back.

## Features

| Menu | Feature | What it does |
|------|---------|--------------|
| 1 | Scan a folder | Reads every file (optionally inside sub-folders) into arrays |
| 2 | Show file list | Table of name, size, category and permission flags |
| 3 | Statistics report | Totals, average, bar chart per category, largest files |
| 4 | Search | Finds files whose name contains a keyword |
| 5 | Exclude a file | Removes one entry from the list (the file stays on disk) |
| 6 | Find duplicates | Groups identical files, shows wasted space, moves copies aside |
| 7 | Organize by type | Preview first, then moves files into category folders |
| 8 | Undo | Reverses the last organize or duplicate move |
| 9 | Folder tree | Indented tree, plus recursive vs iterative file count |
| 10 | Demo folder | Creates safe sample files to test with |

Safety: the program never deletes a file. Duplicates are moved to a
`_Duplicates` folder, organizing always shows a preview and asks first,
name clashes get a `_1`, `_2` suffix, and the last move can be undone.

## How duplicates are found

Checks run from cheapest to most expensive:

1. Sizes differ: not duplicates, skip.
2. Sizes match: compute a 64-bit FNV-1a hash of each file
   (`hash = (hash ^ byte) * prime`).
3. Hashes match: compare the two files byte by byte to be certain.

## Syllabus coverage

| Unit | Topic | Where to find it |
|------|-------|------------------|
| 1 | Data types, variables, constants | `final` constants at the top of every class; `int`, `long`, `double`, `char`, `boolean`, `byte` used throughout |
| 1 | Scanner input, System.out | `Main.readLine`, `readInt`, `readYesNo`; `print`, `println`, `printf` |
| 1 | Arithmetic operators | Average, percentage and bar length in `Main.showStatistics` |
| 1 | Relational and logical operators | Input validation in `Main.readInt`; `&&` short-circuit in `DuplicateFinder.find` |
| 1 | Bitwise operators | `^`, `&`, `>>>` in `DuplicateFinder.hashOf` and `toHex`; `\|`, `&`, `<<` flags in `Categorizer.flagsOf`; `<<` in `FileStore.grow` |
| 2 | if, else, nested if | `Main.scanFolder` |
| 2 | switch | Menu in `Main.main`; fall-through cases in `Categorizer.categoryOf` |
| 2 | for, while, do-while | Everywhere; `do-while` drives the menu; `while` in `Display.formatSize` |
| 2 | break, continue, infinite loop | `FileStore.indexesBySizeDescending`, `DuplicateFinder.find`, `while (true)` in `sameContent` |
| 2 | Nested loops | Duplicate comparison, bubble sort, patterns |
| 2 | Patterns | `Display.triangle`, `pyramid`, `diamond`, bar chart via `Display.bar` |
| 2 | Series | Sum of sizes and `1 + 2 + ... + (n-1)` checked against `n(n-1)/2` in `Main.showStatistics` |
| 3 | Array declaration, allocation | `FileStore` parallel arrays |
| 3 | Insertion | `FileStore.insert` and `grow` |
| 3 | Deletion | `FileStore.deleteAt` |
| 3 | Traversal | `Main.showFiles` |
| 3 | Searching | `FileStore.search`, `Categorizer.indexOfCategory` |
| 4 | String methods | `charAt`, `substring`, `concat`, `equals`, `equalsIgnoreCase`, `lastIndexOf`, `indexOf`, `toLowerCase`, `trim`, `length` in `Categorizer`, `Display.fit`, `Main` |
| 4 | Immutable strings | Explained in `Display.fit` |
| 4 | StringBuilder vs StringBuffer | `Organizer.freeTarget` (StringBuilder) and `Organizer.logSummary` (StringBuffer, with the difference explained) |
| 5 | Methods, return types, parameters | Every class |
| 5 | Overloading | `Display.line` (3 versions), `Display.formatSize` (2 versions) |
| 5 | Access modifiers | `public` API vs `private` helpers and the private move log in `Organizer` |
| 5 | Call by value | Explained in `Organizer.freeTarget` |
| 5 | Recursion | `FileStore.scan`, `printTree`, `countFilesRecursive` |
| 5 | Recursion vs iteration | `countFilesRecursive` vs `countFilesIterative`, timed in menu option 9 |

## Files

```
src/Main.java             menu, input, reports
src/FileStore.java        arrays, array operations, recursive scan
src/Categorizer.java      extension to category, bit flags
src/DuplicateFinder.java  hashing and duplicate grouping
src/Organizer.java        moving files, undo log
src/Display.java          patterns, lines, formatting
src/DemoMaker.java        creates the demo folder
```

## Limitations

- Undo remembers only the most recent move operation, and only while the
  program is still running.
- Hidden files (names starting with a dot) and symbolic links are skipped.
- Empty files are not reported as duplicates of each other.
- `try/catch` appears in three small places because Java requires it for
  reading and writing files; it is outside the syllabus and kept minimal.

## For the presentation

- `docs/PRESENTATION.md`: demo steps, what to say, likely questions with answers.
- `docs/EXPLANATION.md`: the code explained file by file.
- `docs/SAMPLE_OUTPUT.txt`: a recorded run of the full demo.
- `./reset_demo.sh`: deletes `demo_messy` so the demo starts clean.
