package com.pryv.api;

import java.util.Map;

import com.pryv.api.model.Event;

/**
 *
 * callback methods for results of Events manipulation
 *
 * @author ik
 *
 * @param <T>
 *          the format in which the events are returned
 *
 */
public interface EventsCallback<T> {

  void onEventsSuccess(T events);

  void onEventsPartialResult(Map<String, Event> newEvents);

  void onEventsError(String message);

}
