package com.pryv.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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



  private ObjectMapper mapper = new ObjectMapper();

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
      new Event(null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        null, null, null);
    assertNotNull(emptyEvent);
  }

  // @Test
  // public void testCreateEventFromEmptyJson() {
  // Event emptyJsonEvent = new Event("{}");
  // assertNotNull(emptyJsonEvent);
  // }

  @Test
  public void testCreateEventWithFields() {
    checkEventParams(testEvent);
  }

  private void checkEventParams(Event pEvent) {
    assertEquals(DummyData.getId(), pEvent.getId());
    assertEquals(DummyData.getStreamid(), pEvent.getStreamId());
    assertEquals(DummyData.getTime(), pEvent.getTime());
    assertEquals(DummyData.getType(), pEvent.getType());
    assertTrue("tags test failed", pEvent.getTags().contains(DummyData.getTagbasic()));
    assertEquals(DummyData.getDuration(), pEvent.getDuration());
    assertEquals(DummyData.getContent(), pEvent.getContent());
    assertTrue("refs test failed", pEvent.getReferences().contains(DummyData.getRef()));
    assertEquals(DummyData.getDescription(), pEvent.getDescription());
    for (Attachment attachment : pEvent.getAttachments()) {
      assertEquals(DummyData.getAttachId(), attachment.getId());
    }
    assertEquals(DummyData.getClientdata(), pEvent.getClientData());
    assertEquals(DummyData.getTrashed(), pEvent.getTrashed());
    assertEquals(DummyData.getCreated(), pEvent.getCreated());
    assertEquals(DummyData.getCreatedby(), pEvent.getCreatedBy());
    assertEquals(DummyData.getModified(), pEvent.getModified());
    assertEquals(DummyData.getModifiedBy(), pEvent.getModifiedBy());
  }

  @Test
  public void testMergeEvent() {
    Event eventToUpdate =
      new Event(null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        null, null, null);
    Event baseEventRef = eventToUpdate;
    try {
      JsonConverter.resetEventFromJson(DummyData.generateJsonEvent(), eventToUpdate);

      assertTrue(eventToUpdate == baseEventRef);
      assertTrue(testEvent.getId().equals(eventToUpdate.getId()));
      assertTrue(testEvent.getStreamId().equals(eventToUpdate.getStreamId()));
      assertTrue(testEvent.getTime().equals(eventToUpdate.getTime()));
      assertTrue(testEvent.getDuration().equals(eventToUpdate.getDuration()));
      assertTrue(testEvent.getType().equals(eventToUpdate.getType()));
      assertFalse(testEvent.getType() == eventToUpdate.getType());
      assertTrue(testEvent.getContent().equals(eventToUpdate.getContent()));
      assertFalse(testEvent.getContent() == eventToUpdate.getContent());
      // for (String tag : eventToUpdate.getTags()) {
      // for (String tag2 : testEvent.getTags()) {
      // assertTrue(tag.equals(tag2));
      // assertFalse(tag == tag2);
      // }
      // }
      // for (String ref : eventToUpdate.getReferences()) {
      // for (String ref2 : testEvent.getReferences()) {
      // assertTrue(ref.equals(ref2));
      // assertFalse(ref == ref2);
      // }
      // }
      // assertTrue(testEvent.getDescription().equals(eventToUpdate.getDescription()));
      // assertFalse(testEvent.getDescription() ==
      // eventToUpdate.getDescription());
      // for(Attachment attach: attachments) {
      // assertFalse()
      // }
      // for (int i = 0; i < attachments.size(); i++) {
      // assertFalse(testEvent.getAttachments().get(i) ==
      // eventToUpdate.getAttachments().get(i));
      // }

    } catch (JsonParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (JsonMappingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  @Test
  public void testCreateEventFromJson() {
    // Event eventFromJson = gson.fromJson(jsonTestEvent, Event.class);
    Event eventFromJson = new Event();
    try {
      JsonConverter.resetEventFromJson(DummyData.generateJsonEvent(), eventFromJson);
    } catch (JsonParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (JsonMappingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    checkEventParams(eventFromJson);
  }

}
