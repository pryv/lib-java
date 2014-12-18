package com.pryv.unit;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import resources.TestCredentials;

import com.jayway.awaitility.Awaitility;
import com.pryv.Pryv;
import com.pryv.api.EventsCallback;
import com.pryv.api.EventsManager;
import com.pryv.api.OnlineEventsAndStreamsManager;
import com.pryv.api.model.Event;

/**
 *
 * class used to test the creation, retrieval, update and deletion of an Event
 * by the online module
 *
 * @author ik
 *
 */
public class OnlineManipulateEventTest {

  private static EventsManager online;
  private static EventsCallback callback;

  private Map<String, Event> events = new HashMap<String, Event>();

  private boolean success;
  private boolean error;

  // @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Pryv.setStaging();

    String url = "https://" + TestCredentials.USERNAME + "." + Pryv.API_DOMAIN + "/";
    online = new OnlineEventsAndStreamsManager(url, TestCredentials.TOKEN, null);

  }

  // @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  // @Before
  public void setUp() throws Exception {
    success = false;
    error = false;
  }

  // @Test
  public void testCreateEvent() {
    Event newEvent = DummyData.generateFullEvent();
    online.createEvent(newEvent, callback);
    Awaitility.await().until(hasSuccess());
  }

  private Callable<Boolean> hasSuccess() {
    return new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        return success;
      }
    };
  }

  private Callable<Boolean> hasError() {
    return new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        return error;
      }
    };
  }

  private void instanciateCallback() {
    callback = new EventsCallback() {

      @Override
      public void onEventsRetrievalSuccess(Map<String, Event> onlineEvents, Double serverTime) {
        events = onlineEvents;
      }

      @Override
      public void onEventsSuccess(String successMessage, Event event, Integer stoppedId,
        Double pServerTime) {
        success = true;
      }

      @Override
      public void onEventsRetrievalError(String errorMessage, Double pServerTime) {
        error = true;
      }

      @Override
      public void onEventsError(String errorMessage, Double pServerTime) {
        error = true;
      }
    };
  }

}
