package com.pryv.api.database;

import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pryv.Filter;
import com.pryv.model.Event;
import com.pryv.model.Stream;
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

  /**
   * Events Table keys
   */
  public static final String EVENTS_CLIENT_ID_KEY = "CLIENT_ID";
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

  /**
   * Create query to insert or replace Event.
   *
   * @param eventToCache
   *
   * @throws JsonProcessingException
   * @return
   */
  public static String insertOrReplaceEvent(Event eventToCache) throws JsonProcessingException {
    StringBuilder sb = new StringBuilder();
    sb.append("INSERT OR REPLACE INTO "
      + EVENTS_TABLE_NAME
        + " ("
        + EVENTS_CLIENT_ID_KEY
        + ", "
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
        + EVENTS_ATTACHMENTS_KEY
        + ")"
        + " VALUES (");
    sb.append(formatTextValue(eventToCache.getClientId()) + ",");
    sb.append(formatTextValue(eventToCache.getId()) + ",");
    sb.append(formatTextValue(eventToCache.getStreamId()) + ",");
    sb.append(formatDoubleValue(eventToCache.getTime()) + ",");
    sb.append(formatTextValue(eventToCache.getType()) + ",");
    sb.append(formatDoubleValue(eventToCache.getCreated()) + ",");
    sb.append(formatTextValue(eventToCache.getCreatedBy()) + ",");
    sb.append(formatDoubleValue(eventToCache.getModified()) + ",");
    sb.append(formatTextValue(eventToCache.getModifiedBy()) + ",");
    sb.append(formatDoubleValue(eventToCache.getDuration()) + ",");
    sb.append(formatTextValue(eventToCache.getContent()) + ",");
    sb.append(formatSetValue(eventToCache.getTags()) + ",");
    sb.append(formatSetValue(eventToCache.getReferences()) + ",");
    sb.append(formatTextValue(eventToCache.getDescription()) + ",");
    sb.append(formatTextValue(eventToCache.formatClientDataAsString()) + ",");
    sb.append(formatBooleanValue(eventToCache.isTrashed()) + ",");
    sb.append(formatTextValue(JsonConverter.toJson(eventToCache.getAttachments())));
    sb.append(");");
    return sb.toString();
  }

  public static String updateEvent(Event eventToUpdate) throws JsonProcessingException {
    StringBuilder sb = new StringBuilder();
    /*UPDATE table_name
    SET column1 = value1, column2 = value2...., columnN = valueN
    WHERE [condition];*/
    sb.append("UPDATE " + EVENTS_TABLE_NAME + " SET "
            + EVENTS_CLIENT_ID_KEY + "=" + formatTextValue(eventToUpdate.getClientId())
            + ", "
            + EVENTS_ID_KEY + "=" + formatTextValue(eventToUpdate.getId())
            + ", "
            + EVENTS_STREAM_ID_KEY + "=" + formatTextValue(eventToUpdate.getStreamId())
            + ", "
            + EVENTS_TIME_KEY + "=" + formatDoubleValue(eventToUpdate.getTime())
            + ", "
            + EVENTS_TYPE_KEY + "=" + formatTextValue(eventToUpdate.getType())
            + ", "
            + EVENTS_CREATED_KEY + "=" + formatTextValue(eventToUpdate.getCreated())
            + ", "
            + EVENTS_CREATED_BY_KEY + "=" + formatTextValue(eventToUpdate.getCreatedBy())
            + ", "
            + EVENTS_MODIFIED_KEY + "=" + formatTextValue(eventToUpdate.getModified())
            + ", "
            + EVENTS_MODIFIED_BY_KEY + "=" + formatTextValue(eventToUpdate.getModifiedBy())
            + ", "
            + EVENTS_DURATION_KEY + "=" + formatDoubleValue(eventToUpdate.getDuration())
            + ", "
            + EVENTS_CONTENT_KEY + "=" + formatTextValue(eventToUpdate.getContent())
            + ", "
            + EVENTS_TAGS_KEY + "=" + formatSetValue(eventToUpdate.getTags())
            + ", "
            + EVENTS_REFS_KEY + "=" + formatSetValue(eventToUpdate.getReferences())
            + ", "
            + EVENTS_DESCRIPTION_KEY + "=" + formatTextValue(eventToUpdate.getDescription())
            + ", "
            + EVENTS_CLIENT_DATA_KEY + "=" + formatTextValue(eventToUpdate.formatClientDataAsString())
            + ", "
            + EVENTS_TRASHED_KEY + "=" + formatBooleanValue(eventToUpdate.isTrashed())
            + ", "
            + EVENTS_ATTACHMENTS_KEY + "=" + formatTextValue(JsonConverter.toJson(eventToUpdate.getAttachments())));

    sb.append(" WHERE "
            + EVENTS_CLIENT_ID_KEY
            + "="
            + formatTextValue(eventToUpdate.getClientId())
            + " AND "
            + EVENTS_MODIFIED_KEY
            + " < "
            + formatDoubleValue(eventToUpdate.getModified())
            + ";");
    return sb.toString();
  }

  /**
   * Creates the query to delete an Event. It's clientId is used in the request.
   *
   * @param eventToDelete
   * @return the SQLite query
   */
  public static String deleteEvent(Event eventToDelete) {
    return "DELETE FROM "
      + EVENTS_TABLE_NAME
        + " WHERE "
        + EVENTS_CLIENT_ID_KEY
        + "="
        + formatTextValue(eventToDelete.getClientId())
        + " AND "
        + EVENTS_TRASHED_KEY
        + "=1;";
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
      StringBuilder andSeparator = new StringBuilder("");
      // fromTime
      if (filter.getFromTime() != null) {
        filterParams.append(andSeparator + EVENTS_TIME_KEY + ">" + filter.getFromTime());
        andSeparator.replace(0, andSeparator.length(), " AND ");
      }

      // toTime
      if (filter.getToTime() != null) {
        filterParams.append(andSeparator + EVENTS_TIME_KEY + "<" + filter.getToTime());
        andSeparator.replace(0, andSeparator.length(), " AND ");
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
        andSeparator.replace(0, andSeparator.length(), " AND ");
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
        andSeparator.replace(0, andSeparator.length(), " AND ");
      }
      if (filterParams.length() != 0) {
        filterParams.insert(0, " WHERE ");
        baseQuery.append(filterParams.toString());
      }

      if (filter.getLimit() != null) {
        baseQuery.append(" LIMIT " + filter.getLimit());
      }
    }
    baseQuery.append(";");
    return baseQuery.toString();
  }

  /**
   * retrieve an Event from the SQLite database.
   *
   * @param clientId
   *          the client ID of the event
   * @return
   */
  public static String retrieveEvent(String clientId) {
    return "SELECT * FROM "
      + EVENTS_TABLE_NAME
        + " WHERE "
        + EVENTS_CLIENT_ID_KEY
        + "="
        + formatTextValue(clientId)
        + ";";
  }

  /**
   * retrieve a Stream from the SQLite database.
   *
   * @param id
   *          the ID of the stream
   * @return
   */
  public static String retrieveStream(String id) {
    return "SELECT * FROM "
      + STREAMS_TABLE_NAME
        + " WHERE "
        + STREAMS_ID_KEY
        + "="
        + formatTextValue(id)
        + ";";
  }

  /**
   * retrieve the child streams of a Stream
   *
   * @param parentId
   *          the stream whose children we are fetching
   * @return
   */
  public static String retrieveChildren(String parentId) {
    return "SELECT * FROM "
      + STREAMS_TABLE_NAME
        + " WHERE "
        + STREAMS_PARENT_ID_KEY
        + "="
        + formatTextValue(parentId)
        + ";";
  }

  /**
   * Creates query to insert or replace stream in the SQLite database.
   *
   * @param streamToCache
   * @return
   */
  public static String insertOrReplaceStream(Stream streamToCache) {
    StringBuilder sb = new StringBuilder();
    sb.append("INSERT OR REPLACE INTO "
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
    sb.append(formatBooleanValue(streamToCache.isSingleActivity()) + ",");
    sb.append(formatTextValue(streamToCache.formatClientDataAsString()) + ",");
    sb.append(formatBooleanValue(streamToCache.isTrashed()) + ",");
    sb.append(formatDoubleValue(streamToCache.getCreated()) + ",");
    sb.append(formatTextValue(streamToCache.getCreatedBy()) + ",");
    sb.append(formatDoubleValue(streamToCache.getModified()) + ",");
    sb.append(formatTextValue(streamToCache.getModifiedBy()));
    sb.append(");");
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
        + "="
        + formatTextValue(streamToDelete.getId())
        + ""
        + " AND "
        + EVENTS_TRASHED_KEY
        + "=1;";
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
        + EVENTS_CLIENT_ID_KEY
        + " TEXT PRIMARY KEY NOT NULL, "
        + EVENTS_ID_KEY
        + " TEXT, "
        + EVENTS_STREAM_ID_KEY
        + " TEXT  NOT NULL, "
        + EVENTS_TIME_KEY
        + " INTEGER, "
        + EVENTS_TYPE_KEY
        + " TEXT  NOT NULL, "
        + EVENTS_CREATED_KEY
        + " REAL, "
        + EVENTS_CREATED_BY_KEY
        + " TEXT, "
        + EVENTS_MODIFIED_KEY
        + " REAL, "
        + EVENTS_MODIFIED_BY_KEY
        + " TEXT, "
        + EVENTS_DURATION_KEY
        + " REAL, "
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
        + " TEXT  NOT NULL, "
        + STREAMS_CREATED_KEY
        + " REAL, "
        + STREAMS_CREATED_BY_KEY
        + " TEXT, "
        + STREAMS_MODIFIED_KEY
        + " REAL, "
        + STREAMS_MODIFIED_BY_KEY
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
  private static void formatFilterSet(StringBuilder andSeparator, StringBuilder sb,
    Set<String> set,
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
      andSeparator.replace(0, andSeparator.length(), " AND ");
    }
  }

  /**
   * format Double for insert/update in DB, if toAdd == null, returns NULL
   *
   * @param toAdd
   * @return
   */
  private static String formatDoubleValue(Double toAdd) {
    if (toAdd != null) {
      return Double.toString(toAdd);
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
    if (obj != null && !obj.equals("null")) {
      if (obj instanceof String) {
        return "\'" + ((String) obj).replace("\'", "\\\'") + "\'";
      } else {
        return "\'" + obj + "\'";
      }
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
