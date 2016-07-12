package com.pryv.unit;

import com.pryv.model.Attachment;
import com.pryv.model.Event;
import com.pryv.util.TestUtils;
import com.pryv.utils.JsonConverter;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
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

  private String id = "test-id";
  private String streamId = "test-stream-id";
  private String type = "note/txt";
  private String content = "yolo";

  @Before
  public void setUp() throws Exception {
    testEvent = DummyData.generateFullEvent();
    jsonEvent = DummyData.generateJsonEvent();
  }

  @Test
  public void testEmptyConstructor() {
    Event event = new Event();

    event.setStreamId(streamId);
    event.setType(type);
    event.setContent(content);
    assertNotNull(event.getId());
    assertTrue(event.getId().matches("^c[a-z0-9-]{24}$"));
    assertEquals(streamId, event.getStreamId());
    assertEquals(type, event.getType());
    assertEquals(content, event.getContent());
  }

  @Test
  public void testMinimalConstructor() {
    Event event = new Event(streamId, type, content);
    // TODO verify that id field exists and that it fits the id regex
    assertEquals(streamId, event.getStreamId());
    assertEquals(type, event.getType());
    assertEquals(content, event.getContent());
  }

  @Test
  public void testFullConstructor() {
    TestUtils.checkEvent(testEvent, testEvent);
  }


  public void testCreateOrReuse() {

  }

  // TODO test remaining methods

  //@Test
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
    String clientId = "clientId";
    String content = "blablabla";
    Double created = 123.0;
    String createdBy = "bob";
    String description = "testDescription";
    Double duration = 500.0;
    Double modified = 155.0;
    String modifiedBy = "bill";
    String ref1 = "ref1";
    Set<String> refs = new HashSet<String>();
    refs.add(ref1);
    String tag1 = "tag1";
    String tag2 = "tag2";
    Set<String> tags = new HashSet<String>();
    tags.add(tag1);
    tags.add(tag2);
    Double time = 1234.0;
    Boolean trashed = false;
    String eventType = "myType";
    testEvent =
      new Event(clientId, eventId, streamId, time, duration, eventType, content, tags, refs,
        description, attachments, clientData, trashed, created, createdBy, modified, modifiedBy);
    Event mergeDestination = new Event();
    mergeDestination.merge(testEvent, JsonConverter.getCloner());
    assertEquals(eventId, mergeDestination.getId());
    assertEquals(clientId, mergeDestination.getClientId());
    assertEquals(streamId, mergeDestination.getStreamId());
    assertEquals(time, mergeDestination.getTime());
    assertEquals(duration, mergeDestination.getDuration());
    assertEquals(eventType, mergeDestination.getType());
    assertEquals(content, mergeDestination.getContent());
    assertTrue(areStringSetsEqualInContent(tags, mergeDestination.getTags()));
    assertTrue(areStringSetsEqualInContent(refs, mergeDestination.getReferences()));
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
