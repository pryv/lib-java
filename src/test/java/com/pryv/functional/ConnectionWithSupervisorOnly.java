package com.pryv.functional;

import com.jayway.awaitility.Awaitility;
import com.pryv.ConnectionOld;
import com.pryv.Pryv;
import com.pryv.api.EventsCallback;
import com.pryv.interfaces.EventsManager;
import com.pryv.interfaces.StreamsCallback;
import com.pryv.interfaces.StreamsManager;
import com.pryv.api.database.DBinitCallback;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.Callable;

import resources.TestCredentials;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class ConnectionWithSupervisorOnly {

    private static EventsCallback eventsCallback;
    private static StreamsCallback streamsCallback;

    private static Map<String, Event> events;
    private static Map<String, Stream> streams;

    private static Integer stoppedId;

    private static Stream testSupportStream;

    private static Event singleEvent;
    private static Stream singleStream;

    private static boolean success = false;
    private static boolean error = false;

    private static EventsManager eventsManager;
    private static StreamsManager streamsManager;

    @BeforeClass
    public static void setUp() throws Exception {

        instanciateEventsCallback();
        instanciateStreamsCallback();
        Pryv.deactivateOnline();
        Pryv.deactivateCache();

        ConnectionOld pryvConnection =
                new ConnectionOld(TestCredentials.USERNAME, TestCredentials.TOKEN,
                        new DBinitCallback() {
                            @Override
                            public void onError(String message) {
                                System.out.println("DB init Error: " + message);
                            }
                        });

        eventsManager = (EventsManager) pryvConnection;
        streamsManager = (StreamsManager) pryvConnection;

        testSupportStream = new Stream("onlineModuleStreamID", "javaLibTestSupportStream");
        streamsManager.create(testSupportStream, streamsCallback);
        Awaitility.await().until(hasResult());
        assertFalse(error);
    }

    @Before
    public void beforeEachTest() {
        events = null;
        streams = null;
        success = false;
        error = false;
        singleEvent = null;
        singleStream = null;
        stoppedId = null;
    }

    @Test
    public void testCreateEvent() {
        Event event = new Event(testSupportStream.getId(), null, "note/txt", "hi");
        eventsManager.create(event, eventsCallback);
        assertNotNull(event.getClientId());
    }

    private static Callable<Boolean> hasResult() {
        return new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                return (success || error);
            }
        };
    }

    private static void instanciateEventsCallback() {
        eventsCallback = new EventsCallback() {

            @Override
            public void onEventsRetrievalSuccess(Map<String, Event> newEvents, Double serverTime) {
                System.out.println("TestEventsCallback: success with "
                        + newEvents.values().size()
                        + " events");
                events = newEvents;
                success = true;
            }

            @Override
            public void onEventsRetrievalError(String message, Double pServerTime) {
                error = true;
            }

            @Override
            public void onEventsSuccess(String successMessage, Event event, Integer pStoppedId,
                                        Double pServerTime) {
                System.out.println("OnlineEventsManagerTest: eventsSuccess msg: " + successMessage);
                stoppedId = pStoppedId;
                success = true;
                singleEvent = event;
            }

            @Override
            public void onEventsError(String errorMessage, Double pServerTime) {
                error = true;
            }
        };
    }

    private static void instanciateStreamsCallback() {
        streamsCallback = new StreamsCallback() {

            @Override
            public void onStreamsRetrievalSuccess(Map<String, Stream> newStreams, Double serverTime) {
                System.out.println("TestStreamsCallback: success for "
                        + newStreams.values().size()
                        + " streams");
                streams = newStreams;
                success = true;
            }

            @Override
            public void onStreamsRetrievalError(String message, Double pServerTime) {
                error = true;
            }

            @Override
            public void onStreamsSuccess(String successMessage, Stream stream, Double pServerTime) {
                System.out.println("TestStreamsCallback: success msg: " + successMessage);
                singleStream = stream;
                success = true;
            }

            @Override
            public void onStreamError(String errorMessage, Double pServerTime) {
                error = true;
            }
        };
    }
}
