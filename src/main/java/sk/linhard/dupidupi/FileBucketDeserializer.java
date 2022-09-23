package sk.linhard.dupidupi;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.collect.ImmutableList;

import java.io.IOException;

public class FileBucketDeserializer extends StdDeserializer<FileBucket> {

    public FileBucketDeserializer(Class<FileBucket> t) {
        super(t);
    }

    @Override
    public FileBucket deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        long size = node.get("size").longValue();
        ImmutableList.Builder<FileItem> listBuilder = ImmutableList.builder();
        for (JsonNode item : node.get("files")) {
            listBuilder.add(new FileItem(item.asText(), size));
        }
        return new ImmutableFileBucket(listBuilder.build());
    }
}
