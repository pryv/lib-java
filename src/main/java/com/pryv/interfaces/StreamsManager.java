package com.pryv.interfaces;

import com.pryv.api.Filter;
import com.pryv.api.model.Stream;

/**
 * interface used by ConnectionOld, OnlineEventsAndStreamManager and
 * CacheEventsAndStreamManager to manipulate Streams
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
   * @param getStreamsCallback
   *          the callback for streams retrieval
   */
  void get(Filter filter, GetStreamsCallback getStreamsCallback);

  /**
   * Create stream
   *
   * @param newStream
   *          the stream to create
   * @param streamsCallback
   *          the callback for streams creation, notifies success or failure
   */
  void create(Stream newStream, StreamsCallback streamsCallback);

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
  void delete(Stream streamToDelete, boolean mergeEventsWithParent,
              StreamsCallback streamsCallback);

  /**
   * Update Stream
   *
   * @param streamToUpdate
   *          the Stream object containing the updated fields
   * @param streamsCallback
   *          the callback for streams update, notifies success or failure.
   */
  void update(Stream streamToUpdate, StreamsCallback streamsCallback);

}
