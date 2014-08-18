package com.pryv.api;

import java.util.Map;

import com.pryv.api.model.Event;

/**
 *
 * callback methods for results of Events manipulation
 *
 * @author ik
 *
 */
public interface EventsCallback {

  /**
   * callback method for successful online retrieval
   *
   * @param streams
   */
  void onEventsSuccess(Map<String, Event> events);

  /**
   * callback method for successful DB and Supervisor retrieval
   *
   * @param newStreams
   */
  void onEventsPartialResult(Map<String, Event> newEvents);

  /**
   * callback method called when an error occured during Event fetching.
   *
   * @param message
   *          the error message
   */
  void onEventsError(String message);

}
