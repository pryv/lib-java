package com.pryv.api.database;

import java.util.Set;

import com.pryv.api.Filter;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;

/**
 * Class containing methods to generate SQLite queries
 *
 * @author ik
 *
 */
public class QueryGenerator {

  /**
   * Tables names
   */
  private static final String EVENTS_TABLE_NAME = "EVENTS";
  private static final String STREAMS_TABLE_NAME = "STREAMS";
  // private static final String ATTACHMENTS_TABLE_NAME = "ATTACHMENTS";

  /**
   * Events Table keys
   */
  public static final String EVENTS_ID_KEY = "ID";
  public static final String EVENTS_STREAM_ID_KEY = "STREAM_ID";
  public static final String EVENTS_TIME_KEY = "TIME";
  public static final String EVENTS_TYPE_KEY = "TYPE";
  public static final String EVENTS_CREATED_KEY = "CREATED";
  public static final String EVENTS_CREATED_BY_KEY = "CREATED_BY";
  public static final String EVENTS_MODIFIED_KEY = "MODIFIED";
  public static final String EVENTS_MODIFIED_BY_KEY = "MODIFIED_BY";
  public static final String EVENTS_DURATION_KEY = "DURATION";
  public static final String EVENTS_CONTENT_KEY = "CONTENT";
  public static final String EVENTS_TAGS_KEY = "TAGS";
  public static final String EVENTS_REFS_KEY = "REFS";
  public static final String EVENTS_DESCRIPTION_KEY = "DESCRIPTION";
  public static final String EVENTS_CLIENT_DATA_KEY = "CLIENT_DATA";
  public static final String EVENTS_TRASHED_KEY = "TRASHED";
  public static final String EVENTS_TEMP_REF_ID_KEY = "TEMP_REF_ID";

  /**
   * Streams Table keys
   */
  public static final String STREAMS_ID_KEY = "ID";
  public static final String STREAMS_NAME_KEY = "NAME";
  public static final String STREAMS_PARENT_ID_KEY = "PARENT_ID";
  public static final String STREAMS_SINGLE_ACTIVITY_KEY = "SINGLE_ACTIVITY";
  public static final String STREAMS_CLIENT_DATA_KEY = "CLIENT_DATA";
  public static final String STREAMS_TRASHED_KEY = "TRASHED";
  public static final String STREAMS_CREATED_KEY = "CREATED";
  public static final String STREAMS_CREATED_BY_KEY = "CREATED_BEY";
  public static final String STREAMS_MODIFIED_KEY = "MODIFIED";
  public static final String STREAMS_MODIFIED_BY_KEY = "MODIFIED_BY";

  /**
   * Create Event query
   *
   * @param eventToCache
   * @return
   */
  public static String createEvent(Event eventToCache) {
    StringBuilder sb = new StringBuilder();
    sb.append("INSERT INTO "
      + EVENTS_TABLE_NAME
        + " ("
        + EVENTS_ID_KEY
        + ", "
        + EVENTS_STREAM_ID_KEY
        + ", "
        + EVENTS_TIME_KEY
        + ", "
        + EVENTS_TYPE_KEY
        + ", "
        + EVENTS_CREATED_KEY
        + ", "
        + EVENTS_CREATED_BY_KEY
        + ", "
        + EVENTS_MODIFIED_KEY
        + ", "
        + EVENTS_MODIFIED_BY_KEY
        + ", "
        + EVENTS_DURATION_KEY
        + ", "
        + EVENTS_CONTENT_KEY
        + ", "
        + EVENTS_TAGS_KEY
        + ", "
        + EVENTS_REFS_KEY
        + ", "
        + EVENTS_DESCRIPTION_KEY
        + ", "
        + EVENTS_CLIENT_DATA_KEY
        + ", "
        + EVENTS_TRASHED_KEY
        + ", "
        + EVENTS_TEMP_REF_ID_KEY
        + ")"
        + " VALUES (");
    sb.append(formatTextValue(eventToCache.getId()) + ",");
    sb.append(formatTextValue(eventToCache.getStreamId()) + ",");
    sb.append(formatLongValue(eventToCache.getTime()) + ",");
    sb.append(formatTextValue(eventToCache.getType()) + ",");
    sb.append(formatLongValue(eventToCache.getCreated()) + ",");
    sb.append(formatTextValue(eventToCache.getCreatedBy()) + ",");
    sb.append(formatLongValue(eventToCache.getModified()) + ",");
    sb.append(formatTextValue(eventToCache.getModifiedBy()) + ",");
    sb.append(formatLongValue(eventToCache.getDuration()) + ",");
    sb.append(formatTextValue(eventToCache.getContent()) + ",");
    sb.append(formatSetValue(eventToCache.getTags()) + ",");
    sb.append(formatSetValue(eventToCache.getReferences()) + ",");
    sb.append(formatTextValue(eventToCache.getDescription()) + ",");
    // attachments need to be added in their own table
    sb.append(formatTextValue(eventToCache.getClientDataAsString()) + ",");
    sb.append(formatTextValue(eventToCache.getTrashed()) + ",");
    sb.append(formatTextValue(eventToCache.getTempRefId()));
    sb.append(");");
    return sb.toString();
  }

