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
          if (event.getTrashed() == false) {
            returnEvents.put(event.getId(), event);
            logger.log("Supervisor: returning event from main memory: " + event.getId());
          }
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

  public void updateEvents(Map<String, Event> newEvents) {
    for (Event event : newEvents.values()) {
      // case exists: compare modified field
      if (events.containsKey(event.getId())) {
        updateEvent(event);
        // case new Event: simply add
      } else {
        addEvent(event);
      }
    }
  }

  private void updateEvent(Event event) {
    Event memEvent = events.get(event.getId());
    if (memEvent.getModified() > event.getModified()) {
      // do nothing
    } else {
      memEvent.merge(event, JsonConverter.getCloner());
    }
  }
}
