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
   * @param onlineEvents
   *          the events retrieved from the online message
   * @param serverTime
   *          the time of the server in seconds
   */
  void onOnlineRetrieveEventsSuccess(Map<String, Event> onlineEvents, double serverTime);

  /**
   * callback method for successful DB retrieval
   *
   * @param cacheEvents
   */
  void onCacheRetrieveEventsSuccess(Map<String, Event> cacheEvents);

  /**
   * callback method for successful Supervisor retrieval
   *
   * @param supervisorEvents
   */
  void onSupervisorRetrieveEventsSuccess(Map<String, Event> supervisorEvents);

  /**
   * callback method called when an error occured during Event fetching.
   *
   * @param errorMessage
   *          the error message
   */
  void onEventsRetrievalError(String errorMessage);

  /**
   * callback method called when createEvent(), updateEvent() or deleteEvent()
   * execution is successful.
   *
   * @param successMessage
   *          the success message
   * @param event
   *          updated Event, returned after an update is executed on the event.
   * @param stoppedId
   *          Only in singleActivity streams. If set, indicates the id of the
   *          previously running period event that was stopped as a consequence
   *          of inserting the new event.
   */
  void onEventsSuccess(String successMessage, Event event, Integer stoppedId);

  /**
   * callback method called when an error occurs during createEvent(),
   * updateEvent() or deleteEvent() execution.
   *
   * @param errorMessage
   *          the error message
   */
  void onEventsError(String errorMessage);

}
