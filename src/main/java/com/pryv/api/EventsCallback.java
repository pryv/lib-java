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
  void onOnlieRetrieveEventsSuccess(Map<String, Event> onlineEvents);

  /**
   * callback method for successful DB retrieval
   *
   * @param newStreams
   */
  void onCacheRetrieveEventsSuccess(Map<String, Event> cacheEvents);

  /**
   * callback method for successful Supervisor retrieval
   *
   * @param supervisorEvents
   */
  void onSuperVisorRetrieveEventsSuccess(Map<String, Event> supervisorEvents);

  /**
   * callback method called when an error occured during Event fetching.
   *
   * @param message
   *          the error message
   */
  void onEventsRetrievalError(String errorMessage);

  /**
   * callback method called when createEvent(), updateEvent() or deleteEvent()
   * execution is successful.
   *
   * @param successMessage
   *          the success message
   */
  void onEventsSuccess(String successMessage);

  /**
   * callback method called when an error occurs during createEvent(),
   * updateEvent() or deleteEvent() execution.
   *
   * @param errorMessage
   *          the error message
   */
  void onEventsError(String errorMessage);

}
