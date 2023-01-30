package sk.linhard.dupidupi;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FileItemSizeSorterTest {

    @Test
    void testEquality() {

        var sorter1 = new FileItemSizeSorter();
        sorter1.accept(new FileItem("a", 10));
        sorter1.accept(new FileItem("b", 20));
        sorter1.accept(new FileItem("c", 10));
        sorter1.accept(new FileItem("d", 20));
        sorter1.accept(new FileItem("e", 30));

        var sorter2 = new FileItemSizeSorter();
        sorter2.accept(new FileItem("a", 10));
        sorter2.accept(new FileItem("c", 10));
        sorter2.accept(new FileItem("b", 20));
        sorter2.accept(new FileItem("e", 30));
        sorter2.accept(new FileItem("d", 20));

        assertThat(sorter1).isEqualTo(sorter2);
    }
}
