package com.pryv.api;

import com.pryv.api.model.Event;

/**
 *
 * interface used by Connection, OnlineEventsAndStreamsManager and
 * CacheEventsAndStreamsManager to manipulate Events
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
   */
  void createEvent(Event newEvent, EventsCallback eventsCallback);

  /**
   * Delete Event
   *
   * @param eventToDelete
   *          the Event to delete.
   * @param eventsCallback
   *          the callback for events deletion, notifies success or failure
   *
   */
  void deleteEvent(Event eventToDelete, EventsCallback eventsCallback);

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
  void updateEvent(Event eventToUpdate, EventsCallback eventsCallback);

}
