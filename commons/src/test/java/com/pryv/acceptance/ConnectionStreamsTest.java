package com.pryv.acceptance;

import com.pryv.Connection;
import com.pryv.Filter;
import com.pryv.model.Event;
import com.pryv.model.Stream;
import com.pryv.util.TestUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import resources.TestCredentials;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ConnectionStreamsTest {

    private static Stream testSupportStream;

    private static Connection connection;


    @BeforeClass
    public static void setUp() throws IOException {

        connection = new Connection(TestCredentials.USERNAME, TestCredentials.TOKEN, TestCredentials.DOMAIN);

        testSupportStream = new Stream("onlineModuleStreamID", "javaLibTestSupportStream");
        Stream createdStream = connection.streams.create(testSupportStream);
        testSupportStream.merge(createdStream, true);
        assertNotNull(testSupportStream.getId());

        Event testEvent = new Event();
        testEvent.setStreamId(testSupportStream.getId());
        testEvent.setType("note/txt");
        testEvent.setContent("i am a test event");
        connection.events.create(testEvent);
    }

    @AfterClass
    public static void tearDown() throws IOException {
        connection.streams.delete(testSupportStream.getId(), false);
        connection.streams.delete(testSupportStream.getId(), false);
    }

    @Test
    public void testCreateUpdateAndDeleteStream() throws IOException {
        Stream testStream = new Stream("connectionStreamsTestStreamId", "connectionStreamsTestStreamId");

        // create
        Stream createdStream = connection.streams.create(testStream);
        assertNotNull(createdStream);
        assertNotNull(createdStream.getId());
        assertNotNull(createdStream.getCreated());
        assertNotNull(createdStream.getCreatedBy());
        assertNotNull(createdStream.getModified());
        assertNotNull(createdStream.getModifiedBy());

        // update
        String nameUpdate = "connectionStreamsTestNewStreamId";
        createdStream.setName(nameUpdate);
        Stream updatedStream = connection.streams.update(createdStream);
        assertEquals(nameUpdate, updatedStream.getName());

        // trash
        connection.streams.delete(updatedStream.getId(), false);
        /* TODO: review trash return
        Stream trashedStream = connection.streams.delete(updatedStream.getId(), false);
        assertNotNull(trashedStream);
        assertTrue(trashedStream.isTrashed());
        */

        // delete
        connection.streams.delete(updatedStream.getId(), false);
        /* TODO: review delete return
        Stream deletedStream = connection.streams.delete(trashedStream.getId(), false);
        assertNull(deletedStream);
        */
    }

    /**
     * GET STREAMS
     */

    @Test
    public void testFetchStreams() throws IOException {
        Map<String, Stream> retrievedStreams = connection.streams.get(null);
        assertNotNull(retrievedStreams);
        assertTrue(retrievedStreams.size() > 0);
    }

    @Test
    public void testGetStreamsMustReturnATreeOfNonTrashedStreamsWithANullFilter() throws IOException {
        Stream s1 = new Stream(null, "someStreamOne");
        s1.setParentId(testSupportStream.getId());
        Stream s2 = new Stream(null, "someOtherStream");
        s2.setParentId(testSupportStream.getId());
        connection.streams.create(s1);
        connection.streams.create(s2);
        Map<String, Stream> retrievedStreams = connection.streams.get(null);
        assertNotNull(retrievedStreams);

        connection.getRootStreams();
    }

    @Test
    public void testGetStreamsWithParentIdSetMustReturnStreamsMatchingTheGivenFilter() throws IOException {
        // Create children
        Stream childStream1 = new Stream("childStream1", "childStream1");
        childStream1.setParentId(testSupportStream.getId());
        Stream childStream2 = new Stream("childStream2", "childStream2");
        childStream2.setParentId(testSupportStream.getId());
        connection.streams.create(childStream1);
        connection.streams.create(childStream2);

        // Get streams with specified parentID in Filter
        Filter filter = new Filter();
        filter.setParentId(testSupportStream.getId());
        Map<String, Stream> retrievedStreams = connection.streams.get(filter);

        // Should at least return the two children created above
        assertNotNull(retrievedStreams);

        // TODO: check filter handling in cache and uncomment this test
        // Check parentID of retrieved streams
        /*
        if(cacheStreams != null) {
            for(Stream stream: cacheStreams.values()) {
                assertEquals(testSupportStream.getId(),stream.getParentId());
            }
        }*/
        for(Stream stream: retrievedStreams.values()) {
            assertEquals(testSupportStream.getId(),stream.getParentId());
        }
    }

    @Test
    public void testGetStreamsWithStateSetToAllMustReturnTrashedStreamsAsWell() throws IOException {
        // Create child
        Stream trashedChild = new Stream("trashedChild", "trashedChild");
        trashedChild.setParentId(testSupportStream.getId());
        connection.streams.create(trashedChild);

        // Trash child
        connection.streams.delete(trashedChild.getId(), false);

        // Get streams with state all Filter
        Filter filter = new Filter();
        //filter.setParentId(testSupportStream.getId());
        filter.setState(Filter.State.ALL);
        Map<String, Stream> retrievedStreams = connection.streams.get(filter);

        // Check that retrieved streams contain the trashed child
        assertNotNull(retrievedStreams);
        for (Stream stream: retrievedStreams.values()) {
            if (stream.getId().equals(testSupportStream.getId())) {
                Stream child = stream.getChildrenMap().get(trashedChild.getId());
                assertNotNull(child);
                assertTrue(child.isTrashed());
            }
        }
    }

    @Test
    public void testGetStreamsWithIncludeDeletionsMustReturnDeletedStreamsAsWell() throws IOException {
        // Create child
        Stream deletedChild = new Stream("deletedChild", "deletedChild");
        deletedChild.setParentId(testSupportStream.getId());
        Stream createdStream = connection.streams.create(deletedChild);

        Double time = createdStream.getCreated();

        // Trash child
        connection.streams.delete(deletedChild.getId(), false);

        // Delete child
        connection.streams.delete(deletedChild.getId(), false);

        // Get streams with include deletions Filter
        Filter filter = new Filter();
        filter.setIncludeDeletionsSince(time);
        connection.streams.get(filter);

        // Check that retrieved streams contain the deleted child
        /* TODO: handle deletions
        assertNotNull(apiStreamDeletions);
        Double deletionTime = apiStreamDeletions.get(deletedChild.getId());
        assertNotNull(deletionTime);
        assertTrue(deletionTime>=time);
        */
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
    public void testCreateStreamMustAcceptAValidStream() throws IOException {
        Stream newStream = new Stream("myNewId", "myNewStream");
        newStream.setParentId(testSupportStream.getId());
        Stream createdStream = connection.streams.create(newStream);

        assertNotNull(createdStream);
        TestUtils.checkStream(newStream, createdStream);
    }

    @Test(expected = IOException.class)
    public void testCreateStreamMustReturnAnErrorIfAStreamWithTheSameNameExistsAtTheSameTreeLevel() throws IOException {
        Stream someStream = new Stream("someStreamThatWillBotherNext", "my lovely stream name");
        someStream.setParentId(testSupportStream.getId());
        connection.streams.create(someStream);

        Stream duplicateIdStream = new Stream("copyNameSteam", someStream.getName());
        duplicateIdStream.setParentId(testSupportStream.getId());
        connection.streams.create(duplicateIdStream);
    }

    @Test(expected = IOException.class)
    public void testCreateStreamMustReturnAnErrorIfAStreamWithTheSameIdAlreadyExists() throws IOException {
        Stream someStream = new Stream("someStreamWithANiceId", "Well I dont care");
        someStream.setParentId(testSupportStream.getId());
        connection.streams.create(someStream);

        Stream duplicateIdStream = new Stream(someStream.getId(), "I will not be created");
        duplicateIdStream.setParentId(testSupportStream.getId());
        connection.streams.create(duplicateIdStream);
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

}
