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
import com.pryv.api.model.Stream;
import com.pryv.utils.JsonConverter;

/**
 * Unit tests for Stream class params and functions
 *
 * @author ik
 *
 */
public class StreamTest {

  private Stream testStream;
  private String jsonStream;
  private Stream testChildStream;

  @Before
  public void setUp() throws Exception {
    testStream = DummyData.generateFullStream();
    for (Stream testChildren : testStream.getChildren()) {
      testChildStream = testChildren;
    }
    jsonStream = DummyData.generateJsonStream();
  }

  @Test
  public void testCreateStreamWithParams() {
    assertNotNull(testStream);
    checkStreamParams(testStream);
  }


  @Test
  public void testCreateStreamFromMerge() {
    Stream streamToUpdate = new Stream();
    Stream baseStreamRef = streamToUpdate;
    try {
      JsonConverter.resetStreamFromJson(jsonStream, streamToUpdate);
      assertTrue(baseStreamRef == streamToUpdate);
      assertTrue(streamToUpdate.getId().equals(testStream.getId()));
      assertTrue(streamToUpdate.getName().equals(testStream.getName()));
      assertTrue(streamToUpdate.getSingleActivity() == testStream.getSingleActivity());
      assertFalse(streamToUpdate.getClientData() == testStream.getClientData());
      for (String key : testStream.getClientData().keySet()) {
        for (String key2 : streamToUpdate.getClientData().keySet()) {
          assertTrue(key.equals(key2));
          assertTrue(testStream.getClientData().get(key)
              .equals(streamToUpdate.getClientData().get(key2)));
        }
      }
      for (int i = 0; i < streamToUpdate.getChildren().size(); i++) {
        assertTrue(streamToUpdate.getChildren().get(i).getId()
            .equals(testStream.getChildren().get(i).getId()));
      }
      assertTrue(streamToUpdate.getTrashed() == testStream.getTrashed());
      assertTrue(streamToUpdate.getCreated().equals(testStream.getCreated()));
      assertTrue(streamToUpdate.getCreatedBy().equals(testStream.getCreatedBy()));
      assertTrue(streamToUpdate.getModified().equals(testStream.getModified()));
      assertTrue(streamToUpdate.getModifiedBy().equals(testStream.getModifiedBy()));
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

  private void checkStreamParams(Stream pStream) {
    assertEquals(DummyData.getStreamId(), pStream.getId());
    assertEquals(DummyData.getStreamName(), pStream.getName());
    assertEquals(null, pStream.getParentId());
    assertEquals(DummyData.getStreamSingleActivity(), pStream.getSingleActivity());
    assertEquals(DummyData.getStreamClientData(), pStream.getClientData());
    for (Stream child : pStream.getChildren()) {
      assertEquals(DummyData.getStreamChildId(), child.getId());
    }
    assertEquals(DummyData.getStreamTrashed(), pStream.getTrashed());
    assertEquals(DummyData.getStreamCreated(), pStream.getCreated());
    assertEquals(DummyData.getStreamCreatedBy(), pStream.getCreatedBy());
    assertEquals(DummyData.getStreamModified(), pStream.getModified());
    assertEquals(DummyData.getStreamModifiedBy(), pStream.getModifiedBy());
  }

}
