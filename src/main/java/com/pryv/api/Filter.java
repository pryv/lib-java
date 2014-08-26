package com.pryv.api;

import java.util.HashSet;
import java.util.Set;

import com.pryv.api.model.Event;

/**
 * Filter used in Events fetching. All its fields are optional. Either
 * instanciate using full Constructor with 'null' values when fields are unused,
 * or use empty Constructor and add fields using setters.
 *
 * @author ik
 *
 */
public class Filter {

  // keys to filter online requests
  public final static String FROM_TIME_URL_KEY = "fromTime";
  public final static String TO_TIME_URL_KEY = "toTime";
  public final static String STREAMS_URL_KEY = "streams[]";
  public final static String TAGS_URL_KEY = "tags[]";
  public final static String TYPES_URL_KEY = "types[]";
  public final static String RUNNING_URL_KEY = "running";
  public final static String SORT_ASCENDING_URL_KEY = "sortAscending";
  public final static String SKIP_URL_KEY = "skip";
  public final static String LIMIT_URL_KEY = "limit";
  public final static String STATE_URL_KEY = "state";
  public final static String MODIFIED_SINCE_URL_KEY = "modifiedSince";
  public final static String PARENT_ID_URL_KEY = "parentId";

  // filter fields
  private Long fromTime;
  private Long toTime;
  private Set<String> streamIds;
  private Set<String> tags;
  private Set<String> types;
  private Boolean running;
  private Boolean sortAscending;
  private Integer skip;
  private Integer limit;
  private State state;
  private Long modifiedSince;

  /**
   * Filter class constructor. All fields are optional: use null when unused.
   *
   * @param from
   *          from time
   * @param to
   *          to time
   * @param pStreams
   *          stream ids
   * @param pTags
   *          tags
   * @param pTypes
   *          types
   * @param pRunning
   *          running
   * @param pSortAscending
   *          sort ascending
   * @param pSkip
   *          skip how much
   * @param pLimit
   *          how many to get
   * @param pState
   *          which state
   * @param pModifiedSince
   *          since when is it modified
   */
  public Filter(Long from, Long to, Set<String> pStreams, Set<String> pTags, Set<String> pTypes,
    Boolean pRunning, Boolean pSortAscending, Integer pSkip, Integer pLimit, State pState,
    Long pModifiedSince) {
    fromTime = from;
    toTime = to;
    streamIds = pStreams;
    tags = pTags;
    types = pTypes;
    running = pRunning;
    sortAscending = pSortAscending;
    skip = pSkip;
    limit = pLimit;
    state = pState;
    modifiedSince = pModifiedSince;
  }

  /**
   * Empty constructor.
   */
  public Filter() {

  }

  /**
   * Verify if an Event matches this filter.
   *
   * @param event
   *          the tested Event
   * @return
   */
  public Boolean match(Event event) {

    // fromTime
    Boolean fromTimeMatch = true;
    if (fromTime != null) {
      if (fromTime > event.getTime()) {
        fromTimeMatch = false;
      }
    }

    // toTime
    Boolean toTimeMatch = true;
    if (toTime != null) {
      if (toTime < event.getTime()) {
        toTimeMatch = false;
      }
    }

    // streamIds
    Boolean streamIdMatch = true;
    if (streamIds != null) {
      if (!streamIds.contains(event.getStreamId())) {
        streamIdMatch = false;
      }
    }

    // tags
    Boolean tagMatch = true;
    if (tags != null) {
      if (event.getTags() != null) {
        Set<String> intersectSets = new HashSet<String>(tags);
        intersectSets.retainAll(event.getTags());
        if (intersectSets.size() == 0) {
          tagMatch = false;
        }
      }
    }

    // types
    Boolean typesMatch = true;
    if (types != null) {
      if (event.getType() != null) {
        if (!types.contains(event.getType())) {
          typesMatch = false;
        }
      }
    }

    // running
    Boolean runningMatch = true;

    // State
    Boolean stateMatch = true;
    if (state != null) {
      if (event.getTrashed() != null) {
        if (event.getTrashed()
          && state.equals(State.DEFAULT)
            || !event.getTrashed()
            && state.equals(State.TRASHED)) {
          stateMatch = false;

        }
      }
    }

    // modifiedSince
    Boolean modifiedSinceMatch = true;
    if (modifiedSince != null) {
      if (modifiedSince > event.getModified()) {
        modifiedSinceMatch = false;
      }
    }

    return fromTimeMatch
      && toTimeMatch
        && streamIdMatch
        && tagMatch
        && typesMatch
        && runningMatch
        && stateMatch
        && modifiedSinceMatch;
  }

