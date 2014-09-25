package com.pryv.functional;

import static org.junit.Assert.assertFalse;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import resources.TestCredentials;

import com.jayway.awaitility.Awaitility;
import com.pryv.Connection;
import com.pryv.Pryv;
import com.pryv.api.EventsCallback;
import com.pryv.api.EventsManager;
import com.pryv.api.Filter;
import com.pryv.api.StreamsCallback;
import com.pryv.api.StreamsManager;
import com.pryv.api.database.DBinitCallback;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;
import com.pryv.unit.DummyData;

/**
 *
 * test events retrieval
 *
 * @author ik
 *
 */
public class RetrieveEventsTest {

  private static EventsManager eventsManager;
  private static StreamsManager streamsManager;

  private static EventsCallback eventsCallback;
  private static StreamsCallback streamsCallback;

  private static Map<String, Event> events;
  private static Map<String, Stream> streams;

  private static boolean eventsSuccess = false;
  private static boolean eventsError = false;
  private static boolean eventsRetrievalError = false;

  @BeforeClass
  public static void setUp() throws Exception {
    Pryv.setStaging();

    instanciateEventsCallback();
    instanciateStreamsCallback();

    eventsManager =
      new Connection(TestCredentials.USERNAME, TestCredentials.TOKEN, new DBinitCallback() {
        @Override
        public void onError(String message) {
          System.out.println("DB init Error: " + message);
        }
      });
    streamsManager = (StreamsManager) eventsManager;
  }

  @Before
  public void beforeEachTest() {
    events = null;
    streams = null;
  }

  // TODO create full scenario test: create, update, get, delete
  @Test
  public void testManipulateEventTest() {
    streamsManager.getStreams(null, streamsCallback);
    Awaitility.await().until(hasStreams());
    Stream chosenStream = null;
    for (Stream stream : streams.values()) {
      chosenStream = stream;
    }
    System.out.println("i chose dat stream: name="
      + chosenStream.getName()
        + ", cid="
        + chosenStream.getClientId()
        + ", id="
        + chosenStream.getId());
    Event testEvent = new Event();
    testEvent.setStreamId(chosenStream.getId());
    testEvent.setTime(1410963641.0);
    testEvent.setType("note/txt");
    testEvent.setStreamClientId(chosenStream.getClientId());
    testEvent
      .setContent("bla bla bla - awesome big content for the ultimate testing phase BRR BRR BRR");
    testEvent.setTags(DummyData.getTags());
    testEvent.setDescription("this is the ultimate test Event");
    testEvent.setClientData(DummyData.getClientdata());

    eventsManager.createEvent(testEvent, eventsCallback);
    Awaitility.await().until(hasEventsSuccess());
    eventsSuccess = false;
    Awaitility.await().until(hasEventsSuccess());
    eventsSuccess = false;
    Awaitility.await().until(hasEventsSuccess());
    eventsSuccess = false;
    // assertFalse(eventsError);
  }


  public void testFetchEventsForAStream() {

    Filter filter = new Filter();
    final int limit = 20;
    filter.setLimit(limit);
    eventsManager.getEvents(filter, eventsCallback);
    Awaitility.await().until(hasEvents());

    // get a streamCid
    String streamCid = null;
    Iterator<Event> iterator = events.values().iterator();
    while (streamCid == null && iterator.hasNext()) {
      streamCid = iterator.next().getStreamClientId();
    }
    System.out.println("Test: We're going for dat streamcid=" + streamCid);
    filter = new Filter();
    filter.addStreamClientId(streamCid);
    System.out.println("Test: going for dem right EVENTZZZZZ");
    eventsManager.getEvents(filter, eventsCallback);
    Awaitility.await().until(hasFetchedRightEvents(streamCid));
    eventsSuccess = false;
    assertFalse(eventsError);
  }

  private Callable<Boolean> hasFetchedRightEvents(final String streamCid) {
    return new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        if (events != null) {
          if (events.values().size() > 0) {
            boolean match = false;
            for (Event event : events.values()) {
              if (streamCid.equals(event.getStreamClientId())) {
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

  private Callable<Boolean> hasStreams() {
    return new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        if (streams != null) {
          return streams.values().size() > 0;
        } else {
          return false;
        }
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

  @Test
  public void testFetchEvents() {
    eventsManager.getEvents(new Filter(), eventsCallback);
    Awaitility.await().until(hasEvents());
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

  private static void instanciateEventsCallback() {
    eventsCallback = new EventsCallback() {

      int eSuccessCount = 0;

      @Override
      public void onOnlineRetrieveEventsSuccess(Map<String, Event> newEvents, double serverTime) {
        System.out.println("TestEventsCallback: Online success with "
          + newEvents.values().size()
            + " events");
        events = newEvents;
      }

      @Override
      public void onCacheRetrieveEventsSuccess(Map<String, Event> newEvents) {
        System.out.println("TestEventsCallback: Cache success with "
          + newEvents.values().size()
            + " events");
        events = newEvents;
      }

      @Override
      public void onEventsRetrievalError(String message) {
        eventsRetrievalError = true;
      }

      @Override
      public void onSupervisorRetrieveEventsSuccess(Map<String, Event> supervisorEvents) {
        System.out.println("TestEventsCallback: Supervisor success with "
          + supervisorEvents.values().size()
            + " events");
        events = supervisorEvents;
      }

      @Override
      public void onEventsSuccess(String successMessage, Event event, Integer stoppedId) {
        eSuccessCount++;
        System.out.println("from test, onEventsSuccess " + eSuccessCount + "th call.");
        eventsSuccess = true;
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
      public void onOnlineRetrieveStreamsSuccess(Map<String, Stream> newStreams, double serverTime) {
        System.out.println("TestStreamsCallback: success for "
          + newStreams.values().size()
            + " streams");
        streams = newStreams;

      }

      @Override
      public void onCacheRetrieveStreamSuccess(Map<String, Stream> newStreams) {
        streams = newStreams;
      }

      @Override
      public void onStreamsRetrievalError(String message) {
        // TODO Auto-generated method stub

      }

      @Override
      public void onSupervisorRetrieveStreamsSuccess(Map<String, Stream> supervisorStreams) {
        streams = supervisorStreams;
      }

      @Override
      public void onStreamsSuccess(String successMessage) {
        // TODO Auto-generated method stub

      }

      @Override
      public void onStreamError(String errorMessage) {
        // TODO Auto-generated method stub

      }
    };
  }

}
