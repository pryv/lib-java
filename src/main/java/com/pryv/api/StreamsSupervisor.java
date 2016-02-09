package com.pryv.api;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;
import com.pryv.utils.Logger;

/**
 *
 * Contains Pryv Streams loaded in memory
 *
 * @author ik
 *
 */
public class StreamsSupervisor {

  /**
   * Streams with no parent stream. the key is the id
   */
  private Map<String, Stream> rootStreams;

  /**
   * All Streams stored in the Supervisor. the key is the id
   */
  private Map<String, Stream> flatStreams;

  private EventsSupervisor eventsSupervisor;

  private Logger logger = Logger.getInstance();

  /**
   * StreamsSupervisor constructor. Instantiates data structures to store
   * Streams.
   *
   */
  public StreamsSupervisor() {
    rootStreams = new ConcurrentHashMap<String, Stream>();
    flatStreams = new ConcurrentHashMap<String, Stream>();
  }

  public void setEventsSupervisor(EventsSupervisor pEventsSupervisor) {
    eventsSupervisor = pEventsSupervisor;
  }

  /*
   * Streams Management
   */

  /**
   * Returns the root local memory streams
   *
   * @return
   */
  public Map<String, Stream> getRootStreams() {
    recomputeRootStreamsTree();
    return rootStreams;
  }

  /**
   * Returns Stream which has the provided id
   *
   * @param streamId
   *          the client id of the Stream you are looking for
   * @return the reference to the Stream
   */
  public Stream getStreamById(String streamId) {
    return flatStreams.get(streamId);
  }

  /**
   * Update or create stream in Supervisor whether it already exists or not.
   *
   * @param stream
   *          the Stream to add or update
   * @param connectionCallback
   *          the callback to notify success or failure
   */
  public void updateOrCreateStream(Stream stream, StreamsCallback connectionCallback) {
    logger.log("StreamsSupervisor: updateOrCreateStream with id="
      + stream.getId()
        + ", parentId="
        + stream.getParentId()
        + " - "
        + Thread.currentThread().getName());

    // stream.id shouldn't be null here. If Stream comes from API, it has an ID,
    // if it comes from the user, Connection generates an ID for it if needed.

    Stream oldStream = getStreamById(stream.getId());
    if (oldStream != null) {
      updateStream(oldStream, stream, connectionCallback);
    } else {
      addStream(stream, connectionCallback);
    }
  }

  /**
   * update or create multiple streams in Supervisor wether it already exists or
   * not. This method is only called by OnlineManagerStreamsCallback, and
   * CacheStreamsCallback when syncing retrieved streams.
   *
   * @param streams
   *          the Streams Map to synchronize
   * @param connectionCallback
   *          the callback to notify successes or failures
   */
  public void
    updateOrCreateStreams(Map<String, Stream> streams, StreamsCallback connectionCallback) {
    for (Stream stream : streams.values()) {
      Stream oldStream = getStreamById(stream.getId());
      if (oldStream != null) {
        updateStream(oldStream, stream, connectionCallback);
      } else {
        addStream(stream, connectionCallback);
      }
    }
    connectionCallback.onStreamsRetrievalSuccess(rootStreams, null);
  }

  /**
   * Update Stream in Supervisor.
   *
   * @param oldStream
   *          the old instance of the Stream
   * @param streamToUpdate
   *          the instance of updated stream
   * @param connectionCallback
   *          the callback to notify success or failure
   */
  private void updateStream(Stream oldStream, Stream streamToUpdate,
    StreamsCallback connectionCallback) {
    logger.log("StreamsSupervisor: update oldStream (id="
      + oldStream.getId()
        + ", parentId="
        + oldStream.getParentId()
        + ") to streamToUpdate (id="
        + streamToUpdate.getId()
        + ", parentId="
        + streamToUpdate.getParentId()
        + ")"
        + " - "
        + Thread.currentThread().getName());

    oldStream.merge(streamToUpdate, true);
    addChildrenStreamsToFlatStreams(oldStream);

    logger.log("StreamsSupervisor: updated Stream (id="
      + oldStream.getId()
        + ", name="
        + oldStream.getName()
        + ", parentId="
        + oldStream.getParentId()
        + ")"
        + " - "
        + Thread.currentThread().getName());
    recomputeRootStreamsTree();
    connectionCallback.onStreamsSuccess("StreamsSupervisor: Stream with id="
      + oldStream.getId()
        + ", name="
        + oldStream.getName()
        + ", parentCid="
        + oldStream.getParentId()
        + " updated.", oldStream, null);
  }

  /**
   * Add Stream in Supervisor
   *
   * @param newStream
   *          the stream to add
   * @param connectionCallback
   */
  private void addStream(Stream newStream, StreamsCallback connectionCallback) {
    logger.log("StreamsSupervisor: add stream (id="
      + newStream.getId()
        + ", parentId="
        + newStream.getParentId()
        + " - "
        + Thread.currentThread().getName());
    if (newStream.getParentId() == null) {
      rootStreams.put(newStream.getId(), newStream);
    }
    flatStreams.put(newStream.getId(), newStream);
    addChildrenStreamsToFlatStreams(newStream);

    logger.log("StreamsSupervisor: added Stream (id="
      + newStream.getId()
        + ", name="
        + newStream.getName()
        + ", parentId="
        + newStream.getParentId()
        + ")"
        + " - "
        + Thread.currentThread().getName());
    recomputeRootStreamsTree();
    if (connectionCallback != null) {
      connectionCallback.onStreamsSuccess("StreamsSupervisor: Stream with id="
        + newStream.getId()
          + ", name="
          + newStream.getName()
          + ", parentId="
          + newStream.getParentId()
          + " created.", newStream, null);
    }
  }

