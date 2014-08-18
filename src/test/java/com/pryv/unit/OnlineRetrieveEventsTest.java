package com.pryv.unit;

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

/**
 * Test of Retrieval of Events by Online module
 *
 * @author ik
 *
 */
public class OnlineRetrieveEventsTest {

  private OnlineEventsAndStreamsManager online;

  private EventsCallback eventsCallback;

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
    eventsCallback = new EventsCallback() {

      @Override
      public void onOnlieRetrieveEventsSuccess(Map<String, Event> onlineEvents) {
        events = onlineEvents;
      }

      // unused
      @Override
      public void onCacheRetrieveEventsSuccess(Map<String, Event> newEvents) {
      }

      @Override
      public void onEventsRetrievalError(String message) {
        // TODO Auto-generated method stub

      }

      @Override
      public void onSuperVisorRetrieveEventsSuccess(Map<String, Event> supervisorEvents) {
        // TODO Auto-generated method stub

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

}
