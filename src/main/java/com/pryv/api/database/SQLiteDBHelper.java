package com.pryv.api.database;

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

/**
 *
 * Utilitary helper class to manipulate SQLite database
 *
 * @author ik
 *
 */
public class SQLiteDBHelper {

  private Connection dbConnection;
  private Statement statement;

  private List<String> eventFields;

  private List<String> streamFields;

  public SQLiteDBHelper() {
    initDB();
  }

  private void initDB() {
    try {
      Class.forName("org.sqlite.JDBC");
      dbConnection = DriverManager.getConnection("jdbc:sqlite:sqlite-db/test.db");
      System.out.println("Opened database successfully");

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

  private void cacheEvent(Event eventToCache) {

  }

  private void createEventsTable() throws SQLException {
    statement = dbConnection.createStatement();
    String sql =
      "CREATE TABLE EVENT "
        + "(  ID TEXT PRIMARY KEY       NOT NULL,"
          + " STREAMID        TEXT      NOT NULL, "
          + " TIME            INTEGER   NOT NULL, "
          + " TYPE            TEXT      NOT NULL, "
          + " CREATED         INTEGER   NOT NULL, "
          + " CREATEDBY       TEXT      NOT NULL, "
          + " MODIFIED        INTEGER   NOT NULL, "
          + " MODIFIEDBY      TEXT      NOT NULL, "
          + " DURATION        INTEGER, "
          + " CONTENT         BLOB, "
          + " TAGS            TEXT, "
          + " REFERENCES      TEXT, "
          + " DESCRIPTION     TEXT, "
          + " ATTACHMENTS     TEXT, "
          + " CLIENTDATA      TEXT, "
          + " TRASHED         INTEGER, "
          + " TEMPREFID       TEXT)";
    statement.executeUpdate(sql);
    statement.close();
    dbConnection.close();
  }

  public Map<String, Event> getEvents() {

    return new HashMap<String, Event>();
  }
}
