package com.pryv.api;

import com.pryv.api.model.Event;

/**
 *
 * interface used by Connection, Online and Cache to fetch Events
 *
 * @author ik
 *
 */
public interface EventsManager {

  /**
   * retrieve events
   *
   * @param id
   * @return
   */
  void getEvents(EventsCallback eventsCallback);

  Event createEvent(String id);

  void deleteEvent(String id);

  Event updateEvenet(String id);

}
