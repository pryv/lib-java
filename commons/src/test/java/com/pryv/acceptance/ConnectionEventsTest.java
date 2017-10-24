package com.pryv.acceptance;

import com.pryv.Connection;
import com.pryv.Filter;
import com.pryv.model.Attachment;
import com.pryv.model.Event;
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
import static org.junit.Assert.assertTrue;

public class ConnectionEventsTest {

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
    public void testCreateUpdateAndDeleteEvent() throws IOException {

        // create event
        Event testEvent = new Event();
        testEvent.setStreamId(testSupportStream.getId());
        testEvent.setType("note/txt");
        testEvent.setContent("this is the content");
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
        /* TODO: review delete return
        Event trashedEvent = connection.events.delete(updatedEvent.getId());
        assertTrue(trashedEvent.isTrashed());
        Event deletedEvent = connection.events.delete(trashedEvent);
        assertNull(deletedEvent);
        */
    }

    /**
     * GET EVENTS
     */

    @Test
    public void testGetEventsMustReturnNonTrashedEvents() throws IOException {
        List<Event> retrievedEvents = connection.events.get(new Filter());
        assertTrue(retrievedEvents.size() > 0);
        for (Event event: retrievedEvents) {
            assertFalse(event.isTrashed());
        }
    }

    @Test
    public void testGetEventsWithANullFilterShouldReturnNonTrashedEvents() throws IOException {
        List<Event> retrievedEvents = connection.events.get(null);
        assertTrue(retrievedEvents.size() > 0);
    }

    @Test
    public void testFetchEventsWithEmptyFilter() throws IOException {
        List<Event> retrievedEvents = connection.events.get(new Filter());
        assertNotNull(retrievedEvents);
        assertTrue(retrievedEvents.size() > 0);
    }

    // TODO add includeDeletions in Filter
    @Test
    public void testGetEventsMustReturnDeletedEventsWhenIncludeDeletionsIsSet() {
        Filter deletionsFilter = new Filter();
    }

    public void testGetEventsMustReturnEventsMatchingTheFilter() throws IOException {
        Filter filter = new Filter();
        int numLimit = 10;
        String type = "note/txt";
        filter.setLimit(numLimit);
        filter.addType(type);
        List<Event> retrievedEvents = connection.events.get(filter);
        assertTrue(retrievedEvents.size() == 10);
        for (Event event: retrievedEvents) {
            assertTrue(event.getType().equals(type));
        }
    }

    public void testGetEventsMustReturnAnEmptyMapWhenTheFilterMatchesNoEvents() throws IOException {
        Filter filter = new Filter();
        filter.setFromTime(10.0);
        filter.setToTime(11.0);
        List<Event> retrievedEvents = connection.events.get(filter);
        assertTrue(retrievedEvents.size() == 0);
    }

