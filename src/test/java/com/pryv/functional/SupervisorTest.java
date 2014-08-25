package com.pryv.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.pryv.api.StreamsCallback;
import com.pryv.api.Supervisor;
import com.pryv.api.Supervisor.IncompleteFieldsException;
import com.pryv.api.model.Event;
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

  private static Supervisor supervisor;
  private static StreamsCallback callback;
  private static Logger logger = Logger.getInstance();

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    supervisor = new Supervisor();
    instantiateCallback();
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testCreateEmptyEvent() {
    Event emptyEvent = new Event();
    String id = "fakeid";
    emptyEvent.setId(id);
    try {
      supervisor.updateOrCreateEvent(emptyEvent, null);
    } catch (IncompleteFieldsException e) {
      assertNotNull(e);
    }
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
    try {
      supervisor.updateOrCreateStream(parent1, callback);
      supervisor.updateOrCreateStream(parent2, callback);
    } catch (IncompleteFieldsException e) {
      fail("insert valid stream fail");
    }
    assertNotNull(StreamUtils.findStreamReference(parent1Id, supervisor.getStreams()));
    assertNotNull(StreamUtils.findStreamReference(childId, supervisor.getStreams()));

    // change random stuff
    String newName = "myChildNewName";
    Stream childUpdate3 = DummyData.generateFullStream();
    childUpdate3.setId(childId);
    childUpdate3.setParentId(parent1Id);
    childUpdate3.setName(newName);
    childUpdate3.setModified(child.getModified() + 50);
    try {
      supervisor.updateOrCreateStream(childUpdate3, callback);
    } catch (IncompleteFieldsException e) {
      fail("insert valid stream fail");
    }
    assertNotNull(StreamUtils.findStreamReference(childId, supervisor.getStreams()));
    assertEquals(newName, StreamUtils.findStreamReference(childId, supervisor.getStreams())
      .getName());

    // change parents
    Stream childUpdate1 = DummyData.generateFullStream();
    childUpdate1.setId(childId);
    childUpdate1.setParentId(parent2Id);
    childUpdate1.setModified(child.getModified() + 50);
    try {
      supervisor.updateOrCreateStream(childUpdate1, callback);
    } catch (IncompleteFieldsException e) {
      fail("insert valid stream fail");
    }
    assertNull(StreamUtils.findStreamReference(childId, parent1.getChildrenMap()));
    assertNotNull(StreamUtils.findStreamReference(childId, parent2.getChildrenMap()));

    // orphan child
    Stream childUpdate2 = DummyData.generateFullStream();
    childUpdate2.setId(childId);
    childUpdate2.setParentId(null);
    childUpdate2.setModified(child.getModified() + 50);
    try {
      supervisor.updateOrCreateStream(childUpdate2, callback);
    } catch (IncompleteFieldsException e) {
      fail("insert valid stream fail");
    }
    assertNull(StreamUtils.findStreamReference(childId, parent1.getChildrenMap()));
    assertNotNull(StreamUtils.findStreamReference(childId, supervisor.getStreams()));

    // random change as orphan
    Stream childUpdate5 = DummyData.generateFullStream();
    childUpdate5.setId(childId);
    String randomName = "randomName";
    childUpdate5.setName(randomName);
    childUpdate5.setModified(child.getModified() + 50);
    try {
      supervisor.updateOrCreateStream(childUpdate5, callback);
    } catch (IncompleteFieldsException e) {
      fail("insert valid stream fail");
    }
    assertEquals(randomName, StreamUtils.findStreamReference(childId, supervisor.getStreams())
      .getName());

    // add it a parent now
    Stream childUpdate4 = DummyData.generateFullStream();
    childUpdate4.setId(childId);
    childUpdate4.setParentId(parent1Id);
    childUpdate4.setModified(child.getModified() + 50);
    try {
      supervisor.updateOrCreateStream(childUpdate4, callback);
    } catch (IncompleteFieldsException e) {
      fail("insert valid stream fail");
    }
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
      public void onOnlineRetrieveStreamsSuccess(Map<String, Stream> onlineStreams) {
        // TODO Auto-generated method stub

      }

      @Override
      public void onCacheRetrieveStreamSuccess(Map<String, Stream> cacheStreams) {
        // TODO Auto-generated method stub

      }
    };
  }
}
