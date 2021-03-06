package com.pryv.acceptance;


import com.pryv.Connection;
import com.pryv.exceptions.ApiException;
import com.pryv.model.Stream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import resources.TestCredentials;

import static org.junit.Assert.assertTrue;

public class ConnectionTest {

    private static Stream testSupportStream;

    private static Connection connection;

    @BeforeClass
    public static void setup() throws IOException, ApiException {

        connection = new Connection(TestCredentials.USERNAME, TestCredentials.TOKEN, TestCredentials.DOMAIN);

        testSupportStream = new Stream("onlineModuleStreamID", "javaLibTestSupportStream");
        connection.streams.create(testSupportStream);

        Map<String, Stream> retrievedStream = connection.streams.get(null);
        assertTrue(retrievedStream.size() > 0);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.streams.delete(testSupportStream, false);
        connection.streams.delete(testSupportStream, false);
    }

    @Test
    public void testGetRootStreams() {
        Map<String, Stream> rootStreams = connection.getRootStreams();
        assertTrue(rootStreams.size() > 0);
    }

}
