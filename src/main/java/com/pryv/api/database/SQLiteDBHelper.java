package com.pryv.api.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import com.pryv.api.Filter;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;
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

  /**
   * SQLiteDBHelper constructor. Connects to the SQLite database. Creates tables
   * if required.
   */
  public SQLiteDBHelper() {
    System.out.println("dbpath: " + dbPath);
    initDB(dbPath);
  }

  /**
   * Connects to the SQLite database. Creates tables if required.
   *
   * @param path
   */
  private void initDB(String path) {
    try {
      Class.forName("org.sqlite.JDBC");
      dbConnection = DriverManager.getConnection("jdbc:sqlite:" + path);
      logger.log("Opened database successfully");
      createEventsTable();
      createSteamsTable();
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

  /**
   * Update event in the SQLite database.
   *
   * @param eventToUpdate
   *          the event to update
   * @throws SQLException
   */
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
   * Retrieves Events from the SQLite database according to the provided filter.
   * Filter can be null.
   *
   * @param filter
   *          the filter used for the retrieval, use null if no filter is
   *          required.
   * @return Map<String, Event> events, with event ID as key.
   * @throws SQLException
   */
  public Map<String, Event> getEvents(Filter filter) throws SQLException {
    String cmd = QueryGenerator.retrieveEvents(filter);
    logger.log("SQLiteDBHelper: getEvents: " + cmd);
    Statement statement = dbConnection.createStatement();
    ResultSet result = statement.executeQuery(cmd);
    Map<String, Event> retrievedEvents = new HashMap<String, Event>();
    while (result.next()) {
      Event retrievedEvent = new Event(result);
      retrievedEvents.put(retrievedEvent.getId(), retrievedEvent);
    }
    return retrievedEvents;
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
    statement.execute(cmd);
    statement.close();
  }

  /**
   * Create Streams table in the SQLite database.
   *
   * @throws SQLException
   */
  private void createSteamsTable() throws SQLException {
    Statement statement = dbConnection.createStatement();
    String cmd = QueryGenerator.createStreamsTable();
    logger.log("SQLiteDBHelper: createStreamsTable: " + cmd);
    statement.execute(cmd);
    statement.close();
  }

  public void closeDb() throws SQLException {
    dbConnection.close();
  }

  public void getStreams() {
    // TODO Auto-generated method stub

  }

  /**
   * Insert Stream into the SQLite database.
   *
   * @param streamToCache
   *          the stream to insert
   * @throws SQLException
   */
  public void addStream(Stream streamToCache) throws SQLException {
    String cmd = QueryGenerator.createStream(streamToCache);
    logger.log("SQLiteDBHelper: addStream: " + cmd);
    Statement statement = dbConnection.createStatement();
    statement.execute(cmd);
    statement.close();
  }
}
