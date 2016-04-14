package com.pryv.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.jayway.awaitility.Awaitility;
import com.pryv.api.EventsCallback;
import com.pryv.api.Filter;
import com.pryv.interfaces.StreamsCallback;
import com.pryv.api.database.DBinitCallback;
import com.pryv.api.database.SQLiteDBHelper;
import com.pryv.api.model.Attachment;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;

/**
 * unit tests for SQLiteDBHelper class
 *
 * @author ik
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SQLiteDBHelperTest {

  private static SQLiteDBHelper db;

  private static EventsCallback eventsCallback;
  private static StreamsCallback streamsCallback;

  private static Map<String, Stream> streams = new HashMap<String, Stream>();
  private static Map<String, Event> events = new HashMap<String, Event>();

  private static Event singleEvent;
  private static Stream singleStream;

  private static final long MODIFIED_INCREMENT = 50;

  // create, update, delete
  private static boolean success = false;
  private static boolean error = false;

  // executed before all tests once
  @BeforeClass
  public static void beforeClass() {
    singleEvent = DummyData.generateFullEvent();
    singleStream = DummyData.generateFullStream();
    instanciateEventsCallback();
    instanciateStreamsCallback();

    String cacheFolder = "cache/test/";
    new File(cacheFolder).mkdirs();
    db = new SQLiteDBHelper(cacheFolder, null, new DBinitCallback() {

      @Override
      public void onError(String message) {
        System.out.println(message);
      }
    });

  }

  @AfterClass
  public static void cleanUpTestDB() {
    System.out.println("SQLiteDBHelperTest: clean up phase:");
    singleEvent = null;
    db.get(null, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;
    for (Event event : events.values()) {
      System.out.println("trashing event with clientId="
        + event.getClientId()
          + ", cid="
          + event.getClientId());
      db.delete(event, eventsCallback);
      Awaitility.await().until(hasResult());
      assertTrue(success);
      success = false;
      if (singleEvent != null) {
        System.out.println("deleting again: " + singleEvent.getClientId());
        db.delete(event, eventsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(success);
        success = false;
      }
      singleEvent = null;
    }

    singleStream = null;
    db.getStreams(streamsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;
    System.out.println("going to delete " + streams.size() + " streams");
    for (Stream stream : streams.values()) {
      System.out.println("deleting stream with id=" + stream.getId());
      stream.setModified(stream.getModified() + MODIFIED_INCREMENT);
      db.deleteStream(stream, false, streamsCallback);
      Awaitility.await().until(hasResult());
      assertTrue(success);
      success = false;
      if (singleStream != null) {
        System.out.println("deleting for real stream with id=" + singleStream.getId());
        db.deleteStream(stream, false, streamsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(success);
        success = false;
      }
    }
  }

  @Before
  public void beforeEachTest() {
    success = false;
    error = false;
    singleEvent = null;
    singleStream = null;
    events = null;
    streams = null;
  }

  @Test
  public void testInsertIncompleteEventShouldGenerateError() {
    System.out.println("test2");
    Event emptyEvent = new Event();
    emptyEvent.setClientId("yolo");
    emptyEvent.setStreamId("testStreamId");
    emptyEvent.setContent("i am missing mandatory fields");
    db.create(emptyEvent, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(error);
  }

  @Test
  public void testInsertValidEvent() {
    Event newEvent = new Event();
    newEvent.setClientId("newEventClientId");
    newEvent.setStreamId("myStreamId");
    newEvent.setType("activity/plain");
    db.create(newEvent, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;

    db.get(new Filter(), eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    assertNotNull(events.get(newEvent.getClientId()));
  }

  // TODO implement modified_time comparison for this to work
  @Test
  public void testUpdateEventShouldDoNothingBecauseOfModifiedFields() {
    Event eventToUpdate = new Event();
    eventToUpdate.setClientId("myUpdatedEventCid");
    eventToUpdate.setStreamId("someStreamId");
    eventToUpdate.setType("note/txt");
    eventToUpdate.setContent("I will be updated youhou");
    eventToUpdate.setModified(1000.0);

    db.create(eventToUpdate, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;

    eventToUpdate.setContent("new updated content, " +
            "shouldnt be stored in cache because of modified time");
    db.update(eventToUpdate, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;

    db.get(new Filter(), eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    assertNotEquals(events.get(eventToUpdate.getClientId()).getContent(), eventToUpdate.getContent());
  }

  @Test
  public void testUpdateEventShouldModify() {
    Event eventToUpdate = new Event();
    eventToUpdate.setClientId("myUpdatedEventCid");
    eventToUpdate.setStreamId("someStreamId");
    eventToUpdate.setType("note/txt");
    eventToUpdate.setContent("I will be updated youhou");
    eventToUpdate.setModified(1000.0);

    db.create(eventToUpdate, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;

    eventToUpdate.setContent("new updated content, " +
            "should be stored because modified time is later");
    eventToUpdate.setModified(eventToUpdate.getModified() + 500.0);
    db.update(eventToUpdate, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;

    singleEvent.setModified(singleEvent.getModified() + MODIFIED_INCREMENT);
    db.update(singleEvent, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;

    db.get(new Filter(), eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    assertEquals(events.get(eventToUpdate.getClientId()).getContent(), eventToUpdate.getContent());
  }

  @Test
  public void testDeleteEventCalledOnceShouldUpdateItAsTrashed() {
    Event eventToDelete = new Event();
    eventToDelete.setClientId("newEventClientId");
    eventToDelete.setStreamId("myStreamId");
    eventToDelete.setType("activity/plain");
    db.create(eventToDelete, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;

    db.delete(eventToDelete, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;

    db.get(null, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    assertEquals(true, events.get(eventToDelete.getClientId()).isTrashed());
  }

  @Test
  public void testDeleteEventCalledTwiceShouldDeleteIt() {
    Event eventToDelete = new Event();
    eventToDelete.setClientId("newEventClientId");
    eventToDelete.setStreamId("myStreamId");
    eventToDelete.setType("activity/plain");
    db.create(eventToDelete, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;

    db.delete(eventToDelete, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;

    db.delete(eventToDelete, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;

    db.get(null, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    assertNull(events.get(eventToDelete.getClientId()));
  }

  @Test
  public void testGetEventsShouldReturnEventsMatchingTheProvidedFilter() {
    Event noteEvent = new Event("myStream", null, "note/txt", "hi");
    noteEvent.setClientId("myClientId");
    db.create(noteEvent, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;

    Event activityEvent = new Event("myStream", null, "activity/plain", null);
    activityEvent.setClientId("otherClientId");
    db.create(activityEvent, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;

    Filter filter = new Filter();
    String filterType = "note/txt";
    filter.addType(filterType);
    db.get(filter, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    assertTrue(events.size() > 0);
    for (Event event: events.values()) {
      assertEquals(event.getType(), filterType);
    }
  }

  @Test
  public void createStreamWithMissingRequiredFieldsShouldGenerateAnError() {
    Stream incorrectStream = new Stream("myId", null);
    db.updateOrCreateStream(incorrectStream, streamsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(error);
  }

  @Test
  public void createStreamWithValidFieldsShouldPass() {
    Stream validStream = new Stream("myValidStreamId", "Valid stream yo");
    db.updateOrCreateStream(validStream, streamsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;

    db.getStreams(streamsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    assertNotNull(streams.get(validStream.getId()));
  }

  @Test
  public void testUpdateStreamShouldNotModifyEventIfItsField() {
    System.out.println("test update full stream START");
    String newStreamName = "myNewStreamName - dadadaa";
    singleStream.setName(newStreamName);
    db.updateOrCreateStream(singleStream, streamsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;
    db.getStreams(streamsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    assertEquals(newStreamName, streams.get(singleStream.getId()).getName());
  }

  //@Test
  public void test10retrieveStreams() {
    db.getStreams(streamsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    assertTrue(streams.get(singleStream.getId()) != null);
    assertTrue(streams.get(singleStream.getId()).getChildren().size() != 0);
  }

  //@Test
  public void test11RemoveFullStream() {
    System.out.println("SQLiteDBHelperTest: test11RemoveFullStream");
    db.deleteStream(singleStream, false, streamsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;
    assertTrue(singleStream.isTrashed());
    db.deleteStream(singleStream, false, streamsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;
    db.getStreams(streamsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    for (Stream stream : streams.values()) {
      if (stream.getId() == singleStream.getId()) {
        fail("stream delete fail");
      }
    }
  }

  //@Test
  public void test14RetrieveCorrectEvent() {
    Event testedEvent = DummyData.generateFullEvent();
    String newClientId = "myNewClientID";
    testedEvent.setClientId(newClientId);
    db.create(testedEvent, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;
    db.get(null, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;
    Event retrievedEvent = events.get(newClientId);
    assertEquals(testedEvent.getClientId(), retrievedEvent.getClientId());
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
    db.delete(testedEvent, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
  }

  //@Test
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
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;
    db.getStreams(streamsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    Stream retrievedStream = streams.get(newId);
    assertEquals(testedStream.getName(), retrievedStream.getName());
    assertEquals(testedStream.getParentId(), retrievedStream.getParentId());
    assertEquals(testedStream.getSingleActivity(), retrievedStream.getSingleActivity());
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

  //@Test
  public void testInsertAndRetrieveEventWithNullAttachments() {
    Event eventWithoutAttachments = DummyData.generateFullEvent();
    eventWithoutAttachments.setAttachments(null);
    String clientId = "eventWithoutAttachmentsID";
    eventWithoutAttachments.setClientId(clientId);
    db.create(eventWithoutAttachments, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;
    db.get(null, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    assertNull(events.get(clientId).getAttachments());
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
        System.out.println("SQLiteDBHelperTest: events retrieval success with "
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
        System.out.println("SQLiteDBHelperTest: events success msg: " + successMessage + " with Event: " + event);
        success = true;
        singleEvent = event;
      }

      @Override
      public void onEventsError(String errorMessage, Double pServerTime) {
        System.out.println("SQLiteDBHelperTest: events error msg: " + errorMessage);
        error = true;
      }
    };
  }

  private static void instanciateStreamsCallback() {
    streamsCallback = new StreamsCallback() {

      @Override
      public void onStreamsRetrievalSuccess(Map<String, Stream> newStreams, Double serverTime) {
        System.out.println("SQLiteDBHelperTest: streams retrieval success for "
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
        System.out.println("SQLiteDBHelperTest: streams success msg: " + successMessage);
        singleStream = stream;
        success = true;
      }

      @Override
      public void onStreamError(String errorMessage, Double pServerTime) {
        System.out.println("SQLiteDBHelperTest: streams error msg: " + errorMessage);
        error = true;
      }
    };
  }

}
