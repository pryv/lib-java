package com.pryv.unit;

import com.pryv.model.Attachment;
import com.pryv.model.Event;
import com.pryv.util.TestUtils;
import com.pryv.utils.JsonConverter;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
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
  private String jsonEvent;

  private static final String ID = "test-id";
  private static final String STREAM_ID = "test-stream-id";
  private static final String TYPE = "note/txt";
  private static final String CONTENT = "yolo";

  private static final String CUID_REGEX = "^c[a-z0-9-]{24}$";

  @Before
  public void setUp() throws Exception {
    testEvent = DummyData.generateFullEvent();
    jsonEvent = DummyData.generateJsonEvent();
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

  @Test
  public void testMerge() {
    String eventId = "eventId";
    String streamId = "parentId";
    String attachmentId = "aId";
    String filename = "filename";
    String attachmentType = "picture/attached";
    int size = 100;
    String token = "token";
    Attachment attachment = new Attachment(attachmentId, filename, attachmentType, size, token);
    Set<Attachment> attachments = new HashSet<Attachment>();
    attachments.add(attachment);
    Map<String, Object> clientData = new HashMap<String, Object>();
    String key = "key";
    String val = "value";
    clientData.put(key, val);
    String content = "blablabla";
    Double created = 123.0;
    String createdBy = "bob";
    String description = "testDescription";
    Double duration = 500.0;
    Double modified = 155.0;
    String modifiedBy = "bill";
    String ref1 = "ref1";
    String tag1 = "tag1";
    String tag2 = "tag2";
    Set<String> tags = new HashSet<String>();
    tags.add(tag1);
    tags.add(tag2);
    Double time = 1234.0;
    Boolean trashed = false;
    String eventType = "myType";
    testEvent =
      new Event(eventId, streamId, time, duration, eventType, content, tags,
        description, attachments, clientData, trashed, created, createdBy, modified, modifiedBy);
    Event mergeDestination = new Event();
    mergeDestination.merge(testEvent, JsonConverter.getCloner());
    assertEquals(eventId, mergeDestination.getId());
    assertEquals(streamId, mergeDestination.getStreamId());
    assertEquals(time, mergeDestination.getTime());
    assertEquals(duration, mergeDestination.getDuration());
    assertEquals(eventType, mergeDestination.getType());
    assertEquals(content, mergeDestination.getContent());
    assertTrue(areStringSetsEqualInContent(tags, mergeDestination.getTags()));
    assertEquals(description, mergeDestination.getDescription());
    assertNotEquals(attachments, mergeDestination.getAttachments());
    int attachmentsCount = attachments.size();
    for (Attachment att : attachments) {
      for (Attachment mergedAtt : mergeDestination.getAttachments()) {
        if (att.getId().equals(mergedAtt.getId())) {
          attachmentsCount--;
          assertNotEquals(att, mergedAtt);
          assertEquals(att.getFileName(), att.getFileName());
          assertEquals(att.getReadToken(), mergedAtt.getReadToken());
          assertEquals(att.getSize(), mergedAtt.getSize());
          assertEquals(att.getType(), mergedAtt.getType());
        }
      }
    }
    assertEquals(attachmentsCount, 0);
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
