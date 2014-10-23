package com.pryv.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import com.pryv.api.StreamsCallback;
import com.pryv.api.database.DBinitCallback;
import com.pryv.api.database.SQLiteDBHelper;
import com.pryv.api.model.Attachment;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;
import com.pryv.unit.DummyData;

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

  private static Map<String, Stream> streams;
  private static Map<String, Event> events;

  private static Event testEvent;
  private static Stream testStream;

  private static final long MODIFIED_INCREMENT = 50;

  // create, update, delete
  private static boolean eventsSuccess = false;
  private static boolean eventsError = false;
  private static boolean streamsSuccess = false;
  private static boolean streamsError = false;
  // get
  private static boolean eventsRetrievalSuccess = false;
  private static boolean eventsRetrievalError = false;
  private static boolean streamsRetrievalSuccess = false;
  private static boolean streamsRetrievalError = false;

  // executed before all tests once
  @BeforeClass
  public static void beforeClass() {
    testEvent = DummyData.generateFullEvent();
    testStream = DummyData.generateFullStream();
    instanciateEventsCallback();
    instanciateStreamsCallback();

    db = new SQLiteDBHelper("test.db", new DBinitCallback() {

      @Override
      public void onError(String message) {
        System.out.println(message);
      }
    });
  }

  // executed after all tests once
  @AfterClass
  public static void cleanUpTestDB() {
    testEvent = null;
    db.getEvents(null, eventsCallback);
    Awaitility.await().until(hasRetrievedEventSuccessfully());
    for (Event event : events.values()) {
      System.out.println("deleting event with clientId="
        + event.getClientId()
          + ", cid="
          + event.getClientId());
      db.deleteEvent(event, eventsCallback);
      Awaitility.await().until(hasInsertedUpdatedDeletedEventSuccessfully());
      eventsSuccess = false;
      if (testEvent != null) {
        db.deleteEvent(event, eventsCallback);
        Awaitility.await().until(hasInsertedUpdatedDeletedEventSuccessfully());
        eventsSuccess = false;
      }
      testEvent = null;
    }

    testStream = null;
    db.getStreams(streamsCallback);
    Awaitility.await().until(hasRetrievedStreamSuccessfully());

    for (Stream stream : streams.values()) {
      System.out.println("deleting stream with clientid="
        + stream.getClientId()
          + ", id="
          + stream.getId());
      stream.setModified(stream.getModified() + MODIFIED_INCREMENT);
      db.deleteStream(stream, false, streamsCallback);
      Awaitility.await().until(hasInsertedUpdatedDeletedStreamSuccessfully());
      streamsSuccess = false;
      if (testStream != null) {
        db.deleteStream(stream, false, streamsCallback);
        Awaitility.await().until(hasInsertedUpdatedDeletedStreamSuccessfully());
      }
    }
  }

  // executed before each test
  @Before
  public void beforeEachTest() {
    eventsSuccess = false;
    eventsError = false;
    streamsSuccess = false;
    streamsError = false;
    eventsRetrievalSuccess = false;
    eventsRetrievalError = false;
    streamsRetrievalSuccess = false;
    streamsRetrievalError = false;
  }

  @Test
  public void test01InsertEmpyEvent() {
    System.out.println("test2");
    Event emptyEvent = new Event();
    db.updateOrCreateEvent(emptyEvent, eventsCallback);
    Awaitility.await().until(hasErrorWhileInsertingUpdatingDeletingEvent());
  }

  @Test
  public void test02InsertFullEvent() {
    db.updateOrCreateEvent(testEvent, eventsCallback);
    Awaitility.await().until(hasInsertedUpdatedDeletedEventSuccessfully());
  }

  @Test
  public void test03UpdateEvent() {
    String newStreamId = "otherStream";
    testEvent.setStreamId(newStreamId);
    testEvent.setModified(testEvent.getModified() + MODIFIED_INCREMENT);
    db.updateOrCreateEvent(testEvent, eventsCallback);
    Awaitility.await().until(hasInsertedUpdatedDeletedEventSuccessfully());
    db.getEvents(null, eventsCallback);
    Awaitility.await().until(hasRetrievedEventSuccessfully());
    Event modifiedEvent = events.get(testEvent.getClientId());
    if (modifiedEvent != null) {
      assertEquals(
        "new sid: " + modifiedEvent.getStreamId() + " should be : " + testEvent.getStreamId(),
        newStreamId, modifiedEvent.getStreamId());
    } else {
      fail("fail update event");
    }

  }

  @Test
  public void test03UpdateEventShouldDoNothingBecauseOfModifiedFields() {
    testEvent.setModified(testEvent.getModified());
    db.updateOrCreateEvent(testEvent, eventsCallback);
    Awaitility.await().until(hasInsertedUpdatedDeletedEventSuccessfully());
    db.getEvents(null, eventsCallback);
    Awaitility.await().until(hasRetrievedEventSuccessfully());
    Event notModifiedEvent = events.get(testEvent.getClientId());
    assertEquals(notModifiedEvent.getModified(), testEvent.getModified());
  }

  @Test
  public void test03UpdateEventShouldModify() {
    testEvent.setModified(testEvent.getModified() + MODIFIED_INCREMENT);
    db.updateOrCreateEvent(testEvent, eventsCallback);
    Awaitility.await().until(hasInsertedUpdatedDeletedEventSuccessfully());
    db.getEvents(null, eventsCallback);
    Awaitility.await().until(hasRetrievedEventSuccessfully());
    Event modifiedEvent = events.get(testEvent.getClientId());
    assertTrue("new value: "
      + modifiedEvent.getModified()
        + " should be higher than old value: "
        + DummyData.getModified(), modifiedEvent.getModified() > DummyData.getModified());
  }

  @Test
  public void test04RemoveFullEvent() {
    testEvent.setModified(testEvent.getModified() + MODIFIED_INCREMENT);
    db.deleteEvent(testEvent, eventsCallback);
    Awaitility.await().until(hasInsertedUpdatedDeletedEventSuccessfully());
    db.getEvents(null, eventsCallback);
    Awaitility.await().until(hasInsertedUpdatedDeletedEventSuccessfully());
    assertEquals(true, events.get(testEvent.getClientId()).getTrashed());
  }

  @Test
  public void test05RetrieveEventsForFullFilter() {
    Filter filter = DummyData.generateFullFilter();
    db.getEvents(filter, eventsCallback);
    Awaitility.await().until(hasRetrievedEventSuccessfully());
    assertTrue(eventsRetrievalSuccess);
  }

  @Test
  public void test06RetrieveEventForStateEqualsAllFilter() {
    Filter filter = new Filter();
    filter.setState(Filter.State.ALL);
    db.getEvents(filter, eventsCallback);
    Awaitility.await().until(hasRetrievedEventSuccessfully());
    assertTrue(eventsRetrievalSuccess);
  }

  @Test
  public void test07InsertEmptyStreamShouldFail() {
    db.updateOrCreateStream(new Stream(), streamsCallback);
    Awaitility.await().until(hasErrorWhileInsertingUpdatingDeletingStream());
    assertTrue(streamsError);
  }

  @Test
  public void test08InsertFullStream() {
    System.out.println("test insert full stream START");
    db.updateOrCreateStream(testStream, streamsCallback);
    Awaitility.await().until(hasInsertedUpdatedDeletedStreamSuccessfully());
    System.out.println("test insert full stream INSERTION SUCCESS");
    db.getStreams(streamsCallback);
    Awaitility.await().until(hasRetrievedStreamSuccessfully());
    System.out.println("testStream client id: " + streams.get(testStream.getClientId()));
    assertEquals(streams.get(testStream.getClientId()).getId(), testStream.getId());
    System.out.println("test insert full stream END");
  }

  @Test
  public void test09UpdateFullStream() {
    System.out.println("test update full stream START");
    testStream.setTrashed(!testStream.getTrashed());
    Double newModifiedValue = testStream.getModified() + MODIFIED_INCREMENT;
    testStream.setModified(newModifiedValue);
    db.updateOrCreateStream(testStream, streamsCallback);
    Awaitility.await().until(hasInsertedUpdatedDeletedStreamSuccessfully());
    db.getStreams(streamsCallback);
    Awaitility.await().until(hasRetrievedStreamSuccessfully());
    assertEquals(newModifiedValue, streams.get(testStream.getClientId()).getModified());
  }

  @Test
  public void test10retrieveStreams() {
    db.getStreams(streamsCallback);
    Awaitility.await().until(hasRetrievedStreamSuccessfully());
    assertTrue(streams.get(testStream.getClientId()) != null);
    assertTrue(streams.get(testStream.getClientId()).getChildren().size() != 0);
  }

  @Test
  public void test11RemoveFullStream() {
    testStream.setTrashed(true);
    testStream.setModified(testStream.getModified() + MODIFIED_INCREMENT);
    db.deleteStream(testStream, false, streamsCallback);
    Awaitility.await().until(hasInsertedUpdatedDeletedStreamSuccessfully());
    db.getStreams(streamsCallback);
    Awaitility.await().until(hasRetrievedStreamSuccessfully());
    for (Stream stream : streams.values()) {
      if (stream.getId() == testStream.getId()) {
        fail("stream delete fail");
      }
    }
  }

  @Test
  public void test14RetrieveCorrectEvent() {
    Event testedEvent = DummyData.generateFullEvent();
    String newClientId = "myNewClientID";
    testedEvent.setClientId(newClientId);
    db.updateOrCreateEvent(testedEvent, eventsCallback);
    Awaitility.await().until(hasInsertedUpdatedDeletedEventSuccessfully());
    db.getEvents(null, eventsCallback);
    Awaitility.await().until(hasRetrievedEventSuccessfully());
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
    assertEquals(testedEvent.getTrashed(), retrievedEvent.getTrashed());
    assertEquals(testedEvent.getCreated(), retrievedEvent.getCreated());
    assertEquals(testedEvent.getCreatedBy(), retrievedEvent.getCreatedBy());
    assertEquals(testedEvent.getModified(), retrievedEvent.getModified());
    assertEquals(testedEvent.getModifiedBy(), retrievedEvent.getModifiedBy());
    testedEvent.setTrashed(true);
    testedEvent.setModified(testedEvent.getModified() + MODIFIED_INCREMENT);
    db.deleteEvent(testedEvent, eventsCallback);
    Awaitility.await().until(hasInsertedUpdatedDeletedEventSuccessfully());
  }

  @Test
  public void test13RetrieveStreamCorrectly() {
    Stream testedStream = DummyData.generateFullStream();
    String newClientId = "myNewStreamId";
    testedStream.setClientId(newClientId);
    int i = 1;
    for (Stream childStream : testedStream.getChildren()) {
      childStream.setId("childid" + i);
      childStream.setParentClientId(newClientId);
      i++;
    }
    System.out.println("inserting stream with id: " + newClientId);
    db.updateOrCreateStream(testedStream, streamsCallback);
    Awaitility.await().until(hasInsertedUpdatedDeletedStreamSuccessfully());
    db.getStreams(streamsCallback);
    Awaitility.await().until(hasRetrievedStreamSuccessfully());
    Stream retrievedStream = streams.get(newClientId);
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
    assertEquals(testedStream.getTrashed(), retrievedStream.getTrashed());
    assertEquals(testedStream.getCreated(), retrievedStream.getCreated());
    assertEquals(testedStream.getCreatedBy(), retrievedStream.getCreatedBy());
    assertEquals(testedStream.getModified(), retrievedStream.getModified());
    assertEquals(testedStream.getModifiedBy(), retrievedStream.getModifiedBy());
  }

  @Test
  public void testInsertAndRetrieveEventWithNullAttachments() {
    Event eventWithoutAttachments = DummyData.generateFullEvent();
    eventWithoutAttachments.setAttachments(null);
    String clientId = "eventWithoutAttachmentsID";
    eventWithoutAttachments.setClientId(clientId);
    db.updateOrCreateEvent(eventWithoutAttachments, eventsCallback);
    Awaitility.await().until(hasInsertedUpdatedDeletedEventSuccessfully());
    db.getEvents(null, eventsCallback);
    Awaitility.await().until(hasRetrievedEventSuccessfully());
    assertNull(events.get(clientId).getAttachments());
  }

  private static Callable<Boolean> hasRetrievedEventSuccessfully() {
    return new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return eventsRetrievalSuccess;
      }
    };
  }

  private Callable<Boolean> hasErrorWhileRetrievingEvent() {
    return new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return eventsRetrievalError;
      }
    };
  }

  private static Callable<Boolean> hasInsertedUpdatedDeletedEventSuccessfully() {
    return new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return eventsSuccess;
      }
    };
  }

  private Callable<Boolean> hasErrorWhileInsertingUpdatingDeletingEvent() {
    return new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        return eventsError;
      }
    };
  }

  private static Callable<Boolean> hasRetrievedStreamSuccessfully() {
    return new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return streamsRetrievalSuccess;
      }
    };
  }

  private Callable<Boolean> hasErrorWhileRetrievingStream() {
    return new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return streamsRetrievalError;
      }
    };
  }

  private static Callable<Boolean> hasInsertedUpdatedDeletedStreamSuccessfully() {
    return new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return streamsSuccess;
      }
    };
  }

  private Callable<Boolean> hasErrorWhileInsertingUpdatingDeletingStream() {
    return new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        return streamsError;
      }
    };
  }

  private static void instanciateEventsCallback() {
    eventsCallback = new EventsCallback() {


      @Override
      public void onEventsRetrievalSuccess(Map<String, Event> pEvents, double serverTime) {
        events = pEvents;
        eventsRetrievalSuccess = true;
      }

      @Override
      public void onEventsSuccess(String successMessage, Event event, Integer stoppedId) {
        testEvent = event;
        eventsSuccess = true;
      }

      @Override
      public void onEventsRetrievalError(String errorMessage) {
        eventsRetrievalError = true;
      }

      @Override
      public void onEventsError(String errorMessage) {
        eventsError = true;
      }

    };
  }

  private static void instanciateStreamsCallback() {
    streamsCallback = new StreamsCallback() {

      @Override
      public void
        onStreamsRetrievalSuccess(Map<String, Stream> receveiedStreams,
        double serverTime) {
        streams = receveiedStreams;
        streamsRetrievalSuccess = true;
      }

      @Override
      public void onStreamsRetrievalError(String errorMessage) {
        streamsRetrievalError = true;
      }

      @Override
      public void onStreamsSuccess(String successMessage, Stream stream) {
        streamsSuccess = true;
      }

      @Override
      public void onStreamError(String errorMessage) {
        streamsError = true;
      }

    };
  }

}
