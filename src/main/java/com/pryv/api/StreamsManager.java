package com.pryv.api;

import java.util.List;

import com.pryv.api.model.Stream;

/**
 *
 * interface used by Connection, Online and Cache to fetch Streams
 *
 * @author ik
 *
 */
public interface StreamsManager {

  List<Stream> getStreams();

  Stream createStream(String id);

  void deleteStream(String id);

  Stream updateStream(String id);

}
