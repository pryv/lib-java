package com.pryv.functional;

import org.junit.Before;
import org.junit.Test;

import com.pryv.Connection;
import com.pryv.Pryv;
import com.pryv.api.EventManager;

/**
 *
 * test events retrieval
 *
 * @author ik
 *
 */
public class RetrieveEventsTest {

  private EventManager connection;

  @Before
  public void setUp() throws Exception {
    // create Connection with valid username/token for testing
    Pryv.setStaging();
    connection = new Connection("perkikiki", "chu2cc0zy0000iawkyrw7g9gq");
  }

  @Test
  /**
   * test fetching of events
   */
  public void testFetchEvents() {
    connection.getEvents();
  }

}
