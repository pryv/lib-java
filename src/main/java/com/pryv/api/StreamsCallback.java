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
   *          the Streams retrieved from the online message
   * @param serverTime
   *          the server time retrieved from the online message, in seconds
   */
  void onStreamsRetrievalSuccess(Map<String, Stream> onlineStreams, double serverTime);

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
   *          the success message
   * @param stream
   *          the updated stream, not null when update or delete (trash case) of
   *          stream
   */
  void onStreamsSuccess(String successMessage, Stream stream);

  /**
   * callback method called when an error occurs during createStream(),
   * updateStream() or deleteStream() execution.
   *
   * @param errorMessage
   */
  void onStreamError(String errorMessage);

}
