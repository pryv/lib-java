package com.pryv.api;

import java.sql.SQLException;
import java.util.Map;

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
   *
   * @throws SQLException
   * @throws ClassNotFoundException
   */
  public CacheEventsAndStreamsManager(String url, String token, DBinitCallback initCallback) {
    onlineEventsManager = new OnlineEventsAndStreamsManager(url, token);
    onlineStreamsManager = (StreamsManager) onlineEventsManager;
    dbHelper = new SQLiteDBHelper(Pryv.DATABASE_NAME, initCallback);
  }

  /*
   * Events management
   */

  @Override
  public void getEvents(Filter filter, EventsCallback connectionEventsCallback) {
    // retrieve Events from cache
    dbHelper.getEvents(filter, new CacheEventsCallback(connectionEventsCallback));
    logger.log("Cache: retrieved Events from cache: ");

    // forward call to online module
    onlineEventsManager.getEvents(filter, new CacheEventsCallback(connectionEventsCallback));
  }

  @Override
  public void createEvent(Event event, EventsCallback connectionEventsCallback) {
    // create Event in cache
    dbHelper.createEvent(event, new CacheEventsCallback(connectionEventsCallback));

    // forward call to online module
    onlineEventsManager.createEvent(event, new CacheEventsCallback(connectionEventsCallback));
  }

  @Override
  public void deleteEvent(Event eventToDelete, EventsCallback connectionEventsCallback) {
    // delete Event from cache
    dbHelper.deleteEvent(eventToDelete, new CacheEventsCallback(connectionEventsCallback));

    // forward call to online module
    onlineEventsManager.deleteEvent(eventToDelete,
      new CacheEventsCallback(connectionEventsCallback));
  }

  @Override
  public void updateEvent(Event eventToUpdate, EventsCallback connectionEventsCallback) {
    // update Event in cache
    dbHelper.updateEvent(eventToUpdate, new CacheEventsCallback(connectionEventsCallback));

    // forward call to online module
    onlineEventsManager.updateEvent(eventToUpdate,
      new CacheEventsCallback(connectionEventsCallback));
  }

  /*
   * Streams management
   */

  @Override
  public void getStreams(Filter filter, final StreamsCallback connectionStreamsCallback) {
    // retrieve Streams from cache
    logger.log("Cache: retrieved Streams from cache: ");
    dbHelper.getStreams(new CacheStreamsCallback(connectionStreamsCallback));

    onlineStreamsManager.getStreams(filter, new CacheStreamsCallback(connectionStreamsCallback));
  }

  @Override
  public void createStream(Stream newStream, StreamsCallback connectionStreamsCallback) {
    // create Stream in DB
    dbHelper.createStream(newStream, new CacheStreamsCallback(connectionStreamsCallback));

    // forward call to online module
    onlineStreamsManager.createStream(newStream,
      new CacheStreamsCallback(connectionStreamsCallback));
  }

  @Override
  public void deleteStream(Stream streamToDelete, StreamsCallback connectionStreamsCallback) {
    // delete Stream from local db
    dbHelper.deleteStream(streamToDelete, new CacheStreamsCallback(connectionStreamsCallback));

    // forward call to online module
    onlineStreamsManager.deleteStream(streamToDelete, new CacheStreamsCallback(
      connectionStreamsCallback));
  }

  @Override
  public void updateStream(Stream streamToUpdate, StreamsCallback connectionStreamsCallback) {
    // update Stream in local db
    dbHelper.updateStream(streamToUpdate, new CacheStreamsCallback(connectionStreamsCallback));

    // forward call to online module
    onlineStreamsManager.updateStream(streamToUpdate, new CacheStreamsCallback(
      connectionStreamsCallback));
  }

  /**
   * EventsCallback used by Cache
   *
   * @author ik
   *
   */
  private class CacheEventsCallback implements EventsCallback {

    private EventsCallback connectionEventsCallback;

    public CacheEventsCallback(EventsCallback pConnectionEventsCallback) {
      connectionEventsCallback = pConnectionEventsCallback;
    }

    @Override
    public void onOnlineRetrieveEventsSuccess(Map<String, Event> onlineEvents) {
      // update or create Cache with receivedEvents
      for (Event event : onlineEvents.values()) {
        dbHelper.updateEvent(event, new CacheEventsCallback(connectionEventsCallback));
      }
      // TODO SEND UPDATED EVENTS FROM CACHE
      connectionEventsCallback.onOnlineRetrieveEventsSuccess(onlineEvents);
    }

    // unused
    @Override
    public void onCacheRetrieveEventsSuccess(Map<String, Event> cacheEvents) {
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

    public CacheStreamsCallback(StreamsCallback pConnectionStreamsCallback) {
      connectionStreamsCallback = pConnectionStreamsCallback;
    }

    @Override
    public void onOnlineRetrieveStreamsSuccess(Map<String, Stream> onlineStreams) {
      logger.log("Cache: Streams retrieval success");

      for (Stream stream : onlineStreams.values()) {
        dbHelper.updateStream(stream, connectionStreamsCallback);
      }

      // TODO SEND UPDATED STREAMS FROM CACHE
      connectionStreamsCallback.onOnlineRetrieveStreamsSuccess(onlineStreams);
    }

    @Override
    public void onCacheRetrieveStreamSuccess(Map<String, Stream> cacheStreams) {
      connectionStreamsCallback.onCacheRetrieveStreamSuccess(cacheStreams);
    }

    @Override
    public void onStreamsRetrievalError(String message) {
      connectionStreamsCallback.onStreamsRetrievalError(message);
    }

    // unused
    @Override
    public void onSupervisorRetrieveStreamsSuccess(Map<String, Stream> supervisorStreams) {
    }

    @Override
    public void onStreamsSuccess(String successMessage) {
      connectionStreamsCallback.onStreamsSuccess(successMessage);
    }

    @Override
    public void onStreamError(String errorMessage) {
      connectionStreamsCallback.onStreamError(errorMessage);
    }

  }

}
