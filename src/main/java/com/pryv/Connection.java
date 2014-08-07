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
import com.pryv.utils.Logger;
import com.pryv.utils.Supervisor;

/**
 *
 * Pryv API connection
 *
 * @author ik
 *
 */
public class Connection implements EventsManager<Map<String, Event>>, StreamsManager,
  StreamsCallback<Map<String, Stream>> {

  private String username;
  private String token;
  private String apiDomain = Pryv.API_DOMAIN; // pryv.io or pryv.in
  private String apiScheme = "https";
  private String url;
  private EventsManager<Map<String, Event>> cacheEventsManager;
  private StreamsManager streamsManager;
  private Supervisor supervisor;
  private List<StreamsCallback<Map<String, Stream>>> streamsCallbackList;

  private Logger logger = Logger.getInstance();

  public Connection(String pUsername, String pToken) {
    username = pUsername;
    token = pToken;
    url = apiScheme + "://" + username + "." + apiDomain + "/";
    supervisor = new Supervisor();
    cacheEventsManager = new CacheEventsAndStreamsManager(url, token, this);
    streamsManager = (StreamsManager) cacheEventsManager;
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
  public void getEvents(Map<String, String> params,
    final EventsCallback<Map<String, Event>> eventsCallback) {
    eventsCallback.onEventsPartialResult(supervisor.getEvents(params));
    cacheEventsManager.getEvents(params, new EventsCallback<Map<String, Event>>() {

      @Override
      public void onEventsSuccess(Map<String, Event> events) {
        logger.log("Connection: onSuccess");
        for (String key : events.keySet()) {
          if (supervisor.getEvents(null).get(key) != null) {
            supervisor.getEvents(null).get(key).merge(events.get(key), JsonConverter.getCloner());
          } else {
            supervisor.getEvents(null).put(key, events.get(key));
          }
        }
        eventsCallback.onEventsSuccess(supervisor.getEvents(null));
      }

      @Override
      public void onEventsPartialResult(Map<String, Event> newEvents) {
        eventsCallback.onEventsPartialResult(newEvents);
      }

      @Override
      public void onEventsError(String message) {
        eventsCallback.onEventsError(message);
      }
    });
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
  public void onStreamsPartialResult(Map<String, Stream> newStreams) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onStreamsError(String message) {
    for (StreamsCallback<Map<String, Stream>> streamsCallback : streamsCallbackList) {
      streamsCallback.onStreamsError(message);
    }
  }

  public void addStreamsCallback(StreamsCallback<Map<String, Stream>> sCallback) {
    streamsCallbackList.add(sCallback);
  }

}
