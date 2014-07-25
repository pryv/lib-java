package com.pryv;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.pryv.api.CacheEventsManager;
import com.pryv.api.EventsCallback;
import com.pryv.api.EventsManager;
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
public class Connection implements StreamsManager, EventsManager<Map<String, Event>>,
  EventsCallback<Map<String, Event>> {

  private String username;
  private String token;
  private String apiDomain = Pryv.API_DOMAIN; // pryv.io or pryv.in
  private String apiScheme = "https";
  private String url;
  private String eventsUrl;
  private EventsManager<Map<String, Event>> cacheEventsManager;
  private Supervisor supervisor;
  private List<EventsCallback<Map<String, Event>>> eventsCallbackList;

  public Connection(String pUsername, String pToken) {
    username = pUsername;
    token = pToken;
    url = apiScheme + "://" + username + "." + apiDomain + "/";
    eventsUrl = url + "events?auth=" + token;
    supervisor = new Supervisor();
    cacheEventsManager = new CacheEventsManager(eventsUrl, this);
    eventsCallbackList = new ArrayList<EventsCallback<Map<String, Event>>>();
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

  public void get() {
    supervisor.getEvents();
    cacheEventsManager.get();
  }

  public void addEventsCallback(EventsCallback<Map<String, Event>> eCallback) {
    eventsCallbackList.add(eCallback);
  }

  public void onSuccess(Map<String, Event> events) {
    System.out.println("Connection: onSuccess");
    for (String key : events.keySet()) {
      if (supervisor.getEvents().get(key) != null) {
        supervisor.getEvents().get(key).merge(events.get(key), JsonConverter.getCloner());
      } else {
        supervisor.getEvents().put(key, events.get(key));
      }
    }
    for (EventsCallback<Map<String, Event>> ecb : eventsCallbackList) {
      ecb.onSuccess(supervisor.getEvents());
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
