package com.pryv.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

  // /**
  // * Streams with no parent stream. the key is the clientId
  // */
  // private Map<String, Stream> rootStreams;
  /**
   * Streams with no parent stream. the key is the id
   */
  private Map<String, Stream> rootStreams;

  // /**
  // * All Streams stored in the Supervisor. the key is the clientId
  // */
  // private Map<String, Stream> flatStreams;
  /**
   * All Streams stored in the Supervisor. the key is the id
   */
  private Map<String, Stream> flatStreams;

  // /**
  // * Map: key=stream.id, value=stream.clientId
  // */
  // private Map<String, String> idToClientId;

  // /**
  // * Map: key=stream.clientId, value=stream.Id
  // */
  // private Map<String, String> clientIdToId;

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
    rootStreams = new HashMap<String, Stream>();
    flatStreams = new HashMap<String, Stream>();
    // idToClientId = new HashMap<String, String>();
    // clientIdToId = new HashMap<String, String>();
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
    return rootStreams;
  }

  // /**
  // * Returns Stream which has the provided client id
  // *
  // * @param streamClientId
  // * the client id of the Stream you are looking for
  // * @return the reference to the Stream
  // */
  // public Stream getStreamByClientId(String streamClientId) {
  // return flatStreams.get(streamClientId);
  // }

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

  // /**
  // * Returns the clientId of the Stream whose id is provided.
  // *
  // * @param id
  // * @return the stream's clientId if it exists, else null
  // */
  // public String getClientId(String id) {
  // return idToClientId.get(id);
  // }

  // /**
  // * Returns the id of the Stream whose clientId is provided
  // *
  // * @param clientId
  // * @return
  // */
  // public String getId(String clientId) {
  // return clientIdToId.get(clientId);
  // }

  // /**
  // * Returns the Map that allows to translate the stream's Id to its client Id
  // *
  // * @return
  // */
  // public Map<String, String> getStreamsIdToClientIdDictionnary() {
  // return idToClientId;
  // }

  // /**
  // * Returns the Map that allows to translate the stream's client id to the
  // * stream's id
  // *
  // * @return
  // */
  // public Map<String, String> getStreamsClientIdToIdDictionnary() {
  // return clientIdToId;
  // }

  /**
   * Update or create stream in Supervisor whether it already exists or not.
   * generates client Id if necessary
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

    Stream oldStream = getStreamById(stream.getId());
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

    // find out if parent has changed:
    if (oldStream.getParentId() == null && streamToUpdate.getParentId() == null) {
      // case 1: was root, still root
      // do nothing
      oldStream.merge(streamToUpdate, false);
    } else if (oldStream.getParentId() == null && streamToUpdate.getParentId() != null) {
      // case 2: was root, now child
      // add it to its parent's children
      oldStream.merge(streamToUpdate, false);
      addChildStreamToParent(streamToUpdate.getParentId(), oldStream, connectionCallback);
      rootStreams.remove(oldStream.getId());
    } else if (oldStream.getId() != null && streamToUpdate.getId() == null) {
      // case 3: was child, now root
      // remove it from old parents children
      String oldParent = oldStream.getParentId();
      oldStream.merge(streamToUpdate, false);
      removeChildStreamFromParent(oldParent, oldStream, connectionCallback);
      // put it in root streams
      rootStreams.put(streamToUpdate.getId(), oldStream);
    } else {
      // case 4: was child, still child
      if (oldStream.getParentId().equals(streamToUpdate.getParentId())) {
        // case 4a: same parent
        // do nothing
        oldStream.merge(streamToUpdate, false);
      } else {
        // case 4b: parent changed
        // remove it from old parent
        // getStreamByClientId(oldStream.getParentClientId()).removeChildStream(oldStream);
        removeChildStreamFromParent(oldStream.getParentId(), oldStream, connectionCallback);
        // add it to new parent
        oldStream.merge(streamToUpdate, false);
        addChildStreamToParent(streamToUpdate.getParentId(), oldStream, connectionCallback);
      }
    }
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
        + " updated.", oldStream);

    // update children streams
    if (streamToUpdate.getChildren() != null) {
      for (Stream childStream : streamToUpdate.getChildren()) {
        updateOrCreateStream(childStream, connectionCallback);
      }
    }
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
      logger.log("StreamsSupervisor: removing "
        + childStream.getId()
          + " from "
          + parentId
          + "'s children.");
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

    // add entry in dictionnaries
    // idToClientId.put(newStream.getId(), newStream.getClientId());
    // clientIdToId.put(newStream.getClientId(), newStream.getId());

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
        + " created.", newStream);
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
            + " deleted.", null);
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
            + " trashed.", streamToDelete);
      }
    } else {
      // streamToDelete not found
      connectionSCallback.onStreamError("StreamsSupervisor: Stream with id="
        + streamId
          + " not found.");
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
      // logger.log("StreamSupervisor: this Stream has "
      // + parentStream.getChildren().size()
      // + " children in list");
      // logger.log("StreamSupervisor: this Stream has "
      // + parentStream.getChildrenMap().size()
      // + " children in map");
      for (Stream childStream : parentStream.getChildren()) {
        children.add(childStream.getId());
        computeDescendants(children, childStream);
      }
    }
  }

}
