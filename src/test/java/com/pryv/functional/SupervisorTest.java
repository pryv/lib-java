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
    Stream parent1 = DummyData.generateFullStream();
    String parent1Id = "parent1id";
    parent1.setId(parent1Id);
    parent1.clearChildren();

    Stream child = DummyData.generateFullStream();
    String childId = "childid";
    child.setId(childId);
    child.setParentId(parent1Id);
    child.clearChildren();
    parent1.addChildStream(child);

    String parent2Id = "parent2id";
    Stream parent2 = DummyData.generateFullStream();
    parent2.setId(parent2Id);
    parent2.clearChildren();

    // insert parent1 & parent2 and child in supervisor

    System.out.println("Test: first: parent1/child, parent2");
    streamsSupervisor.updateOrCreateStream(parent1, streamsCallback);
    assertNotNull(streamsSupervisor.getRootStreams().get(parent1Id));
    streamsSupervisor.updateOrCreateStream(parent2, streamsCallback);
    assertNotNull(streamsSupervisor.getRootStreams().get(parent2Id));
    streamsSupervisor.updateOrCreateStream(child, streamsCallback);
    assertNull(streamsSupervisor.getRootStreams().get(childId));
    assertNotNull(streamsSupervisor.getStreamById(childId));
    assertTrue(streamsSupervisor.verifyParency(childId, parent1Id));
    assertFalse(streamsSupervisor.verifyParency(childId, parent2Id));

    // change random stuff
    System.out.println("Test: second: parent1/child, parent2 - modify child's name");
    String newName = "myChildNewName";
    Stream childUpdate1 = DummyData.generateFullStream();
    childUpdate1.clearChildren();
    childUpdate1.setId(childId);
    childUpdate1.setParentId(parent1Id);

    childUpdate1.setName(newName);
    assertEquals(newName, childUpdate1.getName());
    childUpdate1.setModified(child.getModified() + TIME_INTERVAL);
    streamsSupervisor.updateOrCreateStream(childUpdate1, streamsCallback);

    assertNotNull(streamsSupervisor.getStreamById(childId));
    assertEquals(newName, streamsSupervisor.getStreamById(childId).getName());

    // change parents 1->2
    System.out.println("Test: third: parent1, parent2/child");
    Stream childUpdate2 = DummyData.generateFullStream();
    childUpdate2.clearChildren();
    childUpdate2.setId(childId);
    childUpdate2.setParentId(parent2Id);
    childUpdate2.setModified(child.getModified() + TIME_INTERVAL);
    streamsSupervisor.updateOrCreateStream(childUpdate2, streamsCallback);
    assertFalse(streamsSupervisor.verifyParency(childId, parent1Id));
    assertTrue(streamsSupervisor.verifyParency(childId, parent2Id));

    // orphan child
    System.out.println("Test: fourth: parent1, parent2, child");
    Stream childUpdate3 = DummyData.generateFullStream();
    childUpdate3.clearChildren();
    childUpdate3.setId(childId);
    childUpdate3.setParentId(null);
    childUpdate3.setModified(child.getModified() + TIME_INTERVAL);
    streamsSupervisor.updateOrCreateStream(childUpdate3, streamsCallback);
    assertFalse(streamsSupervisor.verifyParency(childId, parent2Id));
    assertNotNull(streamsSupervisor.getStreamById(childId));

    // random change as orphan
    System.out.println("Test: fifth: parent1, parent2, child - change child's name");
    Stream childUpdate4 = DummyData.generateFullStream();
    childUpdate4.clearChildren();
    childUpdate4.setId(childId);
    String randomName = "randomName";
    childUpdate4.setName(randomName);
    childUpdate4.setModified(child.getModified() + TIME_INTERVAL);
    streamsSupervisor.updateOrCreateStream(childUpdate4, streamsCallback);
    assertEquals(randomName, streamsSupervisor.getStreamById(childId).getName());

    // add it a parent now
    System.out.println("Test: sixth: parent1/child, parent2");
    Stream childUpdate5 = DummyData.generateFullStream();
    childUpdate5.clearChildren();
    childUpdate5.setId(childId);
    childUpdate5.setParentId(parent1Id);
    childUpdate5.setModified(child.getModified() + TIME_INTERVAL);
    streamsSupervisor.updateOrCreateStream(childUpdate5, streamsCallback);
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
