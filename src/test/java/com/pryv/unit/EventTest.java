package com.pryv.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.pryv.api.model.Attachment;
import com.pryv.api.model.Event;
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

}
