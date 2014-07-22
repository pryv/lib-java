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
  private EventsCallback<Map<String, Event>> eCallback;

  private SQLiteDBHelper dbHelper;

  public CacheEventsManager(EventsManager<String> pOnline) {
    online = pOnline;
    dbHelper = new SQLiteDBHelper();
  }

  public void get(EventsCallback<Map<String, Event>> pECallback) {
    // look in cache and send it onPartialResult
    eCallback = pECallback;
    dbHelper.getEvents();
    eCallback.onPartialResult(new HashMap<String, Event>());
    online.get(this); // fetch online and compare modified fields
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
      eCallback.onSuccess(JsonConverter.createEventsFromJson(jsonEvents));
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

}
