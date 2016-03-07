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
   * represents the Stream client Id's for which the Events are synchronized
   * with the SQLite database.
   */
  private Set<String> scope;

  /**
   * minimum time value
   */
  private static final double MIN_TIME = -Double.MAX_VALUE;

  /**
   * last time value received from server on online Events retrieval.
   */
  private double lastOnlineRetrievalServerTime = MIN_TIME;

  /**
   * ref to StreamsSupervisor
   */
  private StreamsSupervisor streamsSupervisor;

  /**
   * ref to EventsSupervisor
   */
  private EventsSupervisor eventsSupervisor;

  /**
   * The SQLite DB
   */
  private SQLiteDBHelper dbHelper;

  private Logger logger = Logger.getInstance();

  /**
   * Cache constructor. Instanciates online module to which it passes url and
   * token parameters.
   *
   * @param cacheFolder
   *          the path to the cache folder
   * @param url
   *          the url to which online requests are made.
   * @param token
   *          the token used to authenticate online requests.
   * @param initCallback
   *          the callback for SQLite database initialization
   * @param pStreams
   *          the Streams in volatile memory
   * @param pEventsSupervisor
   *          the Events in volatile memory
   * @param pWeakConnection
   *          a reference to the connection object - used to assign
   *          weakreference
   */
  public CacheEventsAndStreamsManager(String cacheFolder, String url, String token,
    DBinitCallback initCallback, StreamsSupervisor pStreams, EventsSupervisor pEventsSupervisor,
    WeakReference<Connection> pWeakConnection) {
    weakConnection = pWeakConnection;
    onlineEventsManager = new OnlineEventsAndStreamsManager(url, token, pWeakConnection);
    onlineStreamsManager = (StreamsManager) onlineEventsManager;
    streamsSupervisor = pStreams;
    eventsSupervisor = pEventsSupervisor;
    if (Pryv.isCacheActive()) {
      dbHelper = new SQLiteDBHelper(cacheFolder, pWeakConnection, initCallback);
      scope = new HashSet<String>();
    }
  }

  /*
   * Events management
   */

  @Override
  public void getEvents(Filter filter, EventsCallback connectionEventsCallback) {
    // TODO when verifying if filter is included in scope, maybe clean the cache
    // when the scope is becoming smaller.
    if (Pryv.isCacheActive()) {
      if (filter.getStreamIds() != null) {
        // case when some specific streams are requested
        if (filter.areStreamIdsContainedInScope(scope, streamsSupervisor)) {
          // make request to online for full scope with field modifiedSince set
          // to lastModified
        } else {
          // filter contains streams that are not in the scope, scope is updated
          // and request is done for full history (retrieval time set to MIN)
          scope.addAll(filter.getStreamIds());
          lastOnlineRetrievalServerTime = MIN_TIME;
        }
      } else {
        // all streams are requested - no need to compare with scope
      }
      // retrieve Events from cache
      filter.setModifiedSince(lastOnlineRetrievalServerTime);
      dbHelper.getEvents(filter, new CacheEventsCallback(filter, connectionEventsCallback));
    }

  }

  @Override
  public void createEvent(Event event, EventsCallback connectionEventsCallback) {
    if (Pryv.isCacheActive()) {
      // create Event in cache
      dbHelper.updateOrCreateEvent(event, connectionEventsCallback);
    }
    if (Pryv.isOnlineActive()) {
      // forward call to online module
      onlineEventsManager.createEvent(event, new OnlineManagerEventsCallback(null,
        connectionEventsCallback));
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
      logger.log("CacheEventsAndStreamsManager: retrieving Streams from cache: ");
      dbHelper.getStreams(new CacheStreamsCallback(connectionStreamsCallback));
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
      onlineStreamsManager.createStream(newStream, new OnlineManagerStreamsCallback(
        connectionStreamsCallback));
    }
  }

  @Override
  public void deleteStream(Stream streamToDelete, boolean mergeEventsWithParent,
    StreamsCallback connectionStreamsCallback) {
    if (Pryv.isCacheActive()) {
      // delete Stream from local db
      dbHelper.deleteStream(streamToDelete, mergeEventsWithParent, new CacheStreamsCallback(
        connectionStreamsCallback));
    }
    if (Pryv.isOnlineActive()) {
      // forward call to online module
      onlineStreamsManager.deleteStream(streamToDelete, mergeEventsWithParent,
        new OnlineManagerStreamsCallback(connectionStreamsCallback));
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
      onlineStreamsManager.updateStream(streamToUpdate, new OnlineManagerStreamsCallback(
        connectionStreamsCallback));
    }
  }

  /**
   * EventsCallback for returns coming from OnlineEventsAndStreamsManager
   *
   * @author ik
   *
   */
  private class OnlineManagerEventsCallback implements EventsCallback {

    private EventsCallback connectionEventsCallback;
    private Filter filter;
    private double serverTime;

    public OnlineManagerEventsCallback(Filter pFilter, EventsCallback pConnectionEventsCallback) {
      connectionEventsCallback = pConnectionEventsCallback;
      filter = pFilter;
    }

    @Override
    public void onEventsRetrievalSuccess(Map<String, Event> onlineEvents, Double pServerTime) {
      // update Events in cache and send result to connection
      serverTime = pServerTime;
      lastOnlineRetrievalServerTime = pServerTime;
      logger.log("OnlineManagerEventsCallback: received online Events with serverTime "
        + serverTime);

      for (Event onlineEvent : onlineEvents.values()) {
        // merge with supervisor
        eventsSupervisor.updateOrCreateEvent(onlineEvent, connectionEventsCallback);
      }
      if(Pryv.isCacheActive()) {
        dbHelper.updateOrCreateEvents(onlineEvents.values(), connectionEventsCallback);
      }
      connectionEventsCallback.onEventsRetrievalSuccess(onlineEvents, pServerTime);
    }

    @Override
    public void onEventsRetrievalError(String errorMessage, Double pServerTime) {
      connectionEventsCallback.onEventsRetrievalError(errorMessage, pServerTime);
    }

    @Override
    public void onEventsSuccess(String successMessage, Event event, Integer stoppedId,
      Double pServerTime) {
      if (event != null && Pryv.isCacheActive()) {
        dbHelper.updateOrCreateEvent(event, new CacheEventsCallback(filter,
          connectionEventsCallback));
      }
      connectionEventsCallback.onEventsSuccess(successMessage, event, stoppedId, pServerTime);
    }

    @Override
    public void onEventsError(String errorMessage, Double pServerTime) {
      connectionEventsCallback.onEventsError(errorMessage, pServerTime);
    }

  }

  /**
   * EventsCallback for returns coming from Cache
   *
   * @author ik
   *
   */
  private class CacheEventsCallback implements EventsCallback {

    private EventsCallback connectionEventsCallback;
    private Filter filter;

    public CacheEventsCallback(Filter pFilter, EventsCallback pConnectionEventsCallback) {
      connectionEventsCallback = pConnectionEventsCallback;
      filter = pFilter;
    }

    @Override
    public void onEventsRetrievalSuccess(Map<String, Event> cacheEvents, Double pServerTime) {
      logger.log("CacheEventsCallback: retrieved events from cache: events amount: "
        + cacheEvents.size());

      // assign connection, update in supervisor
      for (Event cacheEvent : cacheEvents.values()) {
        cacheEvent.assignConnection(weakConnection);
        eventsSupervisor.updateOrCreateEvent(cacheEvent, this);
      }
      connectionEventsCallback.onEventsRetrievalSuccess(cacheEvents, pServerTime);

      if (Pryv.isOnlineActive()) {
        Filter onlineFilter = new Filter();
        onlineFilter.setModifiedSince(lastOnlineRetrievalServerTime);
        onlineFilter.setStreamIds(scope);
        // forward call to online module
        onlineEventsManager.getEvents(onlineFilter, new OnlineManagerEventsCallback(filter,
          connectionEventsCallback));
      }
    }

    @Override
    public void onEventsRetrievalError(String message, Double pServerTime) {
      connectionEventsCallback.onEventsRetrievalError(message, pServerTime);
    }

    @Override
    public void onEventsSuccess(String successMessage, Event event, Integer stoppedId,
      Double pServerTime) {
      connectionEventsCallback.onEventsSuccess(successMessage, event, stoppedId, pServerTime);
    }

    @Override
    public void onEventsError(String errorMessage, Double pServerTime) {
      connectionEventsCallback.onEventsError(errorMessage, pServerTime);
    }
  }

  /**
   * Streams callback for local cache DB
   *
   * @author ik
   *
   */
  private class CacheStreamsCallback implements StreamsCallback {

    private StreamsCallback connectionStreamsCallback;

    public CacheStreamsCallback(StreamsCallback pConnectionStreamsCallback) {
      connectionStreamsCallback = pConnectionStreamsCallback;
    }

    @Override
    public void onStreamsRetrievalSuccess(Map<String, Stream> rootsStreams, Double serverTime) {
      // forward to connection
      logger.log("CacheStreamsCallback: retrieved streams from cache: root streams amount: "
        + rootsStreams.size());

      streamsSupervisor.updateOrCreateStreams(rootsStreams, connectionStreamsCallback);

      connectionStreamsCallback.onStreamsRetrievalSuccess(streamsSupervisor.getRootStreams(),
        serverTime);

      // make the online request
      if (Pryv.isOnlineActive()) {
        onlineStreamsManager.getStreams(null, new OnlineManagerStreamsCallback(
          connectionStreamsCallback));
      }
    }

    @Override
    public void onStreamsRetrievalError(String errorMessage, Double serverTime) {
      // forward to connection
      connectionStreamsCallback.onStreamsRetrievalError(errorMessage, serverTime);
    }

    @Override
    public void onStreamsSuccess(String successMessage, Stream stream, Double serverTime) {
      // forward to connection
      connectionStreamsCallback.onStreamsSuccess(successMessage, stream, serverTime);
    }

    @Override
    public void onStreamError(String errorMessage, Double serverTime) {
      // forward to connection
      connectionStreamsCallback.onStreamError(errorMessage, serverTime);
    }

  }

  /**
   * Streams Callback for Online API calls
   *
   * @author ik
   *
   */
  private class OnlineManagerStreamsCallback implements StreamsCallback {

    private StreamsCallback connectionStreamsCallback;

    public OnlineManagerStreamsCallback(StreamsCallback pConnectionStreamsCallback) {
      connectionStreamsCallback = pConnectionStreamsCallback;
    }

    @Override
    public void onStreamsRetrievalSuccess(Map<String, Stream> onlineStreams, Double serverTime) {
      logger.log("OnlineManagerStreamsCallback: Streams retrieval success");

      streamsSupervisor.updateOrCreateStreams(onlineStreams, connectionStreamsCallback);

      connectionStreamsCallback.onStreamsRetrievalSuccess(streamsSupervisor.getRootStreams(),
        serverTime);

      if (Pryv.isCacheActive()) {
        dbHelper.updateOrCreateStreams(streamsSupervisor.getRootStreams().values(), this);
      }
    }

    @Override
    public void onStreamsRetrievalError(String errorMessage, Double serverTime) {
      // forward to connection
      connectionStreamsCallback.onStreamsRetrievalError(errorMessage, serverTime);
    }

    @Override
    public void onStreamsSuccess(String successMessage, Stream stream, Double serverTime) {
      // forward to connection
      connectionStreamsCallback.onStreamsSuccess(successMessage, stream, serverTime);
    }

    @Override
    public void onStreamError(String errorMessage, Double serverTime) {
      // forward to connection
      connectionStreamsCallback.onStreamError(errorMessage, serverTime);
    }

  }

}
