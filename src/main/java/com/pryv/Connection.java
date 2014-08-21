package com.pryv;

import java.util.Map;

import com.pryv.api.CacheEventsAndStreamsManager;
import com.pryv.api.EventsCallback;
import com.pryv.api.EventsManager;
import com.pryv.api.Filter;
import com.pryv.api.StreamsCallback;
import com.pryv.api.StreamsManager;
import com.pryv.api.Supervisor;
import com.pryv.api.database.DBinitCallback;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;
import com.pryv.utils.Logger;

/**
 *
 * Pryv API connection
 *
 * @author ik
 *
 */
public class Connection implements EventsManager, StreamsManager {

  private String username;
  private String token;
  private String apiDomain = Pryv.API_DOMAIN; // pryv.io or pryv.in
  private String apiScheme = "https";
  private String url;
  private EventsManager cacheEventsManager;
  private StreamsManager cacheStreamsManager;
  private Supervisor supervisor;

  private Logger logger = Logger.getInstance();

  /**
   * Connection constructor. builds the Url to which the online requests are
   * done using the provided username and API domain and Scheme from the Pryv
   * object. Instanciates Supervisor and CacheEventsAndStreamsManager.
   *
   * @param pUsername
   *          username used
   * @param pToken
   * @param dbInitCallback
   */
  public Connection(String pUsername, String pToken, DBinitCallback dbInitCallback) {
    username = pUsername;
    token = pToken;
    url = apiScheme + "://" + username + "." + apiDomain + "/";
    supervisor = new Supervisor();
    cacheEventsManager = new CacheEventsAndStreamsManager(url, token, dbInitCallback);
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

  /*
   * Events management
   */

  @Override
  public void getEvents(final Filter filter, EventsCallback userEventsCallback) {
    // send supervisor's events on User's callback.onEventsPartialResult()
    userEventsCallback.onSupervisorRetrieveEventsSuccess(supervisor.getEvents(filter));

    // forward getEvents() to Cache
    cacheEventsManager.getEvents(filter, new ConnectionEventsCallback(userEventsCallback, filter));
  }

  @Override
  public void createEvent(Event newEvent, EventsCallback userEventsCallback) {
    updateCreated(newEvent);
    // create event in Supervisor
    supervisor.updateOrCreateEvent(newEvent);

    // forward call to cache
    cacheEventsManager
      .createEvent(newEvent, new ConnectionEventsCallback(userEventsCallback, null));
  }

  @Override
  public void deleteEvent(Event eventToDelete, EventsCallback userEventsCallback) {
    updateModified(eventToDelete);
    eventToDelete.setTrashed(true);

    // delete Event in Supervisor
    supervisor.deleteEvent(eventToDelete);

    // forward call to cache
    cacheEventsManager.deleteEvent(eventToDelete, new ConnectionEventsCallback(userEventsCallback,
      null));
  }

  @Override
  public void updateEvent(Event eventToUpdate, EventsCallback userEventsCallback) {
    updateModified(eventToUpdate);
    // update Event in Supervisor
    supervisor.updateOrCreateEvent(eventToUpdate);

    // forward call to cache
    cacheEventsManager.updateEvent(eventToUpdate, new ConnectionEventsCallback(userEventsCallback,
      null));
  }

  /*
   * Streams management
   */

  @Override
  public void getStreams(Filter filter, final StreamsCallback userStreamsCallback) {

    // send Streams retrieved from Supervisor
    userStreamsCallback.onSupervisorRetrieveStreamsSuccess(supervisor.getStreams());

    // forward call to cache
    cacheStreamsManager.getStreams(filter, new ConnectionStreamsCallback(userStreamsCallback));
  }

  @Override
  public void createStream(Stream newStream, StreamsCallback userStreamsCallback) {
    updateCreated(newStream);
    // create Stream in Supervisor
    supervisor.updateOrCreateStream(newStream);
    // forward call to cache
    cacheStreamsManager.createStream(newStream, new ConnectionStreamsCallback(userStreamsCallback));
  }

  @Override
  public void deleteStream(Stream streamToDelete, StreamsCallback userStreamsCallback) {
    updateModified(streamToDelete);
    streamToDelete.setTrashed(true);
    for (Stream childStream : streamToDelete.getChildren()) {
      childStream.setTrashed(true);
    }
    // delete Stream in Supervisor
    supervisor.deleteStream(streamToDelete);
    // forward call to cache
    cacheStreamsManager.deleteStream(streamToDelete, new ConnectionStreamsCallback(
      userStreamsCallback));
  }

  @Override
  public void updateStream(Stream streamToUpdate, StreamsCallback userStreamsCallback) {
    updateModified(streamToUpdate);
    // update Stream in Supervisor
    supervisor.updateOrCreateStream(streamToUpdate);
    // forward call to cache
    cacheStreamsManager.updateStream(streamToUpdate, new ConnectionStreamsCallback(
      userStreamsCallback));
  }

  /**
   * Fill event's "created" and "createdBy" fields.
   *
   * @param event
   */
  private void updateCreated(Event event) {
    event.setCreated(System.currentTimeMillis() / 1000);
    event.setCreatedBy(username);
  }

  /**
   * Fill stream's "created" and "createdBy" fields.
   *
   * @param event
   */
  private void updateCreated(Stream stream) {
    stream.setCreated(System.currentTimeMillis() / 1000);
    stream.setCreatedBy(username);
  }

  /**
   * Update event's "modified" and "modifiedBy" fields.
   *
   * @param event
   *          the event to modifiy
   */
  private void updateModified(Event event) {
    event.setModified(System.currentTimeMillis() / 1000);
    event.setModifiedBy(username);
  }

  /**
   * Update stream's "modified" and modifiedBy" fields.
   *
   * @param stream
   *          the stream to modifiy
   */
  private void updateModified(Stream stream) {
    stream.setModified(System.currentTimeMillis() / 1000);
    stream.setModifiedBy(username);
  }

  /**
   * EventsCallback used by Connection class
   *
   * @author ik
   *
   */
  private class ConnectionEventsCallback implements EventsCallback {

    private EventsCallback userEventsCallback;
    private Filter filter;

    public ConnectionEventsCallback(EventsCallback pUserEventsCallback, Filter pFilter) {
      userEventsCallback = pUserEventsCallback;
      filter = pFilter;
    }

    @Override
    public void onOnlineRetrieveEventsSuccess(Map<String, Event> onlineEvents) {
      logger.log("Connection: onEventsSuccess");
      // update existing references with JSON received from online
      for (Event onlineEvent : onlineEvents.values()) {
        supervisor.updateOrCreateEvent(onlineEvent);
      }
      // return merged events from Supervisor
      userEventsCallback.onOnlineRetrieveEventsSuccess(supervisor.getEvents(filter));
    }

    @Override
    public void onCacheRetrieveEventsSuccess(Map<String, Event> cacheEvents) {
      // update existing Events with those retrieved from the cache
      for (Event cacheEvent : cacheEvents.values()) {
        supervisor.updateOrCreateEvent(cacheEvent);
      }
      // return merged events from Supervisor
      userEventsCallback.onCacheRetrieveEventsSuccess(supervisor.getEvents(filter));
    }

    @Override
    public void onEventsRetrievalError(String message) {
      userEventsCallback.onEventsRetrievalError(message);
    }

    // unused
    @Override
    public void onSupervisorRetrieveEventsSuccess(Map<String, Event> supervisorEvents) {
    }

    @Override
    public void onEventsSuccess(String successMessage) {
      userEventsCallback.onEventsSuccess(successMessage);
    }

    @Override
    public void onEventsError(String errorMessage) {
      userEventsCallback.onEventsError(errorMessage);
    }
  }

  /**
   * StreamsCallback used by Connection class
   *
   * @author ik
   *
   */
  private class ConnectionStreamsCallback implements StreamsCallback {

    private StreamsCallback userStreamsCallback;

    /**
     * public constructor
     *
     * @param pUserStreamsCallback
     *          the class to which the results are forwarder after optional
     *          processing.
     */
    public ConnectionStreamsCallback(StreamsCallback pUserStreamsCallback) {
      userStreamsCallback = pUserStreamsCallback;
    }

    @Override
    public void onOnlineRetrieveStreamsSuccess(Map<String, Stream> onlineStream) {
      for (Stream stream : onlineStream.values()) {
        supervisor.updateOrCreateStream(stream);
      }
      // forward updated Streams
      userStreamsCallback.onOnlineRetrieveStreamsSuccess(supervisor.getStreams());
    }

    @Override
    public void onCacheRetrieveStreamSuccess(Map<String, Stream> cacheStream) {
      for (Stream stream : cacheStream.values()) {
        supervisor.updateOrCreateStream(stream);
      }
      // forward updated Streams
      userStreamsCallback.onCacheRetrieveStreamSuccess(supervisor.getStreams());
    }

    @Override
    public void onStreamsRetrievalError(String message) {
      userStreamsCallback.onStreamsRetrievalError(message);
    }

    // unused
    @Override
    public void onSupervisorRetrieveStreamsSuccess(Map<String, Stream> supervisorStreams) {
    }

    @Override
    public void onStreamsSuccess(String successMessage) {
      userStreamsCallback.onStreamsSuccess(successMessage);
    }

    @Override
    public void onStreamError(String errorMessage) {
      userStreamsCallback.onStreamError(errorMessage);
    }

  }

}
