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
public class OnlineEventsManager implements EventsManager<String>, StreamsManager {

  private String eventsUrl;
  private EventsCallback<String> eCallback;

  public OnlineEventsManager(String pUrl, EventsCallback<String> pECallback) {
    eventsUrl = pUrl;
    eCallback = pECallback;
  }

  public void get() {
    System.out.println("fetching events: " + eventsUrl);
    new FetchEventsThread().start();
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
        eCallback.onError(e.getMessage());
        e.printStackTrace();
      } catch (IOException e) {
        eCallback.onError(e.getMessage());
        e.printStackTrace();
      }
    }
  }


  private ResponseHandler<String> eventsResponseHandler = new ResponseHandler<String>() {

    public String handleResponse(HttpResponse reply) throws ClientProtocolException, IOException {
      String response = EntityUtils.toString(reply.getEntity());
      System.out.println("received: " + response);
      eCallback.onSuccess(response);
      return null;
    }
  };

  public Event create(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  public void delete(String id) {
    // TODO Auto-generated method stub

  }

  public Event update(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  public void addEventsCallback(EventsCallback<String> eCallback) {
    // TODO Auto-generated method stub

  }


}
