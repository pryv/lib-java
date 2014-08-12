package com.pryv.unit;

import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.pryv.api.database.SQLiteDBHelper;
import com.pryv.api.model.Attachment;
import com.pryv.api.model.Event;

/**
 * unit tests for SQLiteDBHelper class
 *
 * @author ik
 *
 */
public class SQLiteDBHelperTest {

  private final String id = "testID";
  private final String streamID = "testStreamID";
  private final Long time = new Long(10000);
  private final Long duration = new Long(20); // opt
  private final String type = "testType";
  private final String content = "testContent its a string"; // opt
  private Set<String> tags = new HashSet<String>();
  private final String tagTest = "test";
  private final String tagBasicTest = "basic test";
  private final Set<String> references = new HashSet<String>();
  private final String testReference = "refTest";
  private final String description = "the test description";
  private final Set<Attachment> attachments = new HashSet<Attachment>();
  private final Map<String, Object> clientData = new HashMap<String, Object>();
  private final String clientKey = "color";
  private final String clientValue = "value";
  private final Boolean trashed = false;
  private final Long created = new Long(10);
  private final String createdBy = "Bob";
  private final Long modified = new Long(50);
  private final String modifiedBy = "Tom";
  private final String attachmentID = "abc";

  private Event testEvent;

  private SQLiteDBHelper db;

  @Before
  public void setUp() throws Exception {
    initTestEvent();
    db = new SQLiteDBHelper();
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
  public void testRemoveFullEvent() {
    try {
      db.deleteEvent(testEvent);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private void initTestEvent() {
    tags.add(tagTest);
    tags.add(tagBasicTest);
    references.add(testReference);
    attachments.add(new Attachment(attachmentID, "testfile", "test", 0, "abc132"));
    clientData.put(clientKey, clientValue);
    testEvent =
      new Event(null, streamID, time, duration, type, content, tags, references, description,
        attachments, clientData, trashed, created, createdBy, modified, modifiedBy, null);
    testEvent.setId(id);
  }

}
