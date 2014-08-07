package com.pryv.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;

import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;
import com.pryv.utils.Logger;

/**
 *
 * OnlineEventsAndStreamsManager fetches objects from online Pryv API
 *
 * @author ik
 *
 */
public class OnlineEventsAndStreamsManager implements EventsManager<String>, StreamsManager {

  private String eventsUrl;
  private String streamsUrl;
  private StreamsCallback<String> streamsCallback;

  private Logger logger = Logger.getInstance();

  public OnlineEventsAndStreamsManager(String pUrl, String token, StreamsCallback<String> sCallback) {
    eventsUrl = pUrl + "events?auth=" + token;
    streamsUrl = pUrl + "streams?auth=" + token;
    streamsCallback = sCallback;
  }

  /**
   * Events management
   *
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   */

  @Override
  public void getEvents(Map<String, String> params, EventsCallback<String> eventsCallback) {
    StringBuilder sb = new StringBuilder();
    String separator = "&";
    if (params != null) {
      for (String key : params.keySet()) {
        sb.append(separator);
        sb.append(key + "=" + params.get(key));
      }
    }
    new FetchEventsThread(sb.toString(), eventsCallback).start();
  }

  public void getE(Map<String, String> params) {

  }

  @Override
  public Event createEvent(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void deleteEvent(String id) {
    // TODO Auto-generated method stub

  }

  @Override
  public Event updateEvent(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Streams management
   */

  @Override
  public List<Stream> getStreams() {
    try {
      Request.Get(streamsUrl).execute().handleResponse(streamsResponseHandler);
    } catch (ClientProtocolException e) {
      streamsCallback.onStreamsError(e.getMessage());
      e.printStackTrace();
    } catch (IOException e) {
      streamsCallback.onStreamsError(e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  private ResponseHandler<String> streamsResponseHandler = new ResponseHandler<String>() {

    @Override
    public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
      String textResponse = EntityUtils.toString(response.getEntity());
      logger.log("Online received streams: " + textResponse);
      streamsCallback.onStreamsSuccess(textResponse);
      return null;
    }
  };

  @Override
  public Stream createStream(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void deleteStream(String id) {
    // TODO Auto-generated method stub

  }

  @Override
  public Stream updateStream(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Thread that executes the Get request to the Pryv server.
   *
   * @author ik
   *
   */
  private class FetchEventsThread extends Thread {
    private String params = "";
    private EventsCallback<String> eventsCallback;

    public FetchEventsThread(String pParams, EventsCallback<String> pEventsCallback) {
      params = pParams;
      eventsCallback = pEventsCallback;
    }

    @Override
    public void run() {
      logger.log("Online: fetching events at " + eventsUrl + params);
      try {
        Request.Get(eventsUrl + params).execute().handleResponse(eventsResponseHandler);
      } catch (ClientProtocolException e) {
        eventsCallback.onEventsError(e.getMessage());
        e.printStackTrace();
      } catch (IOException e) {
        eventsCallback.onEventsError(e.getMessage());
        e.printStackTrace();
      }
    }

    private ResponseHandler<String> eventsResponseHandler = new ResponseHandler<String>() {

      @Override
      public String handleResponse(HttpResponse reply) throws ClientProtocolException, IOException {
        String response = EntityUtils.toString(reply.getEntity());
        logger.log("Online: received events: " + response);
        eventsCallback.onEventsSuccess(response);
        return null;
      }
    };
  }

}
