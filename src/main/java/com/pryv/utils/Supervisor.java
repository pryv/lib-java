package com.pryv.utils;

import java.util.HashMap;
import java.util.Map;

import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;

/**
 *
 * contains Pryv objects loaded in memory
 *
 * @author ik
 *
 */
public class Supervisor {

  private Map<String, Event> events;
  private Map<String, Stream> streams;

  private Logger logger = Logger.getInstance();

  public Supervisor() {
    events = new HashMap<String, Event>();
    streams = new HashMap<String, Stream>();
  }

  public Map<String, Event> getEvents(Map<String, String> params) {
    // use params

    for (String eventID : events.keySet()) {
      logger.log("Supervisor: returning event from main memory: " + eventID);
    }
    return events;
  }

  public Event getEventById(String id) {
    return events.get(id);
  }

  public void addEvent(Event newEvent) {
    events.put(newEvent.getId(), newEvent);
  }

  public Map<String, Stream> getStreams() {
    return streams;
  }

  public void setEvents(Map<String, Event> pEvents) {
    this.events = pEvents;
  }

  public void setStreams(Map<String, Stream> pStreams) {
    this.streams = pStreams;
  }

  public void addEvents(Map<String, Event> newEvents) {
    events.putAll(newEvents);
  }

}
