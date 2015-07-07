package com.pryv.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.pryv.api.Filter;
import com.pryv.api.Filter.State;
import com.pryv.api.StreamsSupervisor;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;

/**
 * class used to test the Filter class's methods
 *
 * @author ik
 *
 */
public class FilterTest {

  @Test
  public void testContructor() {
    Double from = 0.0;
    Double to = 100.0;
    Set<String> streamIds = new HashSet<String>();
    Set<String> tags = new HashSet<String>();
    Set<String> types = new HashSet<String>();
    Boolean running = false;
    Boolean sortAscending = false;
    Integer skip = 0;
    Integer limit = 0;
    State state = State.DEFAULT;
    Double modifiedSince = 50.0;
    Filter filter =
      new Filter(from, to, streamIds, tags, types, running, sortAscending, skip, limit, state,
        modifiedSince);
    assertEquals(from, filter.getFromTime());
    assertEquals(to, filter.getToTime());
    assertEquals(streamIds, filter.getStreamIds());
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
    unmatchingFilter.addStreamId("wrongStreamId");
    unmatchingFilter.addTag("wrongTag");
    unmatchingFilter.setToTime(200.0);
    unmatchingFilter.addType("wrongType");
    unmatchingFilter.setState(State.TRASHED);

    Filter matchingFilter = new Filter();
    matchingFilter.addStreamId("testStreamId");
    String rightStreamId = "testStreamId";
    String rightTag = "tag";
    String rightType = "type";
    matchingFilter.addStreamId(rightStreamId);
    matchingFilter.addTag(rightTag);
    matchingFilter.addType(rightType);

    Event testEvent = new Event();
    testEvent.setStreamId(rightStreamId);
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
    testFilter.addStreamId("testStreamId");
    testFilter.addTag("tag");
    testFilter.setToTime(200.0);
    testFilter.addType("unit");
    testFilter.setState(State.ALL);
    String urlFormat =
      "&fromTime=100.0&toTime=200.0&streams[]=testStreamId&tags[]=tag&types[]=unit&running=false&sortAscending=false&skip=0&limit=100&state=ALL&modifiedSince=150.0";
    assertEquals(urlFormat, testFilter.toUrlParameters());
  }

  @Test
  public void testAreStreamIdsContainedInScope() {
    StreamsSupervisor supervisor = new StreamsSupervisor();
    Stream parent = new Stream("parentId", "parent");
    Stream child = new Stream("childId", "child");
    parent.addChildStream(child);
    supervisor.updateOrCreateStream(parent, null);
    Filter testFilter = new Filter();
    testFilter.addStreamId(child.getId());
    Set<String> scope = new HashSet<String>();
    scope.add(parent.getId());
    assertTrue(testFilter.areStreamIdsContainedInScope(scope, supervisor));

    testFilter.setStreamIds(null);
    testFilter.addStreamId(parent.getId());
    assertTrue(testFilter.areStreamIdsContainedInScope(scope, supervisor));

    Stream other = new Stream("otherId", "other");
    supervisor.updateOrCreateStream(other, null);
    scope.clear();
    scope.add(other.getId());
    assertFalse(testFilter.areStreamIdsContainedInScope(scope, supervisor));
  }

}
