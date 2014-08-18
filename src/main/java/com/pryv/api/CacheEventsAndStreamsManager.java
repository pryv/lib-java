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
  public CacheEventsAndStreamsManager(String url, String token, DBinitCallback initCallback)
    throws ClassNotFoundException, SQLException {
    onlineEventsManager = new OnlineEventsAndStreamsManager(url, token);
    onlineStreamsManager = (StreamsManager) onlineEventsManager;
    dbHelper = new SQLiteDBHelper(Pryv.DATABASE_NAME, initCallback);
  }

  /**
   * Events management
   */

  @Override
  public void getEvents(Filter filter, final EventsCallback connectionEventsCallback) {
    // look in cache and send it onPartialResult
    try {
      connectionEventsCallback.onEventsPartialResult(dbHelper.getEvents(filter));
      logger.log("Cache: retrieved Events from cache: ");
    } catch (SQLException e) {
      connectionEventsCallback.onEventsError("Cache: getEvents error: " + e.getMessage());
      e.printStackTrace();
    }

    // forward call to online module
    onlineEventsManager.getEvents(filter, new EventsCallback() {

      @Override
      public void onEventsSuccess(Map<String, Event> onlineEvents) {
        // update Cache with receivedEvents
        updateEvents(onlineEvents);

        // SEND UPDATED EVENTS FROM CACHE
        connectionEventsCallback.onEventsSuccess(onlineEvents);
      }

      // unused
      @Override
      public void onEventsPartialResult(Map<String, Event> newEvents) {
      }

      @Override
      public void onEventsError(String message) {
        connectionEventsCallback.onEventsError(message);
      }
    });
  }

  /**
   * Store most recently modified Event in Cache
   *
   * @param newEvents
   *          Events received from online module
   */
  private void updateEvents(Map<String, Event> newEvents) {
    for (Event event : newEvents.values()) {
      try {
        dbHelper.updateEvent(event);
      } catch (SQLException e) {

        e.printStackTrace();
      }
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

  /**
   * Streams management
   */

  @Override
  public void getStreams(final StreamsCallback streamsCallback) {
    // look in cache and send it onPartialResult
    // dbHelper.getStreams();
    logger.log("Cache: retrieved Events from cache: ");
    streamsCallback.onStreamsPartialResult(new HashMap<String, Stream>());

    onlineStreamsManager.getStreams(new StreamsCallback() {

      @Override
      public void onStreamsSuccess(Map<String, Stream> onlineStreams) {
        logger.log("Cache: Streams retrieval success");

        updateCache(onlineStreams);

        // SEND UPDATED STREAMS FROM CACHE
        streamsCallback.onStreamsSuccess(onlineStreams);

      }

      private void updateCache(Map<String, Stream> streams) {
        // TODO Auto-generated method stub

      }

      @Override
      public void onStreamsPartialResult(Map<String, Stream> newStreams) {
        // TODO Auto-generated method stub

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
