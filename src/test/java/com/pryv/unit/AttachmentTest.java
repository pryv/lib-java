package com.pryv.unit;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.pryv.api.model.Attachment;

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
    assertEquals(filename, attachment.getFileName());
    assertEquals(type, attachment.getType());
    assertEquals(number, attachment.getSize());
    assertEquals(readToken, attachment.getReadToken());
  }

}
