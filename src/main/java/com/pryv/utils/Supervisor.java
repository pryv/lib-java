package com.pryv.utils;

import java.util.HashMap;
import java.util.Map;

import com.pryv.api.Filter;
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
    Map<String, Event> returnEvents = new HashMap<String, Event>();
    if (params.get(Filter.STREAMS_KEY) != null) {
      for (Event event : events.values()) {
        if (event.getStreamId().equals(params.get(Filter.STREAMS_KEY))) {
          returnEvents.put(event.getId(), event);
          logger.log("Supervisor: returning event from main memory: " + event.getId());
        }
      }
    }
    return returnEvents;
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

  public void updateStreams(Map<String, Stream> pStreams) {
    this.streams = pStreams;
  }

  public void addEvents(Map<String, Event> newEvents) {
    events.putAll(newEvents);
  }

}
