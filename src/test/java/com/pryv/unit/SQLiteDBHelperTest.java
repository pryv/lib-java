package com.pryv.unit;

import static org.junit.Assert.assertEquals;
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

  private static Map<String, Stream> streams;
  private static Map<String, Event> events;

  private static Event testEvent;
  private static Stream testStream;

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
    db.getEvents(null, eventsCallback);
    Awaitility.await().until(hasRetrievedEventSuccessfully());
    for (Event event : events.values()) {
      db.deleteEvent(event, eventsCallback);
      Awaitility.await().until(hasInsertedUpdatedDeletedEventSuccessfully());
    }
    db.getStreams(streamsCallback);
    // awai
  }

  // executed before each test
  @Before
  public void before() {
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
    db.createEvent(emptyEvent, eventsCallback);
    Awaitility.await().until(hasErrorWhileInsertingUpdatingDeletingEvent());
  }

  @Test
  public void test02InsertFullEvent() {
    db.createEvent(testEvent, eventsCallback);
    Awaitility.await().until(hasInsertedUpdatedDeletedEventSuccessfully());
  }

  @Test
  public void test03UpdateEvent() {
    String newStreamId = "otherStream";
    testEvent.setStreamId(newStreamId);
    testEvent.setModified(testEvent.getModified() + 50);
    db.updateEvent(testEvent, eventsCallback);
    Awaitility.await().until(hasInsertedUpdatedDeletedEventSuccessfully());
    db.getEvents(null, eventsCallback);
    Awaitility.await().until(hasRetrievedEventSuccessfully());
    Event modifiedEvent = events.get(testEvent.getId());
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
    db.updateEvent(testEvent, eventsCallback);
    Awaitility.await().until(hasInsertedUpdatedDeletedEventSuccessfully());
    db.getEvents(null, eventsCallback);
    Awaitility.await().until(hasRetrievedEventSuccessfully());
    Event notModifiedEvent = events.get(testEvent.getId());
    assertEquals(notModifiedEvent.getModified(), testEvent.getModified());
  }

  @Test
  public void test03UpdateEventShouldModify() {
    testEvent.setModified(DummyData.getModified() + 50);
    System.out.println("modified value is : " + testEvent.getModified());
    db.updateEvent(testEvent, eventsCallback);
    Awaitility.await().until(hasInsertedUpdatedDeletedEventSuccessfully());
    db.getEvents(null, eventsCallback);
    Awaitility.await().until(hasRetrievedEventSuccessfully());
    Event modifiedEvent = events.get(testEvent.getId());
    assertTrue("new value: "
      + modifiedEvent.getModified()
        + " should be higher than old value: "
        + DummyData.getModified(), modifiedEvent.getModified() > DummyData.getModified());
  }

  @Test
  public void test04RemoveFullEvent() {
    db.deleteEvent(testEvent, eventsCallback);
    Awaitility.await().until(hasInsertedUpdatedDeletedEventSuccessfully());
    db.getEvents(null, eventsCallback);
    Awaitility.await().until(hasInsertedUpdatedDeletedEventSuccessfully());
    assertEquals(events.get(testEvent.getId()), null);
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
    db.createStream(new Stream(), streamsCallback);
    Awaitility.await().until(hasErrorWhileInsertingUpdatingDeletingStream());
    assertTrue(streamsError);
  }

  @Test
  public void test08InsertFullStream() {
    System.out.println("test insert full stream");
    db.createStream(testStream, streamsCallback);
    Awaitility.await().until(hasInsertedUpdatedDeletedStreamSuccessfully());
    db.getStreams(streamsCallback);
    Awaitility.await().until(hasRetrievedStreamSuccessfully());
    assertEquals(streams.get(testStream.getId()).getId(), testStream.getId());
  }

  @Test
  public void test09UpdateFullStream() {
    testStream.setTrashed(!testStream.getTrashed());
    Long newModifiedValue = testStream.getModified() + 50;
    testStream.setModified(newModifiedValue);
    db.updateStream(testStream, streamsCallback);
    Awaitility.await().until(hasInsertedUpdatedDeletedStreamSuccessfully());
    db.getStreams(streamsCallback);
    Awaitility.await().until(hasRetrievedStreamSuccessfully());
    assertEquals(newModifiedValue, streams.get(testStream.getId()).getModified());
  }

  @Test
  public void test10retrieveStreams() {
    db.getStreams(streamsCallback);
    Awaitility.await().until(hasRetrievedStreamSuccessfully());
    assertTrue(streams.get(testStream.getId()) != null);
    assertTrue(streams.get(testStream.getId()).getChildren().size() != 0);
  }

  @Test
  public void test11RemoveFullStream() {
    db.deleteStream(testStream, streamsCallback);
    Awaitility.await().until(hasInsertedUpdatedDeletedStreamSuccessfully());
    db.getStreams(streamsCallback);
    Awaitility.await().until(hasRetrievedStreamSuccessfully());
    for (Stream stream : streams.values()) {
      if (stream.getId() == testStream.getId()) {
        fail("stream delete fail");
      }
    }
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
      public void onSupervisorRetrieveEventsSuccess(Map<String, Event> supervisorEvents) {
      }

      @Override
      public void onOnlineRetrieveEventsSuccess(Map<String, Event> onlineEvents) {
      }

      @Override
      public void onEventsSuccess(String successMessage) {
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

      @Override
      public void onCacheRetrieveEventsSuccess(Map<String, Event> cacheEvents) {
        events = cacheEvents;
        eventsRetrievalSuccess = true;
      }
    };
  }

  private static void instanciateStreamsCallback() {
    streamsCallback = new StreamsCallback() {

      @Override
      public void onOnlineRetrieveStreamsSuccess(Map<String, Stream> onlineStreams) {
      }

      @Override
      public void onCacheRetrieveStreamSuccess(Map<String, Stream> cacheStreams) {
        streams = cacheStreams;
        streamsRetrievalSuccess = true;
      }

      @Override
      public void onSupervisorRetrieveStreamsSuccess(Map<String, Stream> supervisorStreams) {
      }

      @Override
      public void onStreamsRetrievalError(String errorMessage) {
        streamsRetrievalError = true;
      }

      @Override
      public void onStreamsSuccess(String successMessage) {
        streamsSuccess = true;
      }

      @Override
      public void onStreamError(String errorMessage) {
        streamsError = true;
      }

    };
  }

}
