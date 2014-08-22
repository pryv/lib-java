package com.pryv.functional;

import static org.junit.Assert.assertNotNull;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.pryv.api.Supervisor;
import com.pryv.api.Supervisor.IncompleteFieldsException;
import com.pryv.api.model.Event;

/**
 * tests for Supervisor class data manipulation
 *
 * @author ik
 *
 */
public class SupervisorTest {

  private static Supervisor supervisor;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    supervisor = new Supervisor();
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
      supervisor.updateOrCreateEvent(emptyEvent);
    } catch (IncompleteFieldsException e) {
      assertNotNull(e);
    }
  }

  // @Test
  // public void testMoveStream() {
  // // parent1 parent of child, parent2 has no child
  // Stream parent1 = DummyData.generateFullStream();
  // String parent1Id = "parent1";
  // parent1.setId(parent1Id);
  //
  // Stream child = DummyData.generateFullStream();
  // String childId = "childStreamId";
  // child.setId(childId);
  // child.setParentId(parent1.getId());
  // parent1.addChildStream(child);
  //
  // String parent2Id = "parent2";
  // Stream parent2 = DummyData.generateFullStream();
  // parent2.setId(parent2Id);
  //
  // try {
  // supervisor.updateOrCreateStream(parent1);
  // supervisor.updateOrCreateStream(parent2);
  // } catch (IncompleteFieldsException e) {
  // fail("insert valid stream fail");
  // }
  // assertNotNull(supervisor.getStreams().get(parent1));
  // boolean childOfParent1 = false;
  // for (Stream childStream :
  // supervisor.getStreams().get(parent1Id).getChildren()) {
  // if (childStream.getId().equals(childId)) {
  // childOfParent1 = true;
  // }
  // }
  // assertTrue(childOfParent1);
  // child.setParentId(parent2Id);
  // try {
  // supervisor.updateOrCreateStream(child);
  // } catch (IncompleteFieldsException e) {
  // fail("insert valid stream fail");
  // }
  // childOfParent1 = false;
  // for (Stream childStream :
  // supervisor.getStreams().get(parent1Id).getChildren()) {
  // if (childStream.getId().equals(childId)) {
  // childOfParent1 = true;
  // }
  // }
  // assertFalse(childOfParent1);
  // }

}
