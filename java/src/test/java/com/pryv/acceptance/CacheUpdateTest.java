package com.pryv.acceptance;


import com.jayway.awaitility.Awaitility;
import com.pryv.Filter;
import com.pryv.SQLiteDBHelper;
import com.pryv.api.OnlineEventsAndStreamsManager;
import com.pryv.database.DBinitCallback;
import com.pryv.interfaces.EventsCallback;
import com.pryv.interfaces.GetEventsCallback;
import com.pryv.interfaces.GetStreamsCallback;
import com.pryv.interfaces.StreamsCallback;
import com.pryv.interfaces.UpdateCacheCallback;
import com.pryv.model.Event;
import com.pryv.model.Stream;
import com.pryv.utils.Logger;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import resources.TestCredentials;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CacheUpdateTest {

    private static Logger logger = Logger.getInstance();

    private static EventsCallback eventsCallback;
    private static GetEventsCallback getEventsCallback;
    private static StreamsCallback streamsCallback;
    private static GetStreamsCallback getStreamsCallback;

    private static UpdateCacheCallback updateCacheCallback;

    private static List<Event> apiEvents;
    private static List<Event> cacheEvents;
    private static Map<String, Stream> streams;
    private static Map<String, Stream> cacheStreams;

    private static Stream testSupportStream;

    private static String stoppedId;

    private static Event apiEvent;
    private static Event cacheEvent;
    private static Stream apiStream;
    private static Stream cacheStream;

    private static boolean cacheSuccess = false;
    private static boolean cacheError = false;
    private static boolean apiSuccess = false;
    private static boolean apiError = false;
    private static boolean updateSuccess = false;
    private static boolean updateError = false;

    private static OnlineEventsAndStreamsManager api;
    private static SQLiteDBHelper db;

    private static Filter scope;

    @BeforeClass
    public static void setUp() throws Exception {

        instanciateEventsCallback();
        instanciateGetEventsCallback();
        instanciateStreamsCallback();
        instanciateGetStreamsCallback();
        instanciateUpdateCacheCallback();

        String url = "https://" + TestCredentials.USERNAME + "." + TestCredentials.DOMAIN + "/";

        api = new OnlineEventsAndStreamsManager(url, TestCredentials.TOKEN, null);

        String streamId = "onlineModuleStreamID";
        testSupportStream = new Stream(streamId, "javaLibTestSupportStream");
        api.createStream(testSupportStream, streamsCallback);
        Awaitility.await().until(hasApiResult());
        assertFalse(apiError);

        String cacheFolder = "cache/test/";
        new File(cacheFolder).mkdirs();
        scope = new Filter();
        scope.addStream(testSupportStream);

        db = new SQLiteDBHelper(scope, cacheFolder, api, null, new DBinitCallback() {

            @Override
            public void onError(String message) {
                System.out.println(message);
            }
        });
    }

    @AfterClass
    public static void tearDown() throws Exception {
        api.deleteStream(testSupportStream, false, streamsCallback);
        Awaitility.await().until(hasApiResult());
        apiSuccess = false;
        api.deleteStream(testSupportStream, false, streamsCallback);
        Awaitility.await().until(hasApiResult());
    }

    @Before
    public void beforeEachTest() {
        apiEvents = null;
        cacheEvents = null;
        streams = null;
        cacheStreams = null;
        apiSuccess = false;
        apiError = false;
        cacheSuccess = false;
        cacheError = false;
        apiEvent = null;
        apiStream = null;
        stoppedId = null;
        updateSuccess = false;
        updateError = false;
    }

    @Test
    public void testUpdateCache() {

        Event newEvent = new Event(testSupportStream.getId(),
                "note/txt", "i am a test event");
        api.createEvent(newEvent, eventsCallback);
        Awaitility.await().until(hasApiResult());
        assertFalse(apiError);
        apiSuccess = false;
        newEvent = new Event(testSupportStream.getId(),
                "note/txt", "i am another test event");
        api.createEvent(newEvent, eventsCallback);
        Awaitility.await().until(hasApiResult());
        assertFalse(apiError);
        apiSuccess = false;

        api.getEvents(null, getEventsCallback);
        Awaitility.await().until(hasApiResult());
        assertFalse(apiError);
        apiSuccess = false;

        db.update(updateCacheCallback);
        Awaitility.await().until(hasUpdateResult());
        assertFalse(updateError);

        db.getEvents(null, getEventsCallback);
        Awaitility.await().until(hasCacheResult());
        assertFalse(cacheError);
        for (Event cacheEvent: cacheEvents) {
            boolean found = false;
            for (Event apiEvent: apiEvents) {
                if (apiEvent.getId().equals(cacheEvent.getId())) {
                    found = true;
                }
            }
            assertTrue(found);
        }
    }

    private static Callable<Boolean> hasUpdateResult() {
        return new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                return (updateSuccess || updateError);
            }
        };
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

    private static void instanciateUpdateCacheCallback() {
        updateCacheCallback = new UpdateCacheCallback() {
            @Override
            public void apiCallback(List<Event> events, Map<String, Double> eventDeletions,
                                    Map<String, Stream> streams,
                                    Map<String, Double> streamDeletions, Double serverTime) {
                updateSuccess = true;

            }

            @Override
            public void onError(String errorMessage, Double serverTime) {
                updateError = true;
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
            public void apiCallback(List<Event> events, Map<String, Double> eventDeletions,
                                    Double serverTime) {
                logger.log("apiCallback with " + events.size() + " events.");
                apiEvents = events;
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
                stoppedId = pStoppedId;
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
