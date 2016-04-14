/*
package com.pryv.api;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import Event;
import com.pryv.utils.JsonConverter;
import com.pryv.utils.Logger;

*/
/**
 *
 * Contains Events loaded in memory
 *
 * @author ik
 *
 *//*

public class EventsSupervisor {

  */
/**
   * the Events stored in volatile memory
   *//*

  private Map<String, Event> events;

  */
/**
   * Map: key=event.id, value=event.clientId
   *//*

  private Map<String, String> eventIdToClientId;

  private StreamsSupervisor streamsSupervisor;

  private Logger logger = Logger.getInstance();

  */
/**
   * EventsSupervisor constructor. Instantiates data structures to store Events.
   *//*

  public EventsSupervisor(StreamsSupervisor pStreamsSupervisor) {
    streamsSupervisor = pStreamsSupervisor;
    events = new ConcurrentHashMap<String, Event>();
    eventIdToClientId = new ConcurrentHashMap<String, String>();
  }

  */
/*
   * Events Management
   *//*


  */
/**
   * Returns the events matching the provided filter.
   *
   * @param filter
   *          the filter object used to filter the Events.
   * @param connectionCallback
   *//*

  public void get(Filter filter, EventsCallback connectionCallback) {
    logger.log("EventsSupervisor: fetching events");
    Map<String, Event> filteredEvents = new HashMap<String, Event>();

    for (Event event : events.values()) {
      if (filter.match(event)) {
        filteredEvents.put(event.getClientId(), event);
        logger.log("EventsSupervisor: matched: streamId="
          + event.getStreamId()
            + ", cid="
            + event.getClientId());
      }
    }

    logger.log("EventsSupervisor: retrieved " + filteredEvents.size() + " events.");

    // apply limit argument
    if (filter.getLimit() != null) {
      Map<String, Event> limitedEvents = new HashMap<String, Event>();
      int i = 0;
      Iterator<Event> iterator = filteredEvents.values().iterator();
      Event temp;
      while (iterator.hasNext() && i < filter.getLimit()) {
        temp = iterator.next();
        limitedEvents.put(temp.getClientId(), temp);
        i++;
      }
      connectionCallback.onEventsRetrievalSuccess(limitedEvents, null);
    } else {
      connectionCallback.onEventsRetrievalSuccess(filteredEvents, null);
    }
  }

  */
/**
   * Update or create Event in Supervisor whether it already exists or not.
   * generates client Id if necessary
   *
   * @param event
   *          the event to add or update
   * @param connectionCallback
   *          the callback to notify success or failure
   *//*

  public void updateOrCreateEvent(Event event, EventsCallback connectionCallback) {
    logger.log("EventsSupervisor: updateOrCreateEvent with id="
            + event.getId()
            + ", cid="
            + event.getClientId()
            + ", streamId="
            + event.getStreamId());

    Event oldEvent = null;
    String cid = getClientId(event.getId());

    if ((cid == null && event.getClientId() == null)
      || (event.getId() == null && event.getClientId() == null)) {
      // new event from API or new event from user
      event.generateClientId();
    } else if (event.getClientId() == null && cid != null) {
      // existing event loaded from API
      event.setClientId(cid);
    }
    // if event is fresh from DB but not synchronized with API, cid = null, but
    // event.getClientId() != null

    if (cid != null) {
      oldEvent = getEventByClientId(cid);
    }

    if (oldEvent != null) {
      update(oldEvent, event, connectionCallback);
    } else {
      addEvent(event, connectionCallback);
    }
  }

  */
/**
   * Add Event in Supervisor
   *
   * @param newEvent
   *          the event to add
   * @param connectionCallback
   *//*

  private void addEvent(Event newEvent, EventsCallback connectionCallback) {
    events.put(newEvent.getClientId(), newEvent);
    addIdToClientIdEntry(newEvent.getId(), newEvent.getClientId());
    logger.log("EventsSupervisor: added Event (id="
      + newEvent.getId()
        + ", cid="
        + newEvent.getClientId()
        + ", streamId="
        + newEvent.getStreamId()
        + ")");
    if (connectionCallback != null) {
      connectionCallback.onEventsSuccess("EventsSupervisor: Event added", newEvent, null, null);
    }
  }

  */
