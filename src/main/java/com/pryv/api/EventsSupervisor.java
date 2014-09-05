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

  /**
   *
   */
  private Map<String, Event> events;

  /**
   * Map: key=event.id, value=event.clientId
   */
  private Map<String, String> eventIdToClientId;

  private StreamsSupervisor streamsSupervisor;

  private Logger logger = Logger.getInstance();

  /**
   * EventsSupervisor constructor. Instantiates data structures to store Events.
   *
   */
  public EventsSupervisor(StreamsSupervisor pStreamsSupervisor) {
    streamsSupervisor = pStreamsSupervisor;
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
        returnEvents.put(event.getClientId(), event);
        logger.log("EventsSupervisor: matched: streamId="
          + event.getStreamId()
            + ", cid="
            + event.getClientId());
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
   * Update or create Event in Supervisor whether it already exists or not.
   * generates client Id if necessary
   *
   * @param event
   *          the event to add or update
   * @param connectionCallback
   *          the callback to notify success or failure
   */
  public void updateOrCreateEvent(Event event, EventsCallback connectionCallback) {
    System.out.println("EventsSupervisor: updateOrCreateEvent with id="
      + event.getId()
        + ", cid="
        + event.getClientId()
        + ", streamCid="
        + event.getStreamClientId());
    if ((getClientId(event.getId()) == null && event.getClientId() == null)
      || (event.getId() == null && event.getClientId() == null)) {
      event.generateClientId();
    }
    // holding stream should already be inserted - used only when fresh from API
    event.updateStreamClientId(streamsSupervisor.getStreamsIdToClientIdDictionnary());

    // case exists: compare modified field?
    Event oldEvent = getEventByClientId(event.getClientId());
    if (oldEvent != null) {
      // update
      updateEvent(oldEvent, event);
    } else {
      addEvent(event, connectionCallback);
    }
  }

  /**
   * Add Event in Supervisor
   *
   * @param newEvent
   *          the event to add
   * @param connectionCallback
   */
  private void addEvent(Event newEvent, EventsCallback connectionCallback) {
    events.put(newEvent.getClientId(), newEvent);
    eventIdToClientId.put(newEvent.getId(), newEvent.getClientId());
    logger.log("EventsSupervisor: added Event (id="
      + newEvent.getId()
        + ", cid="
        + newEvent.getClientId()
        + ", streamCid="
        + newEvent.getStreamClientId()
        + ")");
    connectionCallback.onEventsSuccess("");
  }

  /**
   * Update event in supervisor with the fields of the event passed in
   * parameters
   *
   * @param oldEvent
   * @param eventToUpdate
   *          the event that may replace the one in place if newer.
   */
  private void updateEvent(Event oldEvent, Event eventToUpdate) {
    logger.log("EventsSupervisor: update oldEvent (id="
      + oldEvent.getId()
        + ", cid="
        + oldEvent.getClientId()
        + ", parentCid="
        + oldEvent.getStreamClientId()
        + ") to eventToUpdate (id="
        + eventToUpdate.getId()
        + ", cid="
        + eventToUpdate.getClientId()
        + ", streamCid="
        + eventToUpdate.getStreamClientId()
        + ")");
    oldEvent.merge(eventToUpdate, JsonConverter.getCloner());
    logger.log("EventsSupervisor: updated Event (id="
      + oldEvent.getId()
        + ", clientId="
        + oldEvent.getClientId()
        + ", streamCid="
        + oldEvent.getStreamClientId()
        + ")");
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
    if (events.get(eventToDelete.getClientId()) != null) {
      if (events.get(eventToDelete.getClientId()).getTrashed() == true) {
        // delete really
        events.remove(eventToDelete.getClientId());
        eventIdToClientId.remove(eventToDelete.getId());
      } else {
        // update "trashed" field
        eventToDelete.setTrashed(true);
        updateEvent(null, eventToDelete);
      }
      connectionCallback.onEventsSuccess("Event with cid="
        + eventToDelete.getClientId()
          + " deleted.");
    } else {
      connectionCallback.onEventsError("Event with cid="
        + eventToDelete.getClientId()
          + " not found.");
    }

  }

  /**
   * Returns the Event with eventId id or null if such event does not exist.
   *
   * @param id
   *          the id of the event to be retrieved
   * @return the Event with the requested id or null
   */
  public Event getEventByClientId(String id) {
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
