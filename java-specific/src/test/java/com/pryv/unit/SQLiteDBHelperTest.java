package com.pryv.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.jayway.awaitility.Awaitility;
import com.pryv.SQLiteDBHelper;
import com.pryv.interfaces.EventsCallback;
import com.pryv.Filter;
import com.pryv.interfaces.GetEventsCallback;
import com.pryv.interfaces.GetStreamsCallback;
import com.pryv.interfaces.StreamsCallback;
import com.pryv.database.DBinitCallback;
import com.pryv.model.Attachment;
import com.pryv.model.Event;
import com.pryv.model.Stream;
import com.pryv.utils.Logger;

/**
 * unit tests for SQLiteDBHelper class
 *
 * @author ik
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SQLiteDBHelperTest {

  private static SQLiteDBHelper db;

  private static Logger logger = Logger.getInstance();

  private static EventsCallback eventsCallback;
  private static GetEventsCallback getEventsCallback;
  private static StreamsCallback streamsCallback;
  private static GetStreamsCallback getStreamsCallback;
  
  private static List<Event> cacheEvents;
  private static Map<String, Stream> cacheStreams;
  
  private static Event cacheEvent;
  private static Stream cacheStream;

  private static boolean cacheSuccess = false;
  private static boolean cacheError = false;

  private static final long MODIFIED_INCREMENT = 50;

  // executed before all tests once
  @BeforeClass
  public static void beforeClass() {
    cacheEvent = DummyData.generateFullEvent();
    cacheStream = DummyData.generateFullStream();

    instanciateEventsCallback();
    instanciateGetEventsCallback();
    instanciateStreamsCallback();
    instanciateGetStreamsCallback();

    String cacheFolder = "cache/test/";
    new File(cacheFolder).mkdirs();
    db = new SQLiteDBHelper(null, cacheFolder, null, null, new DBinitCallback() {

      @Override
      public void onError(String message) {
        System.out.println(message);
      }
    });

  }

  @AfterClass
  public static void cleanUpTestDB() {
    System.out.println("SQLiteDBHelperTest: clean up phase:");
    cacheEvent = null;
    db.getEvents(null, getEventsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    cacheSuccess = false;
    for (Event event : cacheEvents) {
      System.out.println("trashing event with id="
        + event.getId());
      db.deleteEvent(event, eventsCallback);
      Awaitility.await().until(hasCacheResult());
      assertFalse(cacheError);
      cacheSuccess = false;
      if (cacheEvent != null) {
        System.out.println("deleting again: " + cacheEvent.getId());
        db.deleteEvent(event, eventsCallback);
        Awaitility.await().until(hasCacheResult());
        assertFalse(cacheError);
        cacheSuccess = false;
      }
      cacheEvent = null;
    }

    cacheStream = null;
    db.getStreams(getStreamsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    cacheSuccess = false;
    System.out.println("going to delete " + cacheStreams.size() + " streams");
    for (Stream stream : cacheStreams.values()) {
      System.out.println("deleting stream with id=" + stream.getId());
      stream.setModified(stream.getModified() + MODIFIED_INCREMENT);
      db.deleteStream(stream, false, streamsCallback);
      Awaitility.await().until(hasCacheResult());
      assertFalse(cacheError);
      cacheSuccess = false;
      if (cacheStream != null) {
        System.out.println("deleting for real stream with id=" + cacheStream.getId());
        db.deleteStream(stream, false, streamsCallback);
        Awaitility.await().until(hasCacheResult());
        assertFalse(cacheError);
        cacheSuccess = false;
      }
    }
  }

  @Before
  public void beforeEachTest() {
    cacheSuccess = false;
    cacheError = false;
    cacheEvent = null;
    cacheStream = null;
    cacheEvents = null;
    cacheStreams = null;
  }

  /**
   * EVENTS
   */

  @Test
  public void testInsertIncompleteEventShouldGenerateError() {
    System.out.println("test2");
    Event emptyEvent = new Event();
    emptyEvent.setId("yolo");
    emptyEvent.setStreamId("testStreamId");
    emptyEvent.setContent("i am missing mandatory fields");
    db.createEvent(emptyEvent, eventsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheSuccess);
  }

  @Test
  public void testInsertValidEvent() {
    Event newEvent = new Event();
    newEvent.setId("newEventId");
    newEvent.setStreamId("myStreamId");
    newEvent.setType("activity/plain");
    db.createEvent(newEvent, eventsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    cacheSuccess = false;

    db.getEvents(new Filter(), getEventsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    assertNotNull(getEventById(newEvent.getId(), cacheEvents));
  }

  // TODO implement modified_time comparison for this to work
  @Test
  public void testUpdateEventShouldDoNothingBecauseOfModifiedFields() {
    Event eventToUpdate = new Event();
    eventToUpdate.setId("myUpdatedEventId");
    eventToUpdate.setStreamId("someStreamId");
    eventToUpdate.setType("note/txt");
    eventToUpdate.setContent("I will be updated youhou");
    eventToUpdate.setModified(1000.0);

    db.createEvent(eventToUpdate, eventsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    cacheSuccess = false;

    Event updatedEvent = new Event(eventToUpdate.getStreamId(), eventToUpdate.getType(), null);
    updatedEvent.setId(eventToUpdate.getId());
    updatedEvent.setContent("new updated content, " +
            "shouldnt be stored in cache because of modified time");
    db.updateEvent(updatedEvent, eventsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    cacheSuccess = false;

    db.getEvents(new Filter(), getEventsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    Event notModifiedEvent = getEventById(eventToUpdate.getId(), cacheEvents);
    assertEquals(notModifiedEvent.getContent(), eventToUpdate.getContent());
  }

  @Test
  public void testUpdateEventShouldModify() {
    Event eventToUpdate = new Event();
    eventToUpdate.setId("myUpdatedEventId");
    eventToUpdate.setStreamId("someStreamId");
    eventToUpdate.setType("note/txt");
    eventToUpdate.setContent("I will be updated youhou");
    eventToUpdate.setModified(1000.0);

    db.createEvent(eventToUpdate, eventsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    cacheSuccess = false;

    Event updatedEvent = new Event(eventToUpdate.getStreamId(), eventToUpdate.getType(), null);
    updatedEvent.setId(eventToUpdate.getId());
    updatedEvent.setContent("new updated content, " +
            "should be stored because modified time is later");
    updatedEvent.setModified(eventToUpdate.getModified() + MODIFIED_INCREMENT);
    db.updateEvent(updatedEvent, eventsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    cacheSuccess = false;

    db.getEvents(new Filter(), getEventsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    Event modifiedEvent = getEventById(eventToUpdate.getId(), cacheEvents);
    assertNotEquals(modifiedEvent.getContent(), eventToUpdate.getContent());
  }

  @Test
  public void testDeleteEventCalledOnceShouldUpdateItAsTrashed() {
    Event eventToTrash = new Event();
    eventToTrash.setId("newEventId");
    eventToTrash.setStreamId("myStreamId");
    eventToTrash.setType("activity/plain");
    db.createEvent(eventToTrash, eventsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    cacheSuccess = false;

    db.deleteEvent(eventToTrash, eventsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    cacheSuccess = false;

    db.getEvents(null, getEventsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    assertNotNull(cacheEvents);
    Event trashedEvent = getEventById(eventToTrash.getId(), cacheEvents);
    assertEquals(true, trashedEvent.isTrashed());
  }

  @Test
  public void testDeleteEventCalledTwiceShouldDeleteIt() {
    Event eventToDelete = new Event();
    eventToDelete.setId("newEventId");
    eventToDelete.setStreamId("myStreamId");
    eventToDelete.setType("activity/plain");
    db.createEvent(eventToDelete, eventsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    cacheSuccess = false;

    db.deleteEvent(eventToDelete, eventsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    cacheSuccess = false;

    db.deleteEvent(eventToDelete, eventsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    cacheSuccess = false;

    db.getEvents(null, getEventsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    assertNull(getEventById(eventToDelete.getId(), cacheEvents));
  }

  @Test
  public void testGetEventsShouldReturnEventsMatchingTheProvidedFilter() {
    Event noteEvent = new Event("myStream", "note/txt", "hi");
    noteEvent.setId("myId");
    db.createEvent(noteEvent, eventsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    cacheSuccess = false;

    Event activityEvent = new Event("myStream", "activity/plain", null);
    activityEvent.setId("otherId");
    db.createEvent(activityEvent, eventsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    cacheSuccess = false;

    Filter filter = new Filter();
    String filterType = "note/txt";
    filter.addType(filterType);
    db.getEvents(filter, getEventsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    assertTrue(cacheEvents.size() > 0);
    for (Event event: cacheEvents) {
      assertEquals(event.getType(), filterType);
    }
  }

  /**
   * STREAMS
   */

  @Test
  public void createStreamWithMissingRequiredFieldsShouldGenerateAnError() {
    Stream incorrectStream = new Stream("myId", null);
    db.updateOrCreateStream(incorrectStream, streamsCallback);
    Awaitility.await().until(hasCacheResult());
    assertTrue(cacheError);
  }

  @Test
  public void createStreamWithValidFieldsShouldPass() {
    Stream validStream = new Stream("myValidStreamId", "Valid stream yo");
    db.updateOrCreateStream(validStream, streamsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    cacheSuccess = false;

    db.getStreams(getStreamsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    assertNotNull(cacheStreams.get(validStream.getId()));
  }

  @Test
  public void testUpdateStreamShouldNotModifyEventIfItsField() {
    System.out.println("test update full stream START");
    String newStreamName = "myNewStreamName - dadadaa";
    assertNotNull(cacheStream);
    cacheStream.setName(newStreamName);
    db.updateOrCreateStream(cacheStream, streamsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    cacheSuccess = false;
    db.getStreams(getStreamsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    assertEquals(newStreamName, cacheStreams.get(cacheStream.getId()).getName());
  }

  @Test
  public void test10retrieveStreams() {
    db.getStreams(getStreamsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    assertNotNull(cacheStream);
    assertTrue(cacheStreams.get(cacheStream.getId()) != null);
    assertTrue(cacheStreams.get(cacheStream.getId()).getChildren().size() != 0);
  }

  @Test
  public void test11RemoveFullStream() {
    System.out.println("SQLiteDBHelperTest: test11RemoveFullStream");
    assertNotNull(cacheStream);
    db.deleteStream(cacheStream, false, streamsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    cacheSuccess = false;
    assertTrue(cacheStream.isTrashed());
    db.deleteStream(cacheStream, false, streamsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    cacheSuccess = false;
    db.getStreams(getStreamsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    for (Stream stream : cacheStreams.values()) {
      if (stream.getId().equals(cacheStream.getId())) {
        fail("stream delete fail");
      }
    }
  }

  @Test
  public void test14RetrieveCorrectEvent() {
    Event testedEvent = DummyData.generateFullEvent();
    String newId = "myNewID";
    testedEvent.setId(newId);
    db.createEvent(testedEvent, eventsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    cacheSuccess = false;
    db.getEvents(null, getEventsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    cacheSuccess = false;
    Event retrievedEvent = getEventById(newId, cacheEvents);
    assertEquals(testedEvent.getId(), retrievedEvent.getId());
    assertEquals(testedEvent.getStreamId(), retrievedEvent.getStreamId());
    assertEquals(testedEvent.getTime(), retrievedEvent.getTime());
    assertEquals(testedEvent.getDuration(), retrievedEvent.getDuration());
    assertEquals(testedEvent.getType(), retrievedEvent.getType());
    assertEquals(testedEvent.getContent(), retrievedEvent.getContent());
    for (String tag : testedEvent.getTags()) {
      assertTrue(retrievedEvent.getTags().contains(tag));
    }
    assertTrue(retrievedEvent.getReferences().containsAll(testedEvent.getReferences()));
    // test attachments
    for (Attachment testedAttachment : testedEvent.getAttachments()) {
      boolean attachmentsMatch = false;
      for (Attachment trueAttachment : retrievedEvent.getAttachments()) {
        if (testedAttachment.getId().equals(trueAttachment.getId())) {
          attachmentsMatch = true;
          assertEquals(trueAttachment.getFileName(), testedAttachment.getFileName());
          assertEquals(trueAttachment.getReadToken(), testedAttachment.getReadToken());
          assertEquals(trueAttachment.getType(), testedAttachment.getType());
          assertTrue(trueAttachment.getSize() == testedAttachment.getSize());
        }
      }
      assertTrue(attachmentsMatch);
    }
    assertEquals(testedEvent.formatClientDataAsString(), retrievedEvent.formatClientDataAsString());
    assertEquals(testedEvent.isTrashed(), retrievedEvent.isTrashed());
    assertEquals(testedEvent.getCreated(), retrievedEvent.getCreated());
    assertEquals(testedEvent.getCreatedBy(), retrievedEvent.getCreatedBy());
    assertEquals(testedEvent.getModified(), retrievedEvent.getModified());
    assertEquals(testedEvent.getModifiedBy(), retrievedEvent.getModifiedBy());
    testedEvent.setTrashed(true);
    testedEvent.setModified(testedEvent.getModified() + MODIFIED_INCREMENT);
    db.deleteEvent(testedEvent, eventsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
  }

  @Test
  public void test13RetrieveStreamCorrectly() {
    Stream testedStream = DummyData.generateFullStream();
    String newId = "myNewStreamId";
    testedStream.setId(newId);
    int i = 1;
    for (Stream childStream : testedStream.getChildren()) {
      childStream.setId("childid" + i);
      childStream.setParentId(newId);
      i++;
    }
    System.out.println("inserting stream with id: " + newId);
    db.updateOrCreateStream(testedStream, streamsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    cacheSuccess = false;
    db.getStreams(getStreamsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    Stream retrievedStream = cacheStreams.get(newId);
    assertEquals(testedStream.getName(), retrievedStream.getName());
    assertEquals(testedStream.getParentId(), retrievedStream.getParentId());
    assertEquals(testedStream.isSingleActivity(), retrievedStream.isSingleActivity());
    assertEquals(testedStream.formatClientDataAsString(),
      retrievedStream.formatClientDataAsString());
    boolean childrenMatch = false;
    for (Stream testedChild : testedStream.getChildren()) {
      childrenMatch = false;
      for (Stream retrievedChild : retrievedStream.getChildren()) {
        if (testedChild.getId().equals(retrievedChild.getId())) {
          childrenMatch = true;
        }
      }
    }
    assertTrue(childrenMatch);
    assertEquals(testedStream.isTrashed(), retrievedStream.isTrashed());
    assertEquals(testedStream.getCreated(), retrievedStream.getCreated());
    assertEquals(testedStream.getCreatedBy(), retrievedStream.getCreatedBy());
    assertEquals(testedStream.getModified(), retrievedStream.getModified());
    assertEquals(testedStream.getModifiedBy(), retrievedStream.getModifiedBy());
  }

  @Test
  public void testInsertAndRetrieveEventWithNullAttachments() {
    Event eventWithoutAttachments = DummyData.generateFullEvent();
    eventWithoutAttachments.setAttachments(null);
    String id = "eventWithoutAttachmentsID";
    eventWithoutAttachments.setId(id);
    db.createEvent(eventWithoutAttachments, eventsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    cacheSuccess = false;
    db.getEvents(null, getEventsCallback);
    Awaitility.await().until(hasCacheResult());
    assertFalse(cacheError);
    assertNull(getEventById(id, cacheEvents).getAttachments());
  }

  /**
   * UPDATE
   */

  @Test
  public void testUpdateShouldUpdateTheCache() {

  }


  private Event getEventById(String id, List<Event> events) {
    for(Event event: events) {
      if (id.equals(event.getId())) {
        return event;
      }
    }
    return null;
  }

  private static Callable<Boolean> hasCacheResult() {
    return new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        return (cacheSuccess || cacheError);
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
        fail("should not be called");
      }

      @Override
      public void onApiError(String errorMessage, Double serverTime) {
        fail("should not be called");
      }
    };
  }

  private static void instanciateEventsCallback() {
    eventsCallback = new EventsCallback() {

      @Override
      public void onApiSuccess(String successMessage, Event event, String pStoppedId,
                               Double pServerTime) {
        fail("should not be called");
      }

      @Override
      public void onApiError(String errorMessage, Double pServerTime) {
        fail("should not be called");
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
        fail("should not be called");
      }

      @Override
      public void onApiError(String errorMessage, Double pServerTime) {
        fail("should not be called");
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
        fail("should not be called");
      }

      @Override
      public void onApiError(String errorMessage, Double serverTime) {
        fail("should not be called");
      }
    };
  }

}
