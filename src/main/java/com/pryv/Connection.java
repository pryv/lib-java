package com.pryv;

import java.util.Map;

import com.pryv.api.Cache;
import com.pryv.api.EventManager;
import com.pryv.api.EventsCallback;
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
public class Connection implements StreamManager, EventManager, EventsCallback {

  private String username;
  private String token;
  private String apiDomain = Pryv.API_DOMAIN; // pryv.io or pryv.in
  private String apiScheme = "https"; // https
  private String url;
  private String eventsUrl;
  private EventManager cacheEventManager;
  private EventManager onlineEventManager;
  private Supervisor supervisor;

  public Connection(String pUsername, String pToken) {
    username = pUsername;
    token = pToken;
    initURL();

    supervisor = new Supervisor();
    onlineEventManager = new Online(eventsUrl);
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
    sb.append("events?auth=");
    sb.append(token);
    eventsUrl = sb.toString();
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

  public void getEvents(EventsCallback eCallback) {
    cacheEventManager.getEvents(this);
    supervisor.getEvents();
    // do stuff with inMemory events
  }

  public void onSuccess(Map<String, Event> newEvents) {
    // TODO Auto-generated method stub

  }

  public void onPartialResult(Map<String, Event> newEvents) {
    // TODO Auto-generated method stub

  }

  public void onError(String message) {
    // TODO Auto-generated method stub

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

}
