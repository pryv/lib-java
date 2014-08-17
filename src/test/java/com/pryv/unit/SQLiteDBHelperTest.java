package com.pryv.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.Map;

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
  private static Stream testStream;

  @BeforeClass
  public static void beforeClass() {
    testEvent = DummyData.generateFullEvent();
    testStream = DummyData.generateFullStream();
    try {
      db = new SQLiteDBHelper();
    } catch (ClassNotFoundException | SQLException e) {
      e.printStackTrace();
      fail("initialize db fail");
    }
  }

  @Test
  public void test01InsertEmpyEvent() {
    System.out.println("test2");
    Event emptyEvent = new Event();
    try {
      db.createEvent(emptyEvent);
      fail("inserting event without the required fields should throw an exception");
    } catch (SQLException e) {
    }
  }

  @Test
  public void test02InsertFullEvent() {
    try {
      db.createEvent(testEvent);
    } catch (SQLException e) {
      e.printStackTrace();
      fail("fail insert full event");
    }
  }

  @Test
  public void test03UpdateEvent() {
    String newStreamId = "otherStream";
    testEvent.setStreamId(newStreamId);
    testEvent.setModified(testEvent.getModified() + 50);
    try {
      db.updateEvent(testEvent);
      Event modifiedEvent = db.getEvents(null).get(testEvent.getId());
      if (modifiedEvent != null) {
        assertEquals(
          "new sid: " + modifiedEvent.getStreamId() + " should be : " + testEvent.getStreamId(),
          newStreamId, modifiedEvent.getStreamId());
      } else {
        fail("fail update event");
      }
    } catch (SQLException e) {
      fail("update event fail");
      e.printStackTrace();
    }
  }

  @Test
  public void test03UpdateEventIfNewerShouldDoNothing() {
    testEvent.setModified(testEvent.getModified());
    try {
      db.updateEvent(testEvent);
      Event notModifiedEvent = db.getEvents(null).get(testEvent.getId());
      assertEquals(notModifiedEvent.getModified(), testEvent.getModified());
    } catch (SQLException e) {
      fail("fail update if newer");
      e.printStackTrace();
    }
  }

  @Test
  public void test03UpdateEventIfNewerShouldModify() {
    testEvent.setModified(DummyData.getModified() + 50);
    System.out.println("modified value is : " + testEvent.getModified());
    try {
      db.updateEvent(testEvent);
      Event modifiedEvent = db.getEvents(null).get(testEvent.getId());
      assertTrue("new value: "
        + modifiedEvent.getModified()
          + " should be higher than old value: "
          + DummyData.getModified(), modifiedEvent.getModified() > DummyData.getModified());
    } catch (SQLException e) {
      fail("fail update if newer modified");
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
      db.addStream(testStream);
    } catch (SQLException e) {
      e.printStackTrace();
      fail("fail insert full stream");
    }
  }

  @Test
  public void test09UpdateFullStream() {
    testStream.setTrashed(!testStream.getTrashed());
    testStream.setModified(testStream.getModified() + 50);
    try {
      db.updateStream(testStream);
    } catch (SQLException e) {
      e.printStackTrace();
      fail("update stream fail");
    }
  }

  @Test
  public void test10retrieveStreams() {
    try {
      Map<String, Stream> streams = db.getStreams();
      assertTrue(streams.get(testStream.getId()) != null);
      assertTrue(streams.get(testStream.getId()).getChildren().size() != 0);
    } catch (SQLException e) {
      e.printStackTrace();
      fail("retrieve Streams fail");
    }
  }

  @Test
  public void test11RemoveFullStream() {
    try {
      db.deleteStream(testStream);
    } catch (SQLException e) {
      e.printStackTrace();
      fail("fail delete full stream");
    }
  }

  @Test
  public void test12RemoveAllEvents() {
    try {
      for (Event event : db.getEvents(null).values()) {
        db.deleteEvent(event);
      }
    } catch (SQLException e) {
      e.printStackTrace();
      fail("delete events fail");
    }
  }

}
