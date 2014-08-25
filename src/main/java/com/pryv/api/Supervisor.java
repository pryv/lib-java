package com.pryv.api;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.pryv.api.model.Event;
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
public class Supervisor {

  private Map<String, Event> events;
  private Map<String, Stream> streams;

  private Logger logger = Logger.getInstance();

  /**
   * Supervisor constructor. Instantiates data structures to store Streams and
   * Events.
   *
   */
  public Supervisor() {
    events = new HashMap<String, Event>();
    streams = new HashMap<String, Stream>();
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
    return streams;
  }

  /**
   * Update or create Streams in Supervisor whether they already exist or not.
   *
   * @param pStreams
   */
  public void updateOrCreateStream(Stream stream, StreamsCallback connectionCallback)
    throws IncompleteFieldsException {
    if (areFieldsValid(stream)) {
      Stream oldStream = StreamUtils.findStreamReference(stream.getId(), streams);
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
      throw new IncompleteFieldsException();
    }
  }

  /**
   * Update Stream in Supervisor. The condition on the update is the result of
   * the comparison of the modified fields.
   *
   * @param streamToUpdate
   * @throws IncompleteFieldsException
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
        streams.put(streamToUpdate.getId(), streamToUpdate);
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
    Stream newParent = StreamUtils.findStreamReference(parentId, streams);
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
    Stream oldParent = StreamUtils.findStreamReference(parentId, streams);
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
   * @param stream
   *          the stream to add
   */
  private void addStream(Stream stream) {
    logger.log("Stream added: " + stream.getId());
    streams.put(stream.getId(), stream);
  }

