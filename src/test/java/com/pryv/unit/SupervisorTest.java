package com.pryv.unit;

import static org.junit.Assert.assertNotNull;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.pryv.api.Supervisor;
import com.pryv.api.model.Event;

/**
 * tests for Supervisor class data manipulation
 *
 * @author ik
 *
 */
public class SupervisorTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {

  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testCreateEvent() {
    Supervisor sup = new Supervisor();
    Event emptyEvent = new Event();
    String id = "fakeid";
    emptyEvent.setId(id);
    sup.updateOrCreateEvent(emptyEvent);
    assertNotNull(sup.getEventById(id));
  }

}
