# Code explanation

A plain-language walk through the whole program. Read this once with
the source files open beside it.

## 1. The big picture

The program has 7 files. Each file has one job.

| File | Job |
|------|-----|
| `Main.java` | Shows the menu, reads what you type, calls the other files |
| `FileStore.java` | Keeps the list of files in arrays; scans folders |
| `Categorizer.java` | Decides the category of a file from its extension |
| `DuplicateFinder.java` | Finds files with identical content |
| `Organizer.java` | Moves files into folders and remembers moves for undo |
| `Display.java` | Prints lines, titles, patterns and formats sizes |
| `DemoMaker.java` | Creates the sample folder for testing |

Every method is `static`, so they are called as `ClassName.method()`
without creating objects. Classes are used only as containers for
related methods, because objects are outside this course.

### What happens when you run it

```
main()  ->  prints pyramid and title
        ->  do { show menu, read choice, switch(choice) } while (choice != 0)
```

A typical session:

```
option 1   FileStore.scan()         files on disk  ->  arrays in memory
option 6   DuplicateFinder.find()   arrays  ->  groups of identical files
option 7   Organizer.organize()     arrays  ->  files moved on disk
option 8   Organizer.undo()         move log  ->  files moved back
```

## 2. Main.java

**The menu loop (line 30).** A `do-while` loop is used because the menu
must appear at least once before the condition is checked. Inside it a
`switch` picks the action. Each `case` ends with `break`, otherwise Java
would continue into the next case. `default` handles wrong numbers.

**Reading input (lines 110 to 143).** `Scanner input = new Scanner(System.in)`
reads from the keyboard. `readLine()` prints a prompt and returns the
typed line.

`readInt()` converts text to a number by hand:

```java
for (int i = 0; i < text.length(); i++) {
    char c = text.charAt(i);
    if (c < '0' || c > '9') return -1;      // not a digit
    value = value * 10 + (c - '0');
}
```

A `char` is stored as a number, so `'7' - '0'` is `7`. For "42": value
becomes 4, then 4 * 10 + 2 = 42. If you type `abc` it returns -1 and the
menu prints "Invalid choice" instead of crashing.

**scanFolder (line 156).** Shows nested `if / else if`: does the folder
exist, and if it does, is it really a folder. It also changes `~` to the
home folder using `charAt(0)`, `substring(1)` and `concat()`.

**showStatistics (line 227).** Three things to notice:

- Sum of a series: `totalBytes += FileStore.sizes[i]` inside a loop.
- `(double) totalBytes / n` is a type cast. Without it, dividing two
  whole numbers would throw away the decimal part.
- Sum of natural numbers: a loop adds 1 + 2 + ... + (n-1), and the
  result is checked against the formula n(n-1)/2. This number is how
  many pairs of files exist.

The bar chart scales each bar: `count * MAX_BAR / highest`, so the
biggest category always gets 30 characters.

## 3. FileStore.java

This is the data of the program. It uses three **parallel arrays**:

```
index      files[i]        sizes[i]    categories[i]
  0        report.txt        46        "Documents"
  1        photo.jpg         46        "Images"
```

`count` says how many slots are in use. The arrays may be longer than
`count`; the extra slots are empty.

**Insertion (line 45).** Put the new file at position `count`, then
`count++`. An array cannot change length, so when it is full `grow()`
(line 55) creates arrays of double the size and copies every element.
`files.length << 1` is a left shift by one bit, which is the same as
multiplying by 2.

**Deletion (line 76).** To remove index 2, every later element moves
one step left:

```
before:  A B C D E      remove C
after:   A B D E
```

**Searching (line 94).** Linear search: look at every file name, and
if it contains the keyword, store its index. `continue` skips to the
next file when there is no match. Both strings are changed to lower
case first so the search ignores capital letters.

**Sorting (line 112).** Bubble sort. It compares neighbours and swaps
them if they are in the wrong order; after each pass the smallest
remaining item has moved to the end. It sorts an array of *indexes*, so
the real arrays stay in their original order. If a pass makes no swap
the list is already sorted and `break` stops the loop early.

**Recursion (line 140).** This is the key method:

```java
public static void scan(File folder, boolean recursive) {
    File[] entries = folder.listFiles();
    for (each entry) {
        if (entry is a file)        insert(entry);
        else if (entry is a folder) scan(entry, true);   // calls itself
    }
}
```

A folder can contain folders, which contain more folders. The method
handles one folder and calls itself for each folder inside. The **base
case** is a folder with no sub-folders: the loop ends and the method
returns without another call.

**Recursion vs iteration (lines 175 and 195).** Two methods count the
files. `countFilesRecursive` calls itself. `countFilesIterative` uses a
`while` loop and its own array as a stack of folders still to visit.
Both give the same number.

**printTree (line 229).** Recursive as well. The `depth` parameter grows
by 1 with each call and decides how many `|   ` are printed, which
creates the indentation.

## 4. Categorizer.java

**extensionOf (line 31).** `lastIndexOf('.')` finds the last dot.
`substring(dot + 1)` takes everything after it. `toLowerCase()` makes
`JPG` and `jpg` equal. For `Photo.JPG` the answer is `jpg`.

**categoryOf (line 40).** A `switch` on a String. Several labels share
one `return`, called fall-through:

```java
case "jpg":
case "jpeg":
case "png":
    return "Images";
```

This is much shorter than a long `if ... else if` chain, which is the
usual reason to choose `switch`.

**Flags (lines 112 and 130).** Four yes/no facts are stored in one `int`,
one bit each:

```
READ    = 1        0001
WRITE   = 1 << 1   0010
EXECUTE = 1 << 2   0100
BIG     = 1 << 3   1000
```

