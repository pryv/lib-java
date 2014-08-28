package com.pryv.functional;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

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

  private String streamId;

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

  @Test
  public void testFetchEventsForAStream() {
    streamsManager.getStreams(new Filter(), streamsCallback);
    Awaitility.await().until(hasStreams());
    Filter filter = new Filter();
    Set<String> streamIds = new HashSet<String>();
    streamId = "flowerBreath";
    filter.addStreamId(streamId);
    eventsManager.getEvents(filter, eventsCallback);
    Awaitility.await().until(hasFetchedRightStreams());
  }

  private Callable<Boolean> hasFetchedRightStreams() {
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

      @Override
      public void onOnlineRetrieveEventsSuccess(Map<String, Event> newEvents, long serverTime) {
        System.out.println("TestEventsCallback: success with "
          + newEvents.values().size()
            + " events");
        events = newEvents;
      }

      @Override
      public void onCacheRetrieveEventsSuccess(Map<String, Event> newEvents) {
        System.out.println("TestEventsCallback: success with "
          + newEvents.values().size()
            + " events");
        events = newEvents;
      }

      @Override
      public void onEventsRetrievalError(String message) {
        // TODO Auto-generated method stub

      }

      @Override
      public void onSupervisorRetrieveEventsSuccess(Map<String, Event> supervisorEvents) {
        System.out.println("TestEventsCallback: success with "
          + supervisorEvents.values().size()
            + " events");
        events = supervisorEvents;
      }

      @Override
      public void onEventsSuccess(String successMessage) {
        // TODO Auto-generated method stub

      }

      @Override
      public void onEventsError(String errorMessage) {
        // TODO Auto-generated method stub

      }
    };
  }

  private static void instanciateStreamsCallback() {
    streamsCallback = new StreamsCallback() {

      @Override
      public void onOnlineRetrieveStreamsSuccess(Map<String, Stream> newStreams, long serverTime) {
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
