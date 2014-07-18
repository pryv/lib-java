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

  void onSuccess(String jsonEvents);

  void onPartialResult(Map<String, Event> newEvents);

  void onError(String message);
}
