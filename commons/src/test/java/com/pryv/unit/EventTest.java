package com.pryv.unit;

import com.pryv.model.Event;
import com.pryv.util.TestUtils;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;



/**
 * unit test for Event class methods
 *
 * @author ik
 *
 */
public class EventTest {

  private Event testEvent;

  private static final String ID = "test-id";
  private static final String STREAM_ID = "test-stream-id";
  private static final String TYPE = "note/txt";
  private static final String CONTENT = "yolo";

  private static final String CUID_REGEX = "^c[a-z0-9-]{24}$";

  @Before
  public void setUp() throws Exception {
    testEvent = DummyData.generateFullEvent();
  }

  @Test
  public void testGetDateShouldReturnNullWhenNoTimeIsSet() {
    Event e = new Event();
    assertNull(e.getDate());
  }

  @Test
  public void testGetDateShouldReturnAValueWhenTimeIsSet() {
    Event e = new Event();
    double time = System.currentTimeMillis() / 1000.0;
    e.setTime(time);
    assertEquals(time, e.getDate().getMillis() / 1000.0, 0.0);
  }

  @Test
  public void testSetDateShouldWork() {
    Event e = new Event();
    DateTime date = new DateTime();
    e.setDate(date);
    assertEquals(e.getTime(), date.getMillis() / 1000.0, 0.0);
    assertEquals(e.getDate().getMillis(), date.getMillis());
  }

  @Test
  public void testEmptyConstructor() {
    Event event = new Event();
    event.setStreamId(STREAM_ID);
    event.setType(TYPE);
    event.setContent(CONTENT);
    assertTrue(event.getId().matches(CUID_REGEX));
    assertEquals(STREAM_ID, event.getStreamId());
    assertEquals(TYPE, event.getType());
    assertEquals(CONTENT, event.getContent());
  }

  @Test
  public void testMinimalConstructor() {
    Event event = new Event(STREAM_ID, TYPE, CONTENT);
    assertTrue(event.getId().matches(CUID_REGEX));
    assertEquals(STREAM_ID, event.getStreamId());
    assertEquals(TYPE, event.getType());
    assertEquals(CONTENT, event.getContent());
  }

  @Test
  public void testFullConstructor() {
    TestUtils.checkEvent(testEvent, testEvent);
  }

  /**
   * compare the content of 2 sets of Strings. They must not reference the same
   * object, but must have the same content.
   *
   * @param set1
   * @param set2
   * @return
   */
  private boolean areStringSetsEqualInContent(Set<String> set1, Set<String> set2) {
    if (set1 == set2) {
      return false;
    }
    if (set1.size() != set2.size()) {
      return false;
    }
    int itemToCompareCount = set1.size();
    for (String set1Item : set1) {
      for (String set2Item : set2) {
        if (set1Item.equals(set2Item)) {
          itemToCompareCount--;
        }
      }
    }
    return itemToCompareCount == 0;
  }

}
