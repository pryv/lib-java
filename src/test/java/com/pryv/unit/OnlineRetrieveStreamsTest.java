package com.pryv.unit;

import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;

import resources.TestCredentials;

import com.jayway.awaitility.Awaitility;
import com.pryv.Pryv;
import com.pryv.api.Filter;
import com.pryv.api.OnlineEventsAndStreamsManager;
import com.pryv.api.StreamsCallback;
import com.pryv.api.model.Stream;

/**
 * class used to test online module for Streams fetching
 *
 * @author ik
 *
 */
public class OnlineRetrieveStreamsTest {

  private OnlineEventsAndStreamsManager online;
  private StreamsCallback streamsCallback;
  private Map<String, Stream> streams;

  @Before
  public void setUp() throws Exception {
    instanciateCallback();
    Pryv.setStaging();
    String url = "https://" + TestCredentials.USERNAME + "." + Pryv.API_DOMAIN + "/";
    online = new OnlineEventsAndStreamsManager(url, TestCredentials.TOKEN, null);
  }

  @Test
  public void testRetrieveStringAndDeserialize() {
    online.getStreams(new Filter(), streamsCallback);
    Awaitility.await().until(hasDeserializedStreams());
  }

  private Callable<Boolean> hasDeserializedStreams() {
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

  private void instanciateCallback() {
    streamsCallback = new StreamsCallback() {

      @Override
      public void onStreamsRetrievalSuccess(Map<String, Stream> onlineStreams, double serverTime) {
        streams = onlineStreams;
      }

      @Override
      public void onStreamsRetrievalError(String message) {
      }

      @Override
      public void onStreamsSuccess(String successMessage) {
      }

      @Override
      public void onStreamError(String errorMessage) {
      }

    };
  }

}
