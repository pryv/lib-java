package com.pryv.api;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.pryv.Connection;
import com.pryv.Pryv;
import com.pryv.api.database.DBinitCallback;
import com.pryv.api.database.SQLiteDBHelper;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;
import com.pryv.utils.Logger;

/**
 *
 * Cache that fetches objects from local database and forwards requests to
 * OnlineEventsAndStreamsManager when necessary.
 *
 * @author ik
 *
 */
public class CacheEventsAndStreamsManager implements EventsManager, StreamsManager {

  private EventsManager onlineEventsManager;
  private StreamsManager onlineStreamsManager;

  private WeakReference<Connection> weakConnection;

  /**
   * defines the scope of the Events the SQLite DB contains. Concretely it
   * represents the Stream Id's for which the Events are synchronized with the
   * SQLite datbase.
   */
  private Set<String> scope;
  /**
   * indicates last time the cache was updated - in cloud time
   */
  private double lastModified;

  /**
   * ref to StreamsSupervisor
   */
  private StreamsSupervisor streams;

  /**
   * The SQLite DB
   */
  private SQLiteDBHelper dbHelper;

  private Logger logger = Logger.getInstance();

  /**
   * Cache constructor. Instanciates online module to which it passes url and
   * token parameters.
   *
   * @param url
   *          the url to which online requests are made.
   * @param token
   *          the token used to authenticate online requests.
   * @param initCallback
   *          the callback for SQLite database initialization
   * @param pStreams
   *          the Streams in volatile memory
   * @param pWeakConnection
   *          a reference to the connection object - used to assign
   *          weakreference
   */
  public CacheEventsAndStreamsManager(String url, String token, DBinitCallback initCallback,
    StreamsSupervisor pStreams, WeakReference<Connection> pWeakConnection) {
    weakConnection = pWeakConnection;
    onlineEventsManager = new OnlineEventsAndStreamsManager(url, token, pWeakConnection);
    onlineStreamsManager = (StreamsManager) onlineEventsManager;
    streams = pStreams;
    if (Pryv.isCacheActive()) {
      dbHelper = new SQLiteDBHelper(Pryv.DATABASE_NAME, initCallback);
      scope = new HashSet<String>();
      lastModified = 0;
    }
  }

  /*
   * Events management
   */

  @Override
  public void getEvents(Filter filter, EventsCallback connectionEventsCallback) {
    Filter onlineFilter = new Filter();
    if (Pryv.isCacheActive()) {
      // if some specific streams are requested
      if (filter.getStreamIds() != null) {
        // verify scope
        if (filter.areStreamIdsContainedInScope(scope, streams)) {
          // make request to online for full scope with field modifiedSince set
          // to lastModified
          onlineFilter.setModifiedSince(lastModified);
          onlineFilter.setStreamIds(scope);
        } else {
          for (String filterStreamId : filter.getStreamIds()) {
            if (!streams.verifyParency(filterStreamId, scope)) {
              scope.add(filterStreamId);
            }

          }
          // / make request to online for missing streams?
        }
      } else {
        // if all streams are requested

      }
      // retrieve Events from cache
      dbHelper.getEvents(filter, new CacheEventsCallback(null, connectionEventsCallback));
      logger.log("Cache: retrieved Events from cache: ");
    }
    if (Pryv.isOnlineActive()) {
      // forward call to online module
      onlineEventsManager.getEvents(onlineFilter, new CacheEventsCallback(filter,
        connectionEventsCallback));
    }
  }

  @Override
  public void createEvent(Event event, EventsCallback connectionEventsCallback) {
    if (Pryv.isCacheActive()) {
      // create Event in cache
      dbHelper.updateOrCreateEvent(event, new CacheEventsCallback(null, connectionEventsCallback));
    }
    if (Pryv.isOnlineActive()) {
      // forward call to online module
      onlineEventsManager.createEvent(event,
        new CacheEventsCallback(null, connectionEventsCallback));
    }
  }

  @Override
  public void deleteEvent(Event eventToDelete, EventsCallback connectionEventsCallback) {
    if (Pryv.isCacheActive()) {
      // delete Event from cache
      dbHelper.deleteEvent(eventToDelete, new CacheEventsCallback(null, connectionEventsCallback));
    }
    if (Pryv.isOnlineActive()) {
      // forward call to online module
      onlineEventsManager.deleteEvent(eventToDelete, new CacheEventsCallback(null,
        connectionEventsCallback));
    }
  }

  @Override
  public void updateEvent(Event eventToUpdate, EventsCallback connectionEventsCallback) {
    if (Pryv.isCacheActive()) {
      // update Event in cache
      dbHelper.updateOrCreateEvent(eventToUpdate, new CacheEventsCallback(null,
        connectionEventsCallback));
    }
    if (Pryv.isOnlineActive()) {
      // forward call to online module
      onlineEventsManager.updateEvent(eventToUpdate, new CacheEventsCallback(null,
        connectionEventsCallback));
    }
  }

