package com.pryv.api;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.pryv.api.model.Event;
import com.pryv.utils.JsonConverter;
import com.pryv.utils.Logger;

/**
 *
 * Contains Events loaded in memory
 *
 * @author ik
 *
 */
public class EventsSupervisor {

  private Map<String, Event> events;
  /**
   * Map: key=event.id, value=event.clientId
   */
  private Map<String, String> eventIdToClientId;

  private Logger logger = Logger.getInstance();

  /**
   * EventsSupervisor constructor. Instantiates data structures to store Events.
   *
   */
  public EventsSupervisor() {
    events = new HashMap<String, Event>();
    eventIdToClientId = new HashMap<String, String>();
  }

  /*
   * Events Management
   */

  /**
   * Returns the events matching the provided filter.
   *
   * @param filter
   *          the filter object used to filter the Events.
   * @param connectionCallback
   */
  public void getEvents(Filter filter, EventsCallback connectionCallback) {
    Map<String, Event> returnEvents = new HashMap<String, Event>();

    for (Event event : events.values()) {
      if (filter.match(event)) {
        returnEvents.put(event.getId(), event);
        logger.log("Supervisor: matched: streamName="
          + ", streamId="
            + event.getStreamId()
            + ", id="
            + event.getId());
      }
    }

    // apply limit argument
    if (filter.getLimit() != null) {
      returnEvents.keySet().retainAll(
        ImmutableSet.copyOf(Iterables.limit(returnEvents.keySet(), filter.getLimit())));
    }
    logger.log("EventsSupervisor: getEvents returning " + returnEvents.size() + " events.");
    connectionCallback.onSupervisorRetrieveEventsSuccess(returnEvents);
  }

  /**
   * Update or create events in Supervisor whether they already exist or not.
   *
   * @param newEvent
   *          the new Event
   * @param connectionCallback
   *          the callback to notify success or failure
   */
  public void updateOrCreateEvent(Event newEvent, EventsCallback connectionCallback) {
    logger.log("EventsSupervisor: updateOrCreateEvent with id="
      + newEvent.getId()
        + ", cid="
        + newEvent.getClientId());
    if (getClientId(newEvent.getId()) == null && newEvent.getClientId() == null) {
      // new event
      newEvent.generateClientId();
      newEvent.updateStreamClientId(eventIdToClientId);
    }
    // case exists: compare modified field
    if (eventIdToClientId.containsKey(newEvent.getId())) {
      updateEvent(newEvent);
      connectionCallback.onEventsSuccess("Event with id="
        + newEvent.getId()
          + " updated successfully.");
    } else {
      addEvent(newEvent);
      connectionCallback.onEventsSuccess("Event with id="
        + newEvent.getId()
          + " added successfully.");
    }
  }

  /**
   * Add Event in Supervisor
   *
   * @param newEvent
   */
  private void addEvent(Event newEvent) {
    events.put(newEvent.getId(), newEvent);
    eventIdToClientId.put(newEvent.getId(), newEvent.getClientId());
  }

  /**
   * Update event in supervisor with the fields of the event passed in
   * parameters
   *
   * @param event
   *          the event that may replace the one in place if newer.
   */
  private void updateEvent(Event event) {
    Event memEvent = events.get(event.getId());
    memEvent.merge(event, JsonConverter.getCloner());
  }

  /**
   * Delete Event from Supervisor, if trashed is false, sets it to true, else
   * deletes it.
   *
   * @param eventToDelete
   *          the Event to delete
   * @param connectionCallback
   *          callback used to notify success or failure
   */
  public void deleteEvent(Event eventToDelete, EventsCallback connectionCallback) {
    if (events.get(eventToDelete.getId()) != null) {
      if (events.get(eventToDelete.getId()).getTrashed() == true) {
        // delete really
        events.remove(eventToDelete.getId());
        eventIdToClientId.remove(eventToDelete.getId());
      } else {
        // update "trashed" field
        eventToDelete.setTrashed(true);
        updateEvent(eventToDelete);
      }
      connectionCallback.onEventsSuccess("Event with id=" + eventToDelete.getId() + " deleted.");
    } else {
      connectionCallback.onEventsError("Event with id=" + eventToDelete.getId() + " not found.");
    }

  }

  /**
   * Returns the Event with eventId id or null if such event does not exist.
   *
   * @param id
   *          the id of the event to be retrieved
   * @return the Event with the requested id or null
   */
  public Event getEventById(String id) {
    return events.get(id);
  }

  /**
   * Returns the clientId of the Event whose id is provided.
   *
   * @param id
   * @return the event's clientId if it exists, else null
   */
  public String getClientId(String id) {
    return eventIdToClientId.get(id);
  }

}