  /**
   * Delete Stream from Supervisor, if trashed is false, sets it to true, else
   * deletes it.
   *
   * @param streamId
   *          the Id of the Stream to delete.
   * @param connectionSCallback
   *          callback for the Stream deletion
   */
  public void deleteStream(String streamId, StreamsCallback connectionSCallback) {
    Stream streamToDelete = StreamUtils.findStreamReference(streamId, streams);
    if (streamToDelete != null) {
      if (streamToDelete.getTrashed() == true) {
        // delete really

        // delete from parent stream
        Stream parentStream =
          StreamUtils.findStreamReference(streamToDelete.getParentId(), streams);
        parentStream.removeChildStream(streamToDelete);

        // delete Stream's events
        for (Event event : events.values()) {
          if (event.getStreamId().equals(streamId)) {
            deleteEvent(event, null);
          }
        }
        // delete Stream's children Streams
        if (streamToDelete.getChildren() != null) {
          for (String childStream : streamToDelete.getChildrenMap().keySet()) {
            deleteStream(childStream, connectionSCallback);
          }
        }
        // delete Stream
        streamToDelete = null;
      } else {
        // update trashed field of stream to delete and its child streams
        streamToDelete.setTrashed(true);
        for (Stream childstream : streamToDelete.getChildren()) {
          childstream.setTrashed(true);
          for (Event event : events.values()) {
            if (event.getStreamId().equals(streamId)) {
              event.setTrashed(true);
            }
          }
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

  /*
   * Events Management
   */

  /**
   * Returns the events matching the provided filter.
   *
   * @param filter
   *          the filter object used to filter the Events.
   * @return returns the events matching the filter or an empty Map<String,
   *         Event>.
   */
  public void getEvents(Filter filter, EventsCallback connectionCallback) {
    Map<String, Event> returnEvents = new HashMap<String, Event>();

    for (Event event : events.values()) {
      if (filter.match(event)) {
        returnEvents.put(event.getId(), event);
        logger.log("Supervisor: matched: streamName="
        // + streams.get(event.getStreamId()).getName()
          + ", streamId="
            + event.getStreamId()
            + ", id="
            + event.getId());
      }
    }

    // apply limit argument
    if (filter.getLimit() != null) {
      returnEvents.keySet().retainAll(
        ImmutableSet.copyOf(Iterables.limit(returnEvents.keySet(), filter.getLimit())));
    }
    connectionCallback.onSupervisorRetrieveEventsSuccess(returnEvents);
  }

  /**
   * Update or create events in Supervisor whether they already exist or not.
   *
   * @param newEvents
   * @throws IncompleteFieldsException
   */
  public void updateOrCreateEvent(Event newEvent, EventsCallback connectionCallback)
    throws IncompleteFieldsException {
    if (areFieldsValid(newEvent)) {
      // case exists: compare modified field
      if (events.containsKey(newEvent.getId())) {
        updateEvent(newEvent);
        connectionCallback.onEventsSuccess("Event with id="
          + newEvent.getId()
            + " updated successfully.");
      } else {
        addEvent(newEvent);
        connectionCallback.onEventsSuccess("Event with id="
          + newEvent.getId()
            + " added successfully.");
      }
    } else {
      throw new IncompleteFieldsException();
    }
  }

  /**
   * Add Event in Supervisor
   *
   * @param newEvent
   */
  private void addEvent(Event newEvent) {
    logger.log("Supervisor: adding new event: id="
      + newEvent.getId()
        + ", streamId="
        + newEvent.getStreamId());
    events.put(newEvent.getId(), newEvent);
  }

  /**
   * Compare modified field of event with the one stored in the Supervisor to
   * decided wether to replace it or not.
   *
   * @param event
   *          the event that may replace the one in place if newer.
   */
  private void updateEvent(Event event) {
    Event memEvent = events.get(event.getId());
    if (memEvent.getModified() > event.getModified()) {
      // do nothing
    } else {
      logger.log("Supervisor: updating event: id="
        + event.getId()
          + ", streamId="
          + event.getStreamId()
          + ". Old time="
          + memEvent.getTime()
          + ", new Time="
          + event.getTime());
      memEvent.merge(event, JsonConverter.getCloner());
    }
  }

  /**
   * Delete Event from Supervisor, if trashed is false, sets it to true, else
   * deletes it.
   *
   * @param eventToDelete
   *          the Event to delete
   */
  public void deleteEvent(Event eventToDelete, EventsCallback connectionCallback) {
    if (events.get(eventToDelete.getId()) != null) {
      if (events.get(eventToDelete.getId()).getTrashed() == true) {
        // delete really
        events.remove(eventToDelete.getId());
      } else {
        // update trashed field
        eventToDelete.setTrashed(true);
        updateEvent(eventToDelete);
      }
      connectionCallback.onEventsSuccess("Event with id=" + eventToDelete.getId() + " deleted.");
    } else {
      connectionCallback.onEventsError("Event with id=" + eventToDelete.getId() + " not found.");
    }

  }

  /**
   * Returns the Event with eventId id or null if such event does not exist.
   *
   * @param id
   *          the id of the event to be retrieved
   * @return the Event with the requested id or null
   */
  public Event getEventById(String id) {
    return events.get(id);
  }

  /**
   * used to check if input Event have all the required fields not null.
   *
   * @param eventToCheck
   * @return true if all fields are valid, false if any of the mandatory fields
   *         is null or the parameter is null
   */
  private boolean areFieldsValid(Event eventToCheck) {
    if (eventToCheck == null) {
      return false;
    } else {
      return eventToCheck.getId() != null
        && eventToCheck.getStreamId() != null
          && eventToCheck.getCreated() != null
          && eventToCheck.getCreatedBy() != null
          && eventToCheck.getModified() != null
          && eventToCheck.getModifiedBy() != null;
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

  /**
   * custom thrown when user tries to call Supervisor methods with data having
   * mandatory fields as null.
   *
   * @author ik
   *
   */
  public class IncompleteFieldsException extends Exception {

    /**
     * Constructor containing message to display
     *
     * @param message
     */
    public IncompleteFieldsException() {
      super("Supervisor: attempt to Create a Stream or Event with incomplete fields");
    }
  }

}
