package com.pryv.unit;

import com.pryv.model.Filter;
import com.pryv.model.Filter.State;
import com.pryv.model.Event;
import com.pryv.model.Stream;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * class used to test the Filter class's methods
 *
 * @author ik
 */
public class FilterTest {

  @Test
  public void testContructor() {
    Double from = 0.0;
    Double to = 100.0;
    Set<Stream> streams = new HashSet<Stream>();
    Set<String> tags = new HashSet<String>();
    Set<String> types = new HashSet<String>();
    Boolean running = false;
    Boolean sortAscending = false;
    Integer skip = 0;
    Integer limit = 0;
    State state = State.DEFAULT;
    Double modifiedSince = 50.0;
    String parentId = "parentId";
    Boolean includeDeletions = true;
    Double includeDeletionsSince = 50.0;
    Filter filter =
      new Filter(from, to, streams, tags, types, running, sortAscending, skip, limit, state,
        modifiedSince, parentId, includeDeletions, includeDeletionsSince);
    assertEquals(from, filter.getFromTime());
    assertEquals(to, filter.getToTime());
    assertEquals(streams, filter.getStreamIds());
    assertEquals(tags, filter.getTags());
    assertEquals(types, filter.getTypes());
    assertEquals(running, filter.getRunning());
    assertEquals(sortAscending, filter.getSortAscending());
    assertEquals(skip, filter.getSkip());
    assertEquals(limit, filter.getLimit());
    assertEquals(state, filter.getState());
    assertEquals(modifiedSince, filter.getModifiedSince());
    assertEquals(parentId, filter.getParentId());
    assertEquals(includeDeletions, filter.getIncludeDeletions());
    assertEquals(includeDeletionsSince, filter.getIncludeDeletionsSince());
  }

  @Test
  public void testMatchEvent() {
    Filter unmatchingFilter = new Filter()
            .setFromTime(100.0)
            .setLimit(100)
            .setModifiedSince(150.0)
            .setRunning(false)
            .setSkip(0)
            .setSortAscending(false)
            .addStream(new Stream("wrongStreamId", "wrongStreamName"))
            .addTag("wrongTag")
            .setToTime(200.0)
            .addType("wrongType")
            .setState(State.TRASHED);

    Stream rightStream = new Stream("testStreamId","rightStreamName");
    String rightTag = "tag";
    String rightType = "type";
    Filter matchingFilter = new Filter()
            .addStream(new Stream("testStreamId", "someStreamName"))
            .addStream(rightStream)
            .addTag(rightTag)
            .addType(rightType);

    Event testEvent = new Event()
            .setStreamId(rightStream.getId())
            .setType(rightType)
            .setContent("testContent")
            .setTime(125.0)
            .setModified(145.0)
            .addTag(rightTag);

    assertFalse(unmatchingFilter.match(testEvent));
    assertTrue(matchingFilter.match(testEvent));
  }

  @Test
  public void testToUrlParameters() {
    Filter testFilter = new Filter()
            .setFromTime(100.0)
            .setLimit(100)
            .setModifiedSince(150.0)
            .setRunning(false)
            .setSkip(0)
            .setSortAscending(false)
            .addStream(new Stream("testStreamId", "myTestStreamName"))
            .addTag("tag")
            .setToTime(200.0)
            .addType("unit")
            .setState(State.ALL)
            .setParentId("parentId")
            .setIncludeDeletions(true)
            .setIncludeDeletionsSince(50.0);

    String urlFormat =
      "&fromTime=100.0&toTime=200.0&streams[]=testStreamId&tags[]=tag&types[]=unit&running=false&sortAscending=false&skip=0&limit=100&state=all&modifiedSince=150.0&parentId=parentId&includeDeletions=true&includeDeletionsSince=50.0";
    assertEquals(urlFormat, testFilter.toUrlParameters());
  }

  @Test
  public void testAreStreamIdsContainedInScope() {
    Stream grandChild = new Stream("grandChildId", "grandChildName");
    Stream child = new Stream("childId", "childName")
            .addChildStream(grandChild);
    Stream parent = new Stream("parentId", "parentName")
            .addChildStream(child);

    Filter scope = new Filter().addStream(parent);

    Filter testFilter = new Filter().addStream(grandChild);
    assertTrue(testFilter.isIncludedInScope(scope));

    testFilter = new Filter().addStream((child));
    assertTrue(testFilter.isIncludedInScope(scope));

    testFilter = new Filter().addStream(parent);
    assertTrue(testFilter.isIncludedInScope(scope));

    testFilter = new Filter().addStream(new Stream("outSideStreamId", "outsideStreamName"));
    assertFalse(testFilter.isIncludedInScope(scope));

    scope = new Filter();
    assertTrue(testFilter.isIncludedInScope(scope));
  }

}