/**
   * Update event in supervisor with the fields of the event passed in
   * parameters
   *
   * @param oldEvent
   * @param eventToUpdate
   *          the event that may replace the one in place if newer.
   *//*

  private void update(Event oldEvent, Event eventToUpdate, EventsCallback connectionCallback) {
    logger.log("EventsSupervisor: update oldEvent (id="
      + oldEvent.getId()
        + ", cid="
        + oldEvent.getClientId()
        + ", streamId="
        + oldEvent.getStreamId()
        + ") to eventToUpdate (id="
        + eventToUpdate.getId()
        + ", cid="
        + eventToUpdate.getClientId()
        + ", streamId="
        + eventToUpdate.getStreamId()
        + ")");
    System.out.println("Supervisor: merging " + eventToUpdate + " into " + oldEvent);
    oldEvent.merge(eventToUpdate, JsonConverter.getCloner());
    logger.log("EventsSupervisor: updated Event (id="
      + oldEvent.getId()
        + ", clientId="
        + oldEvent.getClientId()
        + ", streamId="
        + oldEvent.getStreamId()
        + ")");
    if (connectionCallback != null) {
      connectionCallback.onEventsSuccess("EventsSupervisor: Event updated", eventToUpdate, null,
              null);
    }
  }

  */
/**
   * Delete Event from Supervisor, if trashed is false, sets it to true, else
   * deletes it.
   *
   * @param eventToDelete
   *          the Event to delete
   * @param connectionCallback
   *          callback used to notify success or failure
   *//*

  public void delete(Event eventToDelete, EventsCallback connectionCallback) {
    logger.log("EventsSupervisor: deleting event with cid="
      + eventToDelete.getClientId()
        + ", id="
        + eventToDelete.getId());
    Event oldEvent = events.get(eventToDelete.getClientId());

    if (events.get(eventToDelete.getClientId()) != null) {
      if (events.get(eventToDelete.getClientId()).isTrashed() == true) {
        // delete really
        events.remove(eventToDelete.getClientId());
        eventIdToClientId.remove(eventToDelete.getId());
        connectionCallback.onEventsSuccess(
          "EventsSupervisor: Event with cid=" + eventToDelete.getClientId() + " deleted.", null,
          null, null);
      } else {
        // update "trashed" field
        eventToDelete.setTrashed(true);
        update(oldEvent, eventToDelete, connectionCallback);
      }
    } else {
      connectionCallback.onEventsError(
              "EventsSupervisor: Event with cid=" + eventToDelete.getClientId() + " not found.", null);
    }
  }

  */
/**
   * Returns the Event with clientId or null if such event does not exist.
   *
   * @param clientId
   *          the clientId of the event to be retrieved
   * @return the Event with the requested id or null
   *//*

  public Event getEventByClientId(String clientId) {
    return events.get(clientId);
  }

  */
/**
   * Returns the clientId of the Event whose id is provided.
   *
   * @param id
   * @return the event's clientId if it exists, else null
   *//*

  public String getClientId(String id) {
    if (id != null) {
      return eventIdToClientId.get(id);
    } else {
      return null;
    }
  }

  */
/**
   * Add a mapping id->clientId
   *
   * @param id
   * @param clientId
   *//*

  private void addIdToClientIdEntry(String id, String clientId) {
    if (id != null && clientId != null) {
      eventIdToClientId.put(id, clientId);
    }
  }

  */
/**
   * Returns the Event with id eventId or null if no such event exists.
   *
   * @param eventId
   * @return
   *//*

  public Event getEventById(String eventId) {
    return events.get(getClientId(eventId));
  }

}
*/
