package com.pryv.api;

import java.util.List;

import com.pryv.api.model.Event;

/**
 *
 * used to fetch objects from local storage
 *
 * @author ik
 *
 */
public class Cache implements EventManager, StreamManager {

  private EventManager online;

  public Cache(EventManager pOnline) {
    online = pOnline;
  }

  public List<Event> getEvents() {
    // look in cache
    online.getEvents(); // fetch online and compare modified fields
    return null;
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

}
