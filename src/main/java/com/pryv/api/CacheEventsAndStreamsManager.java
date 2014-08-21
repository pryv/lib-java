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
  public void getEvents(Filter filter, EventsCallback connectionEventsCallback) {
    // look in cache and send it onPartialResult
    // connectionEventsCallback.onCacheRetrieveEventsSuccess(dbHelper.getEvents(filter));
    dbHelper.getEvents(filter, new CacheEventsCallback(connectionEventsCallback));
    logger.log("Cache: retrieved Events from cache: ");

    // forward call to online module
    onlineEventsManager.getEvents(filter, new CacheEventsCallback(connectionEventsCallback));
  }

  @Override
  public void createEvent(Event event, EventsCallback userEventsCallback) {
    // TODO Auto-generated method stub
  }

  @Override
  public void deleteEvent(Event eventToDelete, EventsCallback userEventsCallback) {
    // TODO Auto-generated method stub

  }

  @Override
  public void updateEvent(Event eventToUpdate, EventsCallback userEventsCallback) {
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
  public void deleteStream(Stream streamToDelete, StreamsCallback connectionStreamsCallback) {
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
