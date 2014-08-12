package com.pryv.api.database;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pryv.api.model.Event;
import com.pryv.utils.Logger;

/**
 *
 * Utilitary helper class to manipulate SQLite database
 *
 * @author ik
 *
 */
public class SQLiteDBHelper {

  private static final String EVENTS_TABLE_NAME = "EVENTS";

  private final String dbName = "../../../../sqlite-db/test.db";
  private File dbFile;

  private Connection dbConnection;
  private Statement statement;

  private Logger logger = Logger.getInstance();

  private List<String> eventFields;

  private List<String> streamFields;

  public SQLiteDBHelper() {
    dbFile = new File("sqlite-db/test.db");
    System.out.println("dbpath: " + dbFile.getAbsolutePath());
    // String path = this.getClass().getResource(dbFile.getPath()).getPath();
    initDB(dbFile.getPath());
  }

  private void initDB(String path) {
    try {
      Class.forName("org.sqlite.JDBC");
      dbConnection = DriverManager.getConnection("jdbc:sqlite:" + path);
      logger.log("Opened database successfully");

      statement = dbConnection.createStatement();

      eventFields = new ArrayList<String>();
      Field[] fields = Event.class.getDeclaredFields();
      for (Field field : fields) {
        eventFields.add(field.getName().toUpperCase());
      }
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Insert event into the SQLite database.
   *
   * @param eventToCache
   *          the event to insert
   * @throws SQLException
   */
  public void addEvent(Event eventToCache) throws SQLException {
    // statement = dbConnection.prepareStatement("INSERT INTO " +
    // EVENTS_TABLE_NAME + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
    // statement.s
    String cmd =
      "INSERT INTO "
        + EVENTS_TABLE_NAME
          + " (ID, STREAM_ID, TIME, TYPE, CREATED, CREATED_BY,"
          + " MODIFIED, MODIFIED_BY, DURATION, CONTENT, TAGS, REFS, DESCRIPTION,"
          + " CLIENT_DATA, TRASHED, TEMP_REF_ID)"
          + " VALUES ("
          + eventToCache.toSQL()
          + ")";

    logger.log("SQLiteDBHelper: addEvent: " + cmd);
    statement.execute(cmd);
  }

  /**
   * Delete event from the SQLite database.
   *
   * @param eventToDelete
   * @throws SQLException
   */
  public void deleteEvent(Event eventToDelete) throws SQLException {
    String cmd =
      "DELETE FROM " + EVENTS_TABLE_NAME + " WHERE ID=\'" + eventToDelete.getId() + "\';";
    statement.execute(cmd);
  }

  public void createEventsTable() throws SQLException {
    statement = dbConnection.createStatement();
    String sql =
      "CREATE TABLE IF NOT EXISTS EVENTS "
        + "(ID TEXT PRIMARY   KEY       NOT NULL,"
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
    statement.executeUpdate(sql);
  }

  public void closeDb() throws SQLException {
    statement.close();
    dbConnection.close();
  }

  public Map<String, Event> getEvents() {

    return new HashMap<String, Event>();
  }

  public void getStreams() {
    // TODO Auto-generated method stub

  }
}
