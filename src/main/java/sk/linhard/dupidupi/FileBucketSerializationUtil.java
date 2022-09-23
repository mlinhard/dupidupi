package sk.linhard.dupidupi;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

public class FileBucketSerializationUtil {

    public static void writeJsonl(String file, Iterable<FileBucket> buckets) {
        try (FileOutputStream fileOut = new FileOutputStream(file);
             SequenceWriter seq = writerMapper().writer()
                     .withRootValueSeparator("\n")
                     .writeValues(fileOut)) {
            for (FileBucket bucket : buckets) {
                seq.write(bucket);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static List<FileBucket> readJsonl(String filesReport) {
        File filesReportFile = new File(filesReport);
        if (!filesReportFile.exists()) {
            return null;
        }
        ImmutableList.Builder<FileBucket> listBuilder = ImmutableList.builder();
        try (FileInputStream fileIn = new FileInputStream(filesReportFile);
             MappingIterator<FileBucket> it = readerMapper().readerFor(FileBucket.class).readValues(fileIn)) {
            while (it.hasNextValue()) {
                listBuilder.add(it.nextValue());
            }
            return listBuilder.build();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static ObjectMapper readerMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(FileBucket.class, new FileBucketDeserializer(FileBucket.class));
        objectMapper.registerModule(module);
        return objectMapper;
    }

    private static ObjectMapper writerMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(FileBucket.class, new FileBucketSerializer(FileBucket.class));
        objectMapper.registerModule(module);
        return objectMapper;
    }
}
