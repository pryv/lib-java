package com.pryv.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.pryv.Filter;
import com.pryv.Filter.State;
import com.pryv.model.Event;
import com.pryv.model.Stream;

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
    Filter filter =
      new Filter(from, to, streams, tags, types, running, sortAscending, skip, limit, state,
        modifiedSince);
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
  }

  @Test
  public void testMatchEvent() {
    Filter unmatchingFilter = new Filter();
    unmatchingFilter.setFromTime(100.0);
    unmatchingFilter.setLimit(100);
    unmatchingFilter.setModifiedSince(150.0);
    unmatchingFilter.setRunning(false);
    unmatchingFilter.setSkip(0);
    unmatchingFilter.setSortAscending(false);
    unmatchingFilter.addStream(new Stream("wrongStreamId", "wrongStreamName"));
    unmatchingFilter.addTag("wrongTag");
    unmatchingFilter.setToTime(200.0);
    unmatchingFilter.addType("wrongType");
    unmatchingFilter.setState(State.TRASHED);

    Filter matchingFilter = new Filter();
    matchingFilter.addStream(new Stream("testStreamId", "someStreamName"));
    Stream rightStream = new Stream("testStreamId","rightStreamName");
    String rightTag = "tag";
    String rightType = "type";
    matchingFilter.addStream(rightStream);
    matchingFilter.addTag(rightTag);
    matchingFilter.addType(rightType);

    Event testEvent = new Event();
    testEvent.setStreamId(rightStream.getId());
    testEvent.setType(rightType);
    testEvent.setContent("testContent");
    testEvent.setTime(125.0);
    testEvent.setModified(145.0);
    testEvent.addTag(rightTag);

    assertFalse(unmatchingFilter.match(testEvent));
    assertTrue(matchingFilter.match(testEvent));
  }

  @Test
  public void testToUrlParameters() {
    Filter testFilter = new Filter();
    testFilter.setFromTime(100.0);
    testFilter.setLimit(100);
    testFilter.setModifiedSince(150.0);
    testFilter.setRunning(false);
    testFilter.setSkip(0);
    testFilter.setSortAscending(false);
    testFilter.addStream(new Stream("testStreamId", "myTestStreamName"));
    testFilter.addTag("tag");
    testFilter.setToTime(200.0);
    testFilter.addType("unit");
    testFilter.setState(State.ALL);
    String urlFormat =
      "&fromTime=100.0&toTime=200.0&streams[]=testStreamId&tags[]=tag&types[]=unit&running=false&sortAscending=false&skip=0&limit=100&state=ALL&modifiedSince=150.0";
    System.out.println("expecting:\t\t" + urlFormat);
    System.out.println("received:\t\t" + testFilter.toUrlParameters());
    assertEquals(urlFormat, testFilter.toUrlParameters());
  }

  @Test
  public void testAreStreamIdsContainedInScope() {
    Stream parent = new Stream("parentId", "parentName");
    Stream child = new Stream("childId", "childName");
    Stream grandChild = new Stream("grandChildId", "grandChildName");
    parent.addChildStream(child);
    child.addChildStream(grandChild);

    Filter scope = new Filter();
    scope.addStream(parent);

    Filter testFilter = new Filter();
    testFilter.addStream(grandChild);
    assertTrue(testFilter.isIncludedInScope(scope));

    testFilter = new Filter();
    testFilter.addStream((child));
    assertTrue(testFilter.isIncludedInScope(scope));

    testFilter = new Filter();
    testFilter.addStream(parent);
    assertTrue(testFilter.isIncludedInScope(scope));

    testFilter = new Filter();
    testFilter.addStream(new Stream("outSideStreamId", "outsideStreamName"));
    assertFalse(testFilter.isIncludedInScope(scope));

    scope = new Filter();
    assertTrue(testFilter.isIncludedInScope(scope));
  }

}
