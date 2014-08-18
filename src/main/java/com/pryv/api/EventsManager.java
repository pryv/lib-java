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
   * @param eventsCallback
   *          the callback for events retrieval
   */
  void getEvents(Filter filter, EventsCallback<T> eventsCallback);

  /**
   * Create event
   *
   * @param newEvent
   *          the event to create
   * @return
   */
  void createEvent(Event newEvent);

  void deleteEvent(String id);

  Event updateEvent(String id);

}
