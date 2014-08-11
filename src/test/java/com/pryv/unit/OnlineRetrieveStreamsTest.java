package com.pryv.unit;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;

import resources.TestCredentials;

import com.jayway.awaitility.Awaitility;
import com.pryv.Pryv;
import com.pryv.api.OnlineEventsAndStreamsManager;
import com.pryv.api.StreamsCallback;
import com.pryv.api.model.Stream;
import com.pryv.utils.JsonConverter;

/**
 * class used to test online module for Streams fetching
 *
 * @author ik
 *
 */
public class OnlineRetrieveStreamsTest {

  private OnlineEventsAndStreamsManager online;
  private StreamsCallback<String> streamsCallback;
  private String stringStreams;
  private Map<String, Stream> streams;

  @Before
  public void setUp() throws Exception {
    instanciateCallback();

    String url = "https://" + TestCredentials.USERNAME + "." + Pryv.API_DOMAIN + "/";
    online = new OnlineEventsAndStreamsManager(url, TestCredentials.TOKEN);
  }

  @Test
  public void testRetrieveStreams() {
    online.getStreams(streamsCallback);
    Awaitility.await().until(hasReceivedString());
  }

  private Callable<Boolean> hasReceivedString() {
    return new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        if (stringStreams != null) {
          return stringStreams.length() > 0;
        } else {
          return false;
        }
      }
    };
  }

  @Test
  public void testRetrieveStringAndDeserialize() {
    online.getStreams(streamsCallback);
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
    streamsCallback = new StreamsCallback<String>() {

      @Override
      public void onStreamsSuccess(String receivedStreams) {
        stringStreams = receivedStreams;
        try {
          streams = JsonConverter.createStreamsFromJson(receivedStreams);
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

      // unused
      @Override
      public void onStreamsPartialResult(Map<String, Stream> newStreams) {
      }

      @Override
      public void onStreamsError(String message) {
        // TODO Auto-generated method stub

      }

    };
  }

}
