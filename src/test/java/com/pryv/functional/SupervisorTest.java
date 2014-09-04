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

import com.pryv.api.EventsSupervisor;
import com.pryv.api.StreamsCallback;
import com.pryv.api.StreamsSupervisor;
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
  private static StreamsCallback callback;
  private static Logger logger = Logger.getInstance();

  private static final long TIME_INTERVAL = 50;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    streamsSupervisor = new StreamsSupervisor();
    eventsSupervisor = new EventsSupervisor(streamsSupervisor);
    instantiateCallback();
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testMoveAndDeleteStream() {
    // parent1 parent of child, parent2 has no child
    Stream parent1 = DummyData.generateFullStream();
    String parent1CID = "parent1CID";
    String parent1Id = "parent1id";
    parent1.setId(parent1Id);
    parent1.setClientId(parent1CID);
    parent1.clearChildren();

    Stream child = DummyData.generateFullStream();
    String childClientId = "childStreamCId";
    String childId = "childid";
    child.setId(childId);
    child.setClientId(childClientId);
    child.setParentId(parent1Id);
    child.setParentClientId(parent1CID);
    child.clearChildren();
    parent1.addChildStream(child);

    String parent2ClientId = "parent2CID";
    String parent2Id = "parent2id";
    Stream parent2 = DummyData.generateFullStream();
    parent2.setClientId(parent2ClientId);
    parent2.setId(parent2Id);
    parent2.clearChildren();

    // insert parent1 & parent2 and child in supervisor

    System.out.println("Test: first: parent1/child, parent2");
    streamsSupervisor.updateOrCreateStream(parent1, callback);
    assertNotNull(streamsSupervisor.getRootStreams().get(parent1CID));
    streamsSupervisor.updateOrCreateStream(parent2, callback);
    assertNotNull(streamsSupervisor.getRootStreams().get(parent2ClientId));
    streamsSupervisor.updateOrCreateStream(child, callback);
    assertNull(streamsSupervisor.getRootStreams().get(childClientId));
    assertNotNull(streamsSupervisor.getStreamByClientId(childClientId));
    assertTrue(streamsSupervisor.verifyParency(childClientId, parent1CID));
    assertFalse(streamsSupervisor.verifyParency(childClientId, parent2ClientId));

    // change random stuff
    System.out.println("Test: second: parent1/child, parent2 - modify child's name");
    String newName = "myChildNewName";
    Stream childUpdate1 = DummyData.generateFullStream();
    childUpdate1.clearChildren();
    childUpdate1.setId(childId);
    childUpdate1.setClientId(childClientId);
    childUpdate1.setParentId(parent1Id);
    childUpdate1.setParentClientId(parent1CID);

    childUpdate1.setName(newName);
    assertEquals(newName, childUpdate1.getName());
    childUpdate1.setModified(child.getModified() + TIME_INTERVAL);
    streamsSupervisor.updateOrCreateStream(childUpdate1, callback);

    assertNotNull(streamsSupervisor.getStreamByClientId(childClientId));
    assertEquals(newName, streamsSupervisor.getStreamByClientId(childClientId).getName());

    // change parents 1->2
    System.out.println("Test: third: parent1, parent2/child");
    Stream childUpdate2 = DummyData.generateFullStream();
    childUpdate2.clearChildren();
    childUpdate2.setId(childId);
    childUpdate2.setClientId(childClientId);
    childUpdate2.setParentId(parent2Id);
    childUpdate2.setParentClientId(parent2ClientId);
    childUpdate2.setModified(child.getModified() + TIME_INTERVAL);
    streamsSupervisor.updateOrCreateStream(childUpdate2, callback);
    assertFalse(streamsSupervisor.verifyParency(childClientId, parent1CID));
    assertTrue(streamsSupervisor.verifyParency(childClientId, parent2ClientId));

    // orphan child
    System.out.println("Test: fourth: parent1, parent2, child");
    Stream childUpdate3 = DummyData.generateFullStream();
    childUpdate3.clearChildren();
    childUpdate3.setId(childId);
    childUpdate3.setClientId(childClientId);
    childUpdate3.setParentId(null);
    childUpdate3.setParentClientId(null);
    childUpdate3.setModified(child.getModified() + TIME_INTERVAL);
    streamsSupervisor.updateOrCreateStream(childUpdate3, callback);
    assertFalse(streamsSupervisor.verifyParency(childClientId, parent2ClientId));
    assertNotNull(streamsSupervisor.getStreamByClientId(childClientId));

    // random change as orphan
    System.out.println("Test: fifth: parent1, parent2, child - change child's name");
    Stream childUpdate4 = DummyData.generateFullStream();
    childUpdate4.clearChildren();
    childUpdate4.setClientId(childClientId);
    childUpdate4.setId(childId);
    String randomName = "randomName";
    childUpdate4.setName(randomName);
    childUpdate4.setModified(child.getModified() + TIME_INTERVAL);
    streamsSupervisor.updateOrCreateStream(childUpdate4, callback);
    assertEquals(randomName, streamsSupervisor.getStreamByClientId(childClientId).getName());

    // add it a parent now
    System.out.println("Test: sixth: parent1/child, parent2");
    Stream childUpdate5 = DummyData.generateFullStream();
    childUpdate5.clearChildren();
    childUpdate5.setClientId(childClientId);
    childUpdate5.setId(childId);
    childUpdate5.setParentClientId(parent1CID);
    childUpdate5.setModified(child.getModified() + TIME_INTERVAL);
    streamsSupervisor.updateOrCreateStream(childUpdate5, callback);
    assertTrue(streamsSupervisor.verifyParency(childClientId, parent1CID));
  }

  private static void instantiateCallback() {
    callback = new StreamsCallback() {

      @Override
      public void onSupervisorRetrieveStreamsSuccess(Map<String, Stream> supervisorStreams) {
      }

      @Override
      public void onStreamsSuccess(String successMessage) {
        logger.log(successMessage);
      }

      @Override
      public void onStreamsRetrievalError(String errorMessage) {
        logger.log(errorMessage);
      }

      @Override
      public void onStreamError(String errorMessage) {
        logger.log(errorMessage);
      }

      @Override
      public void onOnlineRetrieveStreamsSuccess(Map<String, Stream> onlineStreams,
        double serverTime) {
      }

      @Override
      public void onCacheRetrieveStreamSuccess(Map<String, Stream> cacheStreams) {
      }
    };
  }
}
