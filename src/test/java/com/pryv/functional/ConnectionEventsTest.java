package com.pryv.functional;

import com.jayway.awaitility.Awaitility;
import com.pryv.Connection;
import com.pryv.interfaces.EventsCallback;
import com.pryv.interfaces.EventsManager;
import com.pryv.api.Filter;
import com.pryv.interfaces.GetStreamsCallback;
import com.pryv.interfaces.StreamsCallback;
import com.pryv.interfaces.StreamsManager;
import com.pryv.api.database.DBinitCallback;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;
import com.pryv.interfaces.GetEventsCallback;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import resources.TestCredentials;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ConnectionEventsTest {

    private static EventsCallback eventsCallback;
    private static GetEventsCallback getEventsCallback;
    private static StreamsCallback streamsCallback;
    private static GetStreamsCallback getStreamsCallback;

    private static List<Event> events;
    private static List<Event> partialEvents;
    private static Map<String, Stream> streams;
    private static Map<String, Stream> partialStreams;

    private static Stream testSupportStream;

    private static Integer stoppedId;

    private static Event singleEvent;
    private static Stream singleStream;

    private static boolean partialSuccess = false;
    private static boolean partialError = false;
    private static boolean success = false;
    private static boolean error = false;

    private static Connection connection;

    @BeforeClass
    public static void setUp() throws Exception {

        instanciateEventsCallback();
        instanciateGetEventsCallback();
        instanciateStreamsCallback();

        connection =
                new Connection(TestCredentials.USERNAME, TestCredentials.TOKEN, TestCredentials.DOMAIN,
                        new DBinitCallback() {
                    @Override
                    public void onError(String message) {
                        System.out.println("DB init Error: " + message);
                    }
                });

        testSupportStream = new Stream("onlineModuleStreamID", "javaLibTestSupportStream");
        connection.streams.create(testSupportStream, streamsCallback);
        Awaitility.await().until(hasResult());
        assertFalse(error);
        testSupportStream.merge(singleStream, true);
        assertNotNull(testSupportStream.getId());
        success = false;
        connection.events.create(new Event(testSupportStream.getId(), null,
                "note/txt", "i am a test event"), eventsCallback);
        Awaitility.await().until(hasResult());
        assertFalse(error);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.streams.delete(testSupportStream, false, streamsCallback);
        Awaitility.await().until(hasResult());
        success = false;
        connection.streams.delete(testSupportStream, false, streamsCallback);
        Awaitility.await().until(hasResult());
    }

    @Before
    public void beforeEachTest() {
        events = null;
        partialEvents = null;
        streams = null;
        partialStreams = null;
        success = false;
        error = false;
        partialSuccess = false;
        partialError = false;
        singleEvent = null;
        singleStream = null;
        stoppedId = null;
    }

    /**
     * GET EVENTS
     */

    @Test
    public void testGetEventsMustReturnNonTrashedEvents() {
        connection.events.get(new Filter(), getEventsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(success);
        assertTrue(events.size() > 0);
        for (Event event: events) {
            assertFalse(event.isTrashed());
        }
    }

    // TODO add includeDeletions in Filter
    //@Test
    public void testGetEventsMustReturnDeletedEventsWhenIncludeDeletionsIsSet() {
        Filter deletionsFilter = new Filter();
    }

    public void testGetEventsMustReturnEventsMatchingTheFilter() {
        Filter filter = new Filter();
        int numLimit = 10;
        String type = "note/txt";
        filter.setLimit(numLimit);
        filter.addType(type);
        connection.events.get(filter, getEventsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(success);
        assertTrue(events.size() == 10);
        for (Event event: events) {
            assertTrue(event.getType().equals(type));
        }
    }

    //@Test
    public void testGetEventsMustAcceptANullFilter() {

    }

    public void testGetEventsMustReturnAnEmptyMapWhenTheFilterMatchesNoEvents() {
        Filter filter = new Filter();
        filter.setFromTime(10.0);
        filter.setToTime(11.0);
        connection.events.get(filter, getEventsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(success);
        assertTrue(events.size() == 0);
    }

    /**
     * CREATE EVENTS
     */

    @Test
    public void testCreateEventsMustAcceptAnEventWithMinimalParamsAndFillReadOnlyFields() {
        Event minimalEvent = new Event();
        minimalEvent.setStreamId(testSupportStream.getId());
        minimalEvent.setType("note/txt");
        minimalEvent.setContent("I am used in create event test, please delete me");
        connection.events.create(minimalEvent, eventsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(success);
        assertNotNull(singleEvent);
        assertNotNull(singleEvent.getId());
        assertNotNull(singleEvent.getTime());
        assertNotNull(singleEvent.getModified());
        assertNotNull(singleEvent.getModifiedBy());
        assertNotNull(singleEvent.getCreated());
        assertNotNull(singleEvent.getCreatedBy());
        assertNotNull(singleEvent.getTags());
        assertEquals(singleEvent.getType(), minimalEvent.getType());
        assertEquals(singleEvent.getContent(), minimalEvent.getContent());
        assertEquals(singleEvent.getStreamId(), minimalEvent.getStreamId());
    }

    // TODO implement events.start in lib java
    // TODO move all the singleActivity related tests in a separate test class
    //@Test
    public void
    testCreateEventsMustReturnAStoppedIdWhenCalledInASingleActivityStreamWithARunningEvent() {
        // create singleActivity Stream
        Stream singleAcivityStream = new Stream();
        createSingleActivityStream(singleAcivityStream);

        // create running Event
        Event runningEvent = new Event();
        runningEvent.setStreamId(singleAcivityStream.getId());
        runningEvent.setType("activity/plain");
        runningEvent.setDuration(null);
        connection.events.create(runningEvent, eventsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(success);
        success = false;
        assertNotNull(singleEvent);
        runningEvent = singleEvent;
        String myStoppedId = runningEvent.getId();

        // create Event that will stop running event
        Event stopperEvent = new Event(singleAcivityStream.getId(), null, "activity/plain", null);
        connection.events.create(stopperEvent, eventsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(success);
        success = false;
        // TODO compare stoppedId received from callback with the one stored earlier
        assertEquals(stoppedId, myStoppedId);

        // delete singleActivity Stream
        deleteSingleAcitivityStream(singleAcivityStream);
    }

    @Test
    public void testMusReturnAnErrorWhenEventParametersAreInvalid() {
        Event missingStreamIdEvent = new Event();
        missingStreamIdEvent.setType("note/txt");
        missingStreamIdEvent.setContent("i am missing a streamId, will generate error");
        connection.events.create(missingStreamIdEvent, eventsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(error);
    }

    @Test
    public void
    testCreateEventsMustReturnAnErrorWhenCalledInASingleActivityStreamAndPeriodsOverlap() {
        Stream singleActivityStream = new Stream();
        singleActivityStream = createSingleActivityStream(singleActivityStream);

        Double time = 1000.0;
        Double duration = 500.0;
        Event runningEvent = new Event();
        runningEvent.setStreamId(singleActivityStream.getId());
        runningEvent.setType("activity/plain");
        runningEvent.setTime(time);
        runningEvent.setDuration(duration);
        connection.events.create(runningEvent, eventsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(success);
        success = false;

        Event invalidEvent = new Event();
        invalidEvent.setStreamId(singleActivityStream.getId());
        invalidEvent.setType("activity/plain");
        invalidEvent.setTime(time + duration / 2);
        invalidEvent.setDuration(duration);
        connection.events.create(invalidEvent, eventsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(error);
        error = false;

        deleteSingleAcitivityStream(singleActivityStream);
    }

    // TODO add eventsManager.createEventWithAttachment()
    // @Test
    public void testCreateEventsWithAttachmentWithValidDataMustWork() {

    }

    /**
     * UPDATE EVENTS
     */

    public void testUpdateEventMustAcceptAValidAEventAndReturnAFullEvent() {
        Event eventToUpdate = new Event(testSupportStream.getId(), null,
                "note/txt", "i will be updated");
        connection.events.create(eventToUpdate, eventsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(success);
        assertNotNull(singleEvent);
        success = false;

        Event initialEvent = singleEvent;
        assertNotEquals(eventToUpdate, initialEvent);
        assertEquals(initialEvent.getContent(), eventToUpdate.getContent());

        eventToUpdate.setContent("i have beeen updated");
        connection.events.update(eventToUpdate, eventsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(success);
        success = false;
        assertNotNull(singleEvent);
        assertEquals(singleEvent.getId(), initialEvent.getId());
        assertEquals(singleEvent.getContent(), eventToUpdate.getContent());
    }

    public void testUpdateEventMustReturnAnErrorWhenEventDoesntExistYet() {
        Event unexistingEvent = new Event(testSupportStream.getId(),
                null, "note/txt", "I dont exist and will generate an error");
        connection.events.update(unexistingEvent, eventsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(error);
    }

    /**
     * DELETE EVENTS
     */

    @Test
    public void testDeleteEventMustReturnTheEventWithTrashedSetToTrueWhenDeletingOnce() {
        Event eventToTrash = new Event(testSupportStream.getId(),
                null, "note/txt", "i will be trashed");
        connection.events.create(eventToTrash, eventsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(success);
        success = false;
        eventToTrash = singleEvent;
        assertFalse(eventToTrash.isTrashed());

        connection.events.delete(eventToTrash, eventsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(success);
        assertNotNull(singleEvent);
        assertEquals(eventToTrash.getContent(), singleEvent.getContent());
        assertTrue(singleEvent.isTrashed());
    }

    // TODO retrieve deletionId in eventsManager.onEventsSuccess
    @Test
    public void testDeleteEventMustReturnADeletionIdWhenDeletingTwice() {
        // create event
        Event eventToDelete = new Event(testSupportStream.getId(),
                null, "note/txt", "i will be deleted");
        connection.events.create(eventToDelete, eventsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(success);
        success = false;
        eventToDelete = singleEvent;

        // trash event
        connection.events.delete(eventToDelete, eventsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(success);
        assertNotNull(singleEvent);
        assertEquals(eventToDelete.getContent(), singleEvent.getContent());
        assertTrue(singleEvent.isTrashed());

        // delete event
        connection.events.delete(eventToDelete, eventsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(success);
        assertNull(singleEvent);
    }

    private Stream createSingleActivityStream(Stream singleActivityStream) {
        singleActivityStream.setId("singleActivityStream");
        singleActivityStream.setName("singleActivityStream");
        singleActivityStream.setSingleActivity(true);
        connection.streams.create(singleActivityStream, streamsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(success);
        assertEquals(singleStream.getName(), singleActivityStream.getName());
        singleActivityStream = singleStream;
        success = false;
        return singleActivityStream;
    }

    private void deleteSingleAcitivityStream(Stream singleActivityStream) {
        connection.streams.delete(singleActivityStream, false, streamsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(success);
        success = false;
        connection.streams.delete(singleActivityStream, false, streamsCallback);
        Awaitility.await().until(hasResult());
        success = false;
    }

    private static Callable<Boolean> hasResult() {
        return new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                return (success || error);
            }
        };
    }

    private static void instanciateGetEventsCallback() {
        getEventsCallback = new GetEventsCallback() {
            @Override
            public void partialCallback(List<Event> events, Map<String, Event> deletedEvents) {

            }

            @Override
            public void doneCallback(List<Event> events, Double serverTime) {

            }

            @Override
            public void onError(String errorMessage, Double serverTime) {

            }
        };
    }

    private static void instanciateEventsCallback() {
        eventsCallback = new EventsCallback() {

            @Override
            public void onSuccess(String successMessage, Event event, Integer pStoppedId,
                                        Double pServerTime) {
                System.out.println("OnlineEventsManagerTest: eventsSuccess msg: " + successMessage);
                stoppedId = pStoppedId;
                success = true;
                singleEvent = event;
            }

            @Override
            public void onError(String errorMessage, Double pServerTime) {
                error = true;
            }
        };
    }

    private static void instanciateStreamsCallback() {
        streamsCallback = new StreamsCallback() {

            @Override
            public void onSuccess(String successMessage, Stream stream, Double pServerTime) {
                System.out.println("TestStreamsCallback: success msg: " + successMessage);
                singleStream = stream;
                success = true;
            }

            @Override
            public void onError(String errorMessage, Double pServerTime) {
                error = true;
            }
        };
    }

    private static void instanciateGetStreamsCallback() {
        getStreamsCallback = new GetStreamsCallback() {
            @Override
            public void partialCallback(Map<String, Stream> streams, Map<String, Stream> deletedStreams) {
                partialStreams = streams;
                partialSuccess = true;
            }

            @Override
            public void doneCallback(Map<String, Stream> receivedStreams, Double serverTime) {
                streams = receivedStreams;
                success = true;
            }

            @Override
            public void onError(String errorMessage, Double serverTime) {
                error = true;
            }
        };
    }
}
