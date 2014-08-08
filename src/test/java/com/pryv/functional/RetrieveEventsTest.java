package com.pryv.functional;

import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import resources.TestCredentials;

import com.pryv.Connection;
import com.pryv.Pryv;
import com.pryv.api.EventsCallback;
import com.pryv.api.EventsManager;
import com.pryv.api.Filter;
import com.pryv.api.model.Event;

/**
 *
 * test events retrieval
 *
 * @author ik
 *
 */
public class RetrieveEventsTest {

  private EventsManager<Map<String, Event>> connection;

  @Mock
  private EventsCallback<Map<String, Event>> testCallback =
    new EventsCallback<Map<String, Event>>() {

    @Override
    public void onEventsSuccess(Map<String, Event> events) {
      System.out.println("success");
      assertTrue(events.values().size() > 0);
    }

    @Override
    public void onEventsPartialResult(Map<String, Event> newEvents) {
      // TODO Auto-generated method stub

    }

    @Override
    public void onEventsError(String message) {
      // TODO Auto-generated method stub

    }
  };



  @Before
  public void setUp() throws Exception {
    // create Connection with valid username/token for testing
    Pryv.setStaging();
    connection = new Connection(TestCredentials.USERNAME, TestCredentials.TOKEN);
  }

  @Test

  /**
   * test fetching of events
   */
  public void testFetchEvents() {
    System.out.println("testFetchEvents");
    connection.getEvents(new Filter(), testCallback);
  }

}
