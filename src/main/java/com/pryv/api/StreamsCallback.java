package com.pryv.api;

import java.util.Map;

import com.pryv.api.model.Stream;

/**
 *
 * callback interface for Streams management methods
 *
 * @param <T>
 *          format of returned streams
 * @author ik
 *
 */
public interface StreamsCallback<T> {

  void onStreamsSuccess(T streams);

  void onStreamsPartialResult(Map<String, Stream> newStreams);

  void onStreamsError(String message);

}
