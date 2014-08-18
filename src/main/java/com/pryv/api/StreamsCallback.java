package com.pryv.api;

import java.util.Map;

import com.pryv.api.model.Stream;

/**
 *
 * callback interface for Streams management methods
 *
 * @author ik
 *
 */
public interface StreamsCallback {

  /**
   * callback method for successful online retrieval
   *
   * @param streams
   */
  void onStreamsSuccess(Map<String, Stream> streams);

  /**
   * callback method for successful DB and Supervisor retrieval
   *
   * @param newStreams
   */
  void onStreamsPartialResult(Map<String, Stream> newStreams);

  /**
   * callback method called when an error occured during Streams fetching.
   *
   * @param message
   *          the error message
   */
  void onStreamsError(String message);

}
