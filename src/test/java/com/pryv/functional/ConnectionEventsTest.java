package com.pryv.functional;

import com.jayway.awaitility.Awaitility;
import com.pryv.Connection;
import com.pryv.Pryv;
import com.pryv.api.EventsCallback;
import com.pryv.api.EventsManager;
import com.pryv.api.Filter;
import com.pryv.api.OnlineEventsAndStreamsManager;
import com.pryv.api.StreamsCallback;
import com.pryv.api.StreamsManager;
import com.pryv.api.database.DBinitCallback;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
    private static StreamsCallback streamsCallback;

    private static Map<String, Event> events;
    private static Map<String, Stream> streams;

    private static Stream testSupportStream;

    private static Integer stoppedId;

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
        Pryv.setDomain("pryv.li");
        Pryv.deactivateSupervisor();
        Pryv.deactivateCache();

        Connection pryvConnection =
                new Connection(TestCredentials.USERNAME, TestCredentials.TOKEN,
                        new DBinitCallback() {
                    @Override
                    public void onError(String message) {
                        System.out.println("DB init Error: " + message);
                    }
                });

        eventsManager = (EventsManager) pryvConnection;
        streamsManager = (StreamsManager) pryvConnection;

        testSupportStream = new Stream("onlineModuleStreamID", "javaLibTestSupportStream");
        streamsManager.createStream(testSupportStream, streamsCallback);
        Awaitility.await().until(hasResult());
        assertFalse(error);
        testSupportStream.merge(singleStream, true);
        assertNotNull(testSupportStream.getId());
        success = false;
        eventsManager.createEvent(new Event(testSupportStream.getId(), null,
                "note/txt", "i am a test event"), eventsCallback);
        Awaitility.await().until(hasResult());
        assertFalse(error);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        streamsManager.deleteStream(testSupportStream, false, streamsCallback);
        Awaitility.await().until(hasResult());
        success = false;
        streamsManager.deleteStream(testSupportStream, false, streamsCallback);
        Awaitility.await().until(hasResult());
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

    /**
     * GET EVENTS
     */

    @Test
    public void testGetEventsMustReturnNonTrashedEvents() {
        eventsManager.getEvents(new Filter(), eventsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(success);
        assertTrue(events.size() > 0);
        for (Event event: events.values()) {
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
        eventsManager.getEvents(filter, eventsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(success);
        assertTrue(events.size() == 10);
        for (Event event: events.values()) {
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
        eventsManager.getEvents(filter, eventsCallback);
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
        eventsManager.createEvent(minimalEvent, eventsCallback);
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
        eventsManager.createEvent(runningEvent, eventsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(success);
        success = false;
        assertNotNull(singleEvent);
        runningEvent = singleEvent;
        String myStoppedId = runningEvent.getId();

        // create Event that will stop running event
        Event stopperEvent = new Event(singleAcivityStream.getId(), null, "activity/plain", null);
        eventsManager.createEvent(stopperEvent, eventsCallback);
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
        eventsManager.createEvent(missingStreamIdEvent, eventsCallback);
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
        eventsManager.createEvent(runningEvent, eventsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(success);
        success = false;

        Event invalidEvent = new Event();
        invalidEvent.setStreamId(singleActivityStream.getId());
        invalidEvent.setType("activity/plain");
        invalidEvent.setTime(time + duration / 2);
        invalidEvent.setDuration(duration);
        eventsManager.createEvent(invalidEvent, eventsCallback);
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
        eventsManager.createEvent(eventToUpdate, eventsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(success);
        assertNotNull(singleEvent);
        success = false;

        Event initialEvent = singleEvent;
        assertNotEquals(eventToUpdate, initialEvent);
        assertEquals(initialEvent.getContent(), eventToUpdate.getContent());

        eventToUpdate.setContent("i have beeen updated");
        eventsManager.updateEvent(eventToUpdate, eventsCallback);
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
        eventsManager.updateEvent(unexistingEvent, eventsCallback);
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
        eventsManager.createEvent(eventToTrash, eventsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(success);
        success = false;
        eventToTrash = singleEvent;
        assertFalse(eventToTrash.isTrashed());

        eventsManager.deleteEvent(eventToTrash, eventsCallback);
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
        eventsManager.createEvent(eventToDelete, eventsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(success);
        success = false;
        eventToDelete = singleEvent;

        // trash event
        eventsManager.deleteEvent(eventToDelete, eventsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(success);
        assertNotNull(singleEvent);
        assertEquals(eventToDelete.getContent(), singleEvent.getContent());
        assertTrue(singleEvent.isTrashed());

        // delete event
        eventsManager.deleteEvent(eventToDelete, eventsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(success);
        assertNull(singleEvent);
    }

    private Stream createSingleActivityStream(Stream singleActivityStream) {
        singleActivityStream.setId("singleActivityStream");
        singleActivityStream.setName("singleActivityStream");
        singleActivityStream.setSingleActivity(true);
        streamsManager.createStream(singleActivityStream, streamsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(success);
        assertEquals(singleStream.getName(), singleActivityStream.getName());
        singleActivityStream = singleStream;
        success = false;
        return singleActivityStream;
    }

    private void deleteSingleAcitivityStream(Stream singleActivityStream) {
        streamsManager.deleteStream(singleActivityStream, false, streamsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(success);
        success = false;
        streamsManager.deleteStream(singleActivityStream, false, streamsCallback);
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
