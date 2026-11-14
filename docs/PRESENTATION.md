# Presentation guide

Everything you need for the demo: what to type, what to say, and the
questions you are likely to be asked.

## Before you present

```sh
cd /home/eco-svg/warehouse2/iop
./reset_demo.sh      # start from a clean state
./run.sh             # compiles and starts the program
```

Open a second window showing the `demo_messy` folder in the file
manager. When you organize the files, the audience can watch the
folders appear. That is the strongest proof that it works.

If the laptop or Java fails on the day, open `docs/SAMPLE_OUTPUT.txt`.
It is a real recorded run of the full demo below.

## One-line introduction

"This is a console program in core Java that cleans up a messy folder.
It sorts files into folders by type and finds duplicate files by
comparing their content, not their names. It uses only the topics from
our syllabus: arrays, strings, loops, methods and recursion."

## Demo steps (about 4 minutes)

| Step | Type | What to say |
|------|------|-------------|
| 1 | `10` | "Option 10 creates a test folder with 16 sample files, so I never risk real files. Some of them are copies of each other with different names." |
| 2 | `1`, Enter, `y` | "The scan reads every file into arrays. I said yes to sub-folders, so it uses recursion to go inside the `old` folder." |
| 3 | `2` | "This is array traversal. Each row shows the size, the category decided from the extension, and permission flags stored as bits." |
| 4 | `3` | "The report adds all sizes, draws a bar chart with nested loops, and sorts the largest files with bubble sort. The last line is a series: 1 + 2 + ... + 15 = 120, which matches the formula n(n-1)/2." |
| 5 | `4`, `song` | "Linear search through the array. It found two files called song.mp3 in different folders." |
| 6 | `9` | "The folder tree is printed by a recursive method. Below it I count the files twice, once with recursion and once with a loop, to compare the two." |
| 7 | `6`, `y` | "It found 3 groups of duplicates. report_copy.txt has a different name from report.txt but identical content. The copies are moved to a _Duplicates folder. Nothing is deleted." |
| 8 | `7`, `y` | "First it shows a preview, and only moves after I confirm." Now show the file manager window. |
| 9 | `9` | "The same tree again. Every file is now inside its category folder." |
| 10 | `8`, `y` | "Undo walks the move log backwards and puts every file back." |
| 11 | `abc` | "Wrong input does not crash the program. I check each character myself." |
| 12 | `0` | "The exit screen prints a diamond pattern." |

Step 10 undoes only the organize step (the most recent move), so the
four copies stay in `_Duplicates`. That is expected.

## Which syllabus topic is where (say this while showing the code)

| Topic | Open this | Point at |
|-------|-----------|----------|
| Scanner, System.out | `Main.java` line 110 | `readLine`, `readInt` |
| do-while and switch | `Main.java` line 30 | the menu loop |
| Nested if-else | `Main.java` line 156 | `scanFolder` |
| Series | `Main.java` line 227 | end of `showStatistics` |
| Arrays: insert, delete, search | `FileStore.java` lines 45, 76, 94 | `insert`, `deleteAt`, `search` |
| Recursion | `FileStore.java` line 140 | `scan` calls itself |
| Recursion vs iteration | `FileStore.java` lines 175, 195 | the two count methods |
| switch with fall-through | `Categorizer.java` line 40 | `categoryOf` |
| Bitwise operators | `DuplicateFinder.java` lines 39, 61 | `hashOf`, `toHex` |
| Nested loops, break, continue | `DuplicateFinder.java` line 90 | `find` |
| StringBuilder, StringBuffer | `Organizer.java` lines 40, 149 | `freeTarget`, `logSummary` |
| Patterns, overloading | `Display.java` lines 22 to 90 | `line`, `triangle`, `pyramid`, `diamond` |

## Likely questions and answers

**Why did you use arrays and not ArrayList?**
ArrayList is not in our syllabus, and writing insertion, deletion and
searching by hand shows that I understand how they work. When the array
is full, `grow()` makes a new array of double the size and copies the
elements across.

**What are parallel arrays?**
Three arrays where the same index describes the same file: `files[i]`,
`sizes[i]` and `categories[i]`. We have not studied classes and objects
yet, so this is the way to keep several facts about one item.

**How do you know two files are duplicates?**
Three checks, cheapest first. If the sizes differ they cannot be
duplicates. If the sizes match, I compute a hash of each file. If the
hashes match, I compare them byte by byte to be fully sure.

**What is a hash?**
A fingerprint of the file: one 64-bit number calculated from all its
bytes. Same content always gives the same number. Different content
almost always gives a different number.

**Why compare byte by byte if the hashes already match?**
Two different files can, very rarely, get the same hash. That is called
a collision. The final byte comparison removes that risk.

**Where are bitwise operators used?**
In the hash: `hash ^ byte` is XOR. In `toHex`: `>>>` shifts 4 bits to the
right end and `& 0xF` keeps only those 4 bits. In the flags: `|` switches
a bit on, `&` tests whether it is on. In `grow`: `<< 1` doubles a number.

**Why `buffer[i] & 0xff`?**
A Java `byte` is signed, from -128 to 127. `& 0xff` turns it into 0 to
255 so negative bytes do not disturb the hash.

**Where is recursion used and what is the base case?**
`scan()` calls itself for every sub-folder. The base case is a folder
that has no sub-folders: the loop finishes without another call.

**Recursion or iteration, which is better?**
Recursion is shorter and matches the shape of folders inside folders,
but every call uses stack memory, and very deep nesting can cause a
StackOverflowError. Iteration needs me to manage my own stack array,
which is more code, but it has no such limit. Both give the same answer,
which option 9 shows.

**What is method overloading here?**
`Display.line()`, `line(char)` and `line(char, int)` have the same name
and different parameters. Java chooses by the arguments. `formatSize` is
overloaded as well.

**StringBuilder vs StringBuffer?**
Both are changeable strings. StringBuffer is synchronized, which makes
it safe for several threads and a little slower. StringBuilder is not
synchronized and is faster. My program has one thread, so StringBuilder
is the right choice; I used StringBuffer once to show they work alike.

**What does "strings are immutable" mean?**
A String can never change after it is created. `substring()` and
`concat()` return a new String. That is why joining text in a loop is
done with StringBuilder.

**What is call by value?**
Java passes a copy of the argument. Changing the parameter inside the
method does not change the caller's variable.

**Why is there try/catch? It is not in the syllabus.**
Java does not compile file reading or writing code without it. It is in
three small places only, and it just reports the problem instead of
crashing.

**What happens if two files have the same name when organizing?**
`freeTarget()` adds a number: `song.mp3` becomes `song_1.mp3`. No file
is ever overwritten.

**Why does the program not delete duplicates?**
Deleting cannot be undone. Moving them to `_Duplicates` lets the user
check them first and delete them personally.

**What is the time complexity of duplicate finding?**
In the worst case every file is compared with every other file, which
is n(n-1)/2 pairs, so O(n²). The size check makes most pairs very cheap,
because a file is only read when another file has the same size.

**Why bubble sort?**
It is the simplest sort that uses nested loops, and it is fast enough
for a folder listing. It stops early if a pass makes no swap.

**What are the limitations?**
Undo remembers only the last move and only while the program runs.
Hidden files and symbolic links are skipped. Empty files are not counted
as duplicates.

**How could it be improved?**
A graphical interface, saving the undo log to a file, organizing by date
or size, and letting the user add custom categories.
