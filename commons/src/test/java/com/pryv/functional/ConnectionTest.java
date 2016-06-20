package com.pryv.functional;


import com.jayway.awaitility.Awaitility;
import com.pryv.database.DBinitCallback;
import com.pryv.interfaces.EventsCallback;
import com.pryv.interfaces.GetEventsCallback;
import com.pryv.interfaces.GetStreamsCallback;
import com.pryv.interfaces.StreamsCallback;
import com.pryv.model.Event;
import com.pryv.model.Stream;
import com.pryv.utils.Logger;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import resources.TestCredentials;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ConnectionTest {

    private static Logger logger = Logger.getInstance();

    private static EventsCallback eventsCallback;
    private static GetEventsCallback getEventsCallback;
    private static StreamsCallback streamsCallback;
    private static GetStreamsCallback getStreamsCallback;

    private static List<Event> events;
    private static List<Event> cacheEvents;
    private static Map<String, Stream> streams;
    private static Map<String, Stream> cacheStreams;

    private static Stream testSupportStream;

    private static Event apiEvent;
    private static Event cacheEvent;
    private static Stream apiStream;
    private static Stream cacheStream;

    private static boolean cacheSuccess = false;
    private static boolean cacheError = false;
    private static boolean apiSuccess = false;
    private static boolean apiError = false;

    private static Connection connection;

    @BeforeClass
    public static void setup() {

        instanciateEventsCallback();
        instanciateGetEventsCallback();
        instanciateStreamsCallback();
        instanciateGetStreamsCallback();

        connection = new Connection(TestCredentials.USERNAME, TestCredentials.TOKEN,
                TestCredentials.DOMAIN, new DBinitCallback());

        connection.setupCacheScope(null);

        testSupportStream = new Stream("onlineModuleStreamID", "javaLibTestSupportStream");
        connection.streams.create(testSupportStream, streamsCallback);
        Awaitility.await().until(hasCacheResult());
        assertFalse(cacheError);
        Awaitility.await().until(hasApiResult());
        assertFalse(apiError);

        connection.streams.get(null, getStreamsCallback);
        Awaitility.await().until(hasApiResult());
        assertFalse(apiError);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.streams.delete(testSupportStream, false, streamsCallback);
        Awaitility.await().until(hasApiResult());
        apiSuccess = false;
        connection.streams.delete(testSupportStream, false, streamsCallback);
        Awaitility.await().until(hasApiResult());
    }

    @Before
    public void beforeEachTest() {
        events = null;
        cacheEvents = null;
        streams = null;
        cacheStreams = null;
        apiSuccess = false;
        apiError = false;
        cacheSuccess = false;
        cacheError = false;
        apiEvent = null;
        apiStream = null;
    }

    @Test
    public void testGetRootStreams() {
        Map<String, Stream> rootStreams = connection.getRootStreams();
        assertTrue(rootStreams.size() > 0);
    }

    private static Callable<Boolean> hasCacheResult() {
        return new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                return (cacheSuccess || cacheError);
            }
        };
    }

    private static Callable<Boolean> hasApiResult() {
        return new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                return (apiSuccess || apiError);
            }
        };
    }

    private static void instanciateGetEventsCallback() {
        getEventsCallback = new GetEventsCallback() {
            @Override
            public void cacheCallback(List<Event> events, Map<String, Double> eventDeletions) {
                logger.log("cacheCallback with " + events.size() + " events.");
                cacheEvents = events;
                cacheSuccess = true;
            }

            @Override
            public void onCacheError(String errorMessage) {
                logger.log(errorMessage);
                cacheError = true;
            }

            @Override
            public void apiCallback(List<Event> apiEvents, Map<String, Double> eventDeletions,
                                    Double serverTime) {
                logger.log("apiCallback with " + apiEvents.size() + " events.");
                events = apiEvents;
                apiSuccess = true;
            }

            @Override
            public void onApiError(String errorMessage, Double serverTime) {
                apiError = true;
                logger.log(errorMessage);
            }
        };
    }

    private static void instanciateEventsCallback() {
        eventsCallback = new EventsCallback() {

            @Override
            public void onApiSuccess(String successMessage, Event event, String pStoppedId,
                                     Double pServerTime) {
                System.out.println("OnlineEventsManagerTest: eventsSuccess msg: " + successMessage);
                apiSuccess = true;
                apiEvent = event;
            }

            @Override
            public void onApiError(String errorMessage, Double pServerTime) {
                apiError = true;
                logger.log(errorMessage);
            }

            @Override
            public void onCacheSuccess(String successMessage, Event event) {
                cacheSuccess = true;
                logger.log(successMessage);
            }

            @Override
            public void onCacheError(String errorMessage) {
                cacheError = true;
                logger.log(errorMessage);
            }
        };
    }

    private static void instanciateStreamsCallback() {
        streamsCallback = new StreamsCallback() {

            @Override
            public void onApiSuccess(String successMessage, Stream stream, Double pServerTime) {
                System.out.println("TestStreamsCallback: apiSuccess msg: " + successMessage);
                apiStream = stream;
                apiSuccess = true;
            }

            @Override
            public void onApiError(String errorMessage, Double pServerTime) {
                apiError = true;
                logger.log(errorMessage);
            }

            @Override
            public void onCacheSuccess(String successMessage, Stream stream) {
                cacheSuccess = true;
                cacheStream = stream;
                logger.log(successMessage);
            }

            @Override
            public void onCacheError(String errorMessage) {
                cacheError = true;
                logger.log(errorMessage);
            }
        };
    }

    private static void instanciateGetStreamsCallback() {
        getStreamsCallback = new GetStreamsCallback() {
            @Override
            public void cacheCallback(Map<String, Stream> streams, Map<String, Double> streamDeletions) {
                logger.log("cacheCallback with " + streams.size() + " streams.");
                cacheStreams = streams;
                cacheSuccess = true;
            }

            @Override
            public void onCacheError(String errorMessage) {
                logger.log(errorMessage);
                cacheError = true;
            }

            @Override
            public void apiCallback(Map<String, Stream> receivedStreams,
                                    Map<String, Double> streamDeletions, Double serverTime) {
                logger.log("apiCallback with " + receivedStreams.size() + " streams.");
                streams = receivedStreams;
                apiSuccess = true;
            }

            @Override
            public void onApiError(String errorMessage, Double serverTime) {
                apiError = true;
                logger.log(errorMessage);
            }
        };
    }
}
