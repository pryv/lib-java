package com.pryv.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.pryv.model.Attachment;
import com.pryv.model.Event;
import com.pryv.utils.JsonConverter;

/**
 * unit test for Event class methods
 *
 * @author ik
 *
 */
public class EventTest {

  private Event testEvent;
  private String jsonEvent;

  @Before
  public void setUp() throws Exception {
    testEvent = DummyData.generateFullEvent();
    jsonEvent = DummyData.generateJsonEvent();
  }

  @Test
  public void testCreateEmptyEvent() {
    Event emptyEvent =
      new Event(null, null, null, null, null, null, null, null, null, null, null, null, null,
        null, null,
        null, null, null);
    assertNotNull(emptyEvent);
  }

  @Test
  public void testCreateEventWithFields() {
    checkEventParams(testEvent);
  }

  private void checkEventParams(Event pEvent) {
    assertEquals(testEvent.getId(), pEvent.getId());
    assertEquals(testEvent.getStreamId(), pEvent.getStreamId());
    assertEquals(testEvent.getTime(), pEvent.getTime());
    assertEquals(testEvent.getType(), pEvent.getType());
    for (String tag : pEvent.getTags()) {
      assertTrue(testEvent.getTags().contains(tag));
    }
    assertEquals(testEvent.getDuration(), pEvent.getDuration());
    assertEquals(testEvent.getContent(), pEvent.getContent());
    for (String ref : pEvent.getReferences()) {
      assertTrue(testEvent.getReferences().contains(ref));
    }
    assertEquals(testEvent.getDescription(), pEvent.getDescription());
    Set<String> attachIds = new HashSet<String>();
    for (Attachment attach : testEvent.getAttachments()) {
      attachIds.add(attach.getId());
    }
    for (Attachment testedAttachment : pEvent.getAttachments()) {
      boolean attachmentsMatch = false;
      for (Attachment trueAttachment : testEvent.getAttachments()) {
        if (testedAttachment.getId().equals(trueAttachment.getId())) {
          attachmentsMatch = true;
          assertEquals(trueAttachment.getFileName(), testedAttachment.getFileName());
          assertEquals(trueAttachment.getReadToken(), testedAttachment.getReadToken());
          assertEquals(trueAttachment.getType(), testedAttachment.getType());
          assertTrue(trueAttachment.getSize() == testedAttachment.getSize());
        }
      }
      assertTrue(attachmentsMatch);
    }
    assertEquals(testEvent.formatClientDataAsString(), pEvent.formatClientDataAsString());
    assertEquals(testEvent.isTrashed(), pEvent.isTrashed());
    assertEquals(testEvent.getCreated(), pEvent.getCreated());
    assertEquals(testEvent.getCreatedBy(), pEvent.getCreatedBy());
    assertEquals(testEvent.getModified(), pEvent.getModified());
    assertEquals(testEvent.getModifiedBy(), pEvent.getModifiedBy());
  }

  @Test
  public void testCreateEventFromJson() {
    Event eventFromJson = new Event();
    try {
      JsonConverter.resetEventFromJson(jsonEvent, eventFromJson);
    } catch (JsonParseException e) {
      e.printStackTrace();
    } catch (JsonMappingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    checkEventParams(eventFromJson);
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
        description, attachments, clientData, trashed, created, createdBy, modified, modifiedBy,
        null);
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
