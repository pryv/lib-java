package com.pryv.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.pryv.api.EventsCallback;
import com.pryv.api.EventsSupervisor;
import com.pryv.api.StreamsCallback;
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

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testManipulateEvent() {
    Event testEvent = DummyData.generateFullEvent();
    eventsSupervisor.updateOrCreateEvent(testEvent, eventsCallback);
    assertNotNull(eventsSupervisor.getEventByClientId(testEvent.getClientId()));
    Event updateEvent = DummyData.generateFullEvent();
    updateEvent.setDuration(2232.0);
    eventsSupervisor.updateOrCreateEvent(updateEvent, eventsCallback);
  }

  @Test
  public void testManipulateStream() {
    // parent1 parent of child, parent2 has no child
    Stream parent1 = new Stream();
    String parent1Id = "parent1id";
    parent1.setId(parent1Id);
    parent1.setModified(123.0);

    Stream child = new Stream();
    String childId = "childid";
    child.setId(childId);
    child.setModified(123.0);
    child.setParentId(parent1Id);
    parent1.addChildStream(child);

    String parent2Id = "parent2id";
    Stream parent2 = new Stream();
    parent2.setId(parent2Id);
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
    assertTrue(streamsSupervisor.verifyParency(childId, parent1Id));
    assertFalse(streamsSupervisor.verifyParency(childId, parent2Id));
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
    assertFalse(streamsSupervisor.verifyParency(childId, parent1Id));
    assertTrue(streamsSupervisor.verifyParency(childId, parent2Id));

    // orphan child
    logger.log("Test: fourth: parent1, parent2, child");
    // Stream childUpdate3 = DummyData.generateFullStream();
    // childUpdate3.clearChildren();
    // childUpdate3.setId(childId);
    child.setParentId(null);
    child.setModified(child.getModified() + TIME_INTERVAL);
    logger.log("test3: " + parent1 + ", " + parent2);
    streamsSupervisor.updateOrCreateStream(child, streamsCallback);
    assertFalse(streamsSupervisor.verifyParency(childId, parent2Id));
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
    assertTrue(streamsSupervisor.verifyParency(childId, parent1Id));
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
