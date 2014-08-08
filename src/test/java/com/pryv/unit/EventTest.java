package com.pryv.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

  private final String id = "testID";
  private final String streamID = "testStreamID";
  private final Long time = new Long(10000);
  private final Long duration = new Long(20); // opt
  private final String type = "testType";
  private final String content = "testContent it's a string"; // opt
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
  private String jsonTestEvent;

  private ObjectMapper mapper = new ObjectMapper();

  @Before
  public void setUp() throws Exception {
    // gson = new Gson();
    tags.add(tagTest);
    tags.add(tagBasicTest);
    references.add(testReference);
    attachments.add(new Attachment(attachmentID, "testfile", "test", 0, "abc132"));
    clientData.put(clientKey, clientValue);
    testEvent =
      new Event(null, streamID, time, duration, type, content, tags, references, description,
        attachments, clientData, trashed, created, createdBy, modified, modifiedBy, null);
    testEvent.setId(id);
    // jsonTestEvent = gson.toJson(testEvent);
    // jsonTestEvent = new JSONObject(testEvent.toJson());
    jsonTestEvent = JsonConverter.toJson(testEvent);
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
    assertEquals(id, pEvent.getId());
    assertEquals(streamID, pEvent.getStreamId());
    assertEquals(time, pEvent.getTime());
    assertEquals(type, pEvent.getType());
    assertTrue("tags test failed", pEvent.getTags().contains(tagTest));
    assertEquals(duration, pEvent.getDuration());
    assertEquals(content, pEvent.getContent());
    assertTrue("refs test failed", pEvent.getReferences().contains(testReference));
    assertEquals(description, pEvent.getDescription());
    for (Attachment attachment : pEvent.getAttachments()) {
      assertEquals(attachmentID, attachment.getId());
    }
    assertEquals(clientData, pEvent.getClientData());
    assertEquals(trashed, pEvent.getTrashed());
    assertEquals(created, pEvent.getCreated());
    assertEquals(createdBy, pEvent.getCreatedBy());
    assertEquals(modified, pEvent.getModified());
    assertEquals(modifiedBy, pEvent.getModifiedBy());
  }

  @Test
  public void testMergeEvent() {
    Event eventToUpdate =
      new Event(null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        null, null, null);
    Event baseEventRef = eventToUpdate;
    try {
      JsonConverter.resetEventFromJson(jsonTestEvent, eventToUpdate);

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
      JsonConverter.resetEventFromJson(jsonTestEvent, eventFromJson);
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
