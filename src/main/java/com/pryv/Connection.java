package com.pryv;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pryv.api.CacheEventsManager;
import com.pryv.api.EventsCallback;
import com.pryv.api.EventsManager;
import com.pryv.api.OnlineEventsManager;
import com.pryv.api.StreamsManager;
import com.pryv.api.model.Event;
import com.pryv.utils.JsonConverter;
import com.pryv.utils.Supervisor;

/**
 *
 * Pryv API connection
 *
 * @author ik
 *
 */
public class Connection implements StreamsManager, EventsManager, EventsCallback {

  private String username;
  private String token;
  private String apiDomain = Pryv.API_DOMAIN; // pryv.io or pryv.in
  private String apiScheme = "https";
  private String url;
  private String eventsUrl;
  private EventsManager cacheEventsManager;
  private Supervisor supervisor;

  public Connection(String pUsername, String pToken) {
    username = pUsername;
    token = pToken;
    url = apiScheme + "://" + username + "." + apiDomain + "/";
    eventsUrl = url + "events?auth=" + token;

    supervisor = new Supervisor();
    EventsManager onlineEventsManager = new OnlineEventsManager(eventsUrl);
    cacheEventsManager = new CacheEventsManager(onlineEventsManager);
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

  public void get(EventsCallback eCallback) {
    cacheEventsManager.get(this);
    supervisor.getEvents();
    // do stuff with inMemory events
  }

  public void onSuccess(String jsonEvents) {
    try {
      JsonConverter.updateEventsFromJson(jsonEvents, supervisor.getEvents());
    } catch (JsonProcessingException e) {
      this.onError(e.getMessage());
      e.printStackTrace();
    } catch (IOException e) {
      this.onError(e.getMessage());
      e.printStackTrace();
    }

  }

  public void onPartialResult(Map<String, Event> newEvents) {
    // TODO Auto-generated method stub

  }

  public void onError(String message) {
    // TODO Auto-generated method stub

  }

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

}