  /**
   * Adds the children of the parent Stream to flatStreams if it has any.
   *
   * @param parent
   *          the Stream whose children are added
   */
  private void addChildrenStreamsToFlatStreams(Stream parent) {
    Set<Stream> children = parent.getChildren();
    if (children != null) {
      for (Stream child : children) {
        flatStreams.put(child.getId(), child);
        addChildrenStreamsToFlatStreams(child);
      }
    }
  }

  /**
   * Delete Stream from Supervisor, if trashed is false, sets it to true, else
   * deletes it.
   *
   * @param streamId
   *          the Id of the Stream to delete.
   * @param mergeWithParent
   *          true if the deleted Stream's children Streams and Events are to be
   *          merged with the parent Stream
   * @param connectionSCallback
   *          callback for the Stream deletion
   */
  public void deleteStream(String streamId, boolean mergeWithParent,
    StreamsCallback connectionSCallback) {
    Stream streamToDelete = getStreamById(streamId);
    if (streamToDelete != null) {
      if (streamToDelete.isTrashed() == true) {
        // delete really

        // delete from parent stream
        if (streamToDelete.getParentId() != null) {
          Stream parentStream = getStreamById(streamToDelete.getParentId());
          parentStream.removeChildStream(streamToDelete);
        }

        String parentToMergeWithId = streamToDelete.getId();
        // merge them with parent stream if any exists
        if (mergeWithParent == true && parentToMergeWithId != null) {
          Filter eventsToMergeFilter = new Filter();
          eventsToMergeFilter.addStreamId(streamId);

          eventsSupervisor.getEvents(eventsToMergeFilter, new EventsCallback() {

            @Override
            public void onEventsSuccess(String successMessage, Event event, Integer stoppedId,
              Double serverTime) {
            }

            @Override
            public void onEventsRetrievalSuccess(Map<String, Event> events, Double serverTime) {
              for (Event eventToMergeWithParent : events.values()) {
                eventToMergeWithParent.setStreamId(parentToMergeWithId);
              }
            }

            @Override
            public void onEventsRetrievalError(String errorMessage, Double serverTime) {
            }

            @Override
            public void onEventsError(String errorMessage, Double serverTime) {
            }
          });

        }

        // delete Stream
        rootStreams.remove(streamToDelete.getId());
        flatStreams.remove(streamToDelete.getId());
        streamToDelete = null;

        connectionSCallback.onStreamsSuccess("StreamsSupervisor: Stream with id="
          + streamId
            + " deleted.", null, null);
      } else {
        // update trashed field of stream to delete
        streamToDelete.setTrashed(true);
        connectionSCallback.onStreamsSuccess("StreamsSupervisor: Stream with id="
          + streamId
            + " trashed.", streamToDelete, null);
      }
    } else {
      // streamToDelete not found
      connectionSCallback.onStreamError("StreamsSupervisor: Stream with id="
        + streamId
          + " not found.", null);
    }
  }

  /**
   * Find out if Stream with id="childId" is a child of any Stream whose id is
   * contained in parentIds
   *
   * @param childId
   * @param parentIds
   * @return true if stream with id "childId" is a descendant of one of the
   *         streams whose id is in the set parentIds
   */
  public boolean verifyParency(String childId, Set<String> parentIds) {
    Stream parent = null;
    for (String parentId : parentIds) {
      parent = getStreamById(parentId);
      if (parent.hasChild(childId)) {
        return true;
      }
    }
    return false;
  }

  /**
   * fixes Streams' children properties based on parentIds
   */
  private void recomputeRootStreamsTree() {
    rootStreams.clear();

    String parentId = null;
    // set root streams
    for (Stream potentialRootStream : flatStreams.values()) {
      // clear children fields
      potentialRootStream.clearChildren();
      parentId = potentialRootStream.getParentId();
      if (parentId == null) {
        logger.log("StreamsSupervisor: adding rootStream: id="
          + potentialRootStream.getId()
            + ", name="
            + potentialRootStream.getName());
        rootStreams.put(potentialRootStream.getId(), potentialRootStream);
      }
    }

    // assign children
    for (Stream childStream : flatStreams.values()) {
      parentId = childStream.getParentId();
      if (parentId != null) {
        if (flatStreams.containsKey(parentId)) {
          logger.log("StreamsSupervisor: adding childStream: id="
            + childStream.getId()
              + ", name="
              + childStream.getName()
              + " to "
              + parentId);
          Stream parent = flatStreams.get(parentId);
          if (parent != null) {
            parent.addChildStream(childStream);
          }
        }
      }
    }
  }

}
