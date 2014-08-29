package com.pryv.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.pryv.api.model.Stream;
import com.pryv.utils.Logger;

/**
 *
 * Contains Pryv objects loaded in memory
 *
 * @author ik
 *
 */
public class StreamsSupervisor {

  private Map<String, Stream> rootStreams;
  private Map<String, Stream> flatStreams;

  private Logger logger = Logger.getInstance();

  /**
   * Supervisor constructor. Instantiates data structures to store Streams and
   * Events.
   *
   */
  public StreamsSupervisor() {
    rootStreams = new HashMap<String, Stream>();
    flatStreams = new HashMap<String, Stream>();
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
    return rootStreams;
  }

  /**
   * Returns Stream which has the provided Id
   *
   * @param streamId
   *          the Id of the Stream you are looking for
   * @return the reference to the Stream
   */
  public Stream getStreamById(String streamId) {
    return flatStreams.get(streamId);
  }

  /**
   * Update or create Stream in Supervisor whether they already exist or not.
   *
   * @param stream
   * @param connectionCallback
   *          the callback to notify success or failure
   * @throws IncompleteFieldsException
   *           thrown when some mandatory fields are null
   */
  public void updateOrCreateStream(Stream stream, StreamsCallback connectionCallback) {
    Stream oldStream = getStreamById(stream.getId());
    if (oldStream != null) {
      // case exists: compare modified field
      updateStream(oldStream, stream, connectionCallback);
    } else {
      // case new Event: simply add
      addStream(stream, connectionCallback);
    }
  }

  /**
   * Update Stream in Supervisor. The condition on the update is the result of
   * the comparison of the modified fields.
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

    // oldStream.merge(streamToUpdate);
    // find out if parent has changed:
    if (oldStream.getParentId() == null && streamToUpdate.getParentId() == null) {
      // case 1: was root, still root
      // do nothing
      oldStream.merge(streamToUpdate);
    } else if (oldStream.getParentId() == null && streamToUpdate.getParentId() != null) {
      // case 2: was root, now child
      // add it to its parent's children
      oldStream.merge(streamToUpdate);
      addChildStreamToParent(streamToUpdate.getParentId(), oldStream, connectionCallback);
      rootStreams.remove(oldStream.getId());
    } else if (oldStream.getParentId() != null && streamToUpdate.getParentId() == null) {
      // case 3: was child, now root
      // remove it from old parents children
      String oldParent = oldStream.getParentId();
      oldStream.merge(streamToUpdate);
      removeChildStreamFromParent(oldParent, oldStream, connectionCallback);
      rootStreams.put(streamToUpdate.getId(), oldStream);
    } else {
      // case 4: was child, still child
      if (oldStream.getParentId().equals(streamToUpdate.getParentId())) {
        // case 4a: same parent
        // do nothing
        logger.log("StreamsSupervisor: same parents");
        oldStream.merge(streamToUpdate);
      } else {
        // case 4b: parent changed
        // remove it from old parent
        // removeChildStreamFromParent(oldStream.getParentId(),
        // streamToUpdate, connectionCallback);
        logger.log("StreamsSupervisor: changing parents");
        getStreamById(oldStream.getParentId()).removeChildStream(oldStream);
        // add it to new parent
        oldStream.merge(streamToUpdate);
        addChildStreamToParent(oldStream.getParentId(), oldStream, connectionCallback);
      }
    }
    // do the update
    connectionCallback.onStreamsSuccess("Stream updated: " + streamToUpdate.getId());
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
        logger.log(childStream.getId() + " now child of " + parentId);
      } else {
        connectionCallback
          .onStreamError("Stream update failure: trying to add Stream to child of its children");
      }
    } else {
      connectionCallback
        .onStreamError("Stream update failure: trying to add Stream as child of unexisting Stream.");
    }
  }

  /**
   * remove Stream's reference from parent's children list.
   *
   * @param parentId
   *          the id of the parent Stream
   * @param childStream
   *          the reference of the Stream to remove
   * @param connectionCallback
   *          the callback to notify failure
   */
  private void removeChildStreamFromParent(String parentId, Stream childStream,
    StreamsCallback connectionCallback) {
    Stream oldParent = getStreamById(parentId);
    if (oldParent != null) {
      logger.log(childStream.getId() + " removed from " + parentId + "'s children.");
      oldParent.removeChildStream(childStream);
    } else {
      connectionCallback
        .onStreamError("Stream update failure: trying to remove Stream from unexisting Stream's children list");
    }
  }

