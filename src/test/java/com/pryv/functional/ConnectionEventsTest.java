package com.pryv.functional;

import com.jayway.awaitility.Awaitility;
import com.pryv.Connection;
import com.pryv.interfaces.EventsCallback;
import com.pryv.Filter;
import com.pryv.interfaces.GetStreamsCallback;
import com.pryv.interfaces.StreamsCallback;
import com.pryv.api.database.DBinitCallback;
import com.pryv.model.Event;
import com.pryv.model.Stream;
import com.pryv.interfaces.GetEventsCallback;
import com.pryv.utils.Logger;

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

    private static String stoppedId;

    private static Event singleEvent;
    private static Stream singleStream;

    private static boolean cacheSuccess = false;
    private static boolean cacheError = false;
    private static boolean apiSuccess = false;
    private static boolean apiError = false;

    private static Connection connection;

    @BeforeClass
    public static void setUp() throws Exception {

        instanciateEventsCallback();
        instanciateGetEventsCallback();
        instanciateStreamsCallback();
        instanciateGetStreamsCallback();

        connection =
                new Connection(TestCredentials.USERNAME, TestCredentials.TOKEN, TestCredentials.DOMAIN,
                        new DBinitCallback() {
                    @Override
                    public void onError(String message) {
                        System.out.println("DB init Error: " + message);
                    }
                });

        connection.setupCacheScope(new Filter());

        testSupportStream = new Stream("onlineModuleStreamID", "javaLibTestSupportStream");
        connection.streams.create(testSupportStream, streamsCallback);
        Awaitility.await().until(hasCacheResult());
        assertFalse(cacheError);
        Awaitility.await().until(hasApiResult());
        assertFalse(apiError);

        testSupportStream.merge(singleStream, true);
        assertNotNull(testSupportStream.getId());
        cacheSuccess = false;
        apiSuccess = false;

        connection.events.create(new Event(testSupportStream.getId(), null,
                "note/txt", "i am a test event"), eventsCallback);
        Awaitility.await().until(hasCacheResult());
        assertFalse(apiError);
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
        Awaitility.await().until(hasApiResult());
        assertTrue(apiSuccess);
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
        Awaitility.await().until(hasApiResult());
        assertTrue(apiSuccess);
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
        Awaitility.await().until(hasApiResult());
        assertTrue(apiSuccess);
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
        Awaitility.await().until(hasApiResult());
        assertTrue(apiSuccess);
        System.out.println("buggin here: " + singleEvent);
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
        Awaitility.await().until(hasApiResult());
        assertTrue(apiSuccess);
        apiSuccess = false;
        assertNotNull(singleEvent);
        runningEvent = singleEvent;
        String myStoppedId = runningEvent.getId();

        // create Event that will stop running event
        Event stopperEvent = new Event(singleAcivityStream.getId(), null, "activity/plain", null);
        connection.events.create(stopperEvent, eventsCallback);
        Awaitility.await().until(hasApiResult());
        assertTrue(apiSuccess);
        apiSuccess = false;
        // TODO compare stoppedId received from callback with the one stored earlier
        assertEquals(stoppedId, myStoppedId);

        // delete singleActivity Stream
        deleteSingleAcitivityStream(singleAcivityStream);
    }

    @Test
    public void testMusReturnAnErrorWhenEventParametersAreInvalid() {
        Event missingStreamIdEvent = new Event();
        missingStreamIdEvent.setType("note/txt");
        missingStreamIdEvent.setContent("i am missing a streamId, will generate apiError");
        connection.events.create(missingStreamIdEvent, eventsCallback);
        Awaitility.await().until(hasApiResult());
        assertTrue(apiError);
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
        Awaitility.await().until(hasApiResult());
        assertTrue(apiSuccess);
        apiSuccess = false;

        Event invalidEvent = new Event();
        invalidEvent.setStreamId(singleActivityStream.getId());
        invalidEvent.setType("activity/plain");
        invalidEvent.setTime(time + duration / 2);
        invalidEvent.setDuration(duration);
        connection.events.create(invalidEvent, eventsCallback);
        Awaitility.await().until(hasApiResult());
        assertTrue(apiError);
        apiError = false;

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
        Awaitility.await().until(hasApiResult());
        assertTrue(apiSuccess);
        assertNotNull(singleEvent);
        apiSuccess = false;

        Event initialEvent = singleEvent;
        assertNotEquals(eventToUpdate, initialEvent);
        assertEquals(initialEvent.getContent(), eventToUpdate.getContent());

        eventToUpdate.setContent("i have beeen updated");
        connection.events.update(eventToUpdate, eventsCallback);
        Awaitility.await().until(hasApiResult());
        assertTrue(apiSuccess);
        apiSuccess = false;
        assertNotNull(singleEvent);
        assertEquals(singleEvent.getId(), initialEvent.getId());
        assertEquals(singleEvent.getContent(), eventToUpdate.getContent());
    }

    public void testUpdateEventMustReturnAnErrorWhenEventDoesntExistYet() {
        Event unexistingEvent = new Event(testSupportStream.getId(),
                null, "note/txt", "I dont exist and will generate an apiError");
        connection.events.update(unexistingEvent, eventsCallback);
        Awaitility.await().until(hasApiResult());
        assertTrue(apiError);
    }

    /**
     * DELETE EVENTS
     */

    @Test
    public void testDeleteEventMustReturnTheEventWithTrashedSetToTrueWhenDeletingOnce() {
        Event eventToTrash = new Event(testSupportStream.getId(),
                null, "note/txt", "i will be trashed");
        connection.events.create(eventToTrash, eventsCallback);
        Awaitility.await().until(hasApiResult());
        assertTrue(apiSuccess);
        apiSuccess = false;
        eventToTrash = singleEvent;
        assertFalse(eventToTrash.isTrashed());

        connection.events.delete(eventToTrash, eventsCallback);
        Awaitility.await().until(hasApiResult());
        assertTrue(apiSuccess);
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
        Awaitility.await().until(hasApiResult());
        assertTrue(apiSuccess);
        apiSuccess = false;
        eventToDelete = singleEvent;

        // trash event
        connection.events.delete(eventToDelete, eventsCallback);
        Awaitility.await().until(hasApiResult());
        assertTrue(apiSuccess);
        assertNotNull(singleEvent);
        assertEquals(eventToDelete.getContent(), singleEvent.getContent());
        assertTrue(singleEvent.isTrashed());

        // delete event
        connection.events.delete(eventToDelete, eventsCallback);
        Awaitility.await().until(hasApiResult());
        assertTrue(apiSuccess);
        assertNull(singleEvent);
    }

    private Stream createSingleActivityStream(Stream singleActivityStream) {
        singleActivityStream.setId("singleActivityStream");
        singleActivityStream.setName("singleActivityStream");
        singleActivityStream.setSingleActivity(true);
        connection.streams.create(singleActivityStream, streamsCallback);
        Awaitility.await().until(hasApiResult());
        assertTrue(apiSuccess);
        assertEquals(singleStream.getName(), singleActivityStream.getName());
        singleActivityStream = singleStream;
        apiSuccess = false;
        return singleActivityStream;
    }

    private void deleteSingleAcitivityStream(Stream singleActivityStream) {
        connection.streams.delete(singleActivityStream, false, streamsCallback);
        Awaitility.await().until(hasApiResult());
        assertTrue(apiSuccess);
        apiSuccess = false;
        connection.streams.delete(singleActivityStream, false, streamsCallback);
        Awaitility.await().until(hasApiResult());
        apiSuccess = false;
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
            public void cacheCallback(List<Event> events, Map<String, Event> deletedEvents) {
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
            public void apiCallback(List<Event> apiEvents, Double serverTime) {
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
                stoppedId = pStoppedId;
                apiSuccess = true;
                singleEvent = event;
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
                singleStream = stream;
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
            public void cacheCallback(Map<String, Stream> streams, Map<String, Stream> deletedStreams) {
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
            public void apiCallback(Map<String, Stream> receivedStreams, Double serverTime) {
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
