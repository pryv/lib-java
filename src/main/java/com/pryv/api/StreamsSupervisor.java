package com.pryv.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
   * Streams with no parent stream. the key is the clientId
   */
  private Map<String, Stream> rootStreams;

  /**
   * All Streams stored in the Supervisor. the key is the clientId
   */
  private Map<String, Stream> flatStreams;

  /**
   * Map: key=stream.id, value=stream.clientId
   */
  private Map<String, String> idToClientId;

  /**
   * Map: key=stream.clientId, value=stream.Id
   */
  private Map<String, String> clientIdToId;

  private Logger logger = Logger.getInstance();

  /**
   * StreamsSupervisor constructor. Instantiates data structures to store
   * Streams.
   *
   */
  public StreamsSupervisor() {
    rootStreams = new HashMap<String, Stream>();
    flatStreams = new HashMap<String, Stream>();
    idToClientId = new HashMap<String, String>();
    clientIdToId = new HashMap<String, String>();
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
   * Returns Stream which has the provided client id
   *
   * @param streamClientId
   *          the client id of the Stream you are looking for
   * @return the reference to the Stream
   */
  public Stream getStreamByClientId(String streamClientId) {
    return flatStreams.get(streamClientId);
  }

  /**
   * Returns the clientId of the Stream whose id is provided.
   *
   * @param id
   * @return the stream's clientId if it exists, else null
   */
  public String getClientId(String id) {
    return idToClientId.get(id);
  }

  /**
   * Returns the id of the Stream whose clientId is provided
   *
   * @param clientId
   * @return
   */
  public String getId(String clientId) {
    return clientIdToId.get(clientId);
  }

  /**
   * Returns the Map that allows to translate the stream Id to its client Id
   *
   * @return
   */
  public Map<String, String> getStreamsIdToClientIdDictionnary() {
    return idToClientId;
  }

  /**
   * Update or create Stream in Supervisor whether they already exist or not.
   *
   * @param stream
   * @param connectionCallback
   *          the callback to notify success or failure
   */
  public void updateOrCreateStream(Stream stream, StreamsCallback connectionCallback) {
    System.out.println("StreamsSupervisor: updateOrCreateStream with id="
      + stream.getId()
        + ", cid="
        + stream.getClientId());

    if (getClientId(stream.getId()) == null && stream.getClientId() == null) {
      stream.generateClientId();
      logger.log("StreamsSupervisor: Generating new cid for Stream OH NOES");
    }
    stream.updateParentClientId(idToClientId);
    Stream oldStream = getStreamByClientId(getClientId(stream.getId()));
    if (oldStream != null) {
      // case exists: update Stream
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
    logger.log("StreamsSupervisor: updateStream with id="
      + oldStream.getId()
        + ", cid="
        + oldStream.getClientId());

    // find out if parent has changed:
    if (oldStream.getParentClientId() == null && streamToUpdate.getParentClientId() == null) {
      // case 1: was root, still root
      // do nothing
      oldStream.merge(streamToUpdate);
    } else if (oldStream.getParentClientId() == null && streamToUpdate.getParentClientId() != null) {
      // case 2: was root, now child
      // add it to its parent's children
      oldStream.merge(streamToUpdate);
      addChildStreamToParent(streamToUpdate.getParentClientId(), oldStream, connectionCallback);
      rootStreams.remove(oldStream.getClientId());
    } else if (oldStream.getParentClientId() != null && streamToUpdate.getParentClientId() == null) {
      // case 3: was child, now root
      // remove it from old parents children
      String oldParent = oldStream.getParentClientId();
      oldStream.merge(streamToUpdate);
      removeChildStreamFromParent(oldParent, oldStream, connectionCallback);
      // put it in root streams
      rootStreams.put(streamToUpdate.getClientId(), oldStream);
    } else {
      // case 4: was child, still child
      if (oldStream.getParentClientId().equals(streamToUpdate.getParentClientId())) {
        // case 4a: same parent
        // do nothing
        oldStream.merge(streamToUpdate);
      } else {
        // case 4b: parent changed
        // remove it from old parent
        getStreamByClientId(oldStream.getParentClientId()).removeChildStream(oldStream);
        // add it to new parent
        oldStream.merge(streamToUpdate);
        addChildStreamToParent(oldStream.getParentClientId(), oldStream, connectionCallback);
      }
    }
    // do the update
    connectionCallback.onStreamsSuccess("Stream updated: " + streamToUpdate.getId());
    if (streamToUpdate.getChildren() != null) {
      for (Stream childStream : streamToUpdate.getChildren()) {
        updateOrCreateStream(childStream, connectionCallback);
      }
    }
  }

  /**
   * Add Stream's reference to parent's children list
   *
   * @param parentClientId
   *          the client id of the parent Stream
   * @param childStream
   *          the reference of the Stream to add
   * @param connectionCallback
   *          the callback to notify failure
   */
  private void addChildStreamToParent(String parentClientId, Stream childStream,
    StreamsCallback connectionCallback) {
    Stream newParent = flatStreams.get(parentClientId);
    if (newParent != null) {
      // verify that new parent is not the Stream's child - loop bug
      if (!verifyParency(childStream.getClientId(), parentClientId)) {
        newParent.addChildStream(childStream);
        logger.log(childStream.getClientId() + " now child of " + parentClientId);
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
   * @param parentClientId
   *          the client id of the parent Stream
   * @param childStream
   *          the reference of the Stream to remove
   * @param connectionCallback
   *          the callback to notify failure
   */
  private void removeChildStreamFromParent(String parentClientId, Stream childStream,
    StreamsCallback connectionCallback) {
    Stream oldParent = getStreamByClientId(parentClientId);
    if (oldParent != null) {
      logger.log(childStream.getClientId() + " removed from " + parentClientId + "'s children.");
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
   * @param connectionCallback
   */
  private void addStream(Stream newStream, StreamsCallback connectionCallback) {
    logger.log("StreamSupervisor: add Stream with id="
      + newStream.getId()
        + ", cid="
        + newStream.getClientId());
    if (newStream.getParentClientId() == null) {
      rootStreams.put(newStream.getClientId(), newStream);
    }
    flatStreams.put(newStream.getClientId(), newStream);

    // add entry in dictionnaries
    idToClientId.put(newStream.getId(), newStream.getClientId());
    clientIdToId.put(newStream.getClientId(), newStream.getId());

    connectionCallback.onStreamsSuccess("StreamSupervisor: Stream (id="
      + newStream.getId()
        + ", clientId="
        + newStream.getClientId()
        + ", name="
        + newStream.getName()
        + ") added ");

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
   * @param streamClientId
   *          the client Id of the Stream to delete.
   * @param mergeWithParent
   *          true if the deleted Stream's children Streams and Events are to be
   *          merged with the parent Stream
   * @param connectionSCallback
   *          callback for the Stream deletion
   */
  public void deleteStream(String streamClientId, boolean mergeWithParent,
    StreamsCallback connectionSCallback) {
    Stream streamToDelete = getStreamByClientId(streamClientId);
    if (streamToDelete != null) {
      if (streamToDelete.getTrashed() == true) {
        // delete really

        // delete from parent stream
        Stream parentStream = getStreamByClientId(streamToDelete.getParentClientId());
        parentStream.removeChildStream(streamToDelete);

        // delete Stream's children Streams
        if (streamToDelete.getChildren() != null) {
          for (String childStream : streamToDelete.getChildrenMap().keySet()) {
            deleteStream(childStream, mergeWithParent, connectionSCallback);
          }
        }
        // delete Stream
        rootStreams.remove(streamToDelete.getClientId());
        flatStreams.remove(streamToDelete.getClientId());
        idToClientId.remove(streamToDelete.getId());
        clientIdToId.remove(streamToDelete.getClientId());
        streamToDelete = null;
      } else {
        // update trashed field of stream to delete and its child streams
        streamToDelete.setTrashed(true);
        for (Stream childstream : streamToDelete.getChildren()) {
          childstream.setTrashed(true);
        }
        for (Stream childstream : streamToDelete.getChildrenMap().values()) {
          childstream.setTrashed(true);
        }
      }
      connectionSCallback.onStreamsSuccess("Stream with id=" + streamClientId + " deleted.");
    } else {
      // streamToDelete not found
      connectionSCallback.onStreamError("Stream with id=" + streamClientId + " not found.");
    }
  }

  /**
   * Find out if Stream with clientId="childClientId" is a descendant of Stream
   * with clientId="parentClientId"
   *
   * @param childClientId
   * @param parentClientId
   * @return
   */
  public boolean verifyParency(String childClientId, String parentClientId) {
    if (getStreamByClientId(parentClientId) != null) {
      if (getStreamByClientId(parentClientId).getChildren() != null) {
        Set<String> children = new HashSet<String>();
        computeDescendants(children, parentClientId);
        return children.contains(childClientId);
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
   * @return
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
   * Store the ids of Stream with id "parentId"'s descendants.
   *
   * @param children
   *          the Set<String> in which the Stream Id's will be stored
   * @param parentId
   *          the id of the parent Stream.
   */
  private void computeDescendants(Set<String> children, String parentId) {
    logger.log("StreamSupervisor: computing descendency of stream with id=" + parentId);
    if (getStreamByClientId(parentId).getChildren() != null) {
      logger.log("StreamSupervisor: this Stream has "
        + getStreamByClientId(parentId).getChildren().size()
          + " children in list");
      logger.log("StreamSupervisor: this Stream has "
        + getStreamByClientId(parentId).getChildrenMap().size()
          + " children in map");
      for (Stream childStream : getStreamByClientId(parentId).getChildren()) {
        children.add(childStream.getClientId());
        computeDescendants(children, childStream.getClientId());
      }
    }
  }

}
