package com.pryv.api.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import com.pryv.api.EventsCallback;
import com.pryv.api.Filter;
import com.pryv.api.StreamsCallback;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;
import com.pryv.utils.Logger;

/**
 *
 * Utilitary helper class to manipulate SQLite database. Instanciating an
 * SQLiteDBHelper object will establish a connection to the SQLite database and
 * create the tables if required.
 *
 * @author ik
 *
 */
public class SQLiteDBHelper {

  private final String dbPath = "sqlite-db/";
  private final String initDBerrorMessage = "Database initialization error: ";

  // callback interfaces to send async queries requests back to Cache module
  private EventsCallback eventsCallback;
  private StreamsCallback<Map<String, Stream>> streamsCallback;

  private Connection dbConnection;
  private Logger logger = Logger.getInstance();

  /**
   * SQLiteDBHelper constructor. Creates and Connects to the SQLite database
   * located in ./sqlite-db/name. Creates the tables if required.
   *
   * @param name
   *          the name of the database
   *
   * @throws SQLException
   * @throws ClassNotFoundException
   */
  public SQLiteDBHelper(String name, DBinitCallback initCallback) {
    logger.log("SQLiteDBHelper: init DB in: " + dbPath + name);
    initDB(dbPath + name, initCallback);
  }

  /**
   * Connects to the SQLite database. Creates tables if required.
   *
   * @param path
   * @throws SQLException
   * @throws ClassNotFoundException
   */
  private void initDB(final String path, DBinitCallback initCallback) {
    try {
      Class.forName("org.sqlite.JDBC");
    } catch (ClassNotFoundException e) {
      initCallback.onError(initDBerrorMessage + e.getMessage());
      e.printStackTrace();
    }
    try {
      dbConnection = DriverManager.getConnection("jdbc:sqlite:" + path);
      logger.log("Opened database successfully");
      createEventsTable();
      createSteamsTable();
    } catch (SQLException e) {
      initCallback.onError(initDBerrorMessage + e.getMessage());
      e.printStackTrace();
    }

  }

  /**
   * Inserts Event into the SQLite database.
   *
   * @param eventToCache
   *          the event to insert
   * @throws SQLException
   *           if an Event with the same ID already exists in the database.
   */
  public void createEvent(Event eventToCache) throws SQLException {
    String cmd = QueryGenerator.insertEvent(eventToCache);
    logger.log("SQLiteDBHelper: addEvent: " + cmd);
    Statement statement = dbConnection.createStatement();
    statement.execute(cmd);
    statement.close();
  }

  /**
   * Updates Event in the SQLite database. The "modified" fields are compared in
   * order to determine if an update is necessary.
   *
   * @param eventToUpdate
   * @throws SQLException
   */
  public void updateEvent(Event eventToUpdate) throws SQLException {
    String cmd = QueryGenerator.updateEvent(eventToUpdate);
    logger.log("SQLiteDBHelper: updateEvent: " + cmd);
    Statement statement = dbConnection.createStatement();
    statement.executeUpdate(cmd);
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
    statement.executeUpdate(cmd);
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
   * Insert Stream and its children Streams into the SQLite database.
   *
   * @param streamToCache
   *          the stream to insert
   * @throws SQLException
   */
  public void addStream(Stream streamToCache) throws SQLException {
    Statement statement = dbConnection.createStatement();
    String cmd = QueryGenerator.insertStream(streamToCache);
    logger.log("SQLiteDBHelper: addStream: " + cmd);
    statement.executeUpdate(cmd);
    if (streamToCache.getChildren() != null) {
      for (Stream childStream : streamToCache.getChildren()) {
        cmd = QueryGenerator.insertStream(childStream);
        statement.execute(cmd);
        logger.log("SQLiteDBHelper: add child Stream: " + cmd);
      }
    }
    statement.close();
  }

  /**
   * Updates Stream in the SQLite database. The "modified" fields are compared
   * in order to determine if an update is necessary.
   *
   * @param streamToUpdate
   * @throws SQLException
   */
  public void updateStream(Stream streamToUpdate) throws SQLException {
    String cmd = QueryGenerator.updateStream(streamToUpdate);
    logger.log("SQLiteDBHelper: updateStream: " + cmd);
    Statement statement = dbConnection.createStatement();
    statement.executeUpdate(cmd);
    statement.close();
  }

  /**
   * Delete Stream and all its children Streams from the SQLite database.
   *
   * @param streamToDelete
   * @throws SQLException
   */
  public void deleteStream(Stream streamToDelete) throws SQLException {
    String cmd;
    Statement statement = dbConnection.createStatement();
    if (streamToDelete.getChildren() != null) {
      for (Stream childStream : streamToDelete.getChildren()) {
        cmd = QueryGenerator.deleteStream(childStream);
        statement.executeUpdate(cmd);
        logger.log("SQLiteDBHelper: delete child Stream with name "
          + childStream.getName()
            + ": "
            + cmd);
      }
    }
    cmd = QueryGenerator.deleteStream(streamToDelete);
    logger.log("SQLiteDBHelper: deleteStream: " + cmd);
    statement.executeUpdate(cmd);
    statement.close();
  }

  /**
   * Retrieves Streams from the SQLite database
   *
   * @return Map<String, Event> events, with event ID as key.
   * @throws SQLException
   */
  public Map<String, Stream> getStreams() throws SQLException {
    String cmd = QueryGenerator.retrieveStreams();
    logger.log("SQLiteDBHelper: getStreams: " + cmd);
    Statement statement = dbConnection.createStatement();
    ResultSet result = statement.executeQuery(cmd);
    Map<String, Stream> retrievedStreams = new HashMap<String, Stream>();
    while (result.next()) {
      // get the requested Streams
      Stream retrievedStream = new Stream(result);
      retrievedStreams.put(retrievedStream.getId(), retrievedStream);
    }
    for (Stream stream : retrievedStreams.values()) {
      String pid = stream.getParentId();
      if (pid != null) {
        // add this stream as a child
        retrievedStreams.get(pid).addChildStream(stream);
        // remove it from retrievedStreams.
        retrievedStreams.remove(stream.getId());
      }
    }
    return retrievedStreams;
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

  /**
   * closes connection to SQLite database.
   *
   * @throws SQLException
   */
  public void closeDb() throws SQLException {
    dbConnection.close();
  }
}
