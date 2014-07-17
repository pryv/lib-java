package com.pryv.api;

import com.pryv.api.model.Event;

/**
 *
 * interface used by Connection, Online and Cache to fetch Events
 *
 * @author ik
 *
 */
public interface EventsManager {

  /**
   * retrieve events
   *
   * @param id
   * @return
   */
  void get(EventsCallback eventsCallback);

  Event create(String id);

  void delete(String id);

  Event update(String id);

}