`flags | FLAG_WRITE` switches the write bit on (OR).
`(flags & FLAG_WRITE) != 0` tests whether it is on (AND).
A readable and writable file is `0011`, shown as `rw--`.

## 5. DuplicateFinder.java

**The idea.** Reading files is slow, so the cheap check comes first:

1. Sizes differ: not duplicates. No file is read.
2. Sizes match: compute a hash of each file.
3. Hashes match: compare byte by byte to be certain.

**hashOf (line 39).** A hash is a fingerprint: one number made from all
the bytes of the file. The method used is called FNV-1a:

```java
hash = hash ^ (buffer[i] & 0xff);   // XOR the byte in
hash = hash * FNV_PRIME;            // multiply to spread the bits
```

- `^` is XOR: a result bit is 1 when the two bits are different.
- `& 0xff` is needed because a Java `byte` is -128 to 127; the mask
  makes it 0 to 255.
- The file is read 8192 bytes at a time into `buffer`, which is faster
  than one byte at a time.

**toHex (line 61).** Prints the 64-bit hash as 16 hex characters. One hex
digit is 4 bits:

```java
int digit = (int) ((value >>> shift) & 0xF);
```

`>>>` moves the wanted 4 bits to the right end. `& 0xF` (binary 1111)
removes everything else. The digit is then looked up with
`HEX_DIGITS.charAt(digit)`.

**sameContent (line 71).** A `while (true)` loop, which is an infinite
loop that is left with `return`. It reads one byte from each file. A
difference returns `false`; reaching the end of both returns `true`.

**find (line 90).** Nested loops compare file `i` with every later file
`j`. Starting `j` at `i + 1` means no pair is checked twice.

- `groupOf[i]` holds the group number of each file, or -1 for unique.
- `continue` skips files already in a group, empty files, and pairs
  with different sizes.
- `hashed[i]` remembers that a hash has been calculated, so each file is
  read at most once.
- In `hashes[i] == hashes[j] && sameContent(...)` the `&&` is
  short-circuit: the slow byte comparison runs only if the hashes match.

**moveExtraCopies (line 174).** The first file of each group stays; the
rest are moved to `_Duplicates`. The `keptOne` boolean array remembers
which groups already kept a file.

## 6. Organizer.java

**The move log (lines 17 to 19).** Two `private` arrays: `logFrom[i]` was
moved to `logTo[i]`. `private` means only this class can change them;
other classes use the public methods. That is what access modifiers are
for.

**freeTarget (line 40).** If `Music/song.mp3` already exists, a `while`
loop tries `song_1.mp3`, `song_2.mp3` and so on until a free name is
found. The name is built with `StringBuilder`:

```java
StringBuilder name = new StringBuilder(base);
name.append('_').append(number).append(ext);
```

**preview and organize (lines 87 and 101).** Both loop over the files and
skip any that are already in the correct folder. `preview` only prints.
`organize` calls `moveFile`, which uses `File.renameTo()` to move the
file and then writes the move into the log.

**undo (line 122).** A `for` loop that counts backwards
(`i = logCount - 1; i >= 0; i--`) so the last move is reversed first. It
then tries to delete the category folder, which only succeeds if the
folder is empty.

**logSummary (line 149).** Uses `StringBuffer`. It works like
StringBuilder but is synchronized (thread safe) and a little slower.

## 7. Display.java

**Overloading (lines 22 to 34).** Three methods named `line`:

```java
line()            // 64 dashes
line('=')         // 64 of the given character
line('*', 10)     // 10 of the given character
```

The short versions call the long one, so the loop is written once.

**Patterns.** All use nested loops: the outer loop is the row, the inner
loops print spaces and stars.

- `triangle` (line 59): row `i` prints `i` stars, counting down.
- `pyramid` (line 77): row `i` has `rows - i` spaces and `2*i - 1` stars.
  For 4 rows: 1, 3, 5, 7 stars.
- `diamond` (line 90): a pyramid, then the same loop counting down.

**formatSize (line 115).** A `while` loop divides by 1024 until the value
is below 1024, counting how many times. That count is the index into
`{ "B", "KB", "MB", "GB", "TB" }`.

**fit (line 140).** Makes text an exact width for table columns. Long
text is cut with `substring` and `concat("..")`. Short text is padded
with spaces. Strings are **immutable**: these methods return a new
String and the original is unchanged.

## 8. DemoMaker.java

Two parallel arrays, `NAMES` and `CONTENTS`. A loop writes each file
with `FileWriter`. The same text constant (`REPORT`, `PHOTO`, `SONG`) is
used for several names, which is how the demo duplicates are made.

## 9. Things used from the Java library

| Used | For |
|------|-----|
| `java.util.Scanner` | Reading keyboard input |
| `java.io.File` | Listing folders, file size, moving with `renameTo`, `mkdirs` |
| `java.io.FileInputStream`, `BufferedInputStream` | Reading file bytes for hashing and comparing |
| `java.io.FileWriter` | Writing the demo files |
| `String`, `StringBuilder`, `StringBuffer` | Text handling |
| `System.nanoTime()` | Timing recursion against iteration |
| `java.util.Arrays.sort` | Alphabetical order in the folder tree only |

Nothing else: no ArrayList, no HashMap, no external libraries.

## 10. Words to know

| Word | Meaning |
|------|---------|
| Hash | A number calculated from a file's content, used as a fingerprint |
| Collision | Two different files getting the same hash (very rare) |
| Parallel arrays | Several arrays where the same index describes the same item |
| Base case | The situation where a recursive method stops calling itself |
| Fall-through | Several `case` labels sharing one block of code |
| Short-circuit | `&&` not evaluating the right side when the left is false |
| Immutable | Cannot be changed after it is created |
| Overloading | Same method name, different parameters |
