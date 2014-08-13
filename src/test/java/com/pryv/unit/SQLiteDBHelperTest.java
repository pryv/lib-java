package com.pryv.unit;

import static org.junit.Assert.fail;

import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.pryv.api.database.SQLiteDBHelper;
import com.pryv.api.model.Event;

/**
 * unit tests for SQLiteDBHelper class
 *
 * @author ik
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SQLiteDBHelperTest {

  private static SQLiteDBHelper db;

  private static Event testEvent;

  @BeforeClass
  public static void setUp() throws Exception {
    testEvent = DummyData.generateFullEvent();
    db = new SQLiteDBHelper();
  }

  @Test
  public void test01InitDB() {
    System.out.println("test1");
  }

  @Test
  public void test02InsertEmpyEvent() {
    System.out.println("test2");
    Event emptyEvent = new Event();
    try {
      db.addEvent(emptyEvent);
      fail("adding empty Event to SQLite database did not throw any Exception!");
    } catch (SQLException e) {
    }
  }

  @Test
  public void test03InsertFullEvent() {
    try {
      db.addEvent(testEvent);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void test04UpdateEvent() {
    testEvent.setStreamId("otherStream");
    try {
      db.updateEvent(testEvent);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void test05RemoveFullEvent() {
    try {
      db.deleteEvent(testEvent);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testRetrieveFullEvent() {
    // db.g
  }

}
