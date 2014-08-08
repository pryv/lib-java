package com.pryv;

import java.util.Map;

import com.pryv.api.CacheEventsAndStreamsManager;
import com.pryv.api.EventsCallback;
import com.pryv.api.EventsManager;
import com.pryv.api.Filter;
import com.pryv.api.StreamsCallback;
import com.pryv.api.StreamsManager;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;
import com.pryv.utils.Logger;
import com.pryv.utils.Supervisor;

/**
 *
 * Pryv API connection
 *
 * @author ik
 *
 */
public class Connection implements EventsManager<Map<String, Event>>,
  StreamsManager<Map<String, Stream>> {

  private String username;
  private String token;
  private String apiDomain = Pryv.API_DOMAIN; // pryv.io or pryv.in
  private String apiScheme = "https";
  private String url;
  private EventsManager<Map<String, Event>> cacheEventsManager;
  private StreamsManager<Map<String, Stream>> cacheStreamsManager;
  private Supervisor supervisor;

  private Logger logger = Logger.getInstance();

  public Connection(String pUsername, String pToken) {
    username = pUsername;
    token = pToken;
    url = apiScheme + "://" + username + "." + apiDomain + "/";
    supervisor = new Supervisor();
    cacheEventsManager = new CacheEventsAndStreamsManager(url, token);
    cacheStreamsManager = (StreamsManager) cacheEventsManager;
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

  /**
   *
   */
  @Override
  public void
    getEvents(final Filter filter, final EventsCallback<Map<String, Event>> eventsCallback) {

    // send
    eventsCallback.onEventsPartialResult(supervisor.getEvents(filter));

    cacheEventsManager.getEvents(filter, new EventsCallback<Map<String, Event>>() {

      @Override
      public void onEventsSuccess(Map<String, Event> events) {
        logger.log("Connection: onEventsSuccess");

        // update existing references with JSON received from online
        supervisor.updateEvents(events);

        // return merged events from main memory
        eventsCallback.onEventsSuccess(supervisor.getEvents(filter));
      }

      @Override
      public void onEventsPartialResult(Map<String, Event> newEvents) {
        supervisor.updateEvents(newEvents);
        eventsCallback.onEventsPartialResult(supervisor.getEvents(filter));
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
  public void getStreams(final StreamsCallback<Map<String, Stream>> streamsCallback) {

    cacheStreamsManager.getStreams(new StreamsCallback<Map<String, Stream>>() {

      @Override
      public void onStreamsSuccess(Map<String, Stream> streams) {
        supervisor.updateStreams(streams);
        streamsCallback.onStreamsSuccess(supervisor.getStreams());
      }

      @Override
      public void onStreamsPartialResult(Map<String, Stream> newStreams) {
        supervisor.updateStreams(newStreams);
        streamsCallback.onStreamsPartialResult(newStreams);
      }

      @Override
      public void onStreamsError(String message) {
        streamsCallback.onStreamsError(message);
      }
    });
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

}
