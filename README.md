# Dupi-Dupi filesystem de-duplicator

File deduplication tool that uses byte-to-byte comparison to detect duplicate file in a set of file system subtrees.

Run in an empty folder:

```bash
$ echo hello > hello1.txt
$ echo hello > hello2.txt
$ echo world > world1.txt
$ echo world > world2.txt
$
$ dupidupi .
09:55:52.260 [main] INFO sk.linhard.dupidupi.Main - Deduplicating files in
   .
09:55:52.265 [main] INFO sk.linhard.dupidupi.Main - Found 4 files with 1 different sizes
09:55:52.280 [main] INFO sk.linhard.dupidupi.Deduper - Sorting size-6 bucket with 4 files (1/1)
09:55:52.288 [main] INFO sk.linhard.dupidupi.Main - Done sorting. Found 2 duplicates in 2 duplicate sets, total 12 bytes duplicated
09:55:52.288 [main] INFO sk.linhard.dupidupi.Main - Generating duplicate report to output/report.tsv.gz
$
$ gunzip -c output/report.tsv.gz 
0	6	./world1.txt
0	6	./world2.txt
1	6	./hello1.txt
1	6	./hello2.txt
```

The gzipped TSV output (default output format) shows duplicate sets in form `<bucketID> <fileSize> <path>`, i.e.
all files having the same `<bucketID>` are duplicates of given `<fileSize>` and can be found on given `<path>`.

## Dupi-Dupi deduplication algorithm

When discriminating whether two files are equal by content Dupi-dupi first looks at their size. When sizes aren't equal
then the files aren't equal. This is an important property that allows us to sort files to classes with different file
sizes where we can be sure that a file from one size-class is definitely different from file from another size-class. If
a size class contains a single file, this means that this file is unique - it doesn't have duplicates.

If there are multiple

Let's say we have following set of text files

| File   | Size | Content |
|--------|------|---------|
| 01.txt | 0    | ''      |
| 02.txt | 0    | ''      |
| 03.txt | 1    | 'a'     |
| 04.txt | 1    | 'a'     |
| 05.txt | 1    | 'b'     |
| 06.txt | 2    | 'ab'    |
| 07.txt | 2    | 'ac'    |
| 08.txt | 2    | 'bc'    |
| 09.txt | 2    | 'bc'    |
| 10.txt | 3    | 'abc'   |

With duplicates

- 01.txt - 02.txt
- 03.txt - 04.txt
- 08.txt - 09.txt

After file-size sort, we'll have following size-buckets:

| Size | Files                          |
|------|--------------------------------|
| 0    | 01.txt, 02.txt                 |
| 1    | 03.txt, 04.txt, 05.txt         |
| 2    | 06.txt, 07.txt, 08.txt, 09.txt |
| 3    | 10.txt                         |

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
| 1    | a        | 03.txt, 04.txt | <- duplicates |
|      | b        | 05.txt         | <- unique     |
| 2    | a        | 06.txt, 07.txt |               |
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
| 2    | ab       | 06.txt         | <- unique     |
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