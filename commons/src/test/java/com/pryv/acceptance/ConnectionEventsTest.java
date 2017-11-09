package com.pryv.acceptance;

import com.pryv.Connection;
import com.pryv.exceptions.ApiException;
import com.pryv.model.Attachment;
import com.pryv.model.Event;
import com.pryv.model.Filter;
import com.pryv.model.Stream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import resources.TestCredentials;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ConnectionEventsTest {

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
        connection.streams.delete(testSupportStream.getId(), false);
        connection.streams.delete(testSupportStream.getId(), false);
    }

    @Test
    public void testCreateUpdateAndDeleteEvent() throws IOException, ApiException {

        // create event
        Event testEvent = new Event()
                .setStreamId(testSupportStream.getId())
                .setType("note/txt")
                .setContent("this is the content");

        Event createdEvent = connection.events.create(testEvent);

        assertNotNull(createdEvent);
        assertNotNull(createdEvent.getId());
        assertNotNull(createdEvent.getCreated());
        assertNotNull(createdEvent.getCreatedBy());
        assertNotNull(createdEvent.getModified());
        assertNotNull(createdEvent.getModifiedBy());

        // update event
        String newContent = "updated content";
        createdEvent.setContent(newContent);

        Event updatedEvent = connection.events.update(createdEvent);

        assertEquals(updatedEvent.getContent(), newContent);

        // delete event
        /* TODO: Use this if returning an Event instead of a deletion id
        Event trashedEvent = connection.events.delete(updatedEvent.getId());
        assertTrue(trashedEvent.isTrashed());
        Event deletedEvent = connection.events.delete(trashedEvent);
        assertNull(deletedEvent);
        */
        String trashedEventId = connection.events.delete(updatedEvent.getId());
        assertEquals(updatedEvent.getId(), trashedEventId);

        String deletedEventId = connection.events.delete(updatedEvent.getId());
        assertEquals(updatedEvent.getId(), deletedEventId);
    }

    /**
     * GET EVENTS
     */

    @Test
    public void testGetEventsMustReturnNonTrashedEvents() throws IOException, ApiException {
        List<Event> retrievedEvents = connection.events.get(new Filter());
        assertTrue(retrievedEvents.size() > 0);
        for (Event event: retrievedEvents) {
            assertFalse(event.isTrashed());
        }
    }

    @Test
    public void testGetEventsWithANullFilterShouldReturnNonTrashedEvents() throws IOException, ApiException {
        List<Event> retrievedEvents = connection.events.get(null);
        assertTrue(retrievedEvents.size() > 0);
    }

    @Test
    public void testFetchEventsWithEmptyFilter() throws IOException, ApiException {
        List<Event> retrievedEvents = connection.events.get(new Filter());
        assertNotNull(retrievedEvents);
        assertTrue(retrievedEvents.size() > 0);
    }

    // TODO add includeDeletions in Filter
    @Test
    public void testGetEventsMustReturnDeletedEventsWhenIncludeDeletionsIsSet() {
        Filter deletionsFilter = new Filter();
    }

    public void testGetEventsMustReturnEventsMatchingTheFilter() throws IOException, ApiException {
        int numLimit = 10;
        String type = "note/txt";

        Filter filter = new Filter()
                .setLimit(numLimit)
                .addType(type);

        List<Event> retrievedEvents = connection.events.get(filter);

        assertTrue(retrievedEvents.size() == 10);
        for (Event event: retrievedEvents) {
            assertTrue(event.getType().equals(type));
        }
    }

    public void testGetEventsMustReturnAnEmptyMapWhenTheFilterMatchesNoEvents() throws IOException, ApiException {
        Filter filter = new Filter()
                .setFromTime(10.0)
                .setToTime(11.0);

        List<Event> retrievedEvents = connection.events.get(filter);

        assertTrue(retrievedEvents.size() == 0);
    }

    @Test
    public void testFetchEventsForAStream() throws IOException, ApiException {
        // create event
        Event testEvent = new Event()
                .setStreamId(testSupportStream.getId())
                .setType("note/txt")
                .setContent("this is a test Event. Please delete");

        connection.events.create(testEvent);

        // create filter
        Filter filter = new Filter().addStream(testSupportStream);

        // fetch events
        List<Event> retrievedEvents = connection.events.get(filter);

        assertTrue(retrievedEvents.size() > 0);
        for (Event event : retrievedEvents) {
            assertEquals(event.getStreamId(), testSupportStream.getId());
        }
    }

    /**
     * CREATE EVENTS
     */

    @Test
    public void testCreateEventsMustAcceptAnEventWithMinimalParamsAndFillReadOnlyFields() throws IOException, ApiException {
        Event minimalEvent = new Event()
                .setStreamId(testSupportStream.getId())
                .setType("note/txt")
                .setContent("I am used in create event test, please delete me");

        Event createdEvent = connection.events.create(minimalEvent);

        assertNotNull(createdEvent);
        assertNotNull(createdEvent.getId());
        assertNotNull(createdEvent.getTime());
        assertNotNull(createdEvent.getModified());
        assertNotNull(createdEvent.getModifiedBy());
        assertNotNull(createdEvent.getCreated());
        assertNotNull(createdEvent.getCreatedBy());
        assertNotNull(createdEvent.getTags());
        assertEquals(createdEvent.getType(), minimalEvent.getType());
        assertEquals(createdEvent.getContent(), minimalEvent.getContent());
        assertEquals(createdEvent.getStreamId(), minimalEvent.getStreamId());
    }

    // TODO implement events.start in lib java
    // Currenty not working: to create a running event using events.create, you must provide the API
    // with an event with duration=null, but the Java lib doesn't serialize a field if it is set to
    // null
    // TODO move all the singleActivity related tests in a separate test class
    //@Test
    public void
    testCreateEventsMustReturnAStoppedIdWhenCalledInASingleActivityStreamWithARunningEvent() throws IOException, ApiException {
        // create singleActivity Stream
        Stream singleAcivityStream = createSingleActivityStream();

        // create running Event
        Event runningEvent = new Event()
                .setStreamId(singleAcivityStream.getId())
                .setType("activity/plain")
                .setDuration(null);

        Event createdEvent = connection.events.create(runningEvent);

        assertNotNull(createdEvent);
        runningEvent = createdEvent;
        String myStoppedId = runningEvent.getId();

        // create Event that will stop running event
        Event stopperEvent = new Event(singleAcivityStream.getId(), "activity/plain", null);

        connection.events.create(stopperEvent);
        // TODO compare stoppedId

        // delete singleActivity Stream
        deleteSingleActivityStream(singleAcivityStream);
    }

    // TODO same as other
    //@Test(expected = IOException.class)
    public void
    testCreateEventsMustReturnAnErrorWhenCalledInASingleActivityStreamAndPeriodsOverlap() throws IOException, ApiException {
        Stream singleActivityStream = createSingleActivityStream();

        Double time = 1000.0;
        Double duration = 500.0;

        Event runningEvent = new Event()
                .setStreamId(singleActivityStream.getId())
                .setType("activity/plain")
                .setTime(time)
                .setDuration(duration);

        connection.events.create(runningEvent);

        Event invalidEvent = new Event()
                .setStreamId(singleActivityStream.getId())
                .setType("activity/plain")
                .setTime(time + duration / 2)
                .setDuration(duration);

        connection.events.create(invalidEvent);

        deleteSingleActivityStream(singleActivityStream);
    }

    @Test
    public void testMusReturnAnErrorWhenEventParametersAreInvalid() throws IOException, ApiException {
        Event missingStreamIdEvent = new Event()
                .setType("note/txt")
                .setContent("i am missing a streamId, will generate apiError");
        try {
            connection.events.create(missingStreamIdEvent);
        } catch (ApiException e) {
            assertNotNull(e);
            assertNotNull(e.getId());
            assertNotNull(e.getMsg());
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void testCreateEventsWithAttachmentWithValidDataMustWork() throws IOException, ApiException {
        File attachmentFile = new File(getClass().getClassLoader().getResource("resources/photo.PNG").getPath());

        Attachment attachment = new Attachment()
                .setFile(attachmentFile)
                .setType("image/png")
                .setFileName(attachmentFile.getName());

        assertTrue(attachment.getFile().length() > 0);

        // create encapsulating event
        Event eventWithAttachment = new Event()
                .setStreamId(testSupportStream.getId())
                .addAttachment(attachment)
                .setStreamId(testSupportStream.getId())
                .setType("picture/attached")
                .setDescription("This is a test event with an image.");

        // create event with attachment
        Event createdEvent = connection.events.create(eventWithAttachment);

        assertNotNull(createdEvent);
        assertNotNull(createdEvent.getAttachments());
        assertEquals(createdEvent.getAttachments().size(), 1);
        Attachment createdAttachment = createdEvent.getFirstAttachment();
        assertNotNull(createdAttachment.getId());
    }

    /**
     * UPDATE EVENTS
     */

    public void testUpdateEventMustAcceptAValidAEventAndReturnAFullEvent() throws IOException, ApiException {
        Event eventToUpdate = new Event(testSupportStream.getId(),
                "note/txt", "i will be updated");
        Event createdEvent = connection.events.create(eventToUpdate);
        assertNotNull(createdEvent);

        Event initialEvent = createdEvent;
        assertNotEquals(eventToUpdate, initialEvent);
        assertEquals(initialEvent.getContent(), eventToUpdate.getContent());

        eventToUpdate.setContent("i have beeen updated");

        Event updatedEvent = connection.events.update(eventToUpdate);

        assertNotNull(updatedEvent);
        assertEquals(updatedEvent.getId(), initialEvent.getId());
        assertEquals(updatedEvent.getContent(), eventToUpdate.getContent());
    }

    //@Test(expected = IOException.class)
    public void testUpdateEventMustReturnAnErrorWhenEventDoesntExistYet() throws IOException, ApiException {
        Event unexistingEvent = new Event(testSupportStream.getId(), "note/txt", "I dont exist and will generate an apiError");
        connection.events.update(unexistingEvent);
    }

    /**
     * DELETE EVENTS
     */

    @Test
    public void testDeleteEventMustReturnTheEventWithTrashedSetToTrueWhenDeletingOnce() throws IOException, ApiException {
        Event eventToTrash = new Event(testSupportStream.getId(), "note/txt", "i will be trashed");
        Event createdEvent = connection.events.create(eventToTrash);
        eventToTrash = createdEvent;
        assertFalse(eventToTrash.isTrashed());

        /* TODO: Use this if returning an Event instead of a deletion id
        connection.events.delete(eventToTrash);
        assertNotNull(apiEvent);
        assertEquals(eventToTrash.getContent(), apiEvent.getContent());
        assertTrue(apiEvent.isTrashed());
        */
        String eventDeletionId = connection.events.delete(eventToTrash.getId());
        assertEquals(eventToTrash.getId(), eventDeletionId);
    }

    // TODO retrieve deletionId
    @Test
    public void testDeleteEventMustReturnADeletionIdWhenDeletingTwice() throws IOException, ApiException {
        // create event
        Event eventToDelete = new Event(testSupportStream.getId(), "note/txt", "i will be deleted");
        Event createdEvent = connection.events.create(eventToDelete);
        eventToDelete = createdEvent;

        // trash event
        /* TODO: Use this if returning an Event instead of a deletion id
        connection.events.delete(eventToDelete);
        assertNotNull(apiEvent);
        assertEquals(eventToDelete.getContent(), apiEvent.getContent());
        assertTrue(apiEvent.isTrashed());
        */
        String eventTrashingId = connection.events.delete(eventToDelete.getId());
        assertEquals(eventToDelete.getId(), eventTrashingId);

        // delete event
        /* TODO: Use this if returning an Event instead of a deletion id
        connection.events.delete(eventToDelete);
        assertNull(apiEvent);
        */
        String eventDeletionId = connection.events.delete(eventToDelete.getId());
        assertEquals(eventToDelete.getId(), eventDeletionId);
    }

    private Stream createSingleActivityStream() throws IOException, ApiException {
        Stream singleActivityStream = new Stream()
                .setId("singleActivityStream")
                .setName("singleActivityStream")
                .setSingleActivity(true);

        Stream createdStream = connection.streams.create(singleActivityStream);

        assertEquals(createdStream.getName(), singleActivityStream.getName());

        return createdStream;
    }

    private void deleteSingleActivityStream(Stream singleActivityStream) throws IOException, ApiException {
        connection.streams.delete(singleActivityStream.getId(), false);
        connection.streams.delete(singleActivityStream.getId(), false);
    }

}