  /**
   * Verify if another filter encompasses this filter
   *
   * @param other
   *          filter that might be encompassing the caller filter
   * @return
   */
  public boolean isIncludedIn(Filter other) {
    return (fromTime > other.fromTime)
      && (toTime < other.toTime)
        && (other.streamIds.containsAll(streamIds))
        && (other.tags.containsAll(tags))
        && (other.types.containsAll(types))
        && (modifiedSince < other.modifiedSince);
  }

  /**
   * Verify if this filter encompasses another filter
   *
   * @param other
   *          filter that might be encompassed by the caller filter
   * @return
   */
  public boolean includes(Filter other) {
    return (fromTime < other.fromTime)
      && (toTime > other.toTime)
        && (streamIds.containsAll(other.streamIds))
        && (tags.containsAll(other.tags))
        && (types.containsAll(other.types))
        && (modifiedSince > other.modifiedSince);
  }

  /**
   * add a specific stream id to the filter
   *
   * @param pStreamId
   */
  public void addStreamId(String pStreamId) {
    if (streamIds == null) {
      streamIds = new HashSet<>();
    }
    streamIds.add(pStreamId);
  }

  /**
   * format Filter as URL parameters for online requests
   *
   * @return
   */
  public String toUrlParameters() {
    StringBuilder sb = new StringBuilder();
    if (fromTime != null) {
      sb.append("&");
      sb.append(FROM_TIME_URL_KEY + "=" + fromTime);
    }
    if (toTime != null) {
      sb.append("&");
      sb.append(TO_TIME_URL_KEY + "=" + toTime);
    }
    if (streamIds != null) {
      for (String string : streamIds) {
        sb.append("&");
        sb.append(STREAMS_URL_KEY + "=" + string);
      }
    }
    if (tags != null) {
      for (String string : tags) {
        sb.append("&" + TAGS_URL_KEY + "=" + string);
      }
    }
    if (types != null) {
      for (String string : types) {
        sb.append("&" + TYPES_URL_KEY + "=" + string);
      }
    }
    if (running != null) {
      sb.append("&" + RUNNING_URL_KEY + "=" + running);
    }
    if (sortAscending != null) {
      sb.append("&" + SORT_ASCENDING_URL_KEY + "=" + sortAscending);
    }
    if (skip != null) {
      sb.append("&" + SKIP_URL_KEY + "=" + skip);
    }
    if (limit != null) {
      sb.append("&" + LIMIT_URL_KEY + "=" + limit);
    }
    if (state != null) {
      sb.append("&" + STATE_URL_KEY + "=" + state);
    }
    if (modifiedSince != null) {
      sb.append("&" + MODIFIED_SINCE_URL_KEY + "=" + modifiedSince);
    }
    return sb.toString();
  }

  public Long getFromTime() {
    return fromTime;
  }

  public Long getToTime() {
    return toTime;
  }

  public Set<String> getStreamIds() {
    return streamIds;
  }

  public Set<String> getTags() {
    return tags;
  }

  public Set<String> getTypes() {
    return types;
  }

  public Boolean getRunning() {
    return running;
  }

  public Boolean getSortAscending() {
    return sortAscending;
  }

  public Integer getSkip() {
    return skip;
  }

  public Integer getLimit() {
    return limit;
  }

  public State getState() {
    return state;
  }

  public Long getModifiedSince() {
    return modifiedSince;
  }

  public void setFromTime(Long pFromTime) {
    this.fromTime = pFromTime;
  }

  public void setToTime(Long pToTime) {
    this.toTime = pToTime;
  }

  public void setStreamIds(Set<String> pStreams) {
    this.streamIds = pStreams;
  }

  public void setTags(Set<String> pTags) {
    this.tags = pTags;
  }

  public void setTypes(Set<String> pTypes) {
    this.types = pTypes;
  }

  public void setRunning(Boolean pRunning) {
    this.running = pRunning;
  }

  public void setSortAscending(Boolean pSortAscending) {
    this.sortAscending = pSortAscending;
  }

  public void setSkip(Integer pSkip) {
    this.skip = pSkip;
  }

  public void setLimit(Integer pLimit) {
    this.limit = pLimit;
  }

  public void setState(State pState) {
    this.state = pState;
  }

  public void setModifiedSince(Long pModifiedSince) {
    this.modifiedSince = pModifiedSince;
  };

  /**
   * State parameter
   *
   * @author ik
   *
   */
  public enum State {
    DEFAULT, TRASHED, ALL
  }
}
