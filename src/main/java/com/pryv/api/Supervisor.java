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
 * contains Pryv objects loaded in memory
 *
 * @author ik
 *
 */
public class Supervisor {

  private Map<String, Event> events;
  private Map<String, Stream> streams;

  private Logger logger = Logger.getInstance();

  public Supervisor() {
    events = new HashMap<String, Event>();
    streams = new HashMap<String, Stream>();
  }

  /**
   *
   * Streams Management
   *
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
   * Update Streams map with pStreams.
   *
   * @param pStreams
   */
  public void updateStreams(Map<String, Stream> pStreams) {
    for (Stream stream : pStreams.values()) {
      // case exists: compare modified field
      if (events.containsKey(stream.getId())) {
        updateStream(stream);
        // case new Event: simply add
      } else {
        addStream(stream);
      }
    }
  }

  private void updateStream(Stream stream) {
    Stream memStream = streams.get(stream.getId());
    if (memStream.getModified() > stream.getModified()) {
      // do nothing
    } else {
      memStream.merge(stream, JsonConverter.getCloner());
    }
  }

  private void addStream(Stream stream) {
    streams.put(stream.getId(), stream);
  }

  /**
   *
   * Events Management
   *
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
   * updates events with newEvents.
   *
   * @param newEvents
   */
  public void updateEvents(Map<String, Event> newEvents) {
    if (newEvents != null) {
      for (Event event : newEvents.values()) {
        // case exists: compare modified field
        if (events.containsKey(event.getId())) {
          updateEvent(event);
          // case new Event: simply add
        } else {
          addEvent(event);
        }
      }
    }
  }

  private void addEvent(Event newEvent) {
    logger.log("Supervisor: adding new event: id="
      + newEvent.getId()
        + ", streamId="
        + newEvent.getStreamId());
    events.put(newEvent.getId(), newEvent);
  }

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
