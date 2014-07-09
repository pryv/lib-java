package com.pryv.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.pryv.dataStructures.Attachment;
import com.pryv.dataStructures.Event;
import com.pryv.dataStructures.JsonFields;

/**
 * unit test for Event class methods
 *
 * @author ik
 *
 */
public class EventTest {

  private final String id = "testID";
  private final String streamID = "testStreamID";
  private final long time = 10000;
  private final long duration = 20; // opt
  private final String type = "testType";
  private final Object content = "testContent it's a string"; // opt
  private List<String> tags = new ArrayList<String>();
  private final String tagTest = "test";
  private final String tagBasicTest = "basic test";
  private final List<String> references = new ArrayList<String>();
  private final String testReference = "refTest";
  private final String description = "the test description";
  private final List<Attachment> attachments = new ArrayList<Attachment>();
  private final Map<String, String> clientData = new HashMap<String, String>();
  private final String clientKey = "color";
  private final String clientValue = "value";
  private final Boolean trashed = false;
  private final long created = 10;
  private final String createdBy = "Bob";
  private final long modified = 50;
  private final String modifiedBy = "Tom";
  private final String attachmentID = "abc";

  private Event testEvent;
  private JSONObject jsonTestEvent;

  @Before
  public void setUp() throws Exception {
    tags.add(tagTest);
    tags.add(tagBasicTest);
    references.add(testReference);
    attachments.add(new Attachment(attachmentID, "testfile", "test", 0, "abc132"));
    clientData.put(clientKey, clientValue);
    testEvent =
        new Event(null, streamID, time, duration, type, content, tags, references, description,
            attachments, clientData, trashed, created, createdBy, modified, modifiedBy);
    testEvent.setId(id);
    jsonTestEvent = new JSONObject(testEvent.toJson());
  }

  @Test
  public void testCreateEmptyEvent() {
    Event emptyEvent =
        new Event(null, null, 0, 0, null, null, null, null, null, null, null, null, 0, null, 0,
            null);
    assertNotNull(emptyEvent);
  }

  @Test
  public void testCreateEventFromEmptyJson() {
    Event emptyJsonEvent = new Event("{}");
    assertNotNull(emptyJsonEvent);
  }

  @Test
  public void testCreateEventWithFields() {
    checkEventParams(testEvent);
  }

  private void checkEventParams(Event pEvent) {
    assertEquals(id, pEvent.getId());
    assertEquals(streamID, pEvent.getStreamID());
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
  public void testCreateJsonFromEvent() {
    JSONObject jsonEvent = new JSONObject(testEvent.toJson());
    System.out.println("event: " + testEvent.toJson());
    assertEquals(id, jsonEvent.get(JsonFields.ID.toString()));
    assertEquals(type, jsonEvent.get(JsonFields.TYPE.toString()));
    assertEquals(streamID, jsonEvent.get(JsonFields.STREAM_ID.toString()));
    assertEquals(time, jsonEvent.getLong(JsonFields.TIME.toString()));
    assertEquals(duration, jsonEvent.getLong(JsonFields.DURATION.toString()));
    assertEquals(content, jsonEvent.get(JsonFields.CONTENT.toString()));
    assertEquals(description, jsonEvent.get(JsonFields.DESCRIPTION.toString()));
    JSONObject cData = (JSONObject) jsonEvent.get(JsonFields.CLIENT_DATA.toString());
    assertTrue(cData.has(clientKey));
    assertEquals(clientValue, cData.get(clientKey));

    JSONArray jsonAttachments = jsonEvent.getJSONArray(JsonFields.ATTACHMENTS.toString());
    List<Attachment> attachs = new ArrayList<Attachment>();
    for (int i = 0; i < jsonAttachments.length(); i++) {
      attachs.add(new Attachment(jsonAttachments.getJSONObject(i).toString()));
    }

    JSONArray jsonRefsArray = jsonEvent.getJSONArray(JsonFields.REFERENCES.toString());
    List<String> refs = new ArrayList<String>();
    for (int i = 0; i < jsonRefsArray.length(); i++) {
      refs.add(jsonRefsArray.getString(i));
    }
    assertTrue(refs.contains(testReference));

    JSONArray jsonTagsArray = jsonEvent.getJSONArray(JsonFields.TAGS.toString());
    List<String> tagsList = new ArrayList<String>();
    for (int i = 0; i < jsonTagsArray.length(); i++) {
      tagsList.add(jsonTagsArray.getString(i));
    }
    assertTrue("test for " + tagTest + " failed", tagsList.contains(tagTest));
    assertTrue("test for " + tagBasicTest + " failed", tagsList.contains(tagBasicTest));
  }

  @Test
  public void testCreateEventFromJson() {
    Event eventFromJson = new Event(jsonTestEvent.toString());
    checkEventParams(eventFromJson);
  }

}