  /*
   * Streams management
   */

  @Override
  public void getStreams(Filter filter, final StreamsCallback connectionStreamsCallback) {
    if (Pryv.isCacheActive()) {
      // retrieve Streams from cache
      logger.log("Cache: retrieved Streams from cache: ");
      dbHelper.getStreams(new CacheStreamsCallback(connectionStreamsCallback));
    }
    if (Pryv.isOnlineActive()) {
      onlineStreamsManager.getStreams(filter, new CacheStreamsCallback(connectionStreamsCallback));
    }
  }

  @Override
  public void createStream(Stream newStream, StreamsCallback connectionStreamsCallback) {
    if (Pryv.isCacheActive()) {
      // create Stream in DB
      dbHelper.updateOrCreateStream(newStream, new CacheStreamsCallback(connectionStreamsCallback));
    }
    if (Pryv.isOnlineActive()) {
      // forward call to online module
      onlineStreamsManager.createStream(newStream, new CacheStreamsCallback(
        connectionStreamsCallback));
    }
  }

  @Override
  public void deleteStream(Stream streamToDelete, boolean mergeWithParent,
    StreamsCallback connectionStreamsCallback) {
    if (Pryv.isCacheActive()) {
      // delete Stream from local db
      dbHelper.deleteStream(streamToDelete, new CacheStreamsCallback(connectionStreamsCallback));
    }
    if (Pryv.isOnlineActive()) {
      // forward call to online module
      onlineStreamsManager.deleteStream(streamToDelete, mergeWithParent, new CacheStreamsCallback(
        connectionStreamsCallback));
    }
  }

  @Override
  public void updateStream(Stream streamToUpdate, StreamsCallback connectionStreamsCallback) {
    if (Pryv.isCacheActive()) {
      // update Stream in local db
      dbHelper.updateOrCreateStream(streamToUpdate, new CacheStreamsCallback(
        connectionStreamsCallback));
    }
    if (Pryv.isOnlineActive()) {
      // forward call to online module
      onlineStreamsManager.updateStream(streamToUpdate, new CacheStreamsCallback(
        connectionStreamsCallback));
    }
  }

  /**
   * EventsCallback used by Cache
   *
   * @author ik
   *
   */
  private class CacheEventsCallback implements EventsCallback {

    private EventsCallback connectionEventsCallback;
    private EventsCallback cacheUpdateEventsCallback;

    public CacheEventsCallback(Filter filter, EventsCallback pConnectionEventsCallback) {
      connectionEventsCallback = pConnectionEventsCallback;
      cacheUpdateEventsCallback = new CacheUpdateEventsCallback(filter, this);
    }

    @Override
    public void onOnlineRetrieveEventsSuccess(Map<String, Event> onlineEvents, double serverTime) {
      // update Events in cache and send result to connection
      logger.log("Cache: update cache with online Events");
      lastModified = serverTime;

      if (Pryv.isCacheActive()) {
        // update Streams in cache and make a get call on the
        // cacheEventsCallback
        dbHelper.updateOrCreateEvents(onlineEvents.values(), cacheUpdateEventsCallback);
        // forward serverTime to connection
        connectionEventsCallback.onOnlineRetrieveEventsSuccess(null, serverTime);
      } else {
        // forward to connection
        connectionEventsCallback.onOnlineRetrieveEventsSuccess(onlineEvents, serverTime);
      }

      /*
       * new implementation: 1- check if event exists:
       * eventsSupervisor.getEventById(newEvent.getId()) if exists:
       * event.merge(newEvent) else: newEvent.generateClientId(),
       * eventsSupervisor.put(newEvent.getClientId(), newEvent)
       * 
       * 2- out of the loop: DBHelper.updateOrCreateEvents(onlineEvents); 3-
       * forward OK message to connection
       */

    }

    // unused
    @Override
    public void onCacheRetrieveEventsSuccess(Map<String, Event> cacheEvents) {
      logger.log("Cache: retrieved events from cache: events amount: " + cacheEvents.size());
      for (Event cacheEvent : cacheEvents.values()) {
        cacheEvent.assignConnection(weakConnection);
      }
      connectionEventsCallback.onCacheRetrieveEventsSuccess(cacheEvents);
    }

    @Override
    public void onEventsRetrievalError(String message) {
      connectionEventsCallback.onEventsRetrievalError(message);
    }

    // unused
    @Override
    public void onSupervisorRetrieveEventsSuccess(Map<String, Event> supervisorEvents) {
    }

    @Override
    public void onEventsSuccess(String successMessage) {
      connectionEventsCallback.onEventsSuccess(successMessage);
    }

    @Override
    public void onEventsError(String errorMessage) {
      connectionEventsCallback.onEventsError(errorMessage);
    }
  }

