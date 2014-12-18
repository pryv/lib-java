package com.pryv.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;

import resources.TestCredentials;

import com.jayway.awaitility.Awaitility;
import com.pryv.Pryv;
import com.pryv.api.EventsCallback;
import com.pryv.api.Filter;
import com.pryv.api.OnlineEventsAndStreamsManager;
import com.pryv.api.StreamsCallback;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;
import com.pryv.unit.DummyData;

/**
 * Test of Retrieval of Events by Online module
 *
 * @author ik
 *
 */
public class OnlineEventsAndStreamsManagerTest {

  private static OnlineEventsAndStreamsManager online;

  private static EventsCallback eventsCallback;
  private static StreamsCallback streamsCallback;

  private static Map<String, Event> events;
  private static Map<String, Stream> streams;

  private static String streamId;
  private static Event createdEvent;

  private static boolean eventsSuccess = false;
  private static boolean eventsError = false;
  private static boolean eventsRetrievalError = false;

  private static Stream createdStream;

  private static boolean streamsSuccess = false;
  private static boolean streamsError = false;
  private static boolean streamsRetrievalError = false;

  @Before
  public void setUp() throws Exception {
    Pryv.setStaging();

    instanciateEventsCallback();
    instanciateStreamsCallback();

    String url = "https://" + TestCredentials.USERNAME + "." + Pryv.API_DOMAIN + "/";
    online = new OnlineEventsAndStreamsManager(url, TestCredentials.TOKEN, null);
  }

  @Test
  public void testFetchEventsWithEmptyFilterAndDeserializeJSON() {
    online.getEvents(new Filter(), eventsCallback);
    Awaitility.await().until(hasReceivedEvents());
  }

  @Test
  public void testFetchEventsForAStream() {
    online.getEvents(new Filter(), eventsCallback);
    Awaitility.await().until(hasReceivedEvents());
    streamId = "";
    for (Event event : events.values()) {
      streamId = event.getStreamId();
    }
    System.out.println("TEst: streamId chosen:" + streamId);
    Set<String> streamIds = new HashSet<String>();
    streamIds.add(streamId);
    Filter filter = new Filter();
    filter.setStreamIds(streamIds);
    online.getEvents(filter, eventsCallback);
    Awaitility.await().until(hasReceivedEventsForAStream());
  }

  @Test
  public void testCreateUpdateAndDeleteEvent() {
    online.getStreams(null, streamsCallback);
    Awaitility.await().until(hasStreams());
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
    online.createEvent(testEvent, eventsCallback);
    Awaitility.await().until(hasEventsSuccess());
    eventsSuccess = false;

    assertNotNull(createdEvent);
    assertNotNull(createdEvent.getId());

    String newContent = "updated content - Woohoohoo";
    createdEvent.setContent(newContent);
    online.updateEvent(createdEvent, eventsCallback);
    Awaitility.await().until(hasEventsSuccess());
    eventsSuccess = false;
    assertEquals(createdEvent.getContent(), newContent);

    online.deleteEvent(createdEvent, eventsCallback);
    Awaitility.await().until(hasEventsSuccess());
    eventsSuccess = false;
    assertNotNull(createdEvent);
    assertTrue(createdEvent.isTrashed());
    Event eventToDeleteReally = new Event();
    eventToDeleteReally.setId(createdEvent.getId());
    createdEvent = null;
    online.deleteEvent(eventToDeleteReally, eventsCallback);
    Awaitility.await().until(hasEventsSuccess());
    assertNull(createdEvent);
  }

  @Test
  public void testCreateUpdateAndDeleteStreamOnline() {
    Stream testStream = new Stream();
    testStream.setName("testStream name655");

    // create
    System.out.println("### --- testCreateUpdateAndDeleteStreamOnline: create --- ###");
    online.createStream(testStream, streamsCallback);
    Awaitility.await().until(hasStreamSuccess());
    streamsSuccess = false;
    assertNotNull(createdStream);

    // update
    System.out.println("### --- testCreateUpdateAndDeleteStreamOnline: update --- ###");
    String nameUpdate = "testStream name3123 updated - wooohooohoo";
    createdStream.setName(nameUpdate);
    online.updateStream(createdStream, streamsCallback);
    Awaitility.await().until(hasStreamSuccess());
    streamsSuccess = false;
    assertEquals(nameUpdate, createdStream.getName());

    // trash
    System.out.println("### --- testCreateUpdateAndDeleteStreamOnline: trash --- ###");
    online.deleteStream(createdStream, false, streamsCallback);
    Awaitility.await().until(hasStreamSuccess());
    streamsSuccess = false;
    assertNotNull(createdStream);
    assertTrue(createdStream.isTrashed());

    // delete
    System.out.println("### --- testCreateUpdateAndDeleteStreamOnline: delete --- ###");
    online.deleteStream(createdStream, false, streamsCallback);
    Awaitility.await().until(hasStreamSuccess());
    assertNull(createdStream);
  }

  private Callable<Boolean> hasReceivedEvents() {
    return new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        if (events != null) {
          return events.size() > 0;
        } else {
          return false;
        }
      }
    };
  }

  private Callable<Boolean> hasReceivedEventsForAStream() {
    return new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        for (Event event : events.values()) {
          if (!event.getStreamId().equals(streamId)) {
            return false;
          }
        }
        return true;
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

  private Callable<Boolean> hasStreamSuccess() {
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
      public void onEventsRetrievalSuccess(Map<String, Event> newEvents, double serverTime) {
        System.out.println("TestEventsCallback: success with "
          + newEvents.values().size()
            + " events");
        events = newEvents;
      }

      @Override
      public void onEventsRetrievalError(String message) {
        eventsRetrievalError = true;
      }

      @Override
      public void onEventsSuccess(String successMessage, Event event, Integer stoppedId) {
        System.out.println("OnlineEventsManagerTest: eventsSuccess msg: " + successMessage);
        eventsSuccess = true;
        if (event != null) {
          createdEvent = event;
        }
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
      public void onStreamsRetrievalSuccess(Map<String, Stream> newStreams, double serverTime) {
        System.out.println("TestStreamsCallback: success for "
          + newStreams.values().size()
            + " streams");
        streams = newStreams;

      }

      @Override
      public void onStreamsRetrievalError(String message) {
      }

      @Override
      public void onStreamsSuccess(String successMessage, Stream stream) {
        System.out.println("TestStreamsCallback: success msg: " + successMessage);
        createdStream = stream;
        streamsSuccess = true;
      }

      @Override
      public void onStreamError(String errorMessage) {
        streamsError = true;
      }
    };
  }

}
