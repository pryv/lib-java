package com.pryv.api;

import com.pryv.api.model.Stream;

/**
 * interface used by Connection, Online and Cache to fetch Streams
 *
 * @author ik
 *
 */
public interface StreamsManager {

  void getStreams(Filter filter, StreamsCallback streamsCallback);

  void createStream(Stream newStream, StreamsCallback streamsCallback);

  void deleteStream(String id, StreamsCallback streamsCallback);

  void updateStream(Stream streamToUpdate, StreamsCallback streamsCallback);

}
