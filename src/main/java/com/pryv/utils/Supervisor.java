package com.pryv.utils;

import java.util.HashMap;
import java.util.Map;

import com.pryv.api.model.Event;

/**
 *
 * contains Pryv objects loaded in memory
 *
 * @author ik
 *
 */
public class Supervisor {

  private Map<String, Event> events;

  public Supervisor() {
    events = new HashMap<String, Event>();
  }

  public Map<String, Event> getEvents() {
    return events;
  }

  public void addEvents(Map<String, Event> newEvents) {
    events.putAll(newEvents);
  }

}
