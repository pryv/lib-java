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
public class Supervisor {

  private Map<String, Event> events;
  private Map<String, Stream> streams;

  private Logger logger = Logger.getInstance();

  /**
   * Supervisor constructor. Instanciates data structures to store Streams and
   * Events.
   *
   */
  public Supervisor() {
    events = new HashMap<String, Event>();
    streams = new HashMap<String, Stream>();
  }

  /*
   * Streams Management
   */

  /**
   * Returns the local memory streams
   *
   * @return
   */
  public Map<String, Stream> getStreams() {
    return streams;
  }

  /**
   * Update or create Streams in Supervisor whether they already exist or not.
   *
   * @param pStreams
   */
  public void updateOrCreateStream(Stream stream) {
    // case exists: compare modified field
    if (events.containsKey(stream.getId())) {
      updateStream(stream);
      // case new Event: simply add
    } else {
      addStream(stream);
    }
  }

  /**
   * Update Stream in Supervisor. The condition on the update is the result of
   * the comparison of the modified fields.
   *
   * @param stream
   */
  private void updateStream(Stream stream) {
    Stream memStream = streams.get(stream.getId());
    if (memStream.getModified() > stream.getModified()) {
      // do nothing
    } else {
      memStream.merge(stream, JsonConverter.getCloner());
    }
  }

  /**
   * Add Stream in Supervisor
   *
   * @param stream
   */
  private void addStream(Stream stream) {
    streams.put(stream.getId(), stream);
  }

  /**
   * Delete Stream from Supervisor, if trashed is false, sets it to true, else
   * deletes it.
   *
   * @param streamToDelete
   *          the stream to delete
   */
  public void deleteStream(Stream streamToDelete) {
    if (streams.get(streamToDelete.getId()).getTrashed() == true) {
      // delete really
      streams.remove(streamToDelete.getId());
    } else {
      // update trashed field
      streams.get(streamToDelete.getId()).setTrashed(true);
      for (Stream childstream : streams.get(streamToDelete.getId()).getChildren()) {
        childstream.setTrashed(true);
      }
    }
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
  public Map<String, Event> getEvents(Filter filter) {
    Map<String, Event> returnEvents = new HashMap<String, Event>();

    for (Event event : events.values()) {
      if (filter.match(event)) {
        returnEvents.put(event.getId(), event);
        logger
          .log("Supervisor: matched: streamId=" + event.getStreamId() + ", id=" + event.getId());
      }
    }

    // apply limit argument
    if (filter.getLimit() != null) {
      returnEvents.keySet().retainAll(
        ImmutableSet.copyOf(Iterables.limit(returnEvents.keySet(), filter.getLimit())));
    }
    return returnEvents;
  }

  /**
   * Update or create events in Supervisor whether they already exist or not.
   *
   * @param newEvents
   */
  public void updateOrCreateEvent(Event newEvent) {
    if (newEvent != null) {
      // case exists: compare modified field
      if (events.containsKey(newEvent.getId())) {
        updateEvent(newEvent);
        // case new Event: simply add
      } else {
        addEvent(newEvent);
      }
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
   * @param eventId
   */
  public void deleteEvent(Event eventToDelete) {
    if (events.get(eventToDelete.getId()).getTrashed() == true) {
      // delete really
      events.remove(eventToDelete.getId());
    } else {
      // update trashed field
      events.get(eventToDelete.getId()).setTrashed(true);
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

}
