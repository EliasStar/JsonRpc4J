/*
 * Copyright (C) 2020-2021 Elias*
 *
 * This file is part of JsonRpc4J.
 *
 * JsonRpc4J is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * JsonRpc4J is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JsonRpc4J. If not, see <https://www.gnu.org/licenses/>.
 */

package eliasstar.jsonrpc.gson;

import java.io.IOException;
import java.math.BigDecimal;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import eliasstar.jsonrpc.objects.id.Id;
import eliasstar.jsonrpc.objects.id.NullId;
import eliasstar.jsonrpc.objects.id.NumberId;
import eliasstar.jsonrpc.objects.id.StringId;

/**
 * Gson {@link TypeAdapter} for classes implementing {@link Id}.
 *
 * @author Elias*
 * @since 0.1.0
 */
final class IdTypeAdapter extends TypeAdapter<Id<?>> {

    /** The singleton instance. */
    private static IdTypeAdapter instance;

    /** Used once for singleton. */
    private IdTypeAdapter() {}

    /**
     * Getter for singleton instance.
     * <p>
     * The instance is lazyly instanciated, which means the the singleton instance
     * is created when this method is invoked for the first time. Any consecutive
     * calls return the instance.
     *
     * @return The only instance of {@link IdTypeAdapter}
     */
    static IdTypeAdapter instance() {
        if (instance == null)
            instance = new IdTypeAdapter();

        return instance;
    }

    /**
     * Writes one JSON value (an array, object, string, number, boolean or null) for
     * value.
     *
     * @param out The {@link JsonWriter} used as output
     * @param value The {@link Id} which is serialized
     * @throws IOException If serialization fails
     */
    @Override
    public void write(JsonWriter out, Id<?> value) throws IOException {
        value.write(out);
    }

    /**
     * Reads one JSON value (an array, object, string, number, boolean or null) and
     * converts it to an {@link Id}.
     *
     * @param in The {@link JsonReader} used as input
     * @return An {@link Id} containing the read value
     * @throws IOException If deserialization fails
     */
    @Override
    public Id<?> read(JsonReader in) throws IOException {
        switch (in.peek()) {
        case STRING:
            return new StringId(in.nextString());

        case NUMBER:
            return new NumberId(new BigDecimal(in.nextString()));

        case NULL:
            in.nextNull();
            return NullId.instance();

        default:
            throw new RuntimeException("Expected string, number or null, not " + in.peek());
        }
    }

}