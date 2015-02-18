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
   * callback method for successful events retrieval
   *
   * @param events
   *          the retrieved events
   * @param serverTime
   *          the time of the server in seconds
   */
  void onEventsRetrievalSuccess(Map<String, Event> events, Double serverTime);

  /**
   * callback method called when an error occured during Events retrieval.
   *
   * @param errorMessage
   *          the error message
   * @param serverTime
   *          the time of the server in seconds
   */
  void onEventsRetrievalError(String errorMessage, Double serverTime);

  /**
   * callback method called when createEvent(), updateEvent() or deleteEvent()
   * execution is successful.
   *
   * @param successMessage
   *          the success message
   * @param event
   *          updated or created Event, returned after an update/creation is
   *          executed on the event.
   * @param stoppedId
   *          Only in singleActivity streams. If set, indicates the id of the
   *          previously running period event that was stopped as a consequence
   *          of inserting the new event.
   * @param serverTime
   *          the time of the server in seconds
   */
  void onEventsSuccess(String successMessage, Event event, Integer stoppedId, Double serverTime);

  /**
   * callback method called when an error occurs during createEvent(),
   * updateEvent() or deleteEvent() execution.
   *
   * @param errorMessage
   *          the error message
   * @param serverTime
   *          the time of the server in seconds
   */
  void onEventsError(String errorMessage, Double serverTime);

}
