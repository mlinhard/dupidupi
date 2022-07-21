package sk.linhard.dupidupi;

public class SizeBucketSorter {

    /*
    * How to build the dedup algorithm
    *
    * For size 1 size-buckets convert them to unique buckets (with long ids)
    * For size >1 size-buckets put each through size-bucket sorter
    *
    * Read each size-bucket member byte-by-byte and sort them into prefix-buckets
    * Recursively sort prefix-buckets until they are size-1 and can be converted to unique bucket
    *
    * */

}