  /**
   * StreamsCallback used by cache class
   *
   * @author ik
   *
   */
  private class CacheStreamsCallback implements StreamsCallback {

    private StreamsCallback connectionStreamsCallback;
    private StreamsCallback cacheUpdateStreamsCallback;

    public CacheStreamsCallback(StreamsCallback pConnectionStreamsCallback) {
      connectionStreamsCallback = pConnectionStreamsCallback;
      cacheUpdateStreamsCallback = new CacheUpdateStreamsCallback(connectionStreamsCallback);
    }

    @Override
    public void
      onOnlineRetrieveStreamsSuccess(Map<String, Stream> onlineStreams, double serverTime) {
      logger.log("Cache: Streams retrieval success");

      if (Pryv.isCacheActive()) {
        // update Streams in cache and make a get call on the
        // cacheStreamsCallback
        dbHelper.updateOrCreateStreams(onlineStreams.values(), cacheUpdateStreamsCallback);
        // forward serverTime to connection
        connectionStreamsCallback.onOnlineRetrieveStreamsSuccess(null, serverTime);
      } else {
        // forward to connection
        connectionStreamsCallback.onOnlineRetrieveStreamsSuccess(onlineStreams, serverTime);
      }

    }

    @Override
    public void onCacheRetrieveStreamSuccess(Map<String, Stream> cacheStreams) {
      // forward to connection
      logger
        .log("Cache: retrieved streams from cache: root streams amount: " + cacheStreams.size());
      for (Stream cacheStream : cacheStreams.values()) {
        cacheStream.assignConnection(weakConnection);
      }
      connectionStreamsCallback.onCacheRetrieveStreamSuccess(cacheStreams);
    }

    @Override
    public void onStreamsRetrievalError(String message) {
      // forward to connection
      connectionStreamsCallback.onStreamsRetrievalError(message);
    }

    @Override
    public void onSupervisorRetrieveStreamsSuccess(Map<String, Stream> supervisorStreams) {
      // unused
    }

    @Override
    public void onStreamsSuccess(String successMessage) {
      // forward to connection
      connectionStreamsCallback.onStreamsSuccess(successMessage);
    }

    @Override
    public void onStreamError(String errorMessage) {
      // forward to connection
      connectionStreamsCallback.onStreamError(errorMessage);
    }

  }

  /**
   * Used when online retrieved streams need to update the cache and this result
   * is returned to the caller.
   *
   * @author ik
   *
   */
  private class CacheUpdateStreamsCallback implements StreamsCallback {

    private StreamsCallback cacheStreamsCallback;

    public CacheUpdateStreamsCallback(StreamsCallback pCacheStreamsCallback) {
      cacheStreamsCallback = pCacheStreamsCallback;
    }

    @Override
    public void
      onOnlineRetrieveStreamsSuccess(Map<String, Stream> onlineStreams, double serverTime) {
      // unused
    }

    @Override
    public void onCacheRetrieveStreamSuccess(Map<String, Stream> cacheStreams) {
      // unused
    }

    @Override
    public void onSupervisorRetrieveStreamsSuccess(Map<String, Stream> supervisorStreams) {
      // unused
    }

    @Override
    public void onStreamsRetrievalError(String errorMessage) {
      // unused
    }

    @Override
    public void onStreamsSuccess(String successMessage) {
      dbHelper.getStreams(cacheStreamsCallback);
    }

    @Override
    public void onStreamError(String errorMessage) {
      cacheStreamsCallback.onStreamError(errorMessage);
    }

  }

  /**
   * Used when online retrieved events need to update the cache and this result
   * is returned to the caller.
   *
   * @author ik
   *
   */
  private class CacheUpdateEventsCallback implements EventsCallback {

    private EventsCallback cacheEventsCallback;
    private Filter filter;

    public CacheUpdateEventsCallback(Filter pFilter, EventsCallback pCacheEventsCallback) {
      logger.log("cacheUpdateEventsCallback created");
      filter = pFilter;
      cacheEventsCallback = pCacheEventsCallback;
    }

    @Override
    public void onOnlineRetrieveEventsSuccess(Map<String, Event> onlineEvents, double serverTime) {
      // unused
    }

    @Override
    public void onCacheRetrieveEventsSuccess(Map<String, Event> cacheEvents) {
      // unused
    }

    @Override
    public void onSupervisorRetrieveEventsSuccess(Map<String, Event> supervisorEvents) {
      // unused
    }

    @Override
    public void onEventsRetrievalError(String errorMessage) {
      // unused
    }

    @Override
    public void onEventsSuccess(String successMessage) {
      logger.log("Cache: Events from online merge with cache successfully");
      dbHelper.getEvents(filter, cacheEventsCallback);
    }

    @Override
    public void onEventsError(String errorMessage) {
      cacheEventsCallback.onEventsError(errorMessage);
    }

  }
}
