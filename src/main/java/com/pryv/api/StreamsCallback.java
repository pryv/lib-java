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
   * @param onlineStreams
   */
  void onOnlineRetrieveStreamsSuccess(Map<String, Stream> onlineStreams);

  /**
   * callback method for successful DB retrieval
   *
   * @param cacheStreams
   */
  void onCacheRetrieveStreamSuccess(Map<String, Stream> cacheStreams);

  /**
   * callback method for successful Supervisor retrieval
   *
   * @param supervisorStreams
   */
  void onSupervisorRetrieveStreamsSuccess(Map<String, Stream> supervisorStreams);

  /**
   * callback method called when an error occured during Streams fetching.
   *
   * @param errorMessage
   *          the error message
   */
  void onStreamsRetrievalError(String errorMessage);

  /**
   * callback method called when createStream(), updateStream() or
   * deleteStream() execution is successful.
   *
   * @param successMessage
   */
  void onStreamsSuccess(String successMessage);

  /**
   * callback method called when an error occurs during createStream(),
   * updateStream() or deleteStream() execution.
   *
   * @param errorMessage
   */
  void onStreamError(String errorMessage);

}
