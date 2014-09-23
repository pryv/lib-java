package com.pryv;

import java.lang.ref.WeakReference;
import java.util.Map;

import org.joda.time.DateTime;

import com.pryv.api.CacheEventsAndStreamsManager;
import com.pryv.api.EventsCallback;
import com.pryv.api.EventsManager;
import com.pryv.api.EventsSupervisor;
import com.pryv.api.Filter;
import com.pryv.api.StreamsCallback;
import com.pryv.api.StreamsManager;
import com.pryv.api.StreamsSupervisor;
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

  private WeakReference<Connection> weakConnection;

  private double serverTime;
  /**
   * RTT between server and system: deltaTime = serverTime - systemTime
   */
  private double deltaTime = 0;

  private EventsManager cacheEventsManager;
  private StreamsManager cacheStreamsManager;
  private EventsSupervisor eventsSupervisor;
  private StreamsSupervisor streamsSupervisor;

  private Logger logger = Logger.getInstance();

  private final Double millisToSeconds = 1000.0;

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
    weakConnection = new WeakReference<Connection>(this);
    streamsSupervisor = new StreamsSupervisor();
    eventsSupervisor = new EventsSupervisor(streamsSupervisor);
    cacheEventsManager =
      new CacheEventsAndStreamsManager(url, token, dbInitCallback, streamsSupervisor,
        eventsSupervisor, weakConnection);
    cacheStreamsManager = (StreamsManager) cacheEventsManager;
  }

  /*
   * Memory Streams management
   */

  /**
   * Returns the Root Streams, ie. those with field parent set at null
   *
   * @return
   */
  public Map<String, Stream> getRootStreams() {
    return streamsSupervisor.getRootStreams();
  }

  /**
   * Returns a reference to the Stream that has the provided client Id.
   *
   * @param streamClientId
   * @return
   */
  public Stream getStreamByClientId(String streamClientId) {
    return streamsSupervisor.getStreamByClientId(streamClientId);
  }

  /*
   * Events management
   */

  @Override
  public void getEvents(Filter filter, EventsCallback userEventsCallback) {
    // generate the set of stream Ids, which will be passed to the online module
    filter.generateStreamIds(streamsSupervisor.getStreamsClientIdToIdDictionnary());
    if (Pryv.isSupervisorActive()) {
      // make sync request to supervisor
      eventsSupervisor.getEvents(filter, userEventsCallback);
    }
    if (Pryv.isCacheActive() || Pryv.isOnlineActive()) {
      // forward getEvents() to Cache
      cacheEventsManager
        .getEvents(filter, new ConnectionEventsCallback(userEventsCallback, filter));
    }
  }

  @Override
  public void createEvent(Event newEvent, EventsCallback userEventsCallback) {
    newEvent.assignConnection(weakConnection);

    if (Pryv.isSupervisorActive()) {
      // make sync request to supervisor
      eventsSupervisor.updateOrCreateEvent(newEvent, userEventsCallback);
    }
    if (Pryv.isCacheActive() || Pryv.isOnlineActive()) {
      // forward call to cache
      cacheEventsManager.createEvent(newEvent, new ConnectionEventsCallback(userEventsCallback,
        null));
    }
  }

  @Override
  public void deleteEvent(Event eventToDelete, EventsCallback userEventsCallback) {
    eventToDelete.setTrashed(true);

    if (Pryv.isSupervisorActive()) {
      // delete Event in Supervisor
      eventsSupervisor.deleteEvent(eventToDelete, userEventsCallback);
    }
    if (Pryv.isCacheActive() || Pryv.isOnlineActive()) {
      // forward call to cache
      cacheEventsManager.deleteEvent(eventToDelete, new ConnectionEventsCallback(
        userEventsCallback, null));
    }
  }

  @Override
  public void updateEvent(Event eventToUpdate, EventsCallback userEventsCallback) {

    if (Pryv.isSupervisorActive()) {
      // update Event in Supervisor
      eventsSupervisor.updateOrCreateEvent(eventToUpdate, userEventsCallback);
    }
    if (Pryv.isCacheActive() || Pryv.isOnlineActive()) {
      // forward call to cache
      cacheEventsManager.updateEvent(eventToUpdate, new ConnectionEventsCallback(
        userEventsCallback, null));
    }
  }

  /*
   * Streams management
   */

  @Override
  public void getStreams(Filter filter, final StreamsCallback userStreamsCallback) {
    userStreamsCallback.onSupervisorRetrieveStreamsSuccess(streamsSupervisor.getRootStreams());
    if (Pryv.isCacheActive() || Pryv.isSupervisorActive()) {
      // forward call to cache
      cacheStreamsManager.getStreams(filter, new ConnectionStreamsCallback(userStreamsCallback));
    }
  }

  @Override
  public void createStream(Stream newStream, StreamsCallback userStreamsCallback) {
    newStream.assignConnection(weakConnection);

    if (Pryv.isSupervisorActive()) {
      // create Stream in Supervisor
      streamsSupervisor.updateOrCreateStream(newStream, userStreamsCallback);
    }
    if (Pryv.isCacheActive() || Pryv.isOnlineActive()) {
      // forward call to cache
      cacheStreamsManager.createStream(newStream,
        new ConnectionStreamsCallback(userStreamsCallback));
    }
  }

  @Override
  public void deleteStream(Stream streamToDelete, boolean mergeWithParent,
    StreamsCallback userStreamsCallback) {
    streamToDelete.setTrashed(true);
    // TODO check what to do with children
    for (Stream childStream : streamToDelete.getChildren()) {
      childStream.setTrashed(true);
    }
    if (Pryv.isSupervisorActive()) {
      // delete Stream in Supervisor
      streamsSupervisor.deleteStream(streamToDelete.getClientId(), mergeWithParent,
        userStreamsCallback);
    }
    if (Pryv.isCacheActive() || Pryv.isOnlineActive()) {
      // forward call to cache
      cacheStreamsManager.deleteStream(streamToDelete, mergeWithParent,
        new ConnectionStreamsCallback(userStreamsCallback));
    }
  }

  @Override
  public void updateStream(Stream streamToUpdate, StreamsCallback userStreamsCallback) {
    if (Pryv.isSupervisorActive()) {
      // update Stream in Supervisor
      streamsSupervisor.updateOrCreateStream(streamToUpdate, userStreamsCallback);
    }
    if (Pryv.isCacheActive() || Pryv.isOnlineActive()) {
      // forward call to cache
      cacheStreamsManager.updateStream(streamToUpdate, new ConnectionStreamsCallback(
        userStreamsCallback));
    }
  }

  /**
   *
   *
   * @param time
   * @return
   */
  public DateTime serverTimeInSystemDate(double time) {
    return new DateTime(System.currentTimeMillis() / millisToSeconds + deltaTime);
  }

  /**
   * calculates the difference between server and system time: deltaTime =
   * serverTime - systemTime
   *
   * @param pServerTime
   */
  private void computeDelta(double pServerTime) {
    deltaTime = pServerTime - System.currentTimeMillis() / millisToSeconds;
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
    public void onOnlineRetrieveEventsSuccess(Map<String, Event> onlineEvents, double pServerTime) {
      logger.log("Connection: onOnlineRetrieveEventsSuccess");

      // update server time
      serverTime = pServerTime;
      // compute delta time between system time and serverTime
      computeDelta(pServerTime);
      // onlineStreams are not received here,

      // return merged events from Supervisor
      eventsSupervisor.getEvents(filter, this);
    }

    @Override
    public void onCacheRetrieveEventsSuccess(Map<String, Event> cacheEvents) {
      // update existing Events with those retrieved from the cache
      for (Event cacheEvent : cacheEvents.values()) {
        eventsSupervisor.updateOrCreateEvent(cacheEvent, userEventsCallback);
      }
      // return merged events from Supervisor
      eventsSupervisor.getEvents(filter, this);
    }

    @Override
    public void onEventsRetrievalError(String message) {
      userEventsCallback.onEventsRetrievalError(message);
    }

    // unused
    @Override
    public void onSupervisorRetrieveEventsSuccess(Map<String, Event> supervisorEvents) {
      userEventsCallback.onSupervisorRetrieveEventsSuccess(supervisorEvents);
    }

    @Override
    public void onEventsSuccess(String successMessage, Event event, Integer stoppedId) {
      if (event != null) {
        if (event.getId() != null) {
          eventsSupervisor.updateOrCreateEvent(event, userEventsCallback);
        }
      }
      userEventsCallback.onEventsSuccess(successMessage, event, stoppedId);
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
    public void
      onOnlineRetrieveStreamsSuccess(Map<String, Stream> onlineStreams, double pServerTime) {
      // update server time
      serverTime = pServerTime;
      // compute delta time between system time and servertime
      computeDelta(pServerTime);
      // onlineStreams are not received here,

      if (onlineStreams != null) {
        // for (Stream stream : onlineStreams.values()) {
        // streamsSupervisor.updateOrCreateStream(stream, userStreamsCallback);
        // }
        // forward updated Streams
        userStreamsCallback.onOnlineRetrieveStreamsSuccess(onlineStreams, serverTime);
      }
    }

    @Override
    public void onCacheRetrieveStreamSuccess(Map<String, Stream> cacheStream) {
      for (Stream stream : cacheStream.values()) {
        streamsSupervisor.updateOrCreateStream(stream, this);
      }
      // forward updated Streams
      userStreamsCallback.onCacheRetrieveStreamSuccess(streamsSupervisor.getRootStreams());
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

}
