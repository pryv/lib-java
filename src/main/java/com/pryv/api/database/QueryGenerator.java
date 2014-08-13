package com.pryv.api.database;

import java.util.Set;

import com.pryv.api.model.Event;

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
  private static final String ATTACHMENTS_TABLE_NAME = "ATTACHMENTS";

  /**
   * Events Table keys
   */
  private static final String ID_KEY = "ID";
  private static final String STREAM_ID_KEY = "STREAM_ID";
  private static final String TIME_KEY = "TIME";
  private static final String TYPE_KEY = "TYPE";
  private static final String CREATED_KEY = "CREATED";
  private static final String CREATED_BY_KEY = "CREATED_BY";
  private static final String MODIFIED_KEY = "MODIFIED";
  private static final String MODIFIED_BY_KEY = "MODIFIED_BY";
  private static final String DURATION_KEY = "DURATION";
  private static final String CONTENT_KEY = "CONTENT";
  private static final String TAGS_KEY = "TAGS";
  private static final String REFS_KEY = "REFS";
  private static final String DESCRIPTION_KEY = "DESCRIPTION";
  private static final String CLIENT_DATA_KEY = "CLIENT_DATA";
  private static final String TRASHED_KEY = "TRASHED";
  private static final String TEMP_REF_ID_KEY = "TEMP_REF_ID";

  public static String createEvent(Event eventToCache) {
    StringBuilder sb = new StringBuilder();
    sb.append("INSERT INTO "
      + EVENTS_TABLE_NAME
        + " ("
        + ID_KEY
        + ", "
        + STREAM_ID_KEY
        + ", "
        + TIME_KEY
        + ", "
        + TYPE_KEY
        + ", "
        + CREATED_KEY
        + ", "
        + CREATED_BY_KEY
        + ", "
        + MODIFIED_KEY
        + ", "
        + MODIFIED_BY_KEY
        + ", "
        + DURATION_KEY
        + ", "
        + CONTENT_KEY
        + ", "
        + TAGS_KEY
        + ", "
        + REFS_KEY
        + ", "
        + DESCRIPTION_KEY
        + ", "
        + CLIENT_DATA_KEY
        + ", "
        + TRASHED_KEY
        + ", "
        + TEMP_REF_ID_KEY
        + ")"
        + " VALUES (");
    sb.append(formatTextValue(eventToCache.getId()));
    sb.append(formatTextValue(eventToCache.getStreamId()));
    sb.append(formatLongValue(eventToCache.getTime()));
    sb.append(formatTextValue(eventToCache.getType()));
    sb.append(formatLongValue(eventToCache.getCreated()));
    sb.append(formatTextValue(eventToCache.getCreatedBy()));
    sb.append(formatLongValue(eventToCache.getModified()));
    sb.append(formatTextValue(eventToCache.getModifiedBy()));
    sb.append(formatLongValue(eventToCache.getDuration()));
    sb.append(formatTextValue(eventToCache.getContent()));
    sb.append(formatSetValue(eventToCache.getTags()));
    sb.append(formatSetValue(eventToCache.getReferences()));
    sb.append(formatTextValue(eventToCache.getDescription()));
    // attachments need to be added in their own table
    sb.append(formatTextValue(eventToCache.getClientDataAsString()));
    sb.append(formatTextValue(eventToCache.getTrashed()));
    sb.append(formatTextValue(eventToCache.getTempRefId()));
    sb.setLength(sb.length() - 1);
    sb.append(");");
    return sb.toString();
  }

  public static String updateEvent(Event eventToUpdate) {
    StringBuilder sb = new StringBuilder();
    sb.append("UPDATE "
      + EVENTS_TABLE_NAME
        + " SET "
        + ID_KEY
        + "="
        + formatTextValue(eventToUpdate.getId())
        + STREAM_ID_KEY
        + "="
        + formatTextValue(eventToUpdate.getStreamId())
        + TIME_KEY
        + "="
        + formatLongValue(eventToUpdate.getTime())
        + TYPE_KEY
        + "="
        + formatTextValue(eventToUpdate.getType())
        + CREATED_KEY
        + "="
        + formatLongValue(eventToUpdate.getCreated())
        + CREATED_BY_KEY
        + "="
        + formatTextValue(eventToUpdate.getCreatedBy())
        + MODIFIED_KEY
        + "="
        + formatLongValue(eventToUpdate.getModified())
        + MODIFIED_BY_KEY
        + "="
        + formatTextValue(eventToUpdate.getModifiedBy())
        + DURATION_KEY
        + "="
        + formatLongValue(eventToUpdate.getDuration())
        + CONTENT_KEY
        + "="
        + formatTextValue(eventToUpdate.getContent())
        + TAGS_KEY
        + "="
        + formatSetValue(eventToUpdate.getTags())
        + REFS_KEY
        + "="
        + formatSetValue(eventToUpdate.getReferences())
        + DESCRIPTION_KEY
        + "="
        + formatTextValue(eventToUpdate.getDescription())
        + CLIENT_DATA_KEY
        + "="
        + formatTextValue(eventToUpdate.getClientDataAsString())
        + TRASHED_KEY
        + "="
        + formatTextValue(eventToUpdate.getTrashed())
        + TEMP_REF_ID_KEY
        + "="
        + formatTextValue(eventToUpdate.getTempRefId()));
    sb.setLength(sb.length() - 1);
    sb.append(" WHERE " + ID_KEY + "=\'" + eventToUpdate.getId() + "\';");
    return sb.toString();
  }

  public static String deleteEvent(Event eventToDelete) {
    return "DELETE FROM " + EVENTS_TABLE_NAME + " WHERE ID=\'" + eventToDelete.getId() + "\';";
  }

  private static String formatLongValue(Long toAdd) {
    StringBuilder sb = new StringBuilder();
    if (toAdd != null) {
      sb.append(toAdd);
    } else {
      sb.append("NULL");
    }
    sb.append(",");
    return sb.toString();
  }

  private static String formatTextValue(Object prim) {
    StringBuilder sb = new StringBuilder();
    if (prim != null) {
      sb.append("\'" + prim + "\'");
    } else {
      sb.append("NULL");
    }
    sb.append(",");
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
    sb.append(",");
    return sb.toString();
  }

  public static String createEventsTable() {
    return "CREATE TABLE IF NOT EXISTS "
      + EVENTS_TABLE_NAME
        + "(ID TEXT PRIMARY KEY       NOT NULL,"
        + " STREAM_ID       TEXT      NOT NULL,"
        + " TIME            INTEGER   NOT NULL,"
        + " TYPE            TEXT      NOT NULL,"
        + " CREATED         INTEGER   NOT NULL,"
        + " CREATED_BY      TEXT      NOT NULL,"
        + " MODIFIED        INTEGER   NOT NULL,"
        + " MODIFIED_BY     TEXT      NOT NULL,"
        + " DURATION        INTEGER,"
        + " CONTENT         BLOB,"
        + " TAGS            TEXT,"
        + " REFS            TEXT,"
        + " DESCRIPTION     TEXT,"
        + " CLIENT_DATA     TEXT,"
        + " TRASHED         INTEGER,"
        + " TEMP_REF_ID     TEXT)";
  }

}
