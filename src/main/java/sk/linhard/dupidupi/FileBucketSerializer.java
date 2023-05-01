package sk.linhard.dupidupi;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class FileBucketSerializer extends StdSerializer<FileBucket> {

    public FileBucketSerializer(Class<FileBucket> t) {
        super(t);
    }

    @Override
    public void serialize(FileBucket fileBucket, JsonGenerator jgen, SerializerProvider serializerProvider) throws IOException {
        jgen.writeStartObject();
        jgen.writeNumberField("size", fileBucket.fileSize());
        jgen.writeArrayFieldStart("files");
        for (FileItem file : fileBucket.getFiles()) {
            jgen.writeString(file.path());
        }
        jgen.writeEndArray();
        jgen.writeEndObject();
    }
}
