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
   * @param eventsCallback
   *          the callback for events creation, notifies success or failure
   *
   * @return
   */
  void createEvent(Event newEvent, EventsCallback eventsCallback);

  /**
   * Delete Event with event ID id
   *
   * @param id
   *          the ID of the Event to delete.
   * @param eventsCallback
   *          the callback for events deletion, notifies success or failure
   *
   */
  void deleteEvent(String id, EventsCallback eventsCallback);

  /**
   * Update Event.
   *
   * @param eventToUpdate
   *          Event object containing the new fields.
   *
   * @param eventsCallback
   *          the callback for events update, notifies success or failure
   *
   */
  void updateEvent(Event eventToUpdate, EventsCallback eventsCallback);

}