  public static String updateEvent(Event eventToUpdate) {
    StringBuilder sb = new StringBuilder();
    sb.append("UPDATE "
      + EVENTS_TABLE_NAME
        + " SET "
        + EVENTS_ID_KEY
        + "="
        + formatTextValue(eventToUpdate.getId())
        + ", "
        + EVENTS_STREAM_ID_KEY
        + "="
        + formatTextValue(eventToUpdate.getStreamId())
        + ", "
        + EVENTS_TIME_KEY
        + "="
        + formatLongValue(eventToUpdate.getTime())
        + ", "
        + EVENTS_TYPE_KEY
        + "="
        + formatTextValue(eventToUpdate.getType())
        + ", "
        + EVENTS_CREATED_KEY
        + "="
        + formatLongValue(eventToUpdate.getCreated())
        + ", "
        + EVENTS_CREATED_BY_KEY
        + "="
        + formatTextValue(eventToUpdate.getCreatedBy())
        + ", "
        + EVENTS_MODIFIED_KEY
        + "="
        + formatLongValue(eventToUpdate.getModified())
        + ", "
        + EVENTS_MODIFIED_BY_KEY
        + "="
        + formatTextValue(eventToUpdate.getModifiedBy())
        + ", "
        + EVENTS_DURATION_KEY
        + "="
        + formatLongValue(eventToUpdate.getDuration())
        + ", "
        + EVENTS_CONTENT_KEY
        + "="
        + formatTextValue(eventToUpdate.getContent())
        + ", "
        + EVENTS_TAGS_KEY
        + "="
        + formatSetValue(eventToUpdate.getTags())
        + ", "
        + EVENTS_REFS_KEY
        + "="
        + formatSetValue(eventToUpdate.getReferences())
        + ", "
        + EVENTS_DESCRIPTION_KEY
        + "="
        + formatTextValue(eventToUpdate.getDescription())
        + ", "
        + EVENTS_CLIENT_DATA_KEY
        + "="
        + formatTextValue(eventToUpdate.getClientDataAsString())
        + ", "
        + EVENTS_TRASHED_KEY
        + "="
        + formatTextValue(eventToUpdate.getTrashed())
        + ", "
        + EVENTS_TEMP_REF_ID_KEY
        + "="
        + formatTextValue(eventToUpdate.getTempRefId()));
    sb.append(" WHERE " + EVENTS_ID_KEY + "=\'" + eventToUpdate.getId() + "\';");
    return sb.toString();
  }

  public static String deleteEvent(Event eventToDelete) {
    return "DELETE FROM " + EVENTS_TABLE_NAME + " WHERE ID=\'" + eventToDelete.getId() + "\';";
  }

  public static String retrieveEvents(Filter filter) {
    StringBuilder sb = new StringBuilder();
    sb.append("SELECT * FROM " + EVENTS_TABLE_NAME + " ");

    if (filter != null) {
      sb.append("WHERE ");
      String andSeparator = "";
      // fromTime
      if (filter.getFromTime() != null) {
        sb.append(andSeparator + EVENTS_TIME_KEY + ">" + filter.getFromTime());
        andSeparator = " AND ";
      }

      // toTime
      if (filter.getToTime() != null) {
        sb.append(andSeparator + EVENTS_TIME_KEY + "<" + filter.getToTime());
        andSeparator = " AND ";
      }

      // streamIds
      formatFilterSet(andSeparator, sb, filter.getStreamIds(), EVENTS_STREAM_ID_KEY);

      // tags
      formatFilterSet(andSeparator, sb, filter.getTags(), EVENTS_TAGS_KEY);

      // types
      formatFilterSet(andSeparator, sb, filter.getTypes(), EVENTS_TYPE_KEY);

      // TODO handle running parameter

      // modifiedSince
      if (filter.getModifiedSince() != null) {
        sb.append(andSeparator + EVENTS_MODIFIED_KEY + ">" + filter.getModifiedSince());
        andSeparator = " AND ";
      }

      // state
      if (filter.getState() != null) {
        sb.append(andSeparator);
        if (filter.getState().equals(Filter.State.DEFAULT)) {
          sb.append(EVENTS_TRASHED_KEY + "=" + "\'false\'");
        } else if (filter.getState().equals(Filter.State.TRASHED)) {
          sb.append(EVENTS_TRASHED_KEY + "=" + "\'true\'");
        } else {
          sb.append("(" + EVENTS_TRASHED_KEY + "=\'false\' OR " + EVENTS_TRASHED_KEY + "=\'true\')");
        }
        andSeparator = " AND ";
      }
    }
    sb.append(";");
    return sb.toString();
  }

  private static void formatFilterSet(String andSeparator, StringBuilder sb, Set<String> set,
    String key) {
    if (set != null) {
      sb.append(andSeparator + "(");
      String separator = "";
      for (String item : set) {
        sb.append(separator);
        sb.append(key + "=" + formatTextValue(item));
        separator = " OR ";
      }
      sb.append(")");
      andSeparator = " AND ";
    }
  }

