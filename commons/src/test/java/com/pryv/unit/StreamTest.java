package com.pryv.unit;

import com.pryv.model.Stream;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for Stream class params and functions
 *
 * @author ik
 */
public class StreamTest {

  private Stream testStream;

  @Before
  public void setUp() throws Exception {
    testStream = DummyData.generateFullStream();
  }

  @Test
  public void testCreateStreamWithParams() {
    assertNotNull(testStream);
    checkStreamParams(testStream);
  }

  @Test
  public void testAddAndRemoveChild() {
    Stream child = new Stream("childId", "childName");
    Stream parent = new Stream("parentId", "parentName")
            .addChildStream(child);
    assertEquals(parent.getId(), child.getParentId());
    parent.removeChildStream(child);
    assertNull(child.getParentId());
    assertNull(parent.getChildren());
    assertNull(parent.getChildrenMap());
  }

  @Test
  public void testIsChildTrue() {
    Stream grandChild = new Stream("grandChildId", null);
    Stream child = new Stream("childId", null)
            .addChildStream(grandChild);
    Stream parent = new Stream("parentId", null)
            .addChildStream(child);
    assertTrue(parent.hasChild(grandChild.getId()));
  }

  @Test
  public void testIsChildFalse() {
    Stream grandChild = new Stream("grandChildId", null);
    Stream child = new Stream("childId", null)
            .addChildStream(grandChild);
    Stream parent = new Stream("parentId", null)
            .addChildStream(child);
    assertFalse(parent.hasChild("blop"));
  }

  @Test
  public void testCloneMutableFields() {
    Stream immutableStream = new Stream();
    immutableStream.setId("immutable");
    immutableStream.setCreated(0.0);
    immutableStream.setCreatedBy("immutable");
    immutableStream.setModified(0.0);
    immutableStream.setModifiedBy("immutable");
    immutableStream.setChildren(new HashSet<Stream>());
    Stream mutableStream = immutableStream.cloneMutableFields();
    assertNull(mutableStream.getId());
    assertNull(mutableStream.getCreated());
    assertNull(mutableStream.getCreatedBy());
    assertNull(mutableStream.getModified());
    assertNull(mutableStream.getModifiedBy());
    assertNull(mutableStream.getChildren());
  }

  private void checkStreamParams(Stream pStream) {
    assertEquals(DummyData.getStreamId(), pStream.getId());
    assertEquals(DummyData.getStreamName(), pStream.getName());
    assertEquals(null, pStream.getParentId());
    assertEquals(DummyData.getStreamSingleActivity(), pStream.isSingleActivity());
    assertEquals(DummyData.getStreamClientData(), pStream.getClientData());
    assertNotNull(pStream.getChildren());
    for (Stream child : pStream.getChildren()) {
      assertEquals(DummyData.getStreamChildId(), child.getId());
    }
    assertEquals(DummyData.getStreamTrashed(), pStream.isTrashed());
    assertEquals(DummyData.getStreamCreated(), pStream.getCreated());
    assertEquals(DummyData.getStreamCreatedBy(), pStream.getCreatedBy());
    assertEquals(DummyData.getStreamModified(), pStream.getModified());
    assertEquals(DummyData.getStreamModifiedBy(), pStream.getModifiedBy());
  }

}
