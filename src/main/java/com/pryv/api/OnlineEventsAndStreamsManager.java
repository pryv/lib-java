package com.pryv.api;

import java.io.IOException;

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
public class OnlineEventsAndStreamsManager implements EventsManager<String>, StreamsManager<String> {

  private String eventsUrl;
  private String streamsUrl;

  private Logger logger = Logger.getInstance();

  public OnlineEventsAndStreamsManager(String pUrl, String token) {
    eventsUrl = pUrl + "events?auth=" + token;
    streamsUrl = pUrl + "streams?auth=" + token;
  }

  /**
   * Events management
   *
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   */

  @Override
  public void getEvents(Filter filter, EventsCallback<String> eventsCallback) {

    // format parameters for URL
    // StringBuilder sb = new StringBuilder();
    // String separator = "&";
    // if (params != null) {
    // for (String key : params.keySet()) {
    // sb.append(separator);
    // sb.append(key + "=" + params.get(key));
    // }
    // }
    new FetchEventsThread(filter.toUrlParameters(), eventsCallback).start();
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
  public void getStreams(StreamsCallback<String> streamsCallback) {
    new FetchStreamsThread(streamsCallback).start();
  }

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
   * Thread that executes the Get Streams request to the Pryv server and returns
   * the response to the StreamsCallback as a String.
   *
   * @author ik
   *
   */
  private class FetchStreamsThread extends Thread {

    private StreamsCallback<String> streamsCallback;

    public FetchStreamsThread(StreamsCallback<String> pStreamsCallback) {
      streamsCallback = pStreamsCallback;
    }

    @Override
    public void run() {
      try {
        Request.Get(streamsUrl).execute().handleResponse(streamsResponseHandler);
      } catch (ClientProtocolException e) {
        streamsCallback.onStreamsError(e.getMessage());
        e.printStackTrace();
      } catch (IOException e) {
        streamsCallback.onStreamsError(e.getMessage());
        e.printStackTrace();
      }
    }

    private ResponseHandler<String> streamsResponseHandler = new ResponseHandler<String>() {

      @Override
      public String handleResponse(HttpResponse response) throws ClientProtocolException,
        IOException {
        String textResponse = EntityUtils.toString(response.getEntity());
        logger.log("Online received streams: " + textResponse);
        streamsCallback.onStreamsSuccess(textResponse);
        return null;
      }
    };
  }

  /**
   * Thread that executes the Get Events request to the Pryv server and returns
   * the response to the EventsCallback as a String.
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
