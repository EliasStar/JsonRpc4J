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

package eliasstar.jsonrpc;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import eliasstar.jsonrpc.exceptions.ConnectionException;
import eliasstar.jsonrpc.exceptions.ErrorResponseException;
import eliasstar.jsonrpc.exceptions.IdMismatchException;
import eliasstar.jsonrpc.objects.Notification;
import eliasstar.jsonrpc.objects.Request;
import eliasstar.jsonrpc.objects.Response;
import eliasstar.utils.GsonProvider;
import eliasstar.utils.mocks.HttpClientMock;

public final class ConnectionTests {

    private static HttpClientMock client;
    private static Connection connection;
    private static Gson gson;

    @BeforeAll
    public static void initConnection() {
        client = new HttpClientMock();
        connection = new ConnectionBuilder(client, "https://www.example.com").build();
        gson = GsonProvider.gson();
    }

    @Test
    public void testRequestSending() throws ConnectionException {
        var id = connection.requestsMade();

        var req = new Request(id, "test");
        var res = "{\"jsonrpc\":\"2.0\",\"id\":" + id + ",\"result\":\"test\"}";
        client.setResponse(res);

        assertEquals(gson.fromJson(res, Response.class), connection.sendRequest(req).get());
        assertEquals(req, gson.fromJson(client.getRequest(), Request.class));
    }

    @Test
    public void testNotificationRequestSending() throws ConnectionException {
        var req = new Notification("test");
        client.setResponse("test");

        assertEquals(Optional.empty(), connection.sendRequest(req));
        assertEquals(req, gson.fromJson(client.getRequest(), Notification.class));
        assertEquals(req, gson.fromJson(client.getRequest(), Request.class));
    }

    @Test
    public void testConnectionWithId() throws ConnectionException, ErrorResponseException, IdMismatchException {
        var con = new ConnectionBuilder(client, "https://www.example.com").withId("test").build();

        client.setResponse("{\"jsonrpc\":\"2.0\",\"id\":\"test-0\",\"result\":\"test\"}");
        con.callRemoteProcedure("test");

        assertEquals(new Request("test-0", "test"), gson.fromJson(client.getRequest(), Request.class));
    }

    @RepeatedTest(3)
    public void testRequestIdIncrementation() throws ConnectionException, ErrorResponseException, IdMismatchException {
        var id = connection.requestsMade();

        client.setResponse("{\"jsonrpc\":\"2.0\",\"id\":" + id + ",\"result\":\"test\"}");
        connection.callRemoteProcedure("test");

        assertEquals(id + 1, connection.requestsMade());
    }

    @Test
    public void testRemoteProcedureCalling() {
        var cases = new Executable[] {
                () -> {
                    var id = connection.requestsMade();
                    client.setResponse("{\"jsonrpc\":\"2.0\",\"id\":" + id + ",\"result\":\"test\"}");
                    connection.callRemoteProcedure("test");
                    assertEquals(new Request(id, "test"), gson.fromJson(client.getRequest(), Request.class));
                },
                () -> {
                    var id = connection.requestsMade();
                    client.setResponse("{\"jsonrpc\":\"2.0\",\"id\":" + id + ",\"result\":\"test\"}");
                    connection.callRemoteProcedure("test", new JsonArray());
                    assertEquals(new Request(id, "test", new JsonArray()), gson.fromJson(client.getRequest(), Request.class));
                },
                () -> {
                    var id = connection.requestsMade();
                    client.setResponse("{\"jsonrpc\":\"2.0\",\"id\":" + id + ",\"result\":\"test\"}");
                    connection.callRemoteProcedure("test", new JsonObject());
                    assertEquals(new Request(id, "test", new JsonObject()), gson.fromJson(client.getRequest(), Request.class));
                }
        };

        assertAll(cases);
    }

    @Test
    public void testCorrectResponse() throws ConnectionException, ErrorResponseException, IdMismatchException {
        client.setResponse("{\"jsonrpc\":\"2.0\",\"id\":" + connection.requestsMade() + ",\"result\":\"test\"}");
        assertEquals("test", connection.callRemoteProcedure("test").getAsString());
    }

    @Test
    public void testResponseWithWrongId() {
        client.setResponse("{\"jsonrpc\":\"2.0\",\"id\":\"wrong\",\"result\":\"test\"}");

        assertThrows(IdMismatchException.class, () -> {
            connection.callRemoteProcedure("method");
        });
    }

    @Test
    public void testResponseWithError() {
        client.setResponse("{\"jsonrpc\":\"2.0\",\"id\":\"null\",\"error\":{\"code\":-32000,\"message\":\"test\"}}");

        assertThrows(ErrorResponseException.class, () -> {
            connection.callRemoteProcedure("method");
        });
    }

    @Test
    public void testNotificationSending() {
        client.setResponse("test");

        var cases = new Executable[] {
                () -> {
                    connection.sendNotification("test");
                    var json = gson.fromJson("{\"jsonrpc\":\"2.0\",\"method\":\"test\"}", JsonObject.class);
                    assertEquals(json, gson.fromJson(client.getRequest(), JsonObject.class));
                },
                () -> {
                    connection.sendNotification("test", new JsonArray());
                    var json = gson.fromJson("{\"jsonrpc\":\"2.0\",\"method\":\"test\",\"params\":[]}", JsonObject.class);
                    assertEquals(json, gson.fromJson(client.getRequest(), JsonObject.class));
                },
                () -> {
                    connection.sendNotification("test", new JsonObject());
                    var json = gson.fromJson("{\"jsonrpc\":\"2.0\",\"method\":\"test\",\"params\":{}}", JsonObject.class);
                    assertEquals(json, gson.fromJson(client.getRequest(), JsonObject.class));
                }
        };

        assertAll(cases);
    }

    @Test
    public void testBatchRequestSending() throws ConnectionException {
        var req = new Request[] {
                new Request("test1", "test1"),
                new Request("test2", "test2"),
                new Request("test3", "test3")
        };
        var res = "[{\"jsonrpc\":\"2.0\",\"id\":\"test1\",\"result\":\"test1\"},{\"jsonrpc\":\"2.0\",\"id\":\"test2\",\"result\":\"test2\"},{\"jsonrpc\":\"2.0\",\"id\":\"test3\",\"result\":\"test3\"}]";

        client.setResponse(res);

        assertTrue(Arrays.equals(gson.fromJson(res, Response[].class), connection.sendBatchRequest(req).get()));
        assertTrue(Arrays.equals(req, gson.fromJson(client.getRequest(), Request[].class)));
    }

    @Test
    public void testBatchNotificationSending() throws ConnectionException {
        client.setResponse("test");

        assertEquals(Optional.empty(), connection.sendBatchRequest(new Notification[] {
                new Notification("test1"),
                new Notification("test2"),
                new Notification("test3")
        }));

        assertEquals(Optional.empty(), connection.sendBatchRequest(new Request[] {
                new Notification("test1"),
                new Notification("test2"),
                new Notification("test3")
        }));
    }

}
