package com.pryv.unit;

import java.io.IOException;
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
import com.pryv.api.model.Event;
import com.pryv.utils.JsonConverter;

/**
 * Test of Retrieval of Events by Online module
 *
 * @author ik
 *
 */
public class OnlineRetrieveEventsTest {

  private OnlineEventsAndStreamsManager online;

  private EventsCallback<String> eventsCallback;

  private String receivedEvents;
  private Map<String, Event> events;
  private String streamId;

  @Before
  public void setUp() throws Exception {
    Pryv.setStaging();

    instanciateCallback();

    String url = "https://" + TestCredentials.USERNAME + "." + Pryv.API_DOMAIN + "/";
    online = new OnlineEventsAndStreamsManager(url, TestCredentials.TOKEN);
  }

  @Test
  public void testFetchEventsWithEmptyFilterAndReceiveNonEmptyReply() {
    online.getEvents(new Filter(), eventsCallback);
    // Awaitility.
    Awaitility.await().until(hasReceivedString());
  }

  private Callable<Boolean> hasReceivedString() {
    return new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        // TODO Auto-generated method stub
        if (receivedEvents != null) {
          return receivedEvents.length() > 0;
        } else {
          return false;
        }
      }
    };
  }

  @Test
  public void testFetchEventsWithEmptyFilterAndDeserializeJSON() {
    online.getEvents(new Filter(), eventsCallback);
    Awaitility.await().until(hasReceivedEvents());
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

  @Test
  public void testFetchEventsForAStream() {
    online.getEvents(new Filter(), eventsCallback);
    Awaitility.await().until(hasReceivedEvents());
    streamId = "";
    for (Event event : events.values()) {
      streamId = event.getStreamId();
    }
    Set<String> streamIds = new HashSet<String>();
    streamIds.add(streamId);
    Filter filter = new Filter();
    filter.setStreamIds(streamIds);
    online.getEvents(filter, eventsCallback);
    Awaitility.await().until(hasReceivedEventsForAStream());
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

  private void instanciateCallback() {
    eventsCallback = new EventsCallback<String>() {

      @Override
      public void onEventsSuccess(String stringEvents) {
        receivedEvents = stringEvents;
        try {
          events = JsonConverter.createEventsFromJson(stringEvents);
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

      // unused
      @Override
      public void onEventsPartialResult(Map<String, Event> newEvents) {
      }

      @Override
      public void onEventsError(String message) {
        // TODO Auto-generated method stub

      }
    };
  }

}
