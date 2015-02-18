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
   * callback method for successful streams retrieval
   *
   * @param streams
   *          the retrieved streams
   * @param serverTime
   *          the server time retrieved from the online message, in seconds
   */
  void onStreamsRetrievalSuccess(Map<String, Stream> streams, Double serverTime);

  /**
   * callback method called when an error occured during Streams fetching.
   *
   * @param errorMessage
   *          the error message
   * @param serverTime
   *          the server time retrieved from the online message, in seconds
   */
  void onStreamsRetrievalError(String errorMessage, Double serverTime);

  /**
   * callback method called when createStream(), updateStream() or
   * deleteStream() execution is successful.
   *
   * @param successMessage
   *          the success message
   * @param stream
   *          the updated stream, not null when update or delete (trash case) of
   *          stream
   * @param serverTime
   *          the server time retrieved from the online message, in seconds
   */
  void onStreamsSuccess(String successMessage, Stream stream, Double serverTime);

  /**
   * callback method called when an error occurs during createStream(),
   * updateStream() or deleteStream() execution.
   *
   * @param errorMessage
   *          the error message
   * @param serverTime
   *          the server time retrieved from the online message, in seconds
   */
  void onStreamError(String errorMessage, Double serverTime);

}
