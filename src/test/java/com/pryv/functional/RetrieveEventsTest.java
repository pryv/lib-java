package com.pryv.functional;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import resources.TestCredentials;

import com.pryv.Connection;
import com.pryv.Pryv;
import com.pryv.api.EventsCallback;
import com.pryv.api.EventsManager;
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
    connection.get((EventsCallback<Map<String, Event>>) connection);
  }

}
