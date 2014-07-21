package com.pryv.api.database;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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

  private String fieldNames;

  public SQLiteDBHelper() {
    initDB();
  }

  private void initDB() {
    try {
      Class.forName("org.sqlite.JDBC");
      dbConnection = DriverManager.getConnection("jdbc:sqlite:sqlite-db/test.db");
      System.out.println("Opened database successfully");

      statement = dbConnection.createStatement();

      StringBuilder fieldNamesBuilder = new StringBuilder();

      Field[] fields = Event.class.getDeclaredFields();
      for (Field field : fields) {
        fieldNamesBuilder.append(field.getName().toUpperCase());
        fieldNamesBuilder.append(",");
      }
      fieldNamesBuilder.deleteCharAt(fieldNamesBuilder.length() - 1);
      fieldNames = fieldNamesBuilder.toString();
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
        + "(ID TEXT PRIMARY KEY     NOT NULL,"
          + " NAME           TEXT    NOT NULL, "
          + " AGE            INT     NOT NULL, "
          + " ADDRESS        CHAR(50), "
          + " SALARY         REAL)";
    statement.executeUpdate(sql);
    statement.close();
    dbConnection.close();
  }
}
