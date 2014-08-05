package com.pryv.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pryv.api.database.SQLiteDBHelper;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;
import com.pryv.utils.JsonConverter;

/**
 *
 * Cache that fetches objects from local database and forwards requests to
 * OnlineEventsAndStreamsManager when necessary.
 *
 * @author ik
 *
 */
public class CacheEventsAndStreamsManager implements EventsManager<Map<String, Event>>,
  EventsCallback<String>, StreamsManager, StreamsCallback<String> {

  private EventsManager<String> onlineEventsManager;
  private EventsCallback<Map<String, Event>> eventsCallback;

  private StreamsManager onlineStreamsManager;
  private StreamsCallback<Map<String, Stream>> streamsCallback;

  private SQLiteDBHelper dbHelper;

  public CacheEventsAndStreamsManager(String url, String token,
    EventsCallback<Map<String, Event>> pEventsCallback,
    StreamsCallback<Map<String, Stream>> pStreamsCallback) {
    onlineEventsManager = new OnlineEventsAndStreamsManager(url, token, this, this);
    onlineStreamsManager = (StreamsManager) onlineEventsManager;
    eventsCallback = pEventsCallback;
    streamsCallback = pStreamsCallback;
    dbHelper = new SQLiteDBHelper();
  }

  /**
   * Events management
   */

  @Override
  public void getEvents(Map<String, String> params) {
    // look in cache and send it onPartialResult
    dbHelper.getEvents();
    eventsCallback.onEventsPartialResult(new HashMap<String, Event>());
    onlineEventsManager.getEvents(params); // fetch online and compare modified
                                           // fields
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
   * Events callback
   */

  @Override
  public void onEventsSuccess(String jsonEvents) {
    System.out.println("cache: onSuccess");
    try {
      eventsCallback.onEventsSuccess(JsonConverter.createEventsFromJson(jsonEvents));
    } catch (JsonProcessingException e) {
      this.onEventsError(e.getMessage());
      e.printStackTrace();
    } catch (IOException e) {
      this.onEventsError(e.getMessage());
      e.printStackTrace();
    }

  }

  @Override
  public void onEventsPartialResult(Map<String, Event> newEvents) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onEventsError(String message) {

  }

  /**
   * Streams management
   */

  @Override
  public List<Stream> getStreams() {
    // look in cache?

    return onlineStreamsManager.getStreams();
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

  /**
   * Streams callback
   */

  @Override
  public void onStreamsSuccess(String streams) {
    System.out.println("cache stream success: " + streams);
    try {
      streamsCallback.onStreamsSuccess(JsonConverter.createStreamsFromJson(streams));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      this.onStreamsError(e.getMessage());
    } catch (IOException e) {
      e.printStackTrace();
      this.onStreamsError(e.getMessage());
      e.printStackTrace();
    }
  }

  @Override
  public void onStreamsPartialResult(Map<String, Stream> newStreams) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onStreamsError(String message) {
    // TODO Auto-generated method stub
    System.out.println("Cache - onStreamError: " + message);
  }

  /**
   * other
   */

  @Override
  public void addEventsCallback(EventsCallback<Map<String, Event>> eCallback) {
    // TODO Auto-generated method stub

  }

}
