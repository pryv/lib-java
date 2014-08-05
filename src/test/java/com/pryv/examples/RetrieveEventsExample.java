package com.pryv.examples;

import java.util.Map;

import resources.TestCredentials;

import com.pryv.Connection;
import com.pryv.Pryv;
import com.pryv.api.EventsCallback;
import com.pryv.api.EventsManager;
import com.pryv.api.model.Event;

/**
 * test retrieval of events online
 *
 * @author ik
 *
 */
public class RetrieveEventsExample {

  public static void main(String[] args) {
    // create Connection with valid username/token for testing
    Connection connection =
      new Connection(TestCredentials.USERNAME, TestCredentials.TOKEN);
    EventsManager<Map<String, Event>> eventsFetcher = connection;
    EventsCallback<Map<String, Event>> eventsCallback = connection;

    Pryv.setStaging();


  }
}
