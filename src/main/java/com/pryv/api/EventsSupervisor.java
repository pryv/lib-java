package com.pryv.api;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
      connectionCallback.onEventsRetrievalSuccess(limitedEvents, 0);
    }
    connectionCallback.onEventsRetrievalSuccess(filteredEvents, 0);
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

    Event oldEvent = null;
    String cid = getClientId(event.getId());

    // 1st case: event fresh from API
    // 2nd case: event fresh from user
    // 3rd case: existing event loaded from API
    if ((getClientId(event.getId()) == null && event.getClientId() == null)
      || (event.getId() == null && event.getClientId() == null)) {
      event.generateClientId();
    } else if (event.getClientId() == null && getClientId(event.getId()) != null) {
      // set clientId
      oldEvent = getEventByClientId(cid);
      event.setClientId(cid);
    }
    // stream should already be inserted - used only when fresh from API
    String streamCid =
      streamsSupervisor.getStreamsIdToClientIdDictionnary().get(event.getStreamId());
    if (streamCid != null) {
      event.setStreamClientId(streamCid);
    }

    if (oldEvent != null) {
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
    if (connectionCallback != null) {
      connectionCallback.onEventsSuccess("EventsSupervisor: Event added", newEvent, 0);
    }
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
        + ", streamCid="
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
    logger.log("EventsSupervisor: deleting event with cid="
      + eventToDelete.getClientId()
        + ", id="
        + eventToDelete.getId());
    Event tmpEvent = events.get(eventToDelete.getClientId());
    System.out.println("EventsSupervisor: event to delete ref in supervisor: " + tmpEvent);
    System.out.println("ev to Del fields: trashed=" + tmpEvent.getTrashed());
    if (events.get(eventToDelete.getClientId()) != null) {
      if (events.get(eventToDelete.getClientId()).getTrashed() == true) {
        // delete really
        events.remove(eventToDelete.getClientId());
        eventIdToClientId.remove(eventToDelete.getId());
        connectionCallback.onEventsSuccess(
          "EventsSupervisor: Event with cid=" + eventToDelete.getClientId() + " deleted.", null, 0);
      } else {
        // update "trashed" field
        eventToDelete.setTrashed(true);
        updateEvent(null, eventToDelete);
        connectionCallback.onEventsSuccess(
          "EventsSupervisor: Event with cid=" + eventToDelete.getClientId() + " trashed.", null, 0);
      }
    } else {
      connectionCallback.onEventsError("EventsSupervisor: Event with cid="
        + eventToDelete.getClientId()
          + " not found.");
    }

  }

  /**
   * Returns the Event with clientId or null if such event does not exist.
   *
   * @param clientId
   *          the clientId of the event to be retrieved
   * @return the Event with the requested id or null
   */
  public Event getEventByClientId(String clientId) {
    return events.get(clientId);
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
