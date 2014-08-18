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
   * @param params
   *          optional filter parameters
   * @param eventsCallback
   *          the callback for events retrieval
   */
  void getEvents(Filter filter, EventsCallback eventsCallback);

  /**
   * Create event
   *
   * @param newEvent
   *          the event to create
   * @return
   */
  void createEvent(Event newEvent);

  /**
   * Delete Event with event ID id
   *
   * @param id
   *          the ID of the Event to delete.
   */
  void deleteEvent(String id);

  /**
   * Update Event.
   *
   * @param eventToUpdate
   *          Event object containing the new fields.
   * @return
   */
  void updateEvent(Event eventToUpdate);

}
