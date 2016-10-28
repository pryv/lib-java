package com.pryv.acceptance;

import com.jayway.awaitility.Awaitility;
import com.pryv.Connection;
import com.pryv.Filter;
import com.pryv.database.DBinitCallback;
import com.pryv.interfaces.EventsCallback;
import com.pryv.interfaces.GetEventsCallback;
import com.pryv.interfaces.GetStreamsCallback;
import com.pryv.interfaces.StreamsCallback;
import com.pryv.model.Event;
import com.pryv.model.Stream;
import com.pryv.util.TestUtils;
import com.pryv.utils.Logger;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import resources.TestCredentials;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ConnectionStreamsTest {

    private static Logger logger = Logger.getInstance();

    private static EventsCallback eventsCallback;
    private static GetEventsCallback getEventsCallback;
    private static StreamsCallback streamsCallback;
    private static GetStreamsCallback getStreamsCallback;

    private static List<Event> apiEvents;
    private static List<Event> cacheEvents;
    private static Map<String, Stream> apiStreams;
    private static Map<String, Stream> cacheStreams;
    private static Map<String, Double> cacheStreamDeletions;
    private static Map<String, Double> apiStreamDeletions;

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
                new Connection(TestCredentials.USERNAME, TestCredentials.TOKEN, TestCredentials.DOMAIN, true,
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
        apiEvents = null;
        cacheEvents = null;
        apiStreams = null;
        cacheStreams = null;
        apiStreamDeletions = null;
        cacheStreamDeletions = null;
        apiSuccess = false;
        apiError = false;
        cacheSuccess = false;
        cacheError = false;
        apiEvent = null;
        apiStream = null;
        stoppedId = null;
    }

    /**
     * GET STREAMS
     */

    @Test
    public void testGetStreamsMustReturnATreeOfNonTrashedStreamsWithANullFilter() {
        Stream s1 = new Stream(null, "someStreamOne");
        s1.setParentId(testSupportStream.getId());
        Stream s2 = new Stream(null, "someOtherStream");
        s2.setParentId(testSupportStream.getId());
        connection.streams.create(s1, streamsCallback);
        Awaitility.await().until(hasApiResult());
        assertFalse(apiError);
        apiSuccess = false;
        connection.streams.create(s2, streamsCallback);
        Awaitility.await().until(hasApiResult());
        assertFalse(apiError);
        apiSuccess = false;

        connection.streams.get(null, getStreamsCallback);
        Awaitility.await().until(hasCacheResult());
        assertFalse(cacheError);
        Awaitility.await().until(hasApiResult());
        assertFalse(apiError);

        assertNotNull(cacheStreams);
        assertNotNull(apiStreams);

        connection.getRootStreams();
    }

    @Test
    public void testGetStreamsWithParentIdSetMustReturnStreamsMatchingTheGivenFilter() {
        // Create children
        Stream childStream1 = new Stream("childStream1", "childStream1");
        childStream1.setParentId(testSupportStream.getId());
        Stream childStream2 = new Stream("childStream2", "childStream2");
        childStream2.setParentId(testSupportStream.getId());
        connection.streams.create(childStream1, streamsCallback);
        Awaitility.await().until(hasCacheResult());
        assertFalse(cacheError);
        Awaitility.await().until(hasApiResult());
        assertFalse(apiError);
        cacheSuccess = false;
        apiSuccess = false;
        connection.streams.create(childStream2, streamsCallback);
        Awaitility.await().until(hasCacheResult());
        assertFalse(cacheError);
        Awaitility.await().until(hasApiResult());
        assertFalse(apiError);
        cacheSuccess = false;
        apiSuccess = false;

        // Get streams with specified parentID in Filter
        Filter filter = new Filter();
        filter.setParentId(testSupportStream.getId());
        connection.streams.get(filter, getStreamsCallback);

        Awaitility.await().until(hasCacheResult());
        assertFalse(cacheError);
        Awaitility.await().until(hasApiResult());
        assertFalse(apiError);

        // Should at least return the two children created above
        assertFalse(cacheStreams==null && apiStreams==null);

        // TODO: check filter handling in cache and uncomment this test
        // Check parentID of retrieved streams
        /*
        if(cacheStreams != null) {
            for(Stream stream: cacheStreams.values()) {
                assertEquals(testSupportStream.getId(),stream.getParentId());
            }
        }*/
        if(apiStreams != null) {
            for(Stream stream: apiStreams.values()) {
                assertEquals(testSupportStream.getId(),stream.getParentId());
            }
        }
    }

    @Test
    public void testGetStreamsWithStateSetToAllMustReturnTrashedStreamsAsWell() {
        // Create child
        Stream trashedChild = new Stream("trashedChild", "trashedChild");
        trashedChild.setParentId(testSupportStream.getId());
        connection.streams.create(trashedChild, streamsCallback);
        Awaitility.await().until(hasCacheResult());
        assertFalse(cacheError);
        Awaitility.await().until(hasApiResult());
        assertFalse(apiError);
        cacheSuccess = false;
        apiSuccess = false;

        // Trash child
        connection.streams.delete(trashedChild, false, streamsCallback);
        Awaitility.await().until(hasCacheResult());
        assertFalse(cacheError);
        Awaitility.await().until(hasApiResult());
        assertFalse(apiError);
        cacheSuccess = false;
        apiSuccess = false;

        // Get streams with state all Filter
        Filter filter = new Filter();
        //filter.setParentId(testSupportStream.getId());
        filter.setState(Filter.State.ALL);
        connection.streams.get(filter, getStreamsCallback);
        Awaitility.await().until(hasCacheResult());
        assertFalse(cacheError);
        Awaitility.await().until(hasApiResult());
        assertFalse(apiError);

        // TODO: check implementation in cache
        // Check that retrieved streams contain the trashed child
        assertNotNull(apiStreams);
        for (Stream stream: apiStreams.values()) {
            if (stream.getId().equals(testSupportStream.getId())) {
                Stream child = stream.getChildrenMap().get(trashedChild.getId());
                assertNotNull(child);
                assertTrue(child.isTrashed());
            }
        }
    }

    @Test
    public void testGetStreamsWithIncludeDeletionsMustReturnDeletedStreamsAsWell() {
        // Create child
        Stream deletedChild = new Stream("deletedChild", "deletedChild");
        deletedChild.setParentId(testSupportStream.getId());
        connection.streams.create(deletedChild, streamsCallback);
        Awaitility.await().until(hasCacheResult());
        assertFalse(cacheError);
        Awaitility.await().until(hasApiResult());
        assertFalse(apiError);
        cacheSuccess = false;
        apiSuccess = false;

        Double time = apiStream.getCreated();

        // Trash child
        connection.streams.delete(deletedChild, false, streamsCallback);
        Awaitility.await().until(hasCacheResult());
        assertFalse(cacheError);
        Awaitility.await().until(hasApiResult());
        assertFalse(apiError);
        cacheSuccess = false;
        apiSuccess = false;

        // Delete child
        connection.streams.delete(deletedChild, false, streamsCallback);
        Awaitility.await().until(hasCacheResult());
        assertFalse(cacheError);
        Awaitility.await().until(hasApiResult());
        assertFalse(apiError);
        cacheSuccess = false;
        apiSuccess = false;

        // Get streams with include deletions Filter
        Filter filter = new Filter();
        filter.setIncludeDeletionsSince(time);
        connection.streams.get(filter, getStreamsCallback);
        Awaitility.await().until(hasCacheResult());
        assertFalse(cacheError);
        Awaitility.await().until(hasApiResult());
        assertFalse(apiError);

        // TODO: check implementation in cache
        // Check that retrieved streams contain the deleted child
        assertNotNull(apiStreamDeletions);
        Double deletionTime = apiStreamDeletions.get(deletedChild.getId());
        assertNotNull(deletionTime);
        assertTrue(deletionTime>=time);
    }

    // TODO
    public void testGetStreamsMustReturnAnEmptyMapIfThereAreNoMatchingStreams() {

    }

    // TODO
    public void testGetStreamsMustIncludeDeletedStreamsWhenTheFlagIncludeDeletionsIsSet() {

    }

    // TODO check if possible
    public void testGetStreamsMustReturnAnErrorIfTheGivenFilterContainsInvalidParameters() {

    }

    /**
     * CREATE STREAM
     */

    @Test
    public void testCreateStreamMustAcceptAValidStream() {
        Stream newStream = new Stream("myNewId", "myNewStream");
        newStream.setParentId(testSupportStream.getId());
        connection.streams.create(newStream, streamsCallback);
        Awaitility.await().until(hasCacheResult());
        assertFalse(cacheError);
        Awaitility.await().until(hasApiResult());
        assertFalse(apiError);

        assertNotNull(cacheStream);
        TestUtils.checkStream(newStream, cacheStream);
        assertNotNull(apiStream);
        TestUtils.checkStream(newStream, apiStream);
    }

    @Test
    public void testCreateStreamMustReturnAnErrorIfAStreamWithTheSameNameExistsAtTheSameTreeLevel() {
        Stream someStream = new Stream("someStreamThatWillBotherNext", "my lovely stream name");
        someStream.setParentId(testSupportStream.getId());
        connection.streams.create(someStream, streamsCallback);
        Awaitility.await().until(hasCacheResult());
        assertFalse(cacheError);
        Awaitility.await().until(hasApiResult());
        assertFalse(apiError);
        cacheSuccess = false;
        apiSuccess = false;

        Stream duplicateIdStream = new Stream("copyNameSteam", someStream.getName());
        duplicateIdStream.setParentId(testSupportStream.getId());
        connection.streams.create(duplicateIdStream, streamsCallback);
        Awaitility.await().until(hasCacheResult());
        //assertFalse(cacheError);
        Awaitility.await().until(hasApiResult());
        assertFalse(apiSuccess);
    }

    @Test
    public void testCreateStreamMustReturnAnErrorIfAStreamWithTheSameIdAlreadyExists() {
        Stream someStream = new Stream("someStreamWithANiceId", "Well I dont care");
        someStream.setParentId(testSupportStream.getId());
        connection.streams.create(someStream, streamsCallback);
        Awaitility.await().until(hasCacheResult());
        assertFalse(cacheError);
        Awaitility.await().until(hasApiResult());
        assertFalse(apiError);
        cacheSuccess = false;
        apiSuccess = false;

        Stream duplicateIdStream = new Stream(someStream.getId(), "I will not be created");
        duplicateIdStream.setParentId(testSupportStream.getId());
        connection.streams.create(duplicateIdStream, streamsCallback);
        Awaitility.await().until(hasCacheResult());
        //assertFalse(cacheError);
        Awaitility.await().until(hasApiResult());
        assertFalse(apiSuccess);
    }

    // TODO check if possible
    public void testCreateStreamMustReturnAnErrorIfTheStreamDataIsInvalid() {

    }

    /**
     * UDPATE STREAM
     */

    // TODO
    public void testUpdateStreamMustAcceptAValidStream() {

    }

    // TODO
    public void testUpdateStreamMustUpdateTheStreamTreeWhenParentIdWasModified() {

    }

    // TODO
    public void testUpdateStreamMustReturnAnErrorIfNoSuchStreamExistYet() {

    }

    // TODO
    public void testUpdateStreamMustReturnAnErrorWhenIfAStreamWithTheSameNameExistsAtTheSameTreeLevel() {

    }

    /**
     * DELETE STREAM
     */

    // TODO
    public void testDeleteStreamMustAcceptAValidStream() {

    }

    // TODO
    public void testDeleteStreamCalledOnceMustTrashTheStream() {

    }

    // TODO
    public void testDeleteStreamCalledTwiceMustDeleteTheStreamAndReturnTheId() {

    }

    // TODO
    public void testDeleteStreamMustUpdateItsEventsStreamIdsWhenDeletingWithMergeEventsWithParent() {

    }

    // TODO
    public void testDeleteStreamMustDeleteItsEventsWhenDeletingWithoutMergeEventsWithParent() {

    }

    // TODO
    public void testDeleteStreamMustReturnAnErrorWhenTheGivenStreamDoesntExist() {

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
                apiEvents = apiEvents;
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
                cacheStreamDeletions = streamDeletions;
                cacheSuccess = true;
            }

            @Override
            public void onCacheError(String errorMessage) {
                logger.log(errorMessage);
                cacheError = true;
            }

            @Override
            public void apiCallback(Map<String, Stream> receivedStreams,
                                    Map<String, Double> receivedStreamDeletions, Double serverTime) {
                logger.log("apiCallback with " + receivedStreams.size() + " streams.");
                apiStreams = receivedStreams;
                apiStreamDeletions = receivedStreamDeletions;
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
