# Dupi-Dupi filesystem de-duplicator

File deduplication tool that uses byte-to-byte comparison to detect duplicate file in a set of file system subtrees.

## Dupi-Dupi deduplication algorithm

When discriminating whether two files are equal by content Dupi-dupi first looks at their size. When sizes aren't equal
then the files aren't equal. This is an important property that allows us to sort files to classes with different file
sizes where we can be sure that a file from one size-class is definitely different from file from another size-class. If
a size class contains a single file, this means that this file is unique - it doesn't have duplicates.

If there are multiple

Let's say we have following set of text files

| File   | Size | Content |
|--------|------|---------|
| 01.txt |    0 | ''      |
| 02.txt |    0 | ''      |
| 03.txt |    1 | 'a'     |
| 04.txt |    1 | 'a'     |
| 05.txt |    1 | 'b'     |
| 06.txt |    2 | 'ab'    |
| 07.txt |    2 | 'ac'    |
| 08.txt |    2 | 'bc'    |
| 09.txt |    2 | 'bc'    |
| 10.txt |    3 | 'abc'   |

With duplicates

- 01.txt - 02.txt
- 03.txt - 04.txt
- 08.txt - 09.txt

After file-size sort, we'll have following size-buckets:

| Size | Files                          |
|------|--------------------------------|
|    0 | 01.txt, 02.txt                 |
|    1 | 03.txt, 04.txt, 05.txt         |
|    2 | 06.txt, 07.txt, 08.txt, 09.txt |
|    3 | 10.txt                         |

From this first sort we already have some results:

- All size-0 files are trivial duplicates.
- All size buckets with single member represent unique files

So we add size-0 objects to list of duplicates, prune the singleton buckets and search further inside of size 1 and 2
buckets

| # | Duplicate set  |
|---|----------------|
| 0 | 01.txt, 02.txt |

In these left-over buckets, we'll look at the prefixes of length 1

| Size | Prefix-1 | Files          | Note          |
|------|----------|----------------|---------------|
|    1 | a        | 03.txt, 04.txt | <- duplicates |
|      | b        | 05.txt         | <- unique     |
|    2 | a        | 06.txt, 07.txt |               |
|      | b        | 08.txt, 09.txt |               |

Here again we found one pair of duplicates 03.txt-04.txt because actually by seeing prefix of length 1 we saw the whole
file length and also we have another unique file 05.txt to prune

| # | Duplicate set  |
|---|----------------|
| 0 | 01.txt, 02.txt |
| 1 | 03.txt, 04.txt |

By sorting the bucket by the next byte, we're done:

| Size | Prefix-2 | Files          | Note          |
|------|----------|----------------|---------------|
|    2 | ab       | 06.txt         | <- unique     |
|      | ac       | 07.txt         | <- unique     |
|      | bc       | 08.txt, 09.txt | <- duplicates |

Result

| # | Duplicate set  |
|---|----------------|
| 0 | 01.txt, 02.txt |
| 1 | 03.txt, 04.txt |
| 2 | 08.txt, 09.txt |

## Resuming interrupted deduplication process

When deduplicating a lot of data it may be handy to be able to restore the deduplication process from a saved
checkpoint. At this point only the size bucket checkpoints are supported. The restore feature is enabled in config file
by setting `resumable` field to `true`.

For this purpose the process will create three files in the output directory

- walk.tsv.gz - Walk file - information about all files we're considering
- progress-log.tsv - Progress log - stores intermediate deduplication progress
- progress-log.tsv.processing - A copy of progress log that is currently being processed by restore mechanism

## Reporting duplicates from the point of deletability

The deletability report shows files/folders that are possible candidates for deletion

From the point of deduplication all files on the file system can be divided into three categories

- **unique** - there is no other file with the same content
- **original** - duplicated but on path that we consider original and we will be keeping
- **duplicate** - duplicate of an original

It shows table with two columns: Original path and Duplicate path, these are basically interchangeable based on user
preference, the initial original is picked as first by lexicographic order.

Let's say we have duplicate sets

1   10 /home/a/a.txt
1   10 /home/b/a.txt
2   15 /home/a/b.txt
2   15 /home/b/b.txt
3   20 /home/a/c.txt
3   20 /home/b/c.txt
4   25 /home/c/d.txt
4   25 /home/c/e.txt

We'll display table

| Type | Original     | Duplicate     | Completeness | Size         |
|----- |--------------|---------------|--------------|--------------|
| Dir  |/home/a       | /home/b       | 3/3    100 % |           45 |
| File |/home/c/d.txt | /home/c/e.txt |              |           25 |


For deletability report, we work not just with the set of files (paths), but also directories that contain them.
I.e. transitive closure on the set of paths with relation parent(p1, p2)

on this set we define a graph, where the paths (including the ancestors) are the nodes
and the edge exists between two file paths if 

- they are both files and they are duplicates of each other
- they are both directories A, B and they are duplicates of each other, i.e. 
  - A and B have equal number of direct children 
  - for each file f1 from A (A is direct parent of f1), there is a file f2 from B (B is direct parent of f2) such that f1 and f2 are duplicates
  - for each directory d1 from A (A is direct parent of d1), there is a directory d2 from B (B is direct parent of d2) such that d1 and d2 are duplicates

