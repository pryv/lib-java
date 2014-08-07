package com.pryv.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pryv.api.database.SQLiteDBHelper;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;
import com.pryv.utils.JsonConverter;
import com.pryv.utils.Logger;

/**
 *
 * Cache that fetches objects from local database and forwards requests to
 * OnlineEventsAndStreamsManager when necessary.
 *
 * @author ik
 *
 */
public class CacheEventsAndStreamsManager implements EventsManager<Map<String, Event>>,
  StreamsManager<Map<String, Stream>> {

  private EventsManager<String> onlineEventsManager;
  private StreamsManager<String> onlineStreamsManager;

  private SQLiteDBHelper dbHelper;

  private Logger logger = Logger.getInstance();

  public CacheEventsAndStreamsManager(String url, String token) {
    onlineEventsManager = new OnlineEventsAndStreamsManager(url, token);
    onlineStreamsManager = (StreamsManager<String>) onlineEventsManager;
    dbHelper = new SQLiteDBHelper();
  }

  /**
   * Events management
   */

  @Override
  public void getEvents(Map<String, String> params,
    final EventsCallback<Map<String, Event>> eventsCallback) {
    // look in cache and send it onPartialResult
    dbHelper.getEvents();
    logger.log("Cache: retrieved Events from cache: ");
    eventsCallback.onEventsPartialResult(new HashMap<String, Event>());

    // forward call to online module
    onlineEventsManager.getEvents(params, new EventsCallback<String>() {

      @Override
      public void onEventsSuccess(String jsonEvents) {
        logger.log("Cache: Events retrieval success");
        try {
          Map<String, Event> receivedEvents = JsonConverter.createEventsFromJson(jsonEvents);
          eventsCallback.onEventsSuccess(receivedEvents);

          // update Cache with receivedEvents
          updateCache(receivedEvents);

        } catch (JsonProcessingException e) {
          eventsCallback.onEventsError(e.getMessage());
          e.printStackTrace();
        } catch (IOException e) {
          eventsCallback.onEventsError(e.getMessage());
          e.printStackTrace();
        }

      }

      // unused
      @Override
      public void onEventsPartialResult(Map<String, Event> newEvents) {
      }

      @Override
      public void onEventsError(String message) {
        eventsCallback.onEventsError(message);
      }
    });
  }

  private void updateCache(Map<String, Event> newEvents) {

  }

  @Override
  public Event createEvent(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void deleteEvent(String id) {
    // TODO Auto-generated method stub

  }

  @Override
  public Event updateEvent(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Streams management
   */

  @Override
  public void getStreams(final StreamsCallback<Map<String, Stream>> streamsCallback) {
    // look in cache?

    onlineStreamsManager.getStreams(new StreamsCallback<String>() {

      @Override
      public void onStreamsSuccess(String streams) {
        logger.log("Cache: Streams retrieval success");
        try {
          streamsCallback.onStreamsSuccess(JsonConverter.createStreamsFromJson(streams));
        } catch (JsonProcessingException e) {
          e.printStackTrace();
          streamsCallback.onStreamsError(e.getMessage());
        } catch (IOException e) {
          e.printStackTrace();
          streamsCallback.onStreamsError(e.getMessage());
          e.printStackTrace();
        }
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
