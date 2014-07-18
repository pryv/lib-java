package com.pryv.api;

import java.util.HashMap;
import java.util.Map;

import com.pryv.api.model.Event;

/**
 *
 * used to fetch objects from local storage
 *
 * @author ik
 *
 */
public class CacheEventsManager implements EventsManager, StreamsManager, EventsCallback {

  private EventsManager online;
  private EventsCallback eCallback;

  public CacheEventsManager(EventsManager pOnline) {
    online = pOnline;
  }

  public void get(EventsCallback pECallback) {
    // look in cache and send it onPartialResult
    eCallback = pECallback;
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
    // update cache with received events
    eCallback.onSuccess(jsonEvents);
  }

  public void onPartialResult(Map<String, Event> newEvents) {
    // TODO Auto-generated method stub

  }

  public void onError(String message) {
    // TODO Auto-generated method stub

  }

}
