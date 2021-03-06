package com.pryv.acceptance;

import com.pryv.Connection;
import com.pryv.exceptions.ApiException;
import com.pryv.model.Event;
import com.pryv.model.Filter;
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ConnectionStreamsTest {

    private static Stream testSupportStream;

    private static Connection connection;


    @BeforeClass
    public static void setUp() throws IOException, ApiException {

        connection = new Connection(TestCredentials.USERNAME, TestCredentials.TOKEN, TestCredentials.DOMAIN);

        testSupportStream = new Stream("onlineModuleStreamID", "javaLibTestSupportStream");
        Stream createdStream = connection.streams.create(testSupportStream);
        assertNotNull(createdStream.getId());

        Event testEvent = new Event()
                .setStreamId(testSupportStream.getId())
                .setType("note/txt")
                .setContent("i am a test event");

        connection.events.create(testEvent);
    }

    @AfterClass

    public static void tearDown() throws IOException, ApiException {
        connection.streams.delete(testSupportStream, false);
        connection.streams.delete(testSupportStream, false);
    }

    @Test
    public void testCreateUpdateAndDeleteStream() throws IOException, ApiException {
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
        Stream trashedStream = connection.streams.delete(updatedStream, false);
        assertNotNull(trashedStream);
        assertTrue(trashedStream.isTrashed());

        // delete
        Stream deletedStream = connection.streams.delete(trashedStream, false);
        assertNotNull(deletedStream);
        assertTrue(deletedStream.isDeleted());
    }

    /**
     * GET STREAMS
     */

    @Test
    public void testFetchStreams() throws IOException, ApiException {
        Map<String, Stream> retrievedStreams = connection.streams.get(null);
        assertNotNull(retrievedStreams);
        assertTrue(retrievedStreams.size() > 0);
    }

    @Test
    public void testGetStreamsMustReturnATreeOfNonTrashedStreamsWithANullFilter() throws IOException, ApiException {
        Stream s1 = new Stream(null, "someStreamOne")
                .setParentId(testSupportStream.getId());
        Stream s2 = new Stream(null, "someOtherStream")
                .setParentId(testSupportStream.getId());

        connection.streams.create(s1);
        connection.streams.create(s2);

        Map<String, Stream> retrievedStreams = connection.streams.get(null);
        assertNotNull(retrievedStreams);

        connection.getRootStreams();
    }

    @Test
    public void testGetStreamsWithParentIdSetMustReturnStreamsMatchingTheGivenFilter() throws IOException, ApiException {
        // Create children
        Stream childStream1 = new Stream("childStream1", "childStream1")
                .setParentId(testSupportStream.getId());
        Stream childStream2 = new Stream("childStream2", "childStream2")
                .setParentId(testSupportStream.getId());

        connection.streams.create(childStream1);
        connection.streams.create(childStream2);

        // Get streams with specified parentID in Filter
        Filter filter = new Filter()
                .setParentId(testSupportStream.getId());

        Map<String, Stream> retrievedStreams = connection.streams.get(filter);

        // Should at least return the two children created above
        assertNotNull(retrievedStreams);

        for(Stream stream: retrievedStreams.values()) {
            assertEquals(testSupportStream.getId(),stream.getParentId());
        }
    }

    @Test
    public void testGetStreamsWithStateSetToAllMustReturnTrashedStreamsAsWell() throws IOException, ApiException {
        // Create child
        Stream trashedChild = new Stream("trashedChild", "trashedChild")
                .setParentId(testSupportStream.getId());
        connection.streams.create(trashedChild);

        // Trash child
        connection.streams.delete(trashedChild, false);

        // Get streams with state all Filter
        Filter filter = new Filter()
                .setState(Filter.State.ALL);
        //filter.setParentId(testSupportStream.getId());

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
    public void testGetStreamsWithIncludeDeletionsMustReturnDeletedStreamsAsWell() throws IOException, ApiException {
        // Create child
        Stream deletedChild = new Stream("deletedChild", "deletedChild")
                .setParentId(testSupportStream.getId());

        Stream createdStream = connection.streams.create(deletedChild);
        Double time = createdStream.getCreated();

        // Trash child
        connection.streams.delete(deletedChild, false);

        // Delete child
        connection.streams.delete(deletedChild, false);

        // Get streams with include deletions Filter
        Filter filter = new Filter()
                .setIncludeDeletionsSince(time);

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
    public void testCreateStreamMustAcceptAValidStream() throws IOException, ApiException {
        Stream newStream = new Stream("myNewId", "myNewStream")
                .setParentId(testSupportStream.getId());

        Stream createdStream = connection.streams.create(newStream);

        assertNotNull(createdStream);
        TestUtils.checkStream(newStream, createdStream);
    }

    @Test
    public void testCreateStreamMustReturnAnErrorIfAStreamWithTheSameNameExistsAtTheSameTreeLevel() throws IOException, ApiException {
        Stream someStream = new Stream("someStreamThatWillBotherNext", "my lovely stream name")
                .setParentId(testSupportStream.getId());

        connection.streams.create(someStream);

        Stream duplicateIdStream = new Stream("copyNameSteam", someStream.getName())
                .setParentId(testSupportStream.getId());

        try {
            connection.streams.create(duplicateIdStream);
        } catch (ApiException e) {
            assertNotNull(e);
            assertNotNull(e.getId());
            assertNotNull(e.getMsg());
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void testCreateStreamMustReturnAnErrorIfAStreamWithTheSameIdAlreadyExists() throws IOException, ApiException {
        Stream someStream = new Stream("someStreamWithANiceId", "Well I dont care")
                .setParentId(testSupportStream.getId());

        connection.streams.create(someStream);

        Stream duplicateIdStream = new Stream(someStream.getId(), "I will not be created")
                .setParentId(testSupportStream.getId());

        try {
            connection.streams.create(duplicateIdStream);
        } catch (ApiException e) {
            assertNotNull(e);
            assertNotNull(e.getId());
            assertNotNull(e.getMsg());
        } catch (Exception e) {
            assertNull(e);
        }
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
