package com.pryv.unit;

import static org.junit.Assert.assertEquals;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.pryv.api.model.Attachment;
import com.pryv.api.model.JsonFields;

/**
 * Unit test for Attachment object
 *
 * @author ik
 *
 */
public class AttachmentTest {

  private Attachment attachment;
  private final String id = "123";
  private final String filename = "myTestAttachment";
  private final String type = "testType";
  private final int number = 10;
  private final String readToken = "123abc";

  @Before
  public void setUp() throws Exception {
    attachment = new Attachment(id, filename, type, number, readToken);
  }

  @Test
  public void testCreateAttachmentWithParams() {
    assertEquals(id, attachment.getId());
    assertEquals(filename, attachment.getFilename());
    assertEquals(type, attachment.getType());
    assertEquals(number, attachment.getNumber());
    assertEquals(readToken, attachment.getReadToken());
  }

  @Test
  public void testCreateAttachmentFromJSON() {
    JSONObject jsonAttachment = new JSONObject();
    jsonAttachment.putOpt(JsonFields.ID.toString(), id);
    jsonAttachment.putOpt(JsonFields.FILENAME.toString(), filename);
    jsonAttachment.putOpt(JsonFields.TYPE.toString(), type);
    jsonAttachment.putOpt(JsonFields.NUMBER.toString(), number);
    jsonAttachment.putOpt(JsonFields.READ_TOKEN.toString(), readToken);
    Attachment attachmentFromJson = new Attachment(jsonAttachment.toString());

    assertEquals(id, attachmentFromJson.getId());
    assertEquals(filename, attachmentFromJson.getFilename());
    assertEquals(type, attachmentFromJson.getType());
    assertEquals(number, attachmentFromJson.getNumber());
    assertEquals(readToken, attachmentFromJson.getReadToken());
  }

  @Test
  public void testCreateJsonFromAttachment() {
    JSONObject jsonAttachment = new JSONObject(attachment.toJson());
    // System.out.println(attachment.toJson());
    assertEquals(id, jsonAttachment.get(JsonFields.ID.toString()));
    assertEquals(filename, jsonAttachment.get(JsonFields.FILENAME.toString()));
    assertEquals(type, jsonAttachment.get(JsonFields.TYPE.toString()));
    assertEquals(number, jsonAttachment.get(JsonFields.NUMBER.toString()));
    assertEquals(readToken, jsonAttachment.get(JsonFields.READ_TOKEN.toString()));
  }

}
