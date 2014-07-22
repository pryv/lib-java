package com.pryv.api;

import com.pryv.api.model.Event;

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
   * @return
   */
  void get(EventsCallback<T> eventsCallback);

  Event create(String id);

  void delete(String id);

  Event update(String id);

}
