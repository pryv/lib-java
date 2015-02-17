package com.pryv;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
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

  private String userID;
  private String accessToken;
  private String apiDomain = Pryv.API_DOMAIN; // pryv.io or pryv.in
  private String apiScheme = "https";
  private String url; // generated from apiScheme, apiDomain and username

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
    userID = pUsername;
    accessToken = pToken;
    url = getApiBaseUrl();
    String cacheFolder = null;
    // generate caching folder
    if (Pryv.isCacheActive()) {
      cacheFolder = "cache/" + getIdCaching();
      new File(cacheFolder).mkdirs();
      // new File("kk/blob").mkdirs();
    }
    weakConnection = new WeakReference<Connection>(this);
    streamsSupervisor = new StreamsSupervisor();
    eventsSupervisor = new EventsSupervisor(streamsSupervisor);
    streamsSupervisor.setEventsSupervisor(eventsSupervisor);
    cacheEventsManager =
      new CacheEventsAndStreamsManager(cacheFolder, url, accessToken, dbInitCallback,
        streamsSupervisor,
        eventsSupervisor, weakConnection);
    cacheStreamsManager = (StreamsManager) cacheEventsManager;
  }

  /**
   * returns the API URL, built from the API scheme, the userID and the API
   * domain.
   *
   * @return
   */
  private String getApiBaseUrl() {
    return apiScheme + "://" + userID + "." + apiDomain + "/";
  }

  /**
   * returns the API URL with the access token
   *
   * @return
   */
  private String getIdUrl() {
    return getApiBaseUrl() + "?auth=" + accessToken;
  }

  /**
   * returns the caching folder id
   *
   * @return
   */
  public String getIdCaching() {
    return DigestUtils.md5Hex(getIdUrl()) + "_" + userID + apiDomain + "_" + accessToken;
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

  // /**
  // * Returns a reference to the Stream that has the provided client Id.
  // *
  // * @param streamClientId
  // * @return
  // */
  // public Stream getStreamByClientId(String streamClientId) {
  // return streamsSupervisor.getStreamByClientId(streamClientId);
  // }
  /**
   * Returns a reference to the Stream that has the provided Id.
   *
   * @param streamId
   * @return
   */
  public Stream getStreamById(String streamId) {
    return streamsSupervisor.getStreamById(streamId);
  }

  /*
   * Events management
   */

  @Override
  public void getEvents(Filter filter, EventsCallback userEventsCallback) {
    // generate the set of stream Ids, which will be passed to the online module
    // filter.generateStreamIds(streamsSupervisor.getStreamsClientIdToIdDictionnary());
    if (Pryv.isSupervisorActive()) {
      // make sync request to supervisor
      eventsSupervisor.getEvents(filter, userEventsCallback);
    }
    if (Pryv.isCacheActive() || Pryv.isOnlineActive()) {
      // forward getEvents() to Cache
      cacheEventsManager.getEvents(filter, new CacheManagerEventsCallback(userEventsCallback,
        filter));
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
      cacheEventsManager.createEvent(newEvent, new CacheManagerEventsCallback(userEventsCallback,
        null));
    }
  }

  @Override
  public void deleteEvent(Event eventToDelete, EventsCallback userEventsCallback) {
    // eventToDelete.setTrashed(true);

    if (Pryv.isSupervisorActive()) {
      // delete Event in Supervisor
      eventsSupervisor.deleteEvent(eventToDelete, userEventsCallback);
    }
    if (Pryv.isCacheActive() || Pryv.isOnlineActive()) {
      // forward call to cache
      cacheEventsManager.deleteEvent(eventToDelete, new CacheManagerEventsCallback(
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
      cacheEventsManager.updateEvent(eventToUpdate, new CacheManagerEventsCallback(
        userEventsCallback, null));
    }
  }

  /*
   * Streams management
   */

  @Override
  public void getStreams(Filter filter, final StreamsCallback userStreamsCallback) {
    if (Pryv.isSupervisorActive()) {
      logger.log("Connection: retrieving streams from Supervisor");
      userStreamsCallback.onStreamsRetrievalSuccess(streamsSupervisor.getRootStreams(), null);
    }
    if (Pryv.isCacheActive() || Pryv.isOnlineActive()) {
      // forward call to cache
      cacheStreamsManager.getStreams(filter, new CacheManagerStreamsCallback(userStreamsCallback));
    }
  }

  @Override
  public void createStream(Stream newStream, StreamsCallback userStreamsCallback) {
    newStream.assignConnection(weakConnection);

    newStream.generateId();
    logger.log("Connection: Generating new id for stream: " + newStream.getId());

    if (Pryv.isSupervisorActive()) {
      // create Stream in Supervisor
      streamsSupervisor.updateOrCreateStream(newStream, userStreamsCallback);
    }
    if (Pryv.isCacheActive() || Pryv.isOnlineActive()) {
      // forward call to cache
      cacheStreamsManager.createStream(newStream, new CacheManagerStreamsCallback(
        userStreamsCallback));
    }
  }

  @Override
  public void deleteStream(Stream streamToDelete, boolean mergeEventsWithParent,
    StreamsCallback userStreamsCallback) {
    // streamToDelete.setTrashed(true);
    // // TODO check what to do with children
    // for (Stream childStream : streamToDelete.getChildren()) {
    // childStream.setTrashed(true);
    // }
    if (Pryv.isSupervisorActive()) {
      // delete Stream in Supervisor
      streamsSupervisor.deleteStream(streamToDelete.getId(), mergeEventsWithParent,
        userStreamsCallback);
    }
    if (Pryv.isCacheActive() || Pryv.isOnlineActive()) {
      // forward call to cache
      cacheStreamsManager.deleteStream(streamToDelete, mergeEventsWithParent,
        new CacheManagerStreamsCallback(userStreamsCallback));
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
      cacheStreamsManager.updateStream(streamToUpdate, new CacheManagerStreamsCallback(
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
  private void computeDelta(Double pServerTime) {
    if (pServerTime != null) {
      deltaTime = pServerTime - System.currentTimeMillis() / millisToSeconds;
    }
  }

  /**
   * EventsCallback for returns coming from CacheEventsAndStreamsManager
   *
   * @author ik
   *
   */
  private class CacheManagerEventsCallback implements EventsCallback {

    private EventsCallback userEventsCallback;
    private Filter filter;

    public CacheManagerEventsCallback(EventsCallback pUserEventsCallback, Filter pFilter) {
      userEventsCallback = pUserEventsCallback;
      filter = pFilter;
    }

    @Override
    public void onEventsRetrievalSuccess(Map<String, Event> cacheEvents, Double pServerTime) {
      computeDelta(pServerTime);
      // update existing Events with those retrieved from the cache
      for (Event cacheEvent : cacheEvents.values()) {
        eventsSupervisor.updateOrCreateEvent(cacheEvent, userEventsCallback);
      }
      // return merged events from Supervisor
      eventsSupervisor.getEvents(filter, userEventsCallback);
    }

    @Override
    public void onEventsRetrievalError(String message, Double pServerTime) {
      computeDelta(pServerTime);
      userEventsCallback.onEventsRetrievalError(message, pServerTime);
    }

    @Override
    public void onEventsSuccess(String successMessage, Event event, Integer stoppedId,
      Double pServerTime) {
      computeDelta(pServerTime);

      if (event != null) {
        if (event.getId() != null) {
          eventsSupervisor.updateOrCreateEvent(event, userEventsCallback);
        }

        if (event.getClientId() != null) {
          userEventsCallback.onEventsSuccess(successMessage,
            eventsSupervisor.getEventByClientId(event.getClientId()), stoppedId, pServerTime);
        } else {
          userEventsCallback.onEventsSuccess(successMessage,
            eventsSupervisor.getEventByClientId(eventsSupervisor.getClientId(event.getId())),
            stoppedId, pServerTime);
        }
      } else {
        userEventsCallback.onEventsSuccess(successMessage, event, stoppedId, pServerTime);
      }
    }

    @Override
    public void onEventsError(String errorMessage, Double pServerTime) {
      computeDelta(pServerTime);
      userEventsCallback.onEventsError(errorMessage, pServerTime);
    }
  }

  /**
   * Streams Callback for CacheManager
   *
   * @author ik
   *
   */
  private class CacheManagerStreamsCallback implements StreamsCallback {

    private StreamsCallback userStreamsCallback;

    /**
     * public constructor
     *
     * @param pUserStreamsCallback
     *          the class to which the results are forwarded after optional
     *          processing.
     */
    public CacheManagerStreamsCallback(StreamsCallback pUserStreamsCallback) {
      userStreamsCallback = pUserStreamsCallback;
    }

    @Override
    public void onStreamsRetrievalSuccess(Map<String, Stream> cacheManagerStreams,
      Double pServerTime) {
      logger.log("CacheManagerStreamsCallback: Streams retrieval success");

      computeDelta(pServerTime);

      if (cacheManagerStreams != null) {
        // for (Stream stream : cacheManagerStreams.values()) {
        // streamsSupervisor.updateOrCreateStream(stream, userStreamsCallback);
        // }
        // forward updated Streams
        userStreamsCallback.onStreamsRetrievalSuccess(getRootStreams(), serverTime);
      }
    }

    @Override
    public void onStreamsRetrievalError(String errorMessage, Double pServerTime) {
      computeDelta(pServerTime);
      userStreamsCallback.onStreamsRetrievalError(errorMessage, pServerTime);
    }

    @Override
    public void onStreamsSuccess(String successMessage, Stream stream, Double pServerTime) {
      computeDelta(pServerTime);
      userStreamsCallback.onStreamsSuccess(successMessage, stream, pServerTime);
    }

    @Override
    public void onStreamError(String errorMessage, Double pServerTime) {
      computeDelta(pServerTime);
      userStreamsCallback.onStreamError(errorMessage, pServerTime);
    }

  }

  public String getUserID() {
    return userID;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public String getApiDomain() {
    return apiDomain;
  }

  public String getApiScheme() {
    return apiScheme;
  }


}
