package com.pryv.unit;

import static org.junit.Assert.fail;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import com.pryv.api.database.SQLiteDBHelper;
import com.pryv.api.model.Event;

/**
 * unit tests for SQLiteDBHelper class
 *
 * @author ik
 *
 */
public class SQLiteDBHelperTest {

  private SQLiteDBHelper db;

  private Event testEvent;

  @Before
  public void setUp() throws Exception {
    db = new SQLiteDBHelper();
    testEvent = DummyData.generateFullEvent();
  }

  @Test
  public void testEventsFields() {
    testEvent.publishValues();
  }

  @Test
  public void testCreateEventsTable() {
    try {
      db.createEventsTable();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testInsertEmpyEvent() {
    Event emptyEvent = new Event();
    try {
      db.addEvent(emptyEvent);
      fail("adding empty Event to SQLite database did not throw any Exception!");
    } catch (SQLException e) {
    }
  }

  @Test
  public void testInsertFullEvent() {
    try {
      db.addEvent(testEvent);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testRetrieveFullEvent() {
    // db.g
  }

  @Test
  public void testRemoveFullEvent() {
    try {
      db.deleteEvent(testEvent);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
