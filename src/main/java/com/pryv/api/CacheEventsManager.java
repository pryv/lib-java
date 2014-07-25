package com.pryv.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pryv.api.database.SQLiteDBHelper;
import com.pryv.api.model.Event;
import com.pryv.utils.JsonConverter;

/**
 *
 * used to fetch objects from local storage
 *
 * @author ik
 *
 */
public class CacheEventsManager implements EventsManager<Map<String, Event>>, StreamsManager,
  EventsCallback<String> {

  private EventsManager<String> online;
  private EventsCallback<Map<String, Event>> eventsCallback;

  private SQLiteDBHelper dbHelper;

  public CacheEventsManager(String url, EventsCallback<Map<String, Event>> eCallback) {
    online = new OnlineEventsManager(url, this);
    eventsCallback = eCallback;
    dbHelper = new SQLiteDBHelper();
  }

  public void get() {
    // look in cache and send it onPartialResult
    dbHelper.getEvents();
    eventsCallback.onPartialResult(new HashMap<String, Event>());
    online.get(); // fetch online and compare modified fields
  }

  public Event create(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  public void delete(String id) {
    // TODO Auto-generated method stub

  }

  public Event update(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  public void onSuccess(String jsonEvents) {
    System.out.println("cache: onSuccess");
    try {
      eventsCallback.onSuccess(JsonConverter.createEventsFromJson(jsonEvents));
    } catch (JsonProcessingException e) {
      this.onError(e.getMessage());
      e.printStackTrace();
    } catch (IOException e) {
      this.onError(e.getMessage());
      e.printStackTrace();
    }

  }

  public void onPartialResult(Map<String, Event> newEvents) {
    // TODO Auto-generated method stub

  }

  public void onError(String message) {
    // TODO Auto-generated method stub

  }

  public void addEventsCallback(EventsCallback<Map<String, Event>> eCallback) {
    // TODO Auto-generated method stub

  }

}
