package eliasstar.jsonrpc.objects.id;

import java.io.IOException;
import java.util.function.Supplier;

import com.google.gson.stream.JsonWriter;

public interface Id<T> extends Supplier<T> {

    public void write(JsonWriter out) throws IOException;

}