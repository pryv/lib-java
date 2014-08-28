package com.pryv.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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

    Stream child = DummyData.generateFullStream();
    String childId = "childStreamId";
    child.setId(childId);
    child.setParentId(parent1Id);
    parent1.addChildStream(child);

    String parent2Id = "parent2";
    Stream parent2 = DummyData.generateFullStream();
    parent2.setId(parent2Id);

    // insert parent1 & parent2 in supervisor

    streams.updateOrCreateStream(parent1, callback);
    streams.updateOrCreateStream(parent2, callback);
    assertNotNull(StreamUtils.findStreamReference(parent1Id, streams.getRootStreams()));
    assertNotNull(StreamUtils.findStreamReference(childId, streams.getRootStreams()));

    // change random stuff
    String newName = "myChildNewName";
    Stream childUpdate3 = DummyData.generateFullStream();
    childUpdate3.setId(childId);
    childUpdate3.setParentId(parent1Id);
    childUpdate3.setName(newName);
    childUpdate3.setModified(child.getModified() + TIME_INTERVAL);
    streams.updateOrCreateStream(childUpdate3, callback);

    assertNotNull(StreamUtils.findStreamReference(childId, streams.getRootStreams()));
    assertEquals(newName, StreamUtils.findStreamReference(childId, streams.getRootStreams()).getName());

    // change parents
    Stream childUpdate1 = DummyData.generateFullStream();
    childUpdate1.setId(childId);
    childUpdate1.setParentId(parent2Id);
    childUpdate1.setModified(child.getModified() + TIME_INTERVAL);
    streams.updateOrCreateStream(childUpdate1, callback);
    assertNull(StreamUtils.findStreamReference(childId, parent1.getChildrenMap()));
    assertNotNull(StreamUtils.findStreamReference(childId, parent2.getChildrenMap()));

    // orphan child
    Stream childUpdate2 = DummyData.generateFullStream();
    childUpdate2.setId(childId);
    childUpdate2.setParentId(null);
    childUpdate2.setModified(child.getModified() + TIME_INTERVAL);

    streams.updateOrCreateStream(childUpdate2, callback);
    assertNull(StreamUtils.findStreamReference(childId, parent1.getChildrenMap()));
    assertNotNull(streams.getStreamById(childId));

    // random change as orphan
    Stream childUpdate5 = DummyData.generateFullStream();
    childUpdate5.setId(childId);
    String randomName = "randomName";
    childUpdate5.setName(randomName);
    childUpdate5.setModified(child.getModified() + TIME_INTERVAL);
    streams.updateOrCreateStream(childUpdate5, callback);
    assertEquals(randomName, streams.getStreamById(childId).getName());

    // add it a parent now
    Stream childUpdate4 = DummyData.generateFullStream();
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
