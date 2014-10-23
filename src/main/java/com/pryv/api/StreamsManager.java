package com.pryv.api;

import com.pryv.api.model.Stream;

/**
 * interface used by Connection, Online and Cache to fetch Streams
 *
 * @author ik
 *
 */
public interface StreamsManager {

  /**
   * retrieve Streams
   *
   * @param filter
   *          optional filter parameters
   * @param streamsCallback
   *          the callback for streams retrieval
   */
  void getStreams(Filter filter, StreamsCallback streamsCallback);

  /**
   * Create stream
   *
   * @param newStream
   *          the stream to create
   * @param streamsCallback
   *          the callback for streams creation, notifies success or failure
   */
  void createStream(Stream newStream, StreamsCallback streamsCallback);

  /**
   * Trash or delete the specified stream, depending on its current state
   *
   * @param streamToDelete
   *          the Stream to delete
   * @param mergeEventsWithParent
   *          Done when actually deleting the stream. If true, Events of this
   *          Stream will be moved to the parent Stream if any. If false, Events
   *          will be deleted too
   *
   * @param streamsCallback
   *          the callback for streams deletion, notifies success or failure.
   */
  void deleteStream(Stream streamToDelete, boolean mergeEventsWithParent,
    StreamsCallback streamsCallback);

  /**
   * Update Stream
   *
   * @param streamToUpdate
   *          the Stream object containing the new fields
   * @param streamsCallback
   *          the callback for streams update, notifies success or failure.
   */
  void updateStream(Stream streamToUpdate, StreamsCallback streamsCallback);

}
