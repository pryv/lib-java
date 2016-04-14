package com.pryv.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.junit.Before;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.pryv.model.Attachment;
import com.pryv.utils.JsonConverter;

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

  //@Test
  public void testCreateAttachmentWithParams() {
    assertEquals(id, attachment.getId());
    assertEquals(filename, attachment.getFileName());
    assertEquals(type, attachment.getType());
    assertEquals(number, attachment.getSize());
    assertEquals(readToken, attachment.getReadToken());
  }

  //@Test
  public void testSerializeAndDeserializeSetOfAttachments() {
    Set<Attachment> attachments = DummyData.generateAttachments();
    try {
      String jsonAttachments = JsonConverter.toJson(attachments);
      System.out.println("serialized attachments: " + jsonAttachments);
      assertNotNull(jsonAttachments);

      Set<Attachment> deserializedAttachments =
        JsonConverter.deserializeAttachments(jsonAttachments);

      for (Attachment testedAttachment : deserializedAttachments) {
        boolean attachmentsMatch = false;
        for (Attachment trueAttachment : attachments) {
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
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  //@Test
  public void testDeserializeEmptyAttachments() {
    try {
      Set<Attachment> emptyAttachments = JsonConverter.deserializeAttachments(null);
    } catch (JsonParseException e) {
      e.printStackTrace();
    } catch (JsonMappingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  //@Test
  public void testAddFileToAttachment() {
    Attachment attachment = new Attachment();
    File attachmentFile =
      new File(getClass().getClassLoader().getResource("resources/photo.PNG").getPath());
    attachment.setFile(attachmentFile);
    assertNotNull(attachment.getFile());
    assertTrue(attachment.getFile().length() > 0);
  }
}
