package com.pryv;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;

import com.pryv.api.Cache;
import com.pryv.api.EventManager;
import com.pryv.api.Online;
import com.pryv.api.StreamManager;
import com.pryv.api.model.Event;
import com.pryv.utils.Supervisor;

/**
 *
 * Pryv API connection
 *
 * @author ik
 *
 */
public class Connection implements StreamManager, EventManager {

  private String username;
  private String token;
  private String apiDomain = Pryv.API_DOMAIN; // pryv.io or pryv.in
  private String apiScheme = "https"; // https
  private String url;
  private EventManager cacheEventManager;
  private EventManager onlineEventManager;
  private Supervisor supervisor;

  public Connection(String pUsername, String pToken) {
    username = pUsername;
    token = pToken;
    initURL();

    supervisor = new Supervisor();
    onlineEventManager = new Online();
    cacheEventManager = new Cache(onlineEventManager);
  }

  private void initURL() {
    StringBuilder sb = new StringBuilder();
    sb.append(apiScheme);
    sb.append("://");
    sb.append(username);
    sb.append(".");
    sb.append(apiDomain);
    sb.append("/");
    // apiScheme://username.apiDomain/
    url = sb.toString();
  }

  public String getUsername() {
    return username;
  }

  public String getToken() {
    return token;
  }

  public String getApiDomain() {
    return apiDomain;
  }

  public String getApiScheme() {
    return apiScheme;
  }

  public String getUrl() {
    return url;
  }

  public List<Event> getEvents() {
    try {
      cacheEventManager.getEvents();
      String eventsUrl = url + "events?auth=" + token;
      System.out.println("fetching events: " + eventsUrl);
      Request.Get(url + "events?auth=" + token).execute().handleResponse(eventsResponseHandler);
    } catch (ClientProtocolException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

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

  private ResponseHandler<String> eventsResponseHandler = new ResponseHandler<String>() {

    public String handleResponse(HttpResponse reply) throws ClientProtocolException, IOException {
      System.out.println("received: " + EntityUtils.toString(reply.getEntity()));
      return null;
    }
  };

}
