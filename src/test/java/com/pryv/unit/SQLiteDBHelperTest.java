package com.pryv.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.pryv.api.Filter;
import com.pryv.api.database.SQLiteDBHelper;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;

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
  public static void beforeClass() throws Exception {
    testEvent = DummyData.generateFullEvent();
    db = new SQLiteDBHelper();
  }

  @Test
  public void test01InsertEmpyEvent() {
    System.out.println("test2");
    Event emptyEvent = new Event();
    try {
      db.addEvent(emptyEvent);
      fail("adding empty Event to SQLite database did not throw any Exception!");
    } catch (SQLException e) {
    }
  }

  @Test
  public void test02InsertFullEvent() {
    try {
      db.addEvent(testEvent);
    } catch (SQLException e) {
      e.printStackTrace();
      fail("fail insert full event");
    }
  }

  @Test
  public void test03UpdateEvent() {
    String newStreamId = "otherStream";
    testEvent.setStreamId(newStreamId);
    try {
      db.updateEvent(testEvent);
      Filter filter = new Filter();
      filter.addStreamId(newStreamId);
      Event retrievedEvent = db.getEvents(filter).get(testEvent.getId());
      if (retrievedEvent != null) {
        assertEquals(newStreamId, retrievedEvent.getStreamId());
        retrievedEvent.publishValues();
      } else {
        fail("fail update event");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void test04RemoveFullEvent() {
    try {
      db.deleteEvent(testEvent);
    } catch (SQLException e) {
      e.printStackTrace();
      fail("fail remove full event");
    }
  }

  @Test
  public void test05RetrieveEventsForFullFilter() {
    Filter filter = DummyData.generateFullFilter();
    try {
      db.getEvents(filter);
    } catch (SQLException e) {
      e.printStackTrace();
      fail("fail retrieve event with full filter");
    }
  }

  @Test
  public void test06RetrieveEventForStateAllFilter() {
    Filter filter = new Filter();
    filter.setState(Filter.State.ALL);
    try {
      db.getEvents(filter);
    } catch (SQLException e) {
      e.printStackTrace();
      fail("fail retrieve event with state=all filter");
    }
  }

  @Test
  public void test07InsertEmptyStream() {
    try {
      db.addStream(new Stream());
      fail("adding empty Event to SQLite database did not throw any Exception!");
    } catch (SQLException e) {
    }
  }

  @Test
  public void test08InsertFullStream() {
    try {
      db.addStream(DummyData.generateFullStream());
    } catch (SQLException e) {
      e.printStackTrace();
      fail("fail insert full stream");
    }
  }

}
