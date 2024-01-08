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

package eliasstar.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public final class OptionalTypeAdapterTests {

    private static Gson gson;
    private static Gson gsonWithNulls;

    @BeforeAll
    public static void initGson() {
        gson = new GsonBuilder().registerTypeAdapterFactory(OptionalTypeAdapterFactory.instance()).create();
        gsonWithNulls = gson.newBuilder().serializeNulls().create();
    }

    @Test
    public void testOptionalSerialization() {
        var testObj = new OptionalTestObject();
        assertEquals("{}", gson.toJson(testObj));
        assertEquals("{\"testString\":null}", gsonWithNulls.toJson(testObj));

        testObj.testOptional = Optional.of("test");
        assertEquals("{\"testOptional\":\"test\"}", gson.toJson(testObj));
        assertEquals(gsonWithNulls.fromJson("{\"testString\":null,\"testOptional\":\"test\"}", JsonObject.class), gsonWithNulls.fromJson(gsonWithNulls.toJson(testObj), JsonObject.class));

        testObj.testString = "test";
        assertEquals(gson.fromJson("{\"testString\":\"test\",\"testOptional\":\"test\"}", JsonObject.class), gson.fromJson(gson.toJson(testObj), JsonObject.class));
        assertEquals(gsonWithNulls.fromJson("{\"testString\":\"test\",\"testOptional\":\"test\"}", JsonObject.class), gsonWithNulls.fromJson(gsonWithNulls.toJson(testObj), JsonObject.class));

        testObj.testOptional = Optional.empty();
        assertEquals("{\"testString\":\"test\"}", gson.toJson(testObj));
        assertEquals("{\"testString\":\"test\"}", gsonWithNulls.toJson(testObj));
    }

    @Test
    public void testOptionalDeserialization() {
        var testObj = new OptionalTestObject();
        assertEquals(testObj, gson.fromJson("{}", OptionalTestObject.class));

        testObj.testOptional = Optional.of("test");
        assertEquals(testObj, gson.fromJson("{\"testOptional\":\"test\"}", OptionalTestObject.class));

        testObj.testString = "test";
        assertEquals(testObj, gson.fromJson("{\"testString\":\"test\",\"testOptional\":\"test\"}", OptionalTestObject.class));

        testObj.testOptional = Optional.empty();
        assertEquals(testObj, gson.fromJson("{\"testString\":\"test\"}", OptionalTestObject.class));
    }

    private static final class OptionalTestObject {

        private String testString;
        private Optional<String> testOptional;

        private OptionalTestObject() {
            testOptional = Optional.empty();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj != null && obj instanceof OptionalTestObject) {
                var other = (OptionalTestObject) obj;

                if (this == other)
                    return true;

                if (testString == null)
                    return other.testString == null && testOptional.equals(other.testOptional);

                return testString.equals(other.testString) && testOptional.equals(other.testOptional);
            }

            return false;
        }
    }

}
