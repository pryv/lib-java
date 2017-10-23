package com.pryv.acceptance;

import com.jayway.awaitility.Awaitility;
import com.pryv.Connection;
import com.pryv.Filter;
import com.pryv.interfaces.GetStreamsCallback;
import com.pryv.interfaces.StreamsCallback;
import com.pryv.database.DBinitCallback;
import com.pryv.model.Attachment;
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
    public static void setUp() throws Exception {

        instanciateEventsCallback();
        instanciateGetEventsCallback();
        instanciateStreamsCallback();
        instanciateGetStreamsCallback();

        connection =
                new Connection(TestCredentials.USERNAME, TestCredentials.TOKEN, TestCredentials.DOMAIN, true, new DBinitCallback());

        connection.setupCacheScope(new Filter());

        testSupportStream = new Stream("onlineModuleStreamID", "javaLibTestSupportStream");
        connection.streams.create(testSupportStream, streamsCallback);
        Awaitility.await().until(hasCacheResult());
        assertFalse(cacheError);
        Awaitility.await().until(hasApiResult());
        assertFalse(apiError);

        testSupportStream.merge(apiStream, true);
        assertNotNull(testSupportStream.getId());
        cacheSuccess = false;
        apiSuccess = false;

        connection.events.create(new Event(testSupportStream.getId(),
                "note/txt", "i am a test event"), eventsCallback);
        Awaitility.await().until(hasCacheResult());
        assertFalse(cacheError);
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

    @Test
    public void testGetEventsWithANullFilterShouldReturnNonTrashedEvents() {
        connection.events.get(null, getEventsCallback);
        Awaitility.await().until(hasCacheResult());
        assertTrue(cacheSuccess);
        Awaitility.await().until(hasApiResult());
        assertTrue(apiSuccess);

        assertTrue(cacheEvents.size() > 0);
        assertTrue(events.size() > 0);
    }

    // TODO add includeDeletions in Filter
    @Test
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
        assertNotNull(apiEvent);
        assertNotNull(apiEvent.getId());
        assertNotNull(apiEvent.getTime());
        assertNotNull(apiEvent.getModified());
        assertNotNull(apiEvent.getModifiedBy());
        assertNotNull(apiEvent.getCreated());
        assertNotNull(apiEvent.getCreatedBy());
        assertNotNull(apiEvent.getTags());
        assertEquals(apiEvent.getType(), minimalEvent.getType());
        assertEquals(apiEvent.getContent(), minimalEvent.getContent());
        assertEquals(apiEvent.getStreamId(), minimalEvent.getStreamId());
    }

    // TODO implement events.start in lib java
    // Currenty not working: to create a running event using events.create, you must provide the API
    // with an event with duration=null, but the Java lib doesn't serialize a field if it is set to
    // null
    // TODO move all the singleActivity related tests in a separate test class
    //@Test
    public void
    testCreateEventsMustReturnAStoppedIdWhenCalledInASingleActivityStreamWithARunningEvent() {
        // create singleActivity Stream
        Stream singleAcivityStream = createSingleActivityStream();

        // create running Event
        Event runningEvent = new Event();
        runningEvent.setStreamId(singleAcivityStream.getId());
        runningEvent.setType("activity/plain");
        runningEvent.setDuration(null);
        connection.events.create(runningEvent, eventsCallback);
        Awaitility.await().until(hasApiResult());
        assertTrue(apiSuccess);
        apiSuccess = false;
        assertNotNull(apiEvent);
        runningEvent = apiEvent;
        String myStoppedId = runningEvent.getId();

        // create Event that will stop running event
        Event stopperEvent = new Event(singleAcivityStream.getId(), "activity/plain", null);
        connection.events.create(stopperEvent, eventsCallback);
        Awaitility.await().until(hasApiResult());
        assertTrue(apiSuccess);
        apiSuccess = false;
        // TODO compare stoppedId received from callback with the one stored earlier
        assertEquals(myStoppedId, stoppedId);

        // delete singleActivity Stream
        deleteSingleAcitivityStream(singleAcivityStream);
    }

    // TODO same as other
    // @Test
    public void
    testCreateEventsMustReturnAnErrorWhenCalledInASingleActivityStreamAndPeriodsOverlap() {
        Stream singleActivityStream = createSingleActivityStream();

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
    public void testCreateEventsWithAttachmentWithValidDataMustWork() {
        Attachment attachment = new Attachment();
        File attachmentFile = new File(getClass().getClassLoader().getResource("resources/photo.PNG").getPath());
        attachment.setFile(attachmentFile);
        assertTrue(attachment.getFile().length() > 0);
        attachment.setType("image/png");
        attachment.setFileName(attachmentFile.getName());

        // create encapsulating event
        Event eventWithAttachment = new Event();
        eventWithAttachment.setStreamId(testSupportStream.getId());
        eventWithAttachment.addAttachment(attachment);
        eventWithAttachment.setStreamId(testSupportStream.getId());
        eventWithAttachment.setType("picture/attached");
        eventWithAttachment.setDescription("This is a test event with an image.");

        // create event with attachment
        connection.events.createWithAttachment(eventWithAttachment, eventsCallback);
        Awaitility.await().until(hasApiResult());
        assertFalse(apiError);
        assertNotNull(apiEvent);
        assertNotNull(apiEvent.getAttachments());
        assertEquals(apiEvent.getAttachments().size(), 1);
        Attachment createdAttachment = apiEvent.getFirstAttachment();
        assertNotNull(createdAttachment.getId());
    }

    /**
     * UPDATE EVENTS
     */

    public void testUpdateEventMustAcceptAValidAEventAndReturnAFullEvent() {
        Event eventToUpdate = new Event(testSupportStream.getId(),
                "note/txt", "i will be updated");
        connection.events.create(eventToUpdate, eventsCallback);
        Awaitility.await().until(hasApiResult());
        assertTrue(apiSuccess);
        assertNotNull(apiEvent);
        apiSuccess = false;

        Event initialEvent = apiEvent;
        assertNotEquals(eventToUpdate, initialEvent);
        assertEquals(initialEvent.getContent(), eventToUpdate.getContent());

        eventToUpdate.setContent("i have beeen updated");
        connection.events.update(eventToUpdate, eventsCallback);
        Awaitility.await().until(hasApiResult());
        assertTrue(apiSuccess);
        apiSuccess = false;
        assertNotNull(apiEvent);
        assertEquals(apiEvent.getId(), initialEvent.getId());
        assertEquals(apiEvent.getContent(), eventToUpdate.getContent());
    }

    public void testUpdateEventMustReturnAnErrorWhenEventDoesntExistYet() {
        Event unexistingEvent = new Event(testSupportStream.getId(), "note/txt", "I dont exist and will generate an apiError");
        connection.events.update(unexistingEvent, eventsCallback);
        Awaitility.await().until(hasApiResult());
        assertTrue(apiError);
    }

    /**
     * DELETE EVENTS
     */

    @Test
    public void testDeleteEventMustReturnTheEventWithTrashedSetToTrueWhenDeletingOnce() {
        Event eventToTrash = new Event(testSupportStream.getId(), "note/txt", "i will be trashed");
        connection.events.create(eventToTrash, eventsCallback);
        Awaitility.await().until(hasApiResult());
        assertTrue(apiSuccess);
        apiSuccess = false;
        eventToTrash = apiEvent;
        assertFalse(eventToTrash.isTrashed());

        connection.events.delete(eventToTrash, eventsCallback);
        Awaitility.await().until(hasApiResult());
        assertTrue(apiSuccess);
        assertNotNull(apiEvent);
        assertEquals(eventToTrash.getContent(), apiEvent.getContent());
        assertTrue(apiEvent.isTrashed());
    }

    // TODO retrieve deletionId in eventsManager.onEventsSuccess
    @Test
    public void testDeleteEventMustReturnADeletionIdWhenDeletingTwice() {
        // create event
        Event eventToDelete = new Event(testSupportStream.getId(), "note/txt", "i will be deleted");
        connection.events.create(eventToDelete, eventsCallback);
        Awaitility.await().until(hasApiResult());
        assertTrue(apiSuccess);
        apiSuccess = false;
        eventToDelete = apiEvent;

        // trash event
        connection.events.delete(eventToDelete, eventsCallback);
        Awaitility.await().until(hasApiResult());
        assertTrue(apiSuccess);
        assertNotNull(apiEvent);
        assertEquals(eventToDelete.getContent(), apiEvent.getContent());
        assertTrue(apiEvent.isTrashed());

        // delete event
        connection.events.delete(eventToDelete, eventsCallback);
        Awaitility.await().until(hasApiResult());
        assertTrue(apiSuccess);
        assertNull(apiEvent);
    }

    private Stream createSingleActivityStream() {
        apiSuccess = false;
        apiError = false;
        Stream singleActivityStream = new Stream();
        singleActivityStream.setId("singleActivityStream");
        singleActivityStream.setName("singleActivityStream");
        singleActivityStream.setSingleActivity(true);
        connection.streams.create(singleActivityStream, streamsCallback);
        Awaitility.await().until(hasApiResult());
        assertTrue(apiSuccess);
        assertEquals(apiStream.getName(), singleActivityStream.getName());
        singleActivityStream = apiStream;
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
