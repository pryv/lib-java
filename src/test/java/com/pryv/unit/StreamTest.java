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

import com.pryv.dataStructures.JsonFields;
import com.pryv.dataStructures.Stream;

/**
 * Unit tests for Stream class params and functions
 *
 * @author ik
 *
 */
public class StreamTest {

  private Stream testStream;
  private JSONObject jsonStream;
  private final String id = "abc";
  private final String name = "testStream";
  private final String parentId = "ABC";
  private final Boolean singleActivity = true;
  private final Map<String, String> clientData = new HashMap<String, String>();
  private final String clientKey = "testKey";
  private final String clientValue = "testValue";
  private final List<Stream> children = new ArrayList<Stream>();
  private final Boolean trashed = false;
  private final long created = 1000;
  private final String createdBy = "Bob";
  private final long modified = 1500;
  private final String modifiedBy = "Bill";
  private final String childID = "aaa";

  @Before
  public void setUp() throws Exception {
    clientData.put(clientKey, clientValue);
    children.add(new Stream(childID, null, null, null, null, null, false, 0, null, 0, null));
    testStream =
        new Stream(id, name, parentId, singleActivity, clientData, children, trashed, created,
            createdBy, modified, modifiedBy);
    jsonStream = new JSONObject(testStream.toJson());
  }

  @Test
  public void testCreateStreamWithParams() {
    assertNotNull(testStream);
    checkStreamParams(testStream);
  }

  @Test
  public void testCreateJsonFromStream() {
    System.out.println("stream: " + jsonStream);
    assertEquals(id, jsonStream.get(JsonFields.ID.toString()));
    assertEquals(name, jsonStream.get(JsonFields.NAME.toString()));
    assertEquals(parentId, jsonStream.get(JsonFields.PARENT_ID.toString()));
    assertEquals(singleActivity, jsonStream.get(JsonFields.SINGLE_ACTIVITY.toString()));
    JSONObject cData = (JSONObject) jsonStream.get(JsonFields.CLIENT_DATA.toString());
    assertTrue(cData.has(clientKey));
    assertEquals(clientValue, cData.get(clientKey));
    assertEquals(trashed, jsonStream.get(JsonFields.TRASHED.toString()));
    assertEquals(created, jsonStream.getLong(JsonFields.CREATED.toString()));
    assertEquals(createdBy, jsonStream.get(JsonFields.CREATED_BY.toString()));
    assertEquals(modified, jsonStream.getLong(JsonFields.MODIFIED.toString()));
    assertEquals(modifiedBy, jsonStream.get(JsonFields.MODIFIED_BY.toString()));
    JSONArray jsonChildren = jsonStream.getJSONArray(JsonFields.CHILDREN.toString());
    System.out.println("children: " + jsonChildren);
    List<Stream> childs = new ArrayList<Stream>();
    for (int i = 0; i < jsonChildren.length(); i++) {
      System.out.println("child-" + i + ": " + jsonChildren.getJSONObject(i).toString());
      childs.add(new Stream(jsonChildren.getJSONObject(i).toString()));
    }
  }

  @Test
  public void testCreateStreamFromJson() {
    Stream testStreamFromJson = new Stream(jsonStream.toString());
    checkStreamParams(testStreamFromJson);
  }

  private void checkStreamParams(Stream pStream) {
    assertEquals(id, pStream.getId());
    assertEquals(name, pStream.getName());
    assertEquals(parentId, pStream.getParentId());
    assertEquals(singleActivity, pStream.getSingleActivity());
    assertEquals(clientData, pStream.getClientData());
    for (Stream child : pStream.getChildren()) {
      assertEquals(childID, child.getId());
    }
    assertEquals(trashed, pStream.getTrashed());
    assertEquals(created, pStream.getCreated());
    assertEquals(createdBy, pStream.getCreatedBy());
    assertEquals(modified, pStream.getModified());
    assertEquals(modifiedBy, pStream.getModifiedBy());
  }

}
