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

  public Supervisor() {
    events = new HashMap<String, Event>();
    streams = new HashMap<String, Stream>();
  }

  public Map<String, Event> getEvents(Map<String, String> params) {
    // use params
    return events;
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
