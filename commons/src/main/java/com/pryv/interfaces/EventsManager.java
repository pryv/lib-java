package com.pryv.interfaces;

import com.pryv.Filter;
import com.pryv.model.Event;


public interface EventsManager {

  /**
   * retrieve events
   *
   * @param filter
   *          optional filter parameters
   * @param eventsCallback
   *          the callback for events retrieval
   */
  void get(Filter filter, GetEventsCallback eventsCallback);

  /**
   * Create event, generates a id if needed
   *
   * @param newEvent
   *          the event to create
   * @param eventsCallback
   *          the callback for events creation, notifies success or failure
   *
   */
  void create(Event newEvent, EventsCallback eventsCallback);

  /**
   * Delete Event
   *
   * @param eventToDelete
   *          the Event to delete.
   * @param eventsCallback
   *          the callback for events deletion, notifies success or failure
   *
   */
  void delete(Event eventToDelete, EventsCallback eventsCallback);

  /**
   * Update Event.
   *
   * @param eventToUpdate
   *          Event object containing the updated fields.
   *
   * @param eventsCallback
   *          the callback for events update, notifies success or failure
   *
   */
  void update(Event eventToUpdate, EventsCallback eventsCallback);

}
