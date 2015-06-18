package com.pryv.api;

import java.util.HashSet;
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
  private EventsCallback deleteEventsCallback;
  private Map<String, Event> eventsOnDelete;

  private Logger logger = Logger.getInstance();

  /**
   * StreamsSupervisor constructor. Instantiates data structures to store
   * Streams.
   *
   */
  public StreamsSupervisor() {
    rootStreams = new ConcurrentHashMap<String, Stream>();
    flatStreams = new ConcurrentHashMap<String, Stream>();
    instanciateDeleteEventsCallback();
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
    recomputeTree();
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
      recomputeTree();
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
      recomputeTree();
    }
    connectionCallback.onStreamsRetrievalSuccess(getRootStreams(), null);
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

    logger.log("StreamSupervisor: updated Stream (id="
      + oldStream.getId()
        + ", name="
        + oldStream.getName()
        + ", parentId="
        + oldStream.getParentId()
        + ")"
        + " - "
        + Thread.currentThread().getName());
    connectionCallback.onStreamsSuccess("StreamsSupervisor: Stream with id="
      + oldStream.getId()
        + ", name="
        + oldStream.getName()
        + ", parentCid="
        + oldStream.getParentId()
        + " updated.", oldStream, null);
  }

  /**
   * Add Stream's reference to parent's children list
   *
   * @param parentId
   *          the id of the parent Stream
   * @param childStream
   *          the reference of the Stream to add
   * @param connectionCallback
   *          the callback to notify failure
   */
  private void addChildStreamToParent(String parentId, Stream childStream,
    StreamsCallback connectionCallback) {
    Stream newParent = flatStreams.get(parentId);
    if (newParent != null) {
      // verify that new parent is not the Stream's child - loop bug
      if (!verifyParency(childStream.getId(), parentId)) {
        newParent.addChildStream(childStream);
        logger.log("StreamsSupervisor: " + childStream.getId() + " now child of " + parentId);
      } else {
        connectionCallback.onStreamError(
          "Stream update failure: trying to add Stream to child of its children", null);
      }
    } else {
      connectionCallback.onStreamError(
        "Stream update failure: trying to add Stream as child of unexisting Stream.", null);
    }
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

    logger.log("StreamSupervisor: added Stream (id="
      + newStream.getId()
        + ", name="
        + newStream.getName()
        + ", parentId="
        + newStream.getParentId()
        + ")"
        + " - "
        + Thread.currentThread().getName());
    connectionCallback.onStreamsSuccess("StreamsSupervisor: Stream with id="
      + newStream.getId()
        + ", name="
        + newStream.getName()
        + ", parentId="
        + newStream.getParentId()
        + " created.", newStream, null);
    // add its children if any
    if (newStream.getChildren() != null) {
      for (Stream childStream : newStream.getChildren()) {
        updateOrCreateStream(childStream, connectionCallback);
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
          System.out.println("StreamsSupervisor: parentId not null, it is: \'"
            + streamToDelete.getParentId()
              + "\'");
          Stream parentStream = getStreamById(streamToDelete.getParentId());
          parentStream.removeChildStream(streamToDelete);
        }

        // behaviour not defined in API - may be added later (should also delete
        // these streams' events)
        // delete Stream's children Streams
        // if (streamToDelete.getChildren() != null) {
        // for (String childStream : streamToDelete.getChildrenMap().keySet()) {
        // deleteStream(childStream, mergeWithParent, connectionSCallback);
        // }
        // }

        Filter deleteFilter = new Filter();
        deleteFilter.addStreamId(streamId);
        eventsSupervisor.getEvents(deleteFilter, deleteEventsCallback);
        String parentToMergeWithId = streamToDelete.getId();
        if (mergeWithParent == true && parentToMergeWithId != null) {
          // merge them with parent stream if any exists
          for (Event eventToMergeWithParent : eventsOnDelete.values()) {
            eventToMergeWithParent.setStreamId(parentToMergeWithId);
          }
        } else {
          // behaviour not defined in API - delete events
          // for (Event eventToDelete : eventsOnDelete.values()) {
          // eventsSupervisor.deleteEvent(eventToDelete, deleteEventsCallback);
          // }
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
        // behavour not defined in API - trash its child streams
        // for (Stream childstream : streamToDelete.getChildren()) {
        // childstream.setTrashed(true);
        // }
        // for (Stream childstream : streamToDelete.getChildrenMap().values()) {
        // childstream.setTrashed(true);
        // }
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

  private void instanciateDeleteEventsCallback() {
    deleteEventsCallback = new EventsCallback() {

      @Override
      public void onEventsSuccess(String successMessage, Event event, Integer stoppedId,
        Double pServerTime) {
      }

      @Override
      public void onEventsRetrievalSuccess(Map<String, Event> events, Double pServerTime) {
        eventsOnDelete = events;
      }

      @Override
      public void onEventsRetrievalError(String errorMessage, Double pServerTime) {
      }

      @Override
      public void onEventsError(String errorMessage, Double pServerTime) {
      }
    };
  }

  /**
   * Find out if Stream with id="childId" is a descendant of Stream with
   * id="parentId"
   *
   * @param childId
   * @param parentId
   * @return true if stream with id "childId" is a descendant of the stream with
   *         id parentId
   */
  public boolean verifyParency(String childId, String parentId) {
    logger.log("StreamsSupervisor: verifying if " + childId + " is child of " + parentId);
    Stream parentStream = getStreamById(parentId);
    if (parentStream != null) {
      if (parentStream.getChildren() != null) {
        Set<String> children = new HashSet<String>();
        computeDescendants(children, parentStream);
        return children.contains(childId);
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  /**
   * Find out if Stream with clientId="childClientId" is a child of any Stream
   * whose clientId is contained in parentClientIds
   *
   * @param childClientId
   * @param parentClientIds
   * @return true if stream with client id "childClientId" is a descendant of
   *         one of the streams whose client id is in the set parentClientIds
   */
  public boolean verifyParency(String childClientId, Set<String> parentClientIds) {
    for (String parentClientId : parentClientIds) {
      if (verifyParency(childClientId, parentClientId)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Store the ids of parentStream's descendants.
   *
   * @param children
   *          the Set<String> in which the Stream Ids will be stored
   * @param parentStream
   *          the parent Stream.
   */
  private void computeDescendants(Set<String> children, Stream parentStream) {
    if (parentStream.getChildren() != null) {
      logger.log("StreamSupervisor: this Stream has "
        + parentStream.getChildren().size()
          + " children in list");
      logger.log("StreamSupervisor: this Stream has "
        + parentStream.getChildrenMap().size()
          + " children in map");
      for (Stream childStream : parentStream.getChildren()) {
        children.add(childStream.getId());
        computeDescendants(children, childStream);
      }
    }
  }

  /**
   * fixes Streams' children properties based on parentIds
   */
  private void recomputeTree() {
    rootStreams.clear();

    String pid = null;
    // fix root streams
    for (Stream potentialRootStream : flatStreams.values()) {
      potentialRootStream.clearChildren();
      pid = potentialRootStream.getParentId();
      if (pid == null) {
        logger.log("StreamsSupervisor: adding rootStream: id="
          + potentialRootStream.getId()
            + ", name="
            + potentialRootStream.getName());
        rootStreams.put(potentialRootStream.getId(), potentialRootStream);
      }
    }

    // assign children
    for (Stream childStream : flatStreams.values()) {
      pid = childStream.getParentId();
      if (pid != null) {
        if (flatStreams.containsKey(pid)) {
          logger.log("StreamsSupervisor: adding childStream: id="
            + childStream.getId()
              + ", name="
              + childStream.getName()
              + " to "
              + pid);
          addChildStreamToParent(pid, childStream, null);
        }
      }
    }
  }

}