    @Test
    public void testFetchEventsForAStream() throws IOException {
        // create event
        Event testEvent = new Event();
        testEvent.setStreamId(testSupportStream.getId());
        testEvent.setType("note/txt");
        testEvent.setContent("this is a test Event. Please delete");
        connection.events.create(testEvent);

        // create filter
        Filter filter = new Filter();
        filter.addStream(testSupportStream);

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
    public void testCreateEventsMustAcceptAnEventWithMinimalParamsAndFillReadOnlyFields() throws IOException {
        Event minimalEvent = new Event();
        minimalEvent.setStreamId(testSupportStream.getId());
        minimalEvent.setType("note/txt");
        minimalEvent.setContent("I am used in create event test, please delete me");
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
    testCreateEventsMustReturnAStoppedIdWhenCalledInASingleActivityStreamWithARunningEvent() throws IOException {
        // create singleActivity Stream
        Stream singleAcivityStream = createSingleActivityStream();

        // create running Event
        Event runningEvent = new Event();
        runningEvent.setStreamId(singleAcivityStream.getId());
        runningEvent.setType("activity/plain");
        runningEvent.setDuration(null);
        Event createdEvent = connection.events.create(runningEvent);
        assertNotNull(createdEvent);
        runningEvent = createdEvent;
        String myStoppedId = runningEvent.getId();

        // create Event that will stop running event
        Event stopperEvent = new Event(singleAcivityStream.getId(), "activity/plain", null);
        connection.events.create(stopperEvent);
        // TODO compare stoppedId

        // delete singleActivity Stream
        deleteSingleAcitivityStream(singleAcivityStream);
    }

    // TODO same as other
    //@Test(expected = IOException.class)
    public void
    testCreateEventsMustReturnAnErrorWhenCalledInASingleActivityStreamAndPeriodsOverlap() throws IOException {
        Stream singleActivityStream = createSingleActivityStream();

        Double time = 1000.0;
        Double duration = 500.0;
        Event runningEvent = new Event();
        runningEvent.setStreamId(singleActivityStream.getId());
        runningEvent.setType("activity/plain");
        runningEvent.setTime(time);
        runningEvent.setDuration(duration);
        connection.events.create(runningEvent);

        Event invalidEvent = new Event();
        invalidEvent.setStreamId(singleActivityStream.getId());
        invalidEvent.setType("activity/plain");
        invalidEvent.setTime(time + duration / 2);
        invalidEvent.setDuration(duration);
        connection.events.create(invalidEvent);

        deleteSingleAcitivityStream(singleActivityStream);
    }

    @Test(expected = IOException.class)
    public void testMusReturnAnErrorWhenEventParametersAreInvalid() throws IOException {
        Event missingStreamIdEvent = new Event();
        missingStreamIdEvent.setType("note/txt");
        missingStreamIdEvent.setContent("i am missing a streamId, will generate apiError");
        connection.events.create(missingStreamIdEvent);
    }

    @Test
    public void testCreateEventsWithAttachmentWithValidDataMustWork() throws IOException {
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

    public void testUpdateEventMustAcceptAValidAEventAndReturnAFullEvent() throws IOException {
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
    public void testUpdateEventMustReturnAnErrorWhenEventDoesntExistYet() throws IOException {
        Event unexistingEvent = new Event(testSupportStream.getId(), "note/txt", "I dont exist and will generate an apiError");
        connection.events.update(unexistingEvent);
    }

    /**
     * DELETE EVENTS
     */

    @Test
    public void testDeleteEventMustReturnTheEventWithTrashedSetToTrueWhenDeletingOnce() throws IOException {
        Event eventToTrash = new Event(testSupportStream.getId(), "note/txt", "i will be trashed");
        Event createdEvent = connection.events.create(eventToTrash);
        eventToTrash = createdEvent;
        assertFalse(eventToTrash.isTrashed());

        /* Use this if returning an Event instead of a deletion id
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
    public void testDeleteEventMustReturnADeletionIdWhenDeletingTwice() throws IOException {
        // create event
        Event eventToDelete = new Event(testSupportStream.getId(), "note/txt", "i will be deleted");
        Event createdEvent = connection.events.create(eventToDelete);
        eventToDelete = createdEvent;

        // trash event
        /* Use this if returning an Event instead of a deletion id
        connection.events.delete(eventToDelete);
        assertNotNull(apiEvent);
        assertEquals(eventToDelete.getContent(), apiEvent.getContent());
        assertTrue(apiEvent.isTrashed());
        */
        String eventTrashingId = connection.events.delete(eventToDelete.getId());
        assertEquals(eventToDelete.getId(), eventTrashingId);

        // delete event
        /* Use this if returning an Event instead of a deletion id
        connection.events.delete(eventToDelete);
        assertNull(apiEvent);
        */
        String eventDeletionId = connection.events.delete(eventToDelete.getId());
        assertEquals(eventToDelete.getId(), eventDeletionId);
    }

    private Stream createSingleActivityStream() throws IOException {
        Stream singleActivityStream = new Stream();
        singleActivityStream.setId("singleActivityStream");
        singleActivityStream.setName("singleActivityStream");
        singleActivityStream.setSingleActivity(true);
        Stream createdStream = connection.streams.create(singleActivityStream);
        assertEquals(createdStream.getName(), singleActivityStream.getName());
        singleActivityStream = createdStream;
        return singleActivityStream;
    }

    private void deleteSingleAcitivityStream(Stream singleActivityStream) throws IOException {
        connection.streams.delete(singleActivityStream.getId(), false);
        connection.streams.delete(singleActivityStream.getId(), false);
    }

}