  /**
   * Add Stream in Supervisor
   *
   * @param newStream
   *          the stream to add
   */
  private void addStream(Stream newStream, StreamsCallback connectionCallback) {
    if (newStream.getParentId() != null) {
      // add the Stream to its parent's children
      Stream parentStream = getStreamById(newStream.getParentId());
      parentStream.addChildStream(newStream);
    } else {
      rootStreams.put(newStream.getId(), newStream);
    }
    flatStreams.put(newStream.getId(), newStream);

    // add its children
    addChildren(newStream);
    connectionCallback.onStreamsSuccess("StreamSupervisor: Stream (id="
      + newStream.getId()
        + ", name="
        + newStream.getName()
        + ") added ");
    logger.log("StreamsSupervisor: Stream added: " + newStream.getId());
  }

  /**
   * Add all children Streams to StreamsSupervisor recursively
   *
   * @param parentStream
   *          the Stream whose children are added.
   */
  private void addChildren(Stream parentStream) {
    if (parentStream.getChildren() != null) {
      for (Stream childStream : parentStream.getChildren()) {
        flatStreams.put(childStream.getId(), childStream);
        logger.log("StreamsSupervisor: child Stream added: " + childStream.getId());
        addChildren(childStream);
      }
    }
  }

  /**
   * * Delete Stream from Supervisor, if trashed is false, sets it to true, else
   * deletes it.
   *
   * @param streamId
   *          the Id of the Stream to delete.
   * @param mergeWithParent
   * @param connectionSCallback
   *          callback for the Stream deletion
   */
  public void deleteStream(String streamId, boolean mergeWithParent,
    StreamsCallback connectionSCallback) {
    Stream streamToDelete = getStreamById(streamId);
    if (streamToDelete != null) {
      if (streamToDelete.getTrashed() == true) {
        // delete really

        // delete from parent stream
        Stream parentStream = getStreamById(streamToDelete.getParentId());
        parentStream.removeChildStream(streamToDelete);

        // delete Stream's events
        // for (Event event : events.values()) {
        // if (event.getStreamId().equals(streamId)) {
        // deleteEvent(event, null);
        // }
        // }
        // delete Stream's children Streams
        if (streamToDelete.getChildren() != null) {
          for (String childStream : streamToDelete.getChildrenMap().keySet()) {
            deleteStream(childStream, mergeWithParent, connectionSCallback);
          }
        }
        // delete Stream
        rootStreams.remove(streamToDelete.getId());
        flatStreams.remove(streamToDelete.getId());
        streamToDelete = null;
      } else {
        // update trashed field of stream to delete and its child streams
        streamToDelete.setTrashed(true);
        for (Stream childstream : streamToDelete.getChildren()) {
          childstream.setTrashed(true);
          // trash Streams' events
          // for (Event event : events.values()) {
          // if (event.getStreamId().equals(streamId)) {
          // event.setTrashed(true);
          // }
          // }
        }
        for (Stream childstream : streamToDelete.getChildrenMap().values()) {
          childstream.setTrashed(true);
        }
      }
      connectionSCallback.onStreamsSuccess("Stream with id=" + streamId + " deleted.");
    } else {
      // streamToDelete not found
      connectionSCallback.onStreamError("Stream with id=" + streamId + " not found.");
    }
  }

  /**
   * Find out if Stream with id="childId" is a descendant of Stream with
   * id="parentId"
   *
   * @param childId
   * @param parentId
   * @return
   */
  public boolean verifyParency(String childId, String parentId) {
    if (getStreamById(parentId) != null) {
      if (getStreamById(parentId).getChildren() != null) {
        Set<String> children = new HashSet<String>();
        computeDescendants(children, parentId);
        return children.contains(childId);
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  /**
   * Find out if Stream with id="childId" is a child of any Stream whose id is
   * contained in parentIds
   *
   * @param childId
   * @param parentIds
   * @return
   */
  public boolean verifyParency(String childId, Set<String> parentIds) {
    for (String parentId : parentIds) {
      if (verifyParency(childId, parentId)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Store the ids of Stream with id "parentId"'s descendants.
   *
   * @param children
   *          the Set<String> in which the Stream Id's will be stored
   * @param parentId
   *          the id of the parent Stream.
   */
  private void computeDescendants(Set<String> children, String parentId) {
    logger.log("StreamSupervisor: computing descendency of stream with id=" + parentId);
    if (getStreamById(parentId).getChildren() != null) {
      logger.log("StreamSupervisor: this Stream has "
        + getStreamById(parentId).getChildren().size()
          + " children in list");
      logger.log("StreamSupervisor: this Stream has "
        + getStreamById(parentId).getChildrenMap().size()
          + " children in map");
      for (Stream childStream : getStreamById(parentId).getChildren()) {
        children.add(childStream.getId());
        computeDescendants(children, childStream.getId());
      }
    }
  }

}
