package com.pryv.unit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.pryv.api.model.Stream;
import com.pryv.utils.StreamUtils;

/**
 * unitary tests of StreamUtils
 *
 * @author ik
 *
 */
public class StreamUtilsTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testFindReference() {
    String rootId = "root";
    String childId = "child";
    String grandChildId = "grandChild";
    Stream root = new Stream();
    root.setId(rootId);
    Stream child = new Stream();
    child.setId(childId);
    Stream grandChild = new Stream();
    grandChild.setId(grandChildId);
    root.addChildStream(child);
    child.addChildStream(grandChild);
    Map<String, Stream> streams = new HashMap<String, Stream>();
    streams.put(rootId, root);
    assertNotNull(StreamUtils.findStreamReference(rootId, streams));
    assertNotNull(StreamUtils.findStreamReference(childId, streams));
    assertNotNull(StreamUtils.findStreamReference(grandChildId, streams));
    assertNull(StreamUtils.findStreamReference(rootId, root.getChildrenMap()));
    assertNull(StreamUtils.findStreamReference(childId, child.getChildrenMap()));
    assertNull(StreamUtils.findStreamReference(grandChildId, grandChild.getChildrenMap()));
  }

}
