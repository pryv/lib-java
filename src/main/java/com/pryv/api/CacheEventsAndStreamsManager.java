package com.pryv.api;

import java.sql.SQLException;
import java.util.HashMap;
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
  public void getEvents(Filter filter, final EventsCallback connectionEventsCallback) {
    // look in cache and send it onPartialResult
    // connectionEventsCallback.onCacheRetrieveEventsSuccess(dbHelper.getEvents(filter));
    dbHelper.getEvents(filter, new CacheEventsCallback(connectionEventsCallback));
    logger.log("Cache: retrieved Events from cache: ");

    // forward call to online module
    onlineEventsManager.getEvents(filter, new CacheEventsCallback(connectionEventsCallback));
  }

  /**
   * Store most recently modified Event in Cache
   *
   * @param newEvents
   *          Events received from online module
   */
  private void updateEvents(Map<String, Event> newEvents) {
    for (Event event : newEvents.values()) {
        dbHelper.updateEvent(event, new CacheEventsCallback(null));
    }
  }

  @Override
  public void createEvent(Event event) {
    // TODO Auto-generated method stub
  }

  @Override
  public void deleteEvent(String id) {
    // TODO Auto-generated method stub

  }

  @Override
  public void updateEvent(Event eventToUpdate) {
    // TODO Auto-generated method stub
  }

  /*
   * Streams management
   */

  @Override
  public void getStreams(Filter filter, final StreamsCallback connectionStreamsCallback) {
    // look in cache and send it onPartialResult
    // dbHelper.getStreams();
    logger.log("Cache: retrieved Events from cache: ");
    connectionStreamsCallback.onCacheRetrieveStreamSuccess(new HashMap<String, Stream>());

    onlineStreamsManager.getStreams(filter, new CacheStreamsCallback(connectionStreamsCallback));
  }

  @Override
  public void createStream(Stream newStream, StreamsCallback connectionStreamsCallback) {
    // TODO Auto-generated method stub
  }

  @Override
  public void deleteStream(String id, StreamsCallback connectionStreamsCallback) {
    // TODO Auto-generated method stub

  }

  @Override
  public void updateStream(Stream streamToUpdate, StreamsCallback connectionStreamsCallback) {
    // TODO Auto-generated method stub
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
      // update Cache with receivedEvents
      updateEvents(onlineEvents);
      // SEND UPDATED EVENTS FROM CACHE
      connectionEventsCallback.onOnlineRetrieveEventsSuccess(onlineEvents);
    }

    // unused
    @Override
    public void onCacheRetrieveEventsSuccess(Map<String, Event> newEvents) {
    }

    @Override
    public void onEventsRetrievalError(String message) {
      connectionEventsCallback.onEventsRetrievalError(message);
    }

    // unused
    @Override
    public void onSupervisorRetrieveEventsSuccess(Map<String, Event> supervisorEvents) {
      // TODO Auto-generated method stub

    }

    @Override
    public void onEventsSuccess(String successMessage) {
      // TODO Auto-generated method stub

    }

    @Override
    public void onEventsError(String errorMessage) {
      // TODO Auto-generated method stub

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

      updateCache(onlineStreams);

      // SEND UPDATED STREAMS FROM CACHE
      connectionStreamsCallback.onOnlineRetrieveStreamsSuccess(onlineStreams);

    }

    private void updateCache(Map<String, Stream> streams) {
      // TODO Auto-generated method stub

    }

    @Override
    public void onCacheRetrieveStreamSuccess(Map<String, Stream> newStreams) {
      // TODO Auto-generated method stub

    }

    @Override
    public void onStreamsRetrievalError(String message) {
      connectionStreamsCallback.onStreamsRetrievalError(message);
    }

    @Override
    public void onSupervisorRetrieveStreamsSuccess(Map<String, Stream> supervisorStreams) {
      // TODO Auto-generated method stub

    }

    @Override
    public void onStreamsSuccess(String successMessage) {
      // TODO Auto-generated method stub

    }

    @Override
    public void onStreamError(String errorMessage) {
      // TODO Auto-generated method stub

    }

  }

}
