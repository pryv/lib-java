package com.pryv.utils;

import java.util.ArrayList;
import java.util.List;

import com.pryv.api.model.Event;

/**
 *
 * contains Pryv objects loaded in memory
 *
 * @author ik
 *
 */
public class Supervisor {

  private List<Event> events;

  public Supervisor() {
    events = new ArrayList<Event>();
  }

  public List<Event> getEvents() {
    return events;
  }

}