  private static String formatLongValue(Long toAdd) {
    StringBuilder sb = new StringBuilder();
    if (toAdd != null) {
      sb.append(toAdd);
    } else {
      sb.append("NULL");
    }
    return sb.toString();
  }

  private static String formatTextValue(Object prim) {
    StringBuilder sb = new StringBuilder();
    if (prim != null) {
      sb.append("\'" + prim + "\'");
    } else {
      sb.append("NULL");
    }
    return sb.toString();
  }

  private static String formatSetValue(Set<?> set) {
    StringBuilder sb = new StringBuilder();
    String listSeparator = "";
    if (set != null) {
      sb.append("\'");
      for (Object setItem : set) {
        sb.append(listSeparator + setItem);
        listSeparator = ",";
      }
      sb.append("\'");
    } else {
      sb.append("NULL");
    }
    return sb.toString();
  }

  /**
   * Create Events Table Query
   *
   * @return
   */
  public static String createEventsTable() {
    return "CREATE TABLE IF NOT EXISTS "
      + EVENTS_TABLE_NAME
        + "("
        + EVENTS_ID_KEY
        + " TEXT PRIMARY KEY  NOT NULL, "
        + EVENTS_STREAM_ID_KEY
        + " TEXT              NOT NULL, "
        + EVENTS_TIME_KEY
        + " INTEGER           NOT NULL, "
        + EVENTS_TYPE_KEY
        + " TEXT              NOT NULL, "
        + EVENTS_CREATED_KEY
        + " INTEGER           NOT NULL, "
        + EVENTS_CREATED_BY_KEY
        + " TEXT              NOT NULL, "
        + EVENTS_MODIFIED_KEY
        + " INTEGER           NOT NULL, "
        + EVENTS_MODIFIED_BY_KEY
        + " TEXT              NOT NULL, "
        + EVENTS_DURATION_KEY
        + " INTEGER, "
        + EVENTS_CONTENT_KEY
        + " BLOB, "
        + EVENTS_TAGS_KEY
        + " TEXT, "
        + EVENTS_REFS_KEY
        + " TEXT, "
        + EVENTS_DESCRIPTION_KEY
        + " TEXT, "
        + EVENTS_CLIENT_DATA_KEY
        + " TEXT, "
        + EVENTS_TRASHED_KEY
        + " INTEGER, "
        + EVENTS_TEMP_REF_ID_KEY
        + " TEXT)";
  }

  public static String createStreamsTable() {
    return "CREATE TABLE IF NOT EXISTS "
      + STREAMS_TABLE_NAME
        + "("
        + STREAMS_ID_KEY
        + " TEXT PRIMARY KEY  NOT NULL, "
        + STREAMS_NAME_KEY
        + " TEXT              NOT NULL, "
        + STREAMS_CREATED_KEY
        + " INTEGER           NOT NULL, "
        + STREAMS_CREATED_BY_KEY
        + " TEXT              NOT NULL, "
        + STREAMS_MODIFIED_KEY
        + " INTEGER           NOT NULL, "
        + STREAMS_MODIFIED_BY_KEY
        + " TEXT              NOT NULL, "
        + STREAMS_PARENT_ID_KEY
        + " TEXT, "
        + STREAMS_SINGLE_ACTIVITY_KEY
        + " INTEGER, "
        + STREAMS_CLIENT_DATA_KEY
        + " TEXT, "
        + STREAMS_TRASHED_KEY
        + " INTEGER)";
  }

  public static String createStream(Stream streamToCache) {
    StringBuilder sb = new StringBuilder();
    sb.append("INSERT INTO "
      + STREAMS_TABLE_NAME
        + " ("
        + STREAMS_ID_KEY
        + ", "
        + STREAMS_NAME_KEY
        + ", "
        + STREAMS_PARENT_ID_KEY
        + ", "
        + STREAMS_SINGLE_ACTIVITY_KEY
        + ", "
        + STREAMS_CLIENT_DATA_KEY
        + ", "
        + STREAMS_TRASHED_KEY
        + ", "
        + STREAMS_CREATED_KEY
        + ", "
        + STREAMS_CREATED_BY_KEY
        + ", "
        + STREAMS_MODIFIED_KEY
        + ", "
        + STREAMS_MODIFIED_BY_KEY
        + ")"
        + " VALUES (");
    sb.append(formatTextValue(streamToCache.getId()) + ",");
    sb.append(formatTextValue(streamToCache.getName()) + ",");
    sb.append(formatTextValue(streamToCache.getParentId()) + ",");
    sb.append(formatTextValue(streamToCache.getSingleActivity()) + ",");
    sb.append(formatTextValue(streamToCache.getClientDataAsString()) + ",");
    sb.append(formatTextValue(streamToCache.getTrashed()) + ",");
    sb.append(formatLongValue(streamToCache.getCreated()) + ",");
    sb.append(formatTextValue(streamToCache.getCreatedBy()) + ",");
    sb.append(formatLongValue(streamToCache.getModified()) + ",");
    sb.append(formatTextValue(streamToCache.getModifiedBy()));
    sb.append(");");
    return sb.toString();
  }

}
