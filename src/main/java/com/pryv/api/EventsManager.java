package com.pryv.api;

import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;

/**
 *
 * interface used by Connection, Online and Cache to fetch Events
 *
 * @author ik
 *
 * @param <T>
 *          the format in which the events are returned
 *
 */
public interface EventsManager<T> {

  /**
   * retrieve events
   *
   * @param id
   */
  void getEvents();

  /**
   * retrieve events of a particular Stream
   *
   * @param stream
   */
  void getEvents(Stream stream);

  Event createEvent(String id);

  void deleteEvent(String id);

  Event updateEvent(String id);

  void addEventsCallback(EventsCallback<T> eCallback);

}
