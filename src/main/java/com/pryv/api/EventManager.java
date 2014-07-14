package com.pryv.api;

import java.util.List;

import com.pryv.api.model.Event;

/**
 *
 * interface used by Connection, Online and Cache to fetch Events
 *
 * @author ik
 *
 */
public interface EventManager {

  /**
   * retrieve events
   *
   * @param id
   * @return
   */
  List<Event> getEvents();

  Event createEvent(String id);

  void deleteEvent(String id);

  Event updateEvenet(String id);

}
