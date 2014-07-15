package com.pryv.api;

import java.util.Map;

import com.pryv.api.model.Event;

/**
 *
 * used to fetch objects from local storage
 *
 * @author ik
 *
 */
public class Cache implements EventManager, StreamManager, EventsCallback {

  private EventManager online;

  public Cache(EventManager pOnline) {
    online = pOnline;
  }

  public void getEvents(EventsCallback eCallback) {
    // look in cache and send it onPartialResult
    eCallback.onPartialResult(null);
    online.getEvents(this); // fetch online and compare modified fields
  }

  public Event createEvent(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  public void deleteEvent(String id) {
    // TODO Auto-generated method stub

  }

  public Event updateEvenet(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  public void onSuccess(Map<String, Event> newEvents) {
    // TODO Auto-generated method stub

  }

  public void onPartialResult(Map<String, Event> newEvents) {
    // TODO Auto-generated method stub

  }

  public void onError(String message) {
    // TODO Auto-generated method stub

  }

}
