package com.pryv.api;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;

import com.pryv.api.model.Event;

/**
 *
 * used to fetch objects from online Pryv API
 *
 * @author ik
 *
 */
public class OnlineEventsManager implements EventsManager, StreamsManager {

  private String eventsUrl;
  private EventsCallback eCallback;

  public OnlineEventsManager(String pUrl) {
    eventsUrl = pUrl;
  }

  public void getEvents(EventsCallback pECallback) {
    eCallback = pECallback;
    System.out.println("fetching events: " + eventsUrl);
    new FetchEventsThread().start();
  }

  private ResponseHandler<String> eventsResponseHandler = new ResponseHandler<String>() {

    public String handleResponse(HttpResponse reply) throws ClientProtocolException, IOException {
      System.out.println("received: " + EntityUtils.toString(reply.getEntity()));
      // JsonConverter.fromJson()
      // eCallback.onSuccess(newEvents);
      return null;
    }
  };

  public Event createEvent(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  public void deleteEvent(String id) {
    // TODO Auto-generated method stub

  }

  public Event updateEvenet(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  private class FetchEventsThread extends Thread {
    @Override
    public void run() {
      try {
        Request.Get(eventsUrl).execute().handleResponse(eventsResponseHandler);
      } catch (ClientProtocolException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

}
