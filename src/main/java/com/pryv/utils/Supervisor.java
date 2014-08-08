package com.pryv.utils;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.pryv.api.Filter;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;

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

  public Map<String, Event> getEvents(Filter filter) {
    Map<String, Event> returnEvents = new HashMap<String, Event>();

    for (Event event : events.values()) {
      if (filter.match(event)) {
        returnEvents.put(event.getId(), event);
        logger.log("Supervisor: matched id=" + event.getId() + ", streamId=" + event.getStreamId());
      }
    }

    if (filter.getLimit() != null) {
      returnEvents.keySet().retainAll(
        ImmutableSet.copyOf(Iterables.limit(returnEvents.keySet(), filter.getLimit())));
    }
    return returnEvents;
  }

  public Event getEventById(String id) {
    return events.get(id);
  }

  public void addEvent(Event newEvent) {
    logger.log("Supervisor: adding new event: id="
      + newEvent.getId()
        + ", streamId="
        + newEvent.getStreamId());
    events.put(newEvent.getId(), newEvent);
  }

  public Map<String, Stream> getStreams() {
    return streams;
  }

  public void setEvents(Map<String, Event> pEvents) {
    this.events = pEvents;
  }

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

  public void updateEvents(Map<String, Event> newEvents) {
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

  private void updateEvent(Event event) {
    Event memEvent = events.get(event.getId());
    if (memEvent.getModified() > event.getModified()) {
      // do nothing
    } else {
      memEvent.merge(event, JsonConverter.getCloner());
    }
  }
}
