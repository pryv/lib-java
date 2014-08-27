package com.pryv.api;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;
import com.pryv.utils.JsonConverter;
import com.pryv.utils.Logger;

/**
 *
 * Contains Pryv objects loaded in memory
 *
 * @author ik
 *
 */
public class EventsSupervisor {

  private StreamsSupervisor streamsSupervisor;

  private Map<String, Event> events;

  private Logger logger = Logger.getInstance();

  /**
   * Supervisor constructor. Instantiates data structures to store Streams and
   * Events.
   *
   */
  public EventsSupervisor(StreamsSupervisor pStreamsSupervisor) {
    streamsSupervisor = pStreamsSupervisor;
    events = new HashMap<String, Event>();
  }

  /*
   * Events Management
   */

  /**
   * Returns the events matching the provided filter.
   *
   * @param filter
   *          the filter object used to filter the Events.
   * @return returns the events matching the filter or an empty Map<String,
   *         Event>.
   */
  public void getEvents(Filter filter, EventsCallback connectionCallback) {
    Map<String, Event> returnEvents = new HashMap<String, Event>();

    for (Event event : events.values()) {
      if (filter.match(event, streamsSupervisor.getRootStreams())) {
        returnEvents.put(event.getId(), event);
        logger.log("Supervisor: matched: streamName="
        // + streams.get(event.getStreamId()).getName()
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
    connectionCallback.onSupervisorRetrieveEventsSuccess(returnEvents);
  }

  /**
   * Update or create events in Supervisor whether they already exist or not.
   *
   * @param newEvent
   *          the new Event
   * @param connectionCallback
   *          the callback to notify success or failutre
   * @throws IncompleteFieldsException
   *           thrown when some mandatory fields of the Event are null
   */
  public void updateOrCreateEvent(Event newEvent, EventsCallback connectionCallback) {
    if (areFieldsValid(newEvent)) {
      // case exists: compare modified field
      if (events.containsKey(newEvent.getId())) {
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
    } else {
      connectionCallback
        .onEventsError("Supervisor: attempt to Create an Event with incomplete fields: id="
          + newEvent.getId());
    }
  }

  /**
   * Add Event in Supervisor
   *
   * @param newEvent
   */
  private void addEvent(Event newEvent) {
    logger.log("Supervisor: adding new event: id="
      + newEvent.getId()
        + ", streamId="
        + newEvent.getStreamId());
    events.put(newEvent.getId(), newEvent);
  }

  /**
   * Compare modified field of event with the one stored in the Supervisor to
   * decided wether to replace it or not.
   *
   * @param event
   *          the event that may replace the one in place if newer.
   */
  private void updateEvent(Event event) {
    Event memEvent = events.get(event.getId());
    if (memEvent.getModified() > event.getModified()) {
      // do nothing
    } else {
      logger.log("Supervisor: updating event: id="
        + event.getId()
          + ", streamId="
          + event.getStreamId()
          + ". Old time="
          + memEvent.getTime()
          + ", new Time="
          + event.getTime());
      memEvent.merge(event, JsonConverter.getCloner());
    }
  }

  /**
   * Delete Event from Supervisor, if trashed is false, sets it to true, else
   * deletes it.
   *
   * @param eventToDelete
   *          the Event to delete
   */
  public void deleteEvent(Event eventToDelete, EventsCallback connectionCallback) {
    if (events.get(eventToDelete.getId()) != null) {
      if (events.get(eventToDelete.getId()).getTrashed() == true) {
        // delete really
        events.remove(eventToDelete.getId());
      } else {
        // update trashed field
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
   * used to check if input Event have all the required fields not null.
   *
   * @param eventToCheck
   * @return true if all fields are valid, false if any of the mandatory fields
   *         is null or the parameter is null
   */
  private boolean areFieldsValid(Event eventToCheck) {
    if (eventToCheck == null) {
      return false;
    } else {
      return eventToCheck.getId() != null
        && eventToCheck.getStreamId() != null
          && eventToCheck.getCreated() != null
          && eventToCheck.getCreatedBy() != null
          && eventToCheck.getModified() != null
          && eventToCheck.getModifiedBy() != null;
    }
  }

  /**
   * used to check if input Stream has all the required fields as not null.
   *
   * @param streamToCheck
   * @return true if all fields are not null, false if any mandatory field is
   *         null or the stream is null.
   */
  private boolean areFieldsValid(Stream streamToCheck) {
    if (streamToCheck == null) {
      return false;
    } else {
      return streamToCheck.getId() != null
        && streamToCheck.getName() != null
          && streamToCheck.getCreated() != null
          && streamToCheck.getCreatedBy() != null
          && streamToCheck.getModified() != null
          && streamToCheck.getModifiedBy() != null;
    }
  }

}
