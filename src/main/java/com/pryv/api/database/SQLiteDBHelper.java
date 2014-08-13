package com.pryv.api.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import com.pryv.api.Filter;
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

  private final String dbPath = "sqlite-db/test.db";

  private Connection dbConnection;

  private Logger logger = Logger.getInstance();

  public SQLiteDBHelper() {
    System.out.println("dbpath: " + dbPath);
    initDB(dbPath);
  }

  private void initDB(String path) {
    try {
      Class.forName("org.sqlite.JDBC");
      dbConnection = DriverManager.getConnection("jdbc:sqlite:" + path);
      logger.log("Opened database successfully");
      createEventsTable();
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
    String cmd = QueryGenerator.createEvent(eventToCache);
    logger.log("SQLiteDBHelper: addEvent: " + cmd);
    Statement statement = dbConnection.createStatement();
    statement.execute(cmd);
    statement.close();
  }

  public void updateEvent(Event eventToUpdate) throws SQLException {
    String cmd = QueryGenerator.updateEvent(eventToUpdate);
    logger.log("SQLiteDBHelper: updateEvent: " + cmd);
    Statement statement = dbConnection.createStatement();
    statement.execute(cmd);
    statement.close();
  }

  /**
   * Delete event from the SQLite database.
   *
   * @param eventToDelete
   * @throws SQLException
   */
  public void deleteEvent(Event eventToDelete) throws SQLException {
    String cmd = QueryGenerator.deleteEvent(eventToDelete);
    logger.log("SQLiteDBHelper: deleteEvent: " + cmd);
    Statement statement = dbConnection.createStatement();
    statement.execute(cmd);
    statement.close();
  }

  /**
   * Create Events table in the SQLite database.
   *
   * @throws SQLException
   */
  private void createEventsTable() throws SQLException {
    Statement statement = dbConnection.createStatement();
    String cmd = QueryGenerator.createEventsTable();
    logger.log("SQLiteDBHelper: createEventsTable: " + cmd);
    statement.executeUpdate(cmd);
    statement.close();
  }

  public void closeDb() throws SQLException {
    dbConnection.close();
  }

  public Map<String, Event> getEvents(Filter filter) {

    return new HashMap<String, Event>();
  }

  public void getStreams() {
    // TODO Auto-generated method stub

  }
}
