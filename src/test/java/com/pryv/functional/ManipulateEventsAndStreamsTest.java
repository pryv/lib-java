/*
package com.pryv.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import resources.TestCredentials;

import com.jayway.awaitility.Awaitility;
import com.pryv.ConnectionOld;
import com.pryv.Pryv;
import com.pryv.api.EventsCallback;
import com.pryv.interfaces.EventsManager;
import com.pryv.api.Filter;
import com.pryv.interfaces.StreamsCallback;
import com.pryv.interfaces.StreamsManager;
import com.pryv.api.database.DBinitCallback;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;
import com.pryv.unit.DummyData;

*/
/**
 *
 * test events retrieval
 *
 * @author ik
 *
 *//*

public class ManipulateEventsAndStreamsTest {

  private static EventsManager eventsManager;
  private static StreamsManager streamsManager;

  private static EventsCallback eventsCallback;
  private static StreamsCallback streamsCallback;

  private static Map<String, Event> events;
  private static Map<String, Stream> streams;

  private static Event createdEvent;
  private static Stream testStream;

  private static boolean streamsSuccess = false;
  private static boolean streamsReceived = false;
  private static boolean eventsSuccess = false;
  private static boolean eventsError = false;
  private static boolean eventsRetrievalError = false;

  @BeforeClass
  public static void setUp() throws Exception {

    instanciateEventsCallback();
    instanciateStreamsCallback();
    Pryv.setDomain("pryv.li");

    eventsManager =
      new ConnectionOld(TestCredentials.USERNAME, TestCredentials.TOKEN, new DBinitCallback() {
        @Override
        public void onApiError(String message) {
          System.out.println("DB init Error: " + message);
        }
      });

    streamsManager = (StreamsManager) eventsManager;
  }

  @Before
  public void beforeEachTest() {
    events = null;
    streams = null;
    streamsSuccess = false;
    streamsReceived = false;
    eventsSuccess = false;
    eventsError = false;
    eventsRetrievalError = false;
  }

  @Test
  public void testFetchEvents() {
    eventsManager.get(new Filter(), eventsCallback);
    Awaitility.await().until(hasEventsSuccess());
    Awaitility.await().until(hasEvents());
  }

  // TODO create full scenario test: create, update, get, delete
  // @Test
  public void testManipulateEventTest() {
    System.out.println("testManipulateEventTest begins");
    streamsManager.get(null, streamsCallback);
    Awaitility.await().until(hasReceivedStreams());
    System.out.println("RECEIVED STREAMS ONE - LALALA");
    streamsReceived = false;
    Awaitility.await().until(hasReceivedStreams());
    System.out.println("RECEIVED STREAMS TWO - LALALA");
    streamsReceived = false;
    Stream chosenStream = null;
    for (Stream stream : streams.values()) {
      chosenStream = stream;
    }
    System.out.println("i chose dat stream: name="
      + chosenStream.getName()
        + ", id="
        + chosenStream.getId());
    Event testEvent = new Event();
    testEvent.setStreamId(chosenStream.getId());
    testEvent.setTime(1410963641.0);
    testEvent.setType("note/txt");
    testEvent
      .setContent("bla bla bla - awesome big content for the ultimate testing phase BRR BRR BRR");
    testEvent.setTags(DummyData.getTags());
    testEvent.setDescription("this is the ultimate test Event");
    testEvent.setClientData(DummyData.getClientdata());

    // create
    eventsManager.create(testEvent, eventsCallback);
    Awaitility.await().until(hasEventsSuccess());
    eventsSuccess = false;
    System.out.println("RetrieveEventsTest: TEST EVENT CREATED ONE - LALALA");
    Awaitility.await().until(hasEventsSuccess());
    eventsSuccess = false;
    System.out.println("RetrieveEventsTest: TEST EVENT CREATED TWO - LALALA");
    Awaitility.await().until(hasEventsSuccess());
    eventsSuccess = false;
    System.out.println("RetrieveEventsTest: TEST EVENT CREATED THREE - LALALA");

    // delete
    eventsManager.delete(createdEvent, eventsCallback);
    Awaitility.await().until(hasEventsSuccess());
    System.out.println("RetrieveEventsTest: TEST EVENT DELETED ONE - LALALA");
    eventsSuccess = false;
    Awaitility.await().until(hasEventsSuccess());
    System.out.println("RetrieveEventsTest: TEST EVENT DELETED TWO - LALALA");
    eventsSuccess = false;
    Awaitility.await().until(hasEventsSuccess());
    System.out.println("RetrieveEventsTest: TEST EVENT DELETED THREE - LALALA");
    eventsSuccess = false;

    // TODO: delete the created event.
    // retrieve id from online API response, send delete command
  }

  // @Test
  public void testCreateUpdateAndDeleteStream() {
    int num = 54;
    testStream = new Stream(null, "testStream name" + num);

    System.out.println("### --- Create Stream phase --- ###");

    // create
    streamsManager.create(testStream, streamsCallback);
    Awaitility.await().until(hasStreamsSuccess());
    System.out.println("first create success before, streamsSuccess set to: " + streamsSuccess);
    streamsSuccess = false;
    assertNotNull(testStream);
    System.out.println("first create success after, streamsSuccess set to: " + streamsSuccess);
    Awaitility.await().until(hasStreamsSuccess());
    System.out.println("second create success before, streamsSuccess set to: " + streamsSuccess);
    streamsSuccess = false;
    assertNotNull(testStream);
    System.out.println("second create success after, streamsSuccess set to: " + streamsSuccess);
    // Awaitility.await().until(hasStreamsSuccess());
    System.out.println("third create success before, streamsSuccess set to: " + streamsSuccess);
    streamsSuccess = false;
    assertNotNull(testStream);

    // update
    System.out.println("### --- Update Stream phase --- ###");
    String nameUpdate = "testStream name" + num + " updated - wooohooohoo";
    testStream.setName(nameUpdate);
    streamsManager.update(testStream, streamsCallback);
    Awaitility.await().until(hasStreamsSuccess());
    streamsSuccess = false;
    Awaitility.await().until(hasStreamsSuccess());
    streamsSuccess = false;
    assertEquals(nameUpdate, testStream.getName());

    // trash
    System.out.println("### --- Trash Stream phase --- ###");
    streamsManager.delete(testStream, false, streamsCallback);
    Awaitility.await().until(hasStreamsSuccess());
    streamsSuccess = false;
    assertNotNull(testStream);
    assertTrue(testStream.isTrashed());

    // delete
    System.out.println("### --- Delete Stream phase --- ###");
    streamsManager.delete(testStream, false, streamsCallback);
    Awaitility.await().until(hasStreamsSuccess());
    streamsSuccess = false;
    Awaitility.await().until(hasStreamsSuccess());
    assertNull(testStream);
  }

  @Test
  public void testFetchEventsForAStream() {

    Filter filter = new Filter();
    final int limit = 20;
    filter.setLimit(limit);
    eventsManager.get(filter, eventsCallback);
    Awaitility.await().until(hasEvents());

    // get a streamId
    String streamId = null;
    Iterator<Event> iterator = events.values().iterator();
    while (streamId == null && iterator.hasNext()) {
      streamId = iterator.next().getStreamId();
    }
    System.out.println("Test: We're going for dat streamId=" + streamId);
    filter = new Filter();
    filter.addStreamId(streamId);
    System.out.println("Test: going for dem right EVENTZZZZZ");
    eventsManager.get(filter, eventsCallback);
    Awaitility.await().until(hasFetchedRightEvents(streamId));
    eventsSuccess = false;
    assertFalse(eventsError);
  }

  private Callable<Boolean> hasFetchedRightEvents(final String streamId) {
    return new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        if (events != null) {
          if (events.values().size() > 0) {
            boolean match = false;
            for (Event event : events.values()) {
              if (streamId.equals(event.getStreamId())) {
                match = true;
              }
            }
            return match;
          } else {
            return false;
          }
        } else {
          return false;
        }
      }
    };
  }

  private Callable<Boolean> hasReceivedStreams() {
    return new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return streamsReceived;
      }
    };
  }

  private Callable<Boolean> hasEventsSuccess() {
    return new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        return eventsSuccess;
      }
    };
  }

  private Callable<Boolean> hasCreatedEventOnline() {
    return new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        return createdEvent.getId() != null;
      }
    };
  }

  private Callable<Boolean> hasEventsError() {
    return new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        return eventsError;
      }
    };
  }

  private Callable<Boolean> hasEventsRetrievalError() {
    return new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        return eventsRetrievalError;
      }
    };
  }

  private Callable<Boolean> hasEvents() {
    return new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        if (events != null) {
          return events.values().size() > 0;
        } else {
          return false;
        }
      }
    };
  }

  private Callable<Boolean> hasStreamsSuccess() {
    return new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        return streamsSuccess;
      }
    };
  }

  private static void instanciateEventsCallback() {
    eventsCallback = new EventsCallback() {

      @Override
      public void onEventsRetrievalSuccess(Map<String, Event> newEvents, Double serverTime) {
        System.out.println("TestEventsCallback: success with "
          + newEvents.values().size()
            + " events");
        events = newEvents;
      }

      @Override
      public void onEventsRetrievalError(String message, Double pServerTime) {
        eventsRetrievalError = true;
      }

      @Override
      public void onEventsSuccess(String successMessage, Event event, Integer stoppedId,
        Double pServerTime) {
        System.out.println("RetrieveEventsTest: events success called with msg: "
          + successMessage
            + ", event="
            + event);
        eventsSuccess = true;
        if (event != null) {
          createdEvent = event;
        }
      }

      @Override
      public void onEventsError(String errorMessage, Double pServerTime) {
        eventsError = true;
      }
    };
  }

  private static void instanciateStreamsCallback() {
    streamsCallback = new StreamsCallback() {

      @Override
      public void onStreamsRetrievalSuccess(Map<String, Stream> newStreams, Double serverTime) {
        System.out.println("TestStreamsCallback: success for "
          + newStreams.values().size()
            + " streams");
        streams = newStreams;
        streamsReceived = true;
      }

      @Override
      public void onStreamsRetrievalError(String message, Double pServerTime) {
      }

      @Override
      public void onStreamsSuccess(String successMessage, Stream stream, Double pServerTime) {
        System.out.println("TestStreamsCallback: streams success with msg: " + successMessage);
        streamsSuccess = true;
        testStream = stream;
      }

      @Override
      public void onStreamError(String errorMessage, Double pServerTime) {
      }
    };
  }

}
*/
