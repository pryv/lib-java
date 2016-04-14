package com.pryv.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.pryv.api.EventsCallback;
import com.pryv.api.EventsSupervisor;
import com.pryv.interfaces.StreamsCallback;
import com.pryv.api.StreamsSupervisor;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;
import com.pryv.unit.DummyData;
import com.pryv.utils.Logger;

/**
 * tests for Supervisor class data manipulation
 *
 * @author ik
 *
 */
public class SupervisorTest {

  private static EventsSupervisor eventsSupervisor;
  private static StreamsSupervisor streamsSupervisor;
  private static EventsCallback eventsCallback;
  private static StreamsCallback streamsCallback;
  private static Logger logger = Logger.getInstance();

  private static final long TIME_INTERVAL = 50;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    streamsSupervisor = new StreamsSupervisor();
    eventsSupervisor = new EventsSupervisor(streamsSupervisor);
    instantiateStreamsCallback();
    instantiateEventsCallback();
  }

  @Test
  public void testManipulateEvent() {
    Event testEvent = DummyData.generateFullEvent();
    eventsSupervisor.updateOrCreateEvent(testEvent, eventsCallback);
    assertNotNull(eventsSupervisor.getEventByClientId(testEvent.getClientId()));
  }

  @Test
  public void testManipulateStream() {
    // parent1 parent of child, parent2 has no child
    String parent1Id = "parent1id";
    Stream parent1 = new Stream(parent1Id, "blop");
    parent1.setModified(123.0);

    String childId = "childid";
    Stream child = new Stream(childId, "blip");
    child.setModified(123.0);
    child.setParentId(parent1Id);
    parent1.addChildStream(child);

    String parent2Id = "parent2id";
    Stream parent2 = new Stream(parent2Id, null);
    parent2.setModified(123.0);

    // insert parent1 & parent2 and child in supervisor
    logger.log("Test: first: parent1/child, parent2");
    streamsSupervisor.updateOrCreateStream(parent1, streamsCallback);
    assertNotNull(streamsSupervisor.getRootStreams().get(parent1Id));
    streamsSupervisor.updateOrCreateStream(parent2, streamsCallback);
    assertNotNull(streamsSupervisor.getRootStreams().get(parent2Id));
    streamsSupervisor.updateOrCreateStream(child, streamsCallback);
    assertNull(streamsSupervisor.getRootStreams().get(childId));
    assertNotNull(streamsSupervisor.getStreamById(childId));
    assertTrue(streamsSupervisor.getStreamById(parent1Id).hasChild(childId));
    assertFalse(streamsSupervisor.getStreamById(parent2Id).hasChild(childId));
    logger.log("test1: " + parent1 + ", " + parent2);

    // change random stuff
    logger.log("Test: second: parent1/child, parent2 - modify child's name");
    String newName = "myChildNewName";
    child.setName(newName);
    child.setModified(child.getModified() + TIME_INTERVAL);
    assertNotNull(streamsSupervisor.getStreamById(childId));
    assertEquals(newName, streamsSupervisor.getStreamById(childId).getName());

    // change parents 1->2
    logger.log("Test: third: parent1, parent2/child");
    child.setParentId(parent2Id);
    child.setModified(child.getModified() + TIME_INTERVAL);
    logger.log("test2a: " + parent1 + ", " + parent2);
    streamsSupervisor.updateOrCreateStream(child, streamsCallback);
    logger.log("test2b: " + parent1 + ", " + parent2);
    assertFalse(streamsSupervisor.getStreamById(parent1Id).hasChild(childId));
    assertTrue(streamsSupervisor.getStreamById(parent2Id).hasChild(childId));

    // orphan child
    logger.log("Test: fourth: parent1, parent2, child");
    child.setParentId(null);
    child.setModified(child.getModified() + TIME_INTERVAL);
    logger.log("test3: " + parent1 + ", " + parent2);
    streamsSupervisor.updateOrCreateStream(child, streamsCallback);
    assertFalse(streamsSupervisor.getStreamById(parent2Id).hasChild(childId));
    assertNotNull(streamsSupervisor.getStreamById(childId));

    // random change as orphan
    logger.log("Test: fifth: parent1, parent2, child - change child's name");
    String randomName = "randomName";
    child.setName(randomName);
    child.setModified(child.getModified() + TIME_INTERVAL);
    streamsSupervisor.updateOrCreateStream(child, streamsCallback);
    assertEquals(randomName, streamsSupervisor.getStreamById(childId).getName());

    // add it a parent now
    logger.log("Test: sixth: parent1/child, parent2");
    child.setParentId(parent1Id);
    child.setModified(child.getModified() + TIME_INTERVAL);
    streamsSupervisor.updateOrCreateStream(child, streamsCallback);
    assertTrue(streamsSupervisor.getStreamById(parent1Id).hasChild(childId));
  }

  private static void instantiateEventsCallback() {
    eventsCallback = new EventsCallback() {

      @Override
      public void onEventsRetrievalSuccess(Map<String, Event> onlineEvents, Double serverTime) {
      }

      @Override
      public void onEventsSuccess(String successMessage, Event event, Integer stoppedId,
        Double pServerTime) {
        logger.log(successMessage);
      }

      @Override
      public void onEventsRetrievalError(String errorMessage, Double pServerTime) {
        logger.log(errorMessage);
      }

      @Override
      public void onEventsError(String errorMessage, Double pServerTime) {
        logger.log(errorMessage);
      }
    };
  }

  private static void instantiateStreamsCallback() {
    streamsCallback = new StreamsCallback() {

      @Override
      public void onStreamsSuccess(String successMessage, Stream stream, Double pServerTime) {
        logger.log(successMessage);
      }

      @Override
      public void onStreamsRetrievalError(String errorMessage, Double pServerTime) {
        logger.log(errorMessage);
      }

      @Override
      public void onStreamError(String errorMessage, Double pServerTime) {
        logger.log(errorMessage);
      }

      @Override
      public void onStreamsRetrievalSuccess(Map<String, Stream> onlineStreams, Double serverTime) {
      }

    };
  }
}
