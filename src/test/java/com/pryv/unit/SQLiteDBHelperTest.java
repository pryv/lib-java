package com.pryv.unit;

import static org.junit.Assert.assertEquals;
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
import com.pryv.Connection;
import com.pryv.interfaces.EventsCallback;
import com.pryv.api.Filter;
import com.pryv.interfaces.GetEventsCallback;
import com.pryv.interfaces.GetStreamsCallback;
import com.pryv.interfaces.StreamsCallback;
import com.pryv.api.database.DBinitCallback;
import com.pryv.api.database.SQLiteDBHelper;
import com.pryv.model.Attachment;
import com.pryv.model.Event;
import com.pryv.model.Stream;

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
  private static GetEventsCallback getEventsCallback;
  private static StreamsCallback streamsCallback;
  private static GetStreamsCallback getStreamsCallback;

  private static List<Event> events;
  private static List<Event> partialEvents;
  private static Map<String, Stream> streams;
  private static Map<String, Stream> partialStreams;

  private static Stream testSupportStream;

  private static String stoppedId;

  private static Event singleEvent;
  private static Stream singleStream;

  private static boolean partialSuccess = false;
  private static boolean partialError = false;
  private static boolean success = false;
  private static boolean error = false;

  private static Connection connection;

  private static final long MODIFIED_INCREMENT = 50;

  // executed before all tests once
  @BeforeClass
  public static void beforeClass() {
    singleEvent = DummyData.generateFullEvent();
    singleStream = DummyData.generateFullStream();

    instanciateEventsCallback();
    instanciateGetEventsCallback();
    instanciateStreamsCallback();
    instanciateGetStreamsCallback();

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
    db.getEvents(null, getEventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;
    for (Event event : events) {
      System.out.println("trashing event with clientId="
        + event.getClientId()
          + ", cid="
          + event.getClientId());
      db.deleteEvent(event, eventsCallback);
      Awaitility.await().until(hasResult());
      assertTrue(success);
      success = false;
      if (singleEvent != null) {
        System.out.println("deleting again: " + singleEvent.getClientId());
        db.deleteEvent(event, eventsCallback);
        Awaitility.await().until(hasResult());
        assertTrue(success);
        success = false;
      }
      singleEvent = null;
    }

    singleStream = null;
    db.getStreams(getStreamsCallback);
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
    db.createEvent(emptyEvent, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(error);
  }

  @Test
  public void testInsertValidEvent() {
    Event newEvent = new Event();
    newEvent.setClientId("newEventClientId");
    newEvent.setStreamId("myStreamId");
    newEvent.setType("activity/plain");
    db.createEvent(newEvent, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;

    db.getEvents(new Filter(), getEventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    assertNotNull(getEvent(newEvent.getClientId(), events));
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

    db.createEvent(eventToUpdate, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;

    eventToUpdate.setContent("new updated content, " +
            "shouldnt be stored in cache because of modified time");
    db.updateEvent(eventToUpdate, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;

    db.getEvents(new Filter(), getEventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    assertNotEquals(getEvent(eventToUpdate.getClientId(), events).getContent(), eventToUpdate.getContent());
  }

  @Test
  public void testUpdateEventShouldModify() {
    Event eventToUpdate = new Event();
    eventToUpdate.setClientId("myUpdatedEventCid");
    eventToUpdate.setStreamId("someStreamId");
    eventToUpdate.setType("note/txt");
    eventToUpdate.setContent("I will be updated youhou");
    eventToUpdate.setModified(1000.0);

    db.createEvent(eventToUpdate, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;

    eventToUpdate.setContent("new updated content, " +
            "should be stored because modified time is later");
    eventToUpdate.setModified(eventToUpdate.getModified() + 500.0);
    db.updateEvent(eventToUpdate, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;

    singleEvent.setModified(singleEvent.getModified() + MODIFIED_INCREMENT);
    db.updateEvent(singleEvent, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;

    db.getEvents(new Filter(), getEventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    assertEquals(getEvent(eventToUpdate.getClientId(), events).getContent(), eventToUpdate.getContent());
  }

  @Test
  public void testDeleteEventCalledOnceShouldUpdateItAsTrashed() {
    Event eventToDelete = new Event();
    eventToDelete.setClientId("newEventClientId");
    eventToDelete.setStreamId("myStreamId");
    eventToDelete.setType("activity/plain");
    db.createEvent(eventToDelete, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;

    db.deleteEvent(eventToDelete, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;

    db.getEvents(null, getEventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    assertEquals(true, getEvent(eventToDelete.getClientId(), events).isTrashed());
  }

  @Test
  public void testDeleteEventCalledTwiceShouldDeleteIt() {
    Event eventToDelete = new Event();
    eventToDelete.setClientId("newEventClientId");
    eventToDelete.setStreamId("myStreamId");
    eventToDelete.setType("activity/plain");
    db.createEvent(eventToDelete, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;

    db.deleteEvent(eventToDelete, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;

    db.deleteEvent(eventToDelete, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;

    db.getEvents(null, getEventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    assertNull(getEvent(eventToDelete.getClientId(), events));
  }

  @Test
  public void testGetEventsShouldReturnEventsMatchingTheProvidedFilter() {
    Event noteEvent = new Event("myStream", null, "note/txt", "hi");
    noteEvent.setClientId("myClientId");
    db.createEvent(noteEvent, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;

    Event activityEvent = new Event("myStream", null, "activity/plain", null);
    activityEvent.setClientId("otherClientId");
    db.createEvent(activityEvent, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;

    Filter filter = new Filter();
    String filterType = "note/txt";
    filter.addType(filterType);
    db.getEvents(filter, getEventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    assertTrue(events.size() > 0);
    for (Event event: events) {
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

    db.getStreams(getStreamsCallback);
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
    db.getStreams(getStreamsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    assertEquals(newStreamName, streams.get(singleStream.getId()).getName());
  }

  //@Test
  public void test10retrieveStreams() {
    db.getStreams(getStreamsCallback);
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
    db.getStreams(getStreamsCallback);
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
    db.createEvent(testedEvent, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;
    db.getEvents(null, getEventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;
    Event retrievedEvent = getEvent(newClientId, events);
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
    db.deleteEvent(testedEvent, eventsCallback);
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
    db.getStreams(getStreamsCallback);
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
    db.createEvent(eventWithoutAttachments, eventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    success = false;
    db.getEvents(null, getEventsCallback);
    Awaitility.await().until(hasResult());
    assertTrue(success);
    assertNull(getEvent(clientId, events).getAttachments());
  }

  private Event getEvent(String key, List<Event> items) {
    for(Event item: items) {
      if (item.getClientId().equals(key)) {
        return item;
      }
    }
    return null;
  }

  private static Callable<Boolean> hasResult() {
    return new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        return (success || error);
      }
    };
  }

  private static void instanciateGetEventsCallback() {
    getEventsCallback = new GetEventsCallback() {
      @Override
      public void cacheCallback(List<Event> events, Map<String, Event> deletedEvents) {

      }

      @Override
      public void onCacheError(String errorMessage) {

      }

      @Override
      public void apiCallback(List<Event> events, Double serverTime) {

      }

      @Override
      public void onApiError(String errorMessage, Double serverTime) {

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
        success = true;
        singleEvent = event;
      }

      @Override
      public void onApiError(String errorMessage, Double pServerTime) {
        error = true;
      }

      @Override
      public void onCacheSuccess(String successMessage, Event event) {

      }

      @Override
      public void onCacheError(String errorMessage) {

      }
    };
  }

  private static void instanciateStreamsCallback() {
    streamsCallback = new StreamsCallback() {

      @Override
      public void onApiSuccess(String successMessage, Stream stream, Double pServerTime) {
        System.out.println("TestStreamsCallback: success msg: " + successMessage);
        singleStream = stream;
        success = true;
      }

      @Override
      public void onApiError(String errorMessage, Double pServerTime) {
        error = true;
      }

      @Override
      public void onCacheSuccess(String successMessage, Stream stream) {

      }

      @Override
      public void onCacheError(String errorMessage) {

      }
    };
  }

  private static void instanciateGetStreamsCallback() {
    getStreamsCallback = new GetStreamsCallback() {
      @Override
      public void cacheCallback(Map<String, Stream> streams, Map<String, Stream> deletedStreams) {
        partialStreams = streams;
        partialSuccess = true;
      }

      @Override
      public void onCacheError(String errorMessage) {

      }

      @Override
      public void apiCallback(Map<String, Stream> receivedStreams, Double serverTime) {
        streams = receivedStreams;
        success = true;
      }

      @Override
      public void onApiError(String errorMessage, Double serverTime) {
        error = true;
      }
    };
  }

}
