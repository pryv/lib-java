/*
package com.pryv;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;

import com.pryv.api.CacheEventsAndStreamsManager;
import com.pryv.api.EventsCallback;
import com.pryv.callbacks.EventsManager;
import com.pryv.api.EventsSupervisor;
import com.pryv.api.Filter;
import com.pryv.callbacks.StreamsCallback;
import com.pryv.callbacks.StreamsManager;
import com.pryv.api.StreamsSupervisor;
import com.pryv.api.database.DBinitCallback;
import Event;
import Stream;
import com.pryv.utils.Logger;

*/
/**
 *
 * Pryv API connection - Object used to manipulate Events and Streams data.
 *
 * @author ik
 *
 *//*

public class ConnectionOld implements EventsManager, StreamsManager {

  private String userID;
  private String accessToken;
  private String apiDomain = Pryv.DOMAIN;
  private String apiScheme = "https";

  private WeakReference<ConnectionOld> weakConnection;

  private double serverTime;
  */
/**
   * RTT between server and system: deltaTime = serverTime - systemTime
   *//*

  private double deltaTime = 0;

  private EventsManager cacheEventsManager;
  private StreamsManager cacheStreamsManager;
  private EventsSupervisor eventsSupervisor;
  private StreamsSupervisor streamsSupervisor;

  private Logger logger = Logger.getInstance();

  private final Double millisToSeconds = 1000.0;

  */
/**
   * ConnectionOld constructor. builds the Url to which the online requests are
   * done using the provided username and API domain and Scheme from the Pryv
   * object. Instanciates Supervisor and CacheEventsAndStreamsManager.
   *
   * @param pUserId
   *          username used
   * @param pAccessToken
   * @param pDBInitCallback
   *//*

  public ConnectionOld(String pUserId, String pAccessToken, DBinitCallback pDBInitCallback) {
    userID = pUserId;
    accessToken = pAccessToken;

    weakConnection = new WeakReference<ConnectionOld>(this);

    if (Pryv.isSupervisorActive()) {
      streamsSupervisor = new StreamsSupervisor();
      eventsSupervisor = new EventsSupervisor(streamsSupervisor);
      streamsSupervisor.setEventsSupervisor(eventsSupervisor);
    }

    if (Pryv.isCacheActive() || Pryv.isOnlineActive()) {
      String cacheFolder = null;

      if (Pryv.isCacheActive()) {
        // generate caching folder
        cacheFolder = "cache/" + getIdCaching() + "/";
        new File(cacheFolder).mkdirs();
      }
      cacheEventsManager =
              new CacheEventsAndStreamsManager(cacheFolder, getApiBaseUrl(), accessToken, pDBInitCallback,
                      streamsSupervisor, eventsSupervisor, weakConnection);
      cacheStreamsManager = (StreamsManager) cacheEventsManager;
    }
  }

  */
/**
   * returns the API URL, built from the API scheme, the userID and the API
   * domain.
   *
   * @return
   *//*

  private String getApiBaseUrl() {
    return apiScheme + "://" + userID + "." + apiDomain + "/";
  }

  */
/**
   * returns the API URL with the access token
   *
   * @return
   *//*

  private String getIdUrl() {
    return getApiBaseUrl() + "?auth=" + accessToken;
  }

  */
/**
   * returns the caching folder id
   *
   * @return
   *//*

  public String getIdCaching() {
    return DigestUtils.md5Hex(getIdUrl()) + "_" + userID + apiDomain + "_" + accessToken;
  }

  */
/*
   * Memory Streams management
   *//*


  */
/**
   * Returns the Root Streams, ie. those with field parent set at null
   *
   * @return
   *//*

  public Map<String, Stream> getRootStreams() {
    return streamsSupervisor.getRootStreams();
  }

  */
/**
   * Returns a reference to the Stream that has the provided Id.
   *
   * @param streamId
   * @return
   *//*

  public Stream getStreamById(String streamId) {
    return streamsSupervisor.getStreamById(streamId);
  }

  */
/*
   * Events management
   *//*


  @Override
  public void get(Filter filter, EventsCallback userEventsCallback) {
    if (Pryv.isSupervisorActive()) {
      // make sync request to supervisor
      eventsSupervisor.get(filter, userEventsCallback);
    }
    if (Pryv.isCacheActive() || Pryv.isOnlineActive()) {
      // forward get() to Cache
      cacheEventsManager.get(filter, new CacheManagerEventsCallback(userEventsCallback,
              filter));
    }
  }

  @Override
  public void create(Event newEvent, EventsCallback userEventsCallback) {
    newEvent.assignConnection(weakConnection);

    if (Pryv.isSupervisorActive()) {
      // make sync request to supervisor
      eventsSupervisor.updateOrCreateEvent(newEvent, userEventsCallback);
    }
    if (Pryv.isCacheActive() || Pryv.isOnlineActive()) {
      // forward call to cache
      cacheEventsManager.create(newEvent, new CacheManagerEventsCallback(userEventsCallback,
              null));
    }
  }

  @Override
  public void delete(Event eventToDelete, EventsCallback userEventsCallback) {

    if (Pryv.isSupervisorActive()) {
      // delete Event in Supervisor
      eventsSupervisor.delete(eventToDelete, userEventsCallback);
    }
    if (Pryv.isCacheActive() || Pryv.isOnlineActive()) {
      // forward call to cache
      cacheEventsManager.delete(eventToDelete, new CacheManagerEventsCallback(
              userEventsCallback, null));
    }
  }

  @Override
  public void update(Event eventToUpdate, EventsCallback userEventsCallback) {

    if (Pryv.isSupervisorActive()) {
      // update Event in Supervisor
      eventsSupervisor.updateOrCreateEvent(eventToUpdate, userEventsCallback);
    }
    if (Pryv.isCacheActive() || Pryv.isOnlineActive()) {
      // forward call to cache
      cacheEventsManager.update(eventToUpdate, new CacheManagerEventsCallback(
              userEventsCallback, null));
    }
  }

  */
