package com.pryv;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.pryv.api.CacheEventsAndStreamsManager;
import com.pryv.api.EventsCallback;
import com.pryv.api.EventsManager;
import com.pryv.api.StreamsCallback;
import com.pryv.api.StreamsManager;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;
import com.pryv.utils.JsonConverter;
import com.pryv.utils.Supervisor;

/**
 *
 * Pryv API connection
 *
 * @author ik
 *
 */
public class Connection implements EventsManager<Map<String, Event>>,
  EventsCallback<Map<String, Event>>, StreamsManager, StreamsCallback<Map<String, Stream>> {

  private String username;
  private String token;
  private String apiDomain = Pryv.API_DOMAIN; // pryv.io or pryv.in
  private String apiScheme = "https";
  private String url;
  private EventsManager<Map<String, Event>> cacheEventsManager;
  private StreamsManager streamsManager;
  private Supervisor supervisor;
  private List<EventsCallback<Map<String, Event>>> eventsCallbackList;
  private List<StreamsCallback<Map<String, Stream>>> streamsCallbackList;

  public Connection(String pUsername, String pToken) {
    username = pUsername;
    token = pToken;
    url = apiScheme + "://" + username + "." + apiDomain + "/";
    supervisor = new Supervisor();
    cacheEventsManager = new CacheEventsAndStreamsManager(url, token, this, this);
    streamsManager = (StreamsManager) cacheEventsManager;
    eventsCallbackList = new ArrayList<EventsCallback<Map<String, Event>>>();
    streamsCallbackList = new ArrayList<StreamsCallback<Map<String, Stream>>>();
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

  /**
   * Events management
   */

  @Override
  public void getEvents(Map<String, String> params) {
    supervisor.getEvents();
    cacheEventsManager.getEvents(params);
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
   * Events callback
   */

  @Override
  public void onEventsSuccess(Map<String, Event> events) {
    System.out.println("Connection: onSuccess");
    for (String key : events.keySet()) {
      if (supervisor.getEvents().get(key) != null) {
        supervisor.getEvents().get(key).merge(events.get(key), JsonConverter.getCloner());
      } else {
        supervisor.getEvents().put(key, events.get(key));
      }
    }
    for (EventsCallback<Map<String, Event>> ecb : eventsCallbackList) {
      ecb.onEventsSuccess(supervisor.getEvents());
    }
  }

  @Override
  public void onEventsPartialResult(Map<String, Event> newEvents) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onEventsError(String message) {
    // TODO Auto-generated method stub

  }

  /**
   * Streams management
   */

  @Override
  public List<Stream> getStreams() {
    return streamsManager.getStreams();
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
   * Streams callback
   */
  @Override
  public void onStreamsSuccess(Map<String, Stream> streams) {
    supervisor.setStreams(streams);
    for (StreamsCallback<Map<String, Stream>> streamsCallback : streamsCallbackList) {
      streamsCallback.onStreamsSuccess(supervisor.getStreams());
    }
  }

  @Override
  public void onStreamsPartialResult(Map newStreams) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onStreamsError(String message) {
    // TODO Auto-generated method stub

  }

  @Override
  public void addEventsCallback(EventsCallback<Map<String, Event>> eCallback) {
    eventsCallbackList.add(eCallback);
  }

  public void addStreamsCallback(StreamsCallback<Map<String, Stream>> sCallback) {
    streamsCallbackList.add(sCallback);
  }

}
