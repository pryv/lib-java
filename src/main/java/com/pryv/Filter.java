package com.pryv;

import java.util.HashSet;
import java.util.Set;

import com.pryv.model.Event;
import com.pryv.model.Stream;

/**
 * Filter used in Events fetching. All its fields are optional. Either
 * instantiate using full Constructor with 'null' values when fields are unused,
 * or use empty Constructor and add fields using setters or add methods.
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
  private Double fromTime;
  private Double toTime;
  private Set<Stream> streams;
  private Set<String> tags;
  private Set<String> types;
  private Boolean running;
  private Boolean sortAscending;
  private Integer skip;
  private Integer limit;
  private State state;
  private Double modifiedSince;

  /**
   * Filter class constructor. All fields are optional: use null when unused.
   *
   * @param from
   *          from time
   * @param to
   *          to time
   * @param streams
   *          streams
   * @param tags
   *          tags
   * @param types
   *          types
   * @param running
   *          running
   * @param sortAscending
   *          sort ascending
   * @param skip
   *          skip how much
   * @param limit
   *          how many to get
   * @param state
   *          which state
   * @param modifiedSince
   *          since when is it modified
   */
  public Filter(Double from, Double to, Set<Stream> streams, Set<String> tags,
    Set<String> types, Boolean running, Boolean sortAscending, Integer skip, Integer limit,
    State state, Double modifiedSince) {
    this.fromTime = from;
    this.toTime = to;
    this.streams = streams;
    this.tags = tags;
    this.types = types;
    this.running = running;
    this.sortAscending = sortAscending;
    this.skip = skip;
    this.limit = limit;
    this.state = state;
    this.modifiedSince = modifiedSince;
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
    if (streams != null) {
      streamIdMatch = false;
      for (Stream stream : streams) {
        if ((stream.getId()).equals(event.getStreamId())) {
          streamIdMatch = true;
        }
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
      if (event.isTrashed() != null) {
        if (event.isTrashed()
          && state.equals(State.DEFAULT)
            || !event.isTrashed()
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
   * add a stream to the filter
   *
   * @param stream
   */
  public void addStream(Stream stream) {
    if (this.streams == null) {
      this.streams = new HashSet<Stream>();
    }
    this.streams.add(stream);
  }

  /**
   * add a tag to the filter
   *
   * @param pTag
   */
  public void addTag(String pTag) {
    if (tags == null) {
      tags = new HashSet<String>();
    }
    tags.add(pTag);
  }

  /**
   * add a type to the filter
   *
   * @param pType
   */
  public void addType(String pType) {
    if (types == null) {
      types = new HashSet<String>();
    }
    types.add(pType);
  }

  /**
   * format Filter as URL parameters for online requests
   *
   * @return
   */
  public String toUrlParameters() {
    StringBuilder sb = new StringBuilder();
    if (fromTime != null) {
      sb.append("&" + FROM_TIME_URL_KEY + "=" + fromTime);
    }
    if (toTime != null) {
      sb.append("&" + TO_TIME_URL_KEY + "=" + toTime);
    }
    if (streams != null) {
      for (Stream stream : streams) {
        sb.append("&" + STREAMS_URL_KEY + "=" + stream.getId());
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

  /**
   * check if this filter is included in the scope passed in argument. If scope.streams is null,
   * we consider that the scope is the whole Pryv data.
   *
   * @param scope a Filter object representing a scope
   * @return
   */
  public boolean isIncludedInScope(Filter scope) {
    if (scope.streams == null) {
      return true;
    } else {
      // test for each scope streamId if it is or its child is
      for (Stream stream : this.streams) {
        boolean isStreamOrParentFound = false;
        for (Stream scopeStream : scope.streams) {
          if (scopeStream.getId().equals(stream.getId())) {
            isStreamOrParentFound = true;
          } else if (scopeStream.hasChild(stream.getId())) {
            isStreamOrParentFound = true;
          }
        }
        if (!isStreamOrParentFound) {
          return false;
        }
      }
      return true;
    }
  }

  /**
   * check if the event is included in the scope
   *
   * @param event
   * @return
   */
  public boolean hasInScope(Event event) {
    return hasInScope(event.getStreamId());
  }

  /**
   * check if the streamId is included in the scope. Currently the scope is only represented by
   * the streams. A Filter with no streams represents a total Filter.
   *
   * @param streamId
   * @return
   */
  public boolean hasInScope(String streamId) {
    if (streams == null) {
      return true;
    } else {
      for (Stream scopeStream : streams) {
        if (streamId.equals(scopeStream.getId())) {
          return true;
        } else if (scopeStream.hasChild(streamId)) {
          return true;
        }
      }
      return false;
    }
  }

  /**
   * Returns the streamIds of this Filter, null if there are none.
   *
   * @return
   */
  public Set<String> getStreamIds() {
    if (streams != null) {
      Set<String> streamIds = new HashSet<String>();
      for (Stream stream: streams) {
        streamIds.add(stream.getId());
      }
      return streamIds;
    } else {
     return null;
    }
  }

  public Double getFromTime() {
    return fromTime;
  }

  public Double getToTime() {
    return toTime;
  }

  public Set<Stream> getStreams() {
    return streams;
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

  public Double getModifiedSince() {
    return modifiedSince;
  }

  public void setFromTime(Double pFromTime) {
    this.fromTime = pFromTime;
  }

  public void setToTime(Double pToTime) {
    this.toTime = pToTime;
  }

  public void setStreamIds(Set<Stream> streams) {
    this.streams = streams;
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

  public void setModifiedSince(Double pModifiedSince) {
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
