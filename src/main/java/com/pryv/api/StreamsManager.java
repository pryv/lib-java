package com.pryv.api;

import com.pryv.api.model.Stream;

/**
 * interface used by Connection, Online and Cache to fetch Streams
 *
 * @author ik
 *
 * @param <T>
 *          format of returned Streams
 */
public interface StreamsManager<T> {

  void getStreams(StreamsCallback<T> streams);

  Stream createStream(String id);

  void deleteStream(String id);

  Stream updateStream(String id);

}