/*
   * Streams management
   *//*


  @Override
  public void get(Filter filter, final StreamsCallback userStreamsCallback) {
    if (Pryv.isSupervisorActive()) {
      logger.log("ConnectionOld: retrieving streams from Supervisor");
      userStreamsCallback.onStreamsRetrievalSuccess(streamsSupervisor.getRootStreams(), serverTime);
    }
    if (Pryv.isCacheActive() || Pryv.isOnlineActive()) {
      // forward call to cache
      cacheStreamsManager.get(filter, new CacheManagerStreamsCallback(userStreamsCallback));
    }
  }

  @Override
  public void create(Stream newStream, StreamsCallback userStreamsCallback) {
    newStream.assignConnection(weakConnection);

    // generate an id if it wasn't set during creation
    if (newStream.getId() == null) {
      newStream.generateId();
      logger.log("ConnectionOld: Generated new id for stream: " + newStream.getId());
    }

    if (Pryv.isSupervisorActive()) {
      // create Stream in Supervisor
      streamsSupervisor.updateOrCreateStream(newStream, userStreamsCallback);
    }
    if (Pryv.isCacheActive() || Pryv.isOnlineActive()) {
      // forward call to cache
      cacheStreamsManager.create(newStream, new CacheManagerStreamsCallback(
              userStreamsCallback));
    }
  }

  @Override
  public void delete(Stream streamToDelete, boolean mergeEventsWithParent,
    StreamsCallback userStreamsCallback) {
    // // TODO check what to do with children
    // for (Stream childStream : streamToDelete.getChildren()) {
    // childStream.setTrashed(true);
    // }
    if (Pryv.isSupervisorActive()) {
      // delete Stream in Supervisor
      streamsSupervisor.delete(streamToDelete.getId(), mergeEventsWithParent,
        userStreamsCallback);
    }
    if (Pryv.isCacheActive() || Pryv.isOnlineActive()) {
      // forward call to cache
      cacheStreamsManager.delete(streamToDelete, mergeEventsWithParent,
        new CacheManagerStreamsCallback(userStreamsCallback));
    }
  }

  @Override
  public void update(Stream streamToUpdate, StreamsCallback userStreamsCallback) {
    if (Pryv.isSupervisorActive()) {
      // update Stream in Supervisor
      streamsSupervisor.updateOrCreateStream(streamToUpdate, userStreamsCallback);
    }
    if (Pryv.isCacheActive() || Pryv.isOnlineActive()) {
      // forward call to cache
      cacheStreamsManager.update(streamToUpdate, new CacheManagerStreamsCallback(
        userStreamsCallback));
    }
  }



  */
/**
   * EventsCallback for returns coming from CacheEventsAndStreamsManager
   *
   * @author ik
   *
   *//*

  private class CacheManagerEventsCallback implements EventsCallback {

    private EventsCallback userEventsCallback;
    private Filter filter;

    public CacheManagerEventsCallback(EventsCallback pUserEventsCallback, Filter pFilter) {
      userEventsCallback = pUserEventsCallback;
      filter = pFilter;
    }

    @Override
    public void onEventsRetrievalSuccess(Map<String, Event> cacheManagerEvents, Double pServerTime) {
      computeDelta(pServerTime);
      // return merged events from Supervisor - cacheEvents aren't used because
      // they don't contain the merged events
      if (Pryv.isSupervisorActive()) {
        eventsSupervisor.get(filter, userEventsCallback);
      } else {
        userEventsCallback.onEventsRetrievalSuccess(cacheManagerEvents, pServerTime);
      }
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

      if (eventsSupervisor != null) {
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

  */
/**
   * Streams Callback for CacheManager
   *
   * @author ik
   *
   *//*

  private class CacheManagerStreamsCallback implements StreamsCallback {

    private StreamsCallback userStreamsCallback;

    */
/**
     * public constructor
     *
     * @param pUserStreamsCallback
     *          the class to which the results are forwarded after optional
     *          processing.
     *//*

    public CacheManagerStreamsCallback(StreamsCallback pUserStreamsCallback) {
      userStreamsCallback = pUserStreamsCallback;
    }

    @Override
    public void onStreamsRetrievalSuccess(Map<String, Stream> cacheManagerStreams,
      Double pServerTime) {
      logger.log("CacheManagerStreamsCallback: Streams retrieval success");

      computeDelta(pServerTime);

      if (cacheManagerStreams != null) {
        // forward updated Streams
        userStreamsCallback.onStreamsRetrievalSuccess(cacheManagerStreams, serverTime);
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
*/
