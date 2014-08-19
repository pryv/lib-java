package com.pryv.api.database;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pryv.api.Filter;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;
import com.pryv.utils.JsonConverter;

/**
 * Class containing methods to generate SQLite queries.
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
  // "REFERENCES" is a reserved command in SQLite
  public static final String EVENTS_DESCRIPTION_KEY = "DESCRIPTION";
  public static final String EVENTS_CLIENT_DATA_KEY = "CLIENT_DATA";
  public static final String EVENTS_TRASHED_KEY = "TRASHED";
  public static final String EVENTS_TEMP_REF_ID_KEY = "TEMP_REF_ID";
  public static final String EVENTS_ATTACHMENTS_KEY = "ATTACHMENTS";

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
  public static final String STREAMS_CREATED_BY_KEY = "CREATED_BY";
  public static final String STREAMS_MODIFIED_KEY = "MODIFIED";
  public static final String STREAMS_MODIFIED_BY_KEY = "MODIFIED_BY";
  public static final String STREAMS_CHILDREN_KEY = "CHILDREN";

  /**
   * Create Event query.
   *
   * @param eventToCache
   *
   * @throws JsonProcessingException
   * @return
   */
  public static String insertEvent(Event eventToCache) throws JsonProcessingException {
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
        + ", "
        + EVENTS_ATTACHMENTS_KEY
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
    sb.append(formatTextValue(eventToCache.formatClientDataAsString()) + ",");
    sb.append(formatBooleanValue(eventToCache.getTrashed()) + ",");
    sb.append(formatTextValue(eventToCache.getTempRefId()) + ",");
    sb.append(formatTextValue(JsonConverter.toJson(eventToCache.getAttachments())));
    sb.append(");");
    return sb.toString();
  }

  /**
   * Create query to update Event if "modified" field is higher.
   *
   * @param eventToUpdate
   *
   * @throws JsonProcessingException
   *
   * @return
   */
  public static String updateEvent(Event eventToUpdate) throws JsonProcessingException {
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
        + formatTextValue(eventToUpdate.formatClientDataAsString())
        + ", "
        + EVENTS_TRASHED_KEY
        + "="
        + formatBooleanValue(eventToUpdate.getTrashed())
        + ", "
        + EVENTS_TEMP_REF_ID_KEY
        + "="
        + formatTextValue(eventToUpdate.getTempRefId())
        + ", "
        + EVENTS_ATTACHMENTS_KEY
        + "="
        + formatTextValue(JsonConverter.toJson(eventToUpdate.getAttachments())));
    sb.append(" WHERE "
      + EVENTS_ID_KEY
        + "=\'"
        + eventToUpdate.getId()
        + "\' AND "
        + EVENTS_MODIFIED_KEY
        + "<"
        + eventToUpdate.getModified()
        + ";");
    return sb.toString();
  }

  /**
   * Creates the query to delete an Event. It's id is used in the request.
   *
   * @param eventToDelete
   * @return the SQLite query
   */
  public static String deleteEvent(Event eventToDelete) {
    return "DELETE FROM "
      + EVENTS_TABLE_NAME
        + " WHERE "
        + EVENTS_ID_KEY
        + "=\'"
        + eventToDelete.getId()
        + "\';";
  }

  /**
   * creates the query to retrieve Events from DB according to the provided
   * filter.
   *
   * @param filter
   * @return the SQLite query
   */
  public static String retrieveEvents(Filter filter) {
    StringBuilder baseQuery = new StringBuilder();
    baseQuery.append("SELECT * FROM " + EVENTS_TABLE_NAME + " ");
    StringBuilder filterParams = new StringBuilder();

    if (filter != null) {
      String andSeparator = "";
      // fromTime
      if (filter.getFromTime() != null) {
        filterParams.append(andSeparator + EVENTS_TIME_KEY + ">" + filter.getFromTime());
        andSeparator = " AND ";
      }

      // toTime
      if (filter.getToTime() != null) {
        filterParams.append(andSeparator + EVENTS_TIME_KEY + "<" + filter.getToTime());
        andSeparator = " AND ";
      }

      // streamIds
      formatFilterSet(andSeparator, filterParams, filter.getStreamIds(), EVENTS_STREAM_ID_KEY);

      // tags
      formatFilterSet(andSeparator, filterParams, filter.getTags(), EVENTS_TAGS_KEY);

      // types
      formatFilterSet(andSeparator, filterParams, filter.getTypes(), EVENTS_TYPE_KEY);

      // TODO handle running parameter

      // modifiedSince
      if (filter.getModifiedSince() != null) {
        filterParams.append(andSeparator + EVENTS_MODIFIED_KEY + ">" + filter.getModifiedSince());
        andSeparator = " AND ";
      }

      // state
      if (filter.getState() != null) {
        filterParams.append(andSeparator);
        if (filter.getState().equals(Filter.State.DEFAULT)) {
          filterParams.append(EVENTS_TRASHED_KEY + "=" + "\'false\'");
        } else if (filter.getState().equals(Filter.State.TRASHED)) {
          filterParams.append(EVENTS_TRASHED_KEY + "=" + "\'true\'");
        } else {
          filterParams.append("("
            + EVENTS_TRASHED_KEY
              + "=\'false\' OR "
              + EVENTS_TRASHED_KEY
              + "=\'true\')");
          // alternative implementation
          // sb.setLength(sb.length() - andSeparator.length());
        }
        andSeparator = " AND ";
      }
    }
    if (filterParams.length() != 0) {
      filterParams.insert(0, " WHERE ");
      filterParams.append(";");
      baseQuery.append(filterParams.toString());
    }
    return baseQuery.toString();
  }

  /**
   * Creates query to insert stream in the SQLite database.
   *
   * @param streamToCache
   * @return
   */
  public static String insertStream(Stream streamToCache) {
    StringBuilder sb = new StringBuilder();
    sb.append("INSERT INTO "
      + STREAMS_TABLE_NAME
        + " ("
        + STREAMS_ID_KEY
        + ", "
        + STREAMS_NAME_KEY
        + ", "
        + STREAMS_CHILDREN_KEY
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
    if (streamToCache.getChildren() != null) {
      Set<String> childIds = new HashSet<String>();
      for (Stream childStream : streamToCache.getChildren()) {
        childIds.add(childStream.getId());
      }
      sb.append(formatSetValue(childIds) + ",");
    } else {
      sb.append("NULL,");
    }
    sb.append(formatTextValue(streamToCache.getParentId()) + ",");
    sb.append(formatBooleanValue(streamToCache.getSingleActivity()) + ",");
    sb.append(formatTextValue(streamToCache.formatClientDataAsString()) + ",");
    sb.append(formatBooleanValue(streamToCache.getTrashed()) + ",");
    sb.append(formatLongValue(streamToCache.getCreated()) + ",");
    sb.append(formatTextValue(streamToCache.getCreatedBy()) + ",");
    sb.append(formatLongValue(streamToCache.getModified()) + ",");
    sb.append(formatTextValue(streamToCache.getModifiedBy()));
    sb.append(");");
    return sb.toString();
  }

  /**
   * Create query to update Stream if modified field is higher.
   *
   * @param streamToUpdate
   * @return
   */
  public static String updateStream(Stream streamToUpdate) {
    StringBuilder sb = new StringBuilder();
    Set<String> childIds = null;
    if (streamToUpdate.getChildren() != null) {
      childIds = new HashSet<String>();
      for (Stream childStream : streamToUpdate.getChildren()) {
        childIds.add(childStream.getId());
      }
    }
    sb.append("UPDATE "
      + STREAMS_TABLE_NAME
        + " SET "
        + STREAMS_ID_KEY
        + "="
        + formatTextValue(streamToUpdate.getId())
        + ", "
        + STREAMS_NAME_KEY
        + "="
        + formatTextValue(streamToUpdate.getName())
        + ", "
        + STREAMS_CHILDREN_KEY
        + "="
        + formatSetValue(childIds)
        + ", "
        + STREAMS_PARENT_ID_KEY
        + "="
        + formatTextValue(streamToUpdate.getParentId())
        + ", "
        + STREAMS_SINGLE_ACTIVITY_KEY
        + "="
        + formatBooleanValue(streamToUpdate.getSingleActivity())
        + ", "
        + STREAMS_CLIENT_DATA_KEY
        + "="
        + formatTextValue(streamToUpdate.formatClientDataAsString())
        + ", "
        + STREAMS_TRASHED_KEY
        + "="
        + formatBooleanValue(streamToUpdate.getTrashed())
        + ", "
        + STREAMS_CREATED_KEY
        + "="
        + formatLongValue(streamToUpdate.getCreated())
        + ", "
        + STREAMS_CREATED_BY_KEY
        + "="
        + formatTextValue(streamToUpdate.getCreatedBy())
        + ", "
        + STREAMS_MODIFIED_KEY
        + "="
        + formatLongValue(streamToUpdate.getModified())
        + ", "
        + STREAMS_MODIFIED_BY_KEY
        + "="
        + formatTextValue(streamToUpdate.getModifiedBy()));
    sb.append(" WHERE "
      + STREAMS_ID_KEY
        + "=\'"
        + streamToUpdate.getId()
        + "\' AND "
        + STREAMS_MODIFIED_KEY
        + "<"
        + streamToUpdate.getModified()
        + ";");
    return sb.toString();
  }

  /**
   * Creates query to delete stream in the SQLite database.
   *
   * @param streamToDelete
   * @return
   */
  public static String deleteStream(Stream streamToDelete) {
    return "DELETE FROM "
      + STREAMS_TABLE_NAME
        + " WHERE "
        + STREAMS_ID_KEY
        + "=\'"
        + streamToDelete.getId()
        + "\';";
  }

  /**
   * Creates query to retrieve all Streams from SQLite database
   *
   * @return
   */
  public static String retrieveStreams() {
    return "SELECT * FROM " + STREAMS_TABLE_NAME + ";";
  }

  /**
   * Creates query to create the Events table if it doesn't exist yet.
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
        + " TEXT, "
        + EVENTS_ATTACHMENTS_KEY
        + " TEXT);";
  }

  /**
   * Creates query to create the Streams table if it doesn't exist yet.
   *
   * @return
   */
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
        + STREAMS_CHILDREN_KEY
        + " TEXT, "
        + STREAMS_PARENT_ID_KEY
        + " TEXT REFERENCES "
        + STREAMS_TABLE_NAME
        + "("
        + STREAMS_ID_KEY
        + "), "
        + STREAMS_SINGLE_ACTIVITY_KEY
        + " INTEGER, "
        + STREAMS_CLIENT_DATA_KEY
        + " TEXT, "
        + STREAMS_TRASHED_KEY
        + " INTEGER)";
  }

  /**
   * format Set values to be used in retrieval query. eg.: [AND] (key=itemA OR
   * key=itemB OR ...)
   *
   * @param andSeparator
   *          " AND " or ""
   * @param sb
   *          current StringBuilder used in the request
   * @param set
   *          the set of conditions
   * @param key
   *          the column on which the conditions are tested in the DB
   */
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

  /**
   * format Long for insert/update in DB, if toAdd == null, returns NULL
   *
   * @param toAdd
   * @return
   */
  private static String formatLongValue(Long toAdd) {
    if (toAdd != null) {
      return Long.toString(toAdd);
    } else {
      return "NULL";
    }
  }

  /**
   * format Boolean for insert/update in DB, if toAdd = null, returns NULL
   *
   * @param toAdd
   * @return
   */
  private static String formatBooleanValue(Boolean toAdd) {
    if (toAdd != null) {
      if (toAdd == true) {
        return "1";
      } else {
        return "0";
      }
    } else {
      return "NULL";
    }
  }

  /**
   * format Object's value as string for insert/update in DB. eg.:
   * 'obj.toString()'. If obj == null, returns NULL
   *
   * @param obj
   * @return
   */
  private static String formatTextValue(Object obj) {
    if (obj != null) {
      return "\'" + obj + "\'";
    } else {
      return "NULL";
    }
  }

  /**
   * format Set values for insertion/update in DB. eg.: 'itemA.toString(),
   * itemB.toString(),...'
   *
   * @param set
   * @return
   */
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

}
