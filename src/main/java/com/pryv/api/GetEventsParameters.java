package com.pryv.api;

import java.util.List;

/**
 * Class containing optional parameters with which getEvents can be called
 *
 * @author ik
 *
 */
public class GetEventsParameters {

  private long fromTime;
  private long toTime;
  private List<String> streams;
  private List<String> tags;
  private List<String> types;
  private Boolean running;
  private Boolean sortAscending;
  private int skip;
  private int limit;
  private State state;
  private long modifiedSince;

  public GetEventsParameters(long from, long to, List<String> pStreams, List<String> pTags,
    List<String> pTypes, Boolean pRunning, Boolean pSortAscending, int pSkip, int pLimit,
    State pState, long pModifiedSince) {
    fromTime = from;
    toTime = to;
    streams = pStreams;
    tags = pTags;
    types = pTypes;
    running = pRunning;
    sortAscending = pSortAscending;
    skip = pSkip;
    limit = pLimit;
    state = pState;
    modifiedSince = pModifiedSince;
  }

  public long getFromTime() {
    return fromTime;
  }

  public long getToTime() {
    return toTime;
  }

  public List<String> getStreams() {
    return streams;
  }

  public List<String> getTags() {
    return tags;
  }

  public List<String> getTypes() {
    return types;
  }

  public Boolean getRunning() {
    return running;
  }

  public Boolean getSortAscending() {
    return sortAscending;
  }

  public int getSkip() {
    return skip;
  }

  public int getLimit() {
    return limit;
  }

  public State getState() {
    return state;
  }

  public long getModifiedSince() {
    return modifiedSince;
  }

  public void setFromTime(long fromTime) {
    this.fromTime = fromTime;
  }

  public void setToTime(long toTime) {
    this.toTime = toTime;
  }

  public void setStreams(List<String> streams) {
    this.streams = streams;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public void setTypes(List<String> types) {
    this.types = types;
  }

  public void setRunning(Boolean running) {
    this.running = running;
  }

  public void setSortAscending(Boolean sortAscending) {
    this.sortAscending = sortAscending;
  }

  public void setSkip(int skip) {
    this.skip = skip;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public void setState(State state) {
    this.state = state;
  }

  public void setModifiedSince(long modifiedSince) {
    this.modifiedSince = modifiedSince;
  }

  /**
   * State parameter
   *
   * @author ik
   *
   */
  private enum State {
    DEFAULT, TRASHED, ALL
  };

}
