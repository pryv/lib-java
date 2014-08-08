package com.pryv.api;

import com.pryv.api.model.Event;

/**
 *
 * interface used by Connection, Online and Cache to fetch Events
 *
 * @author ik
 *
 * @param <T>
 *          the format in which the events are returned
 *
 */
public interface EventsManager<T> {

  /**
   * retrieve events
   *
   * @param params
   *          optional filter parameters
   */
  void getEvents(Filter filter, EventsCallback<T> eventsCallback);

  Event createEvent(String id);

  void deleteEvent(String id);

  Event updateEvent(String id);

}
