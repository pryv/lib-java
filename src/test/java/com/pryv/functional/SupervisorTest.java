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
import com.pryv.utils.StreamUtils;

/**
 * tests for Supervisor class data manipulation
 *
 * @author ik
 *
 */
public class SupervisorTest {

  private static EventsSupervisor supervisor;
  private static StreamsSupervisor streams;
  private static StreamsCallback callback;
  private static Logger logger = Logger.getInstance();

  private static final long TIME_INTERVAL = 50;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    streams = new StreamsSupervisor();
    supervisor = new EventsSupervisor(streams);
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
    String parent1Id = "parent1";
    parent1.setId(parent1Id);
    parent1.clearChildren();

    Stream child = DummyData.generateFullStream();
    String childId = "childStreamId";
    child.setId(childId);
    child.setParentId(parent1Id);
    child.clearChildren();

    String parent2Id = "parent2";
    Stream parent2 = DummyData.generateFullStream();
    parent2.setId(parent2Id);
    parent2.clearChildren();

    // insert parent1 & parent2 and child in supervisor

    streams.updateOrCreateStream(parent1, callback);
    assertNotNull(streams.getRootStreams().get(parent1Id));
    streams.updateOrCreateStream(parent2, callback);
    assertNotNull(streams.getRootStreams().get(parent2Id));
    streams.updateOrCreateStream(child, callback);
    assertNull(streams.getRootStreams().get(childId));
    assertNotNull(streams.getStreamById(childId));
    assertTrue(streams.verifyParency(childId, parent1Id));
    assertFalse(streams.verifyParency(childId, parent2Id));

    // change random stuff
    String newName = "myChildNewName";
    Stream childUpdate3 = DummyData.generateFullStream();
    childUpdate3.clearChildren();
    childUpdate3.setId(childId);
    childUpdate3.setParentId(parent1Id);
    childUpdate3.setName(newName);
    assertEquals(newName, childUpdate3.getName());
    childUpdate3.setModified(child.getModified() + TIME_INTERVAL);
    streams.updateOrCreateStream(childUpdate3, callback);

    assertNotNull(streams.getStreamById(childId));
    assertEquals(newName, streams.getStreamById(childId).getName());

    // change parents 1->2
    Stream childUpdate1 = DummyData.generateFullStream();
    childUpdate1.clearChildren();
    childUpdate1.setId(childId);
    childUpdate1.setParentId(parent2Id);
    childUpdate1.setModified(child.getModified() + TIME_INTERVAL);
    streams.updateOrCreateStream(childUpdate1, callback);
    assertFalse(streams.verifyParency(childId, parent1Id));
    assertTrue(streams.verifyParency(childId, parent2Id));

    // orphan child
    Stream childUpdate2 = DummyData.generateFullStream();
    childUpdate2.clearChildren();
    childUpdate2.setId(childId);
    childUpdate2.setParentId(null);
    childUpdate2.setModified(child.getModified() + TIME_INTERVAL);

    streams.updateOrCreateStream(childUpdate2, callback);
    assertFalse(streams.verifyParency(childId, parent2Id));
    assertNotNull(streams.getStreamById(childId));

    // random change as orphan
    Stream childUpdate5 = DummyData.generateFullStream();
    childUpdate5.clearChildren();
    childUpdate5.setId(childId);
    String randomName = "randomName";
    childUpdate5.setName(randomName);
    childUpdate5.setModified(child.getModified() + TIME_INTERVAL);
    streams.updateOrCreateStream(childUpdate5, callback);
    assertEquals(randomName, streams.getStreamById(childId).getName());

    // add it a parent now
    Stream childUpdate4 = DummyData.generateFullStream();
    childUpdate4.clearChildren();
    childUpdate4.setId(childId);
    childUpdate4.setParentId(parent1Id);
    childUpdate4.setModified(child.getModified() + TIME_INTERVAL);
    streams.updateOrCreateStream(childUpdate4, callback);
    assertNotNull(StreamUtils.findStreamReference(childId, parent1.getChildrenMap()));
  }

  private static void instantiateCallback() {
    callback = new StreamsCallback() {

      @Override
      public void onSupervisorRetrieveStreamsSuccess(Map<String, Stream> supervisorStreams) {
        // TODO Auto-generated method stub

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
      public void
        onOnlineRetrieveStreamsSuccess(Map<String, Stream> onlineStreams, long serverTime) {
        // TODO Auto-generated method stub

      }

      @Override
      public void onCacheRetrieveStreamSuccess(Map<String, Stream> cacheStreams) {
        // TODO Auto-generated method stub

      }
    };
  }
}
