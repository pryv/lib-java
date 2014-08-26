package com.pryv.api;

import java.util.HashMap;
import java.util.Map;

import com.pryv.api.model.Stream;
import com.pryv.utils.JsonConverter;
import com.pryv.utils.Logger;
import com.pryv.utils.StreamUtils;

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
  public Map<String, Stream> getStreams() {
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
    if (areFieldsValid(stream)) {
      Stream oldStream = getStreamById(stream.getId());
      if (oldStream != null) {
        // case exists: compare modified field
        updateStream(oldStream, stream, connectionCallback);
      } else {
        // case new Event: simply add
        addStream(stream);
        connectionCallback.onStreamsSuccess("Stream with id="
          + stream.getId()
            + " added successfully");
      }
    } else {
      connectionCallback
        .onStreamError("Supervisor: attempt to Create a Stream or Event with incomplete fields: id="
          + stream.getId()
            + ", name="
            + stream.getName());
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
    if (oldStream.getModified() > streamToUpdate.getModified()) {
      // do nothing
      connectionCallback.onStreamsSuccess("Stream with id="
        + streamToUpdate.getId()
          + " not updated since it is older than the stored version.");
    } else {
      // find out if parent has changed:
      if (oldStream.getParentId() == null && streamToUpdate.getParentId() == null) {
        // case 1: was root, still root
        // do nothing
      } else if (oldStream.getParentId() == null && streamToUpdate.getParentId() != null) {
        // case 2: was root, now child
        // add it to its parent's children
        addChildStreamToParent(streamToUpdate.getParentId(), streamToUpdate, connectionCallback);
      } else if (oldStream.getParentId() != null && streamToUpdate.getParentId() == null) {
        // case 3: was child, now root
        // remove it from old parents children
        removeChildStreamFromParent(oldStream.getParentId(), streamToUpdate, connectionCallback);
        rootStreams.put(streamToUpdate.getId(), streamToUpdate);
      } else {
        // case 4: was child, still child
        if (oldStream.getParentId().equals(streamToUpdate.getParentId())) {
          // case 4a: same parent
          // do nothing
        } else {
          // case 4b: parent changed
          // remove it from old parent
          removeChildStreamFromParent(oldStream.getParentId(), streamToUpdate, connectionCallback);
          // add it to new parent
          addChildStreamToParent(streamToUpdate.getParentId(), streamToUpdate, connectionCallback);
        }
      }
    }
    // do the update
    oldStream.merge(streamToUpdate, JsonConverter.getCloner());
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
    Stream newParent = StreamUtils.findStreamReference(parentId, rootStreams);
    if (newParent != null) {
      // verify that new parent is not the Stream's child - loop bug
      if (StreamUtils.findStreamReference(childStream.getId(), newParent.getChildrenMap()) == null) {
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
  private void addStream(Stream newStream) {
    logger.log("Stream added: " + newStream.getId());
    if (newStream.getParentId() != null) {
      Stream parentStream = getStreamById(newStream.getParentId());
      parentStream.addChildStream(newStream);
    }
    rootStreams.put(newStream.getId(), newStream);
    flatStreams.put(newStream.getId(), newStream);
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
   * used to check if input Stream has all the required fields as not null.
   *
   * @param streamToCheck
   * @return true if all fields are not null, false if any mandatory field is
   *         null or the stream is null.
   */
  private boolean areFieldsValid(Stream streamToCheck) {
    if (streamToCheck == null) {
      return false;
    } else {
      return streamToCheck.getId() != null
        && streamToCheck.getName() != null
          && streamToCheck.getCreated() != null
          && streamToCheck.getCreatedBy() != null
          && streamToCheck.getModified() != null
          && streamToCheck.getModifiedBy() != null;
    }
  }

  // /**
  // * custom thrown when user tries to call Supervisor methods with data having
  // * mandatory fields as null.
  // *
  // * @author ik
  // *
  // */
  // public class IncompleteFieldsException extends Exception {
  //
  // /**
  // * Constructor containing message to display
  // *
  // * @param message
  // */
  // public IncompleteFieldsException(String id, String name) {
  // super("Supervisor: attempt to Create a Stream or Event with incomplete fields: id="
  // + id
  // + ", name="
  // + name);
  // }
  // }

}
