package com.pryv.api;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;

import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;

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
  private EventsCallback<String> eventsCallback;
  private StreamsCallback<String> streamsCallback;

  public OnlineEventsAndStreamsManager(String pUrl, String token,
    EventsCallback<String> eCallback, StreamsCallback<String> sCallback) {
    eventsUrl = pUrl + "events?auth=" + token;
    streamsUrl = pUrl + "streams?auth=" + token;
    eventsCallback = eCallback;
    streamsCallback = sCallback;
  }

  /**
   * Events management
   */

  public void getEvents() {
    System.out.println("fetching events: " + eventsUrl);
    new FetchEventsThread().start();
  }

  public Event createEvent(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  public void deleteEvent(String id) {
    // TODO Auto-generated method stub

  }

  public Event updateEvent(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Streams management
   */

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

    public String handleResponse(HttpResponse response) throws ClientProtocolException,
      IOException {
      String textResponse = EntityUtils.toString(response.getEntity());
      System.out.println("received: " + textResponse);
      streamsCallback.onStreamsSuccess(textResponse);
      return null;
    }
  };

  public Stream createStream(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  public void deleteStream(String id) {
    // TODO Auto-generated method stub

  }

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
    @Override
    public void run() {
      try {
        Request.Get(eventsUrl).execute().handleResponse(eventsResponseHandler);
      } catch (ClientProtocolException e) {
        eventsCallback.onEventsError(e.getMessage());
        e.printStackTrace();
      } catch (IOException e) {
        eventsCallback.onEventsError(e.getMessage());
        e.printStackTrace();
      }
    }
  }

  private ResponseHandler<String> eventsResponseHandler = new ResponseHandler<String>() {

    public String handleResponse(HttpResponse reply) throws ClientProtocolException, IOException {
      String response = EntityUtils.toString(reply.getEntity());
      System.out.println("received: " + response);
      eventsCallback.onEventsSuccess(response);
      return null;
    }
  };

  /**
   * other
   */

  public void addEventsCallback(EventsCallback<String> eCallback) {
    // TODO Auto-generated method stub

  }

}
