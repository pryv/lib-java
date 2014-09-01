package com.pryv.api.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
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

  private Connection dbConnection;

  private Logger logger = Logger.getInstance();

  /**
   * SQLiteDBHelper constructor. Creates and Connects to the SQLite database
   * located in ./sqlite-db/name. Creates the tables if required.
   *
   * @param name
   *          the name of the database
   * @param initCallback
   *          callback to notify failure
   */
  public SQLiteDBHelper(String name, DBinitCallback initCallback) {
    logger.log("SQLiteDBHelper: init DB in: " + dbPath + name);
    initDB(dbPath + name, initCallback);
  }

  /**
   * Connects to the SQLite database. Creates tables if required.
   *
   * @param path
   */
  private void initDB(final String path, DBinitCallback initCallback) {
    try {
      Class.forName("org.sqlite.JDBC");
      dbConnection = DriverManager.getConnection("jdbc:sqlite:" + path);
      createEventsTable();
      createSteamsTable();
    } catch (SQLException e) {
      initCallback.onError(initDBerrorMessage + e.getMessage());
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      initCallback.onError(initDBerrorMessage + e.getMessage());
      e.printStackTrace();
    }

  }

  /**
   * Inserts Event into the SQLite database.
   *
   * @param eventToCache
   *          the event to insert
   * @param cacheEventsCallback
   *          callback to notify success or failure
   */
  public void updateOrCreateEvent(Event eventToCache, EventsCallback cacheEventsCallback) {
    new Thread() {
      @Override
      public void run() {
        try {
          String cmd = QueryGenerator.insertOrReplaceEvent(eventToCache);
          logger.log("SQLiteDBHelper: addEvent: " + cmd);
          Statement statement = dbConnection.createStatement();
          statement.execute(cmd);
          statement.close();
          cacheEventsCallback.onEventsSuccess("item added");
        } catch (SQLException e) {
          cacheEventsCallback.onEventsError(e.getMessage());
        } catch (JsonProcessingException e) {
          cacheEventsCallback.onEventsError(e.getMessage());
        }
      }
    }.start();
  }

  /**
   * Update Events in the SQLite database. used only when the cache receives
   * events from online.
   *
   * @param eventsToCache
   *          the events to insert in the cache
   * @param cacheEventsCallback
   *          callback to notify success or failure
   */
  public void updateOrCreateEvents(Collection<Event> eventsToUpdate, EventsCallback cacheEventsCallback) {
    new Thread() {
      @Override
      public void run() {

        for (Event event : eventsToUpdate) {
          try {
            Statement statement = dbConnection.createStatement();
            String cmd = QueryGenerator.insertOrReplaceEvent(event);
            logger.log("SQLiteDBHelper: updateEvent: " + cmd);
            statement.execute(cmd);
            logger.log("inserted " + event.getId() + " into DB.");
            statement.close();
          } catch (SQLException e) {
            cacheEventsCallback.onEventsError(e.getMessage());
            e.printStackTrace();
          } catch (JsonProcessingException e) {
            cacheEventsCallback.onEventsError(e.getMessage());
            e.printStackTrace();
          }
        }
        cacheEventsCallback.onEventsSuccess("events updated");

      }
    }.start();
  }

  /**
   * Delete event from the SQLite database.
   *
   * @param eventToDelete
   *          the event to delete
   *
   * @param cacheEventsCallback
   *          callback to notify success or failure
   */
  public void deleteEvent(Event eventToDelete, EventsCallback cacheEventsCallback) {
    new Thread() {
      @Override
      public void run() {
        try {
          String cmd = QueryGenerator.deleteEvent(eventToDelete);
          logger.log("SQLiteDBHelper: deleteEvent: " + cmd);
          Statement statement = dbConnection.createStatement();
          int done = statement.executeUpdate(cmd);
          // set trashed field to 1
          if (done == 0) {
            updateOrCreateEvent(eventToDelete, cacheEventsCallback);
          }
          statement.close();
          cacheEventsCallback.onEventsSuccess("Event deleted");
        } catch (SQLException e) {
          cacheEventsCallback.onEventsError(e.getMessage());
          e.printStackTrace();
        }
      }
    }.start();

  }

  /**
   * Retrieves Events from the SQLite database according to the provided filter.
   * Filter can be null.
   *
   * @param filter
   *          the filter used for the retrieval, use null if no filter is
   *          required.
   * @param cacheEventsCallback
   *          callback to return retrieved events
   */
  public void getEvents(Filter filter, EventsCallback cacheEventsCallback) {
    new Thread() {
      @Override
      public void run() {
        try {
          String cmd = QueryGenerator.retrieveEvents(filter);
          logger.log("SQLiteDBHelper: getEvents: " + cmd);
          Statement statement = dbConnection.createStatement();
          ResultSet result = statement.executeQuery(cmd);
          Map<String, Event> retrievedEvents = new HashMap<String, Event>();
          while (result.next()) {
            Event retrievedEvent = new Event(result);
            retrievedEvents.put(retrievedEvent.getId(), retrievedEvent);
          }
          cacheEventsCallback.onCacheRetrieveEventsSuccess(retrievedEvents);
        } catch (SQLException e) {
          cacheEventsCallback.onEventsRetrievalError(e.getMessage());
          e.printStackTrace();
        } catch (JsonParseException e) {
          cacheEventsCallback.onEventsRetrievalError(e.getMessage());
          e.printStackTrace();
        } catch (JsonMappingException e) {
          cacheEventsCallback.onEventsRetrievalError(e.getMessage());
          e.printStackTrace();
        } catch (IOException e) {
          cacheEventsCallback.onEventsRetrievalError(e.getMessage());
          e.printStackTrace();
        }
      }
    }.start();
  }

  /**
   * Insert Stream and its children Streams into the SQLite database.
   *
   * @param streamToCache
   *          the stream to insert
   * @param cacheStreamsCallback
   *          callback to notify success or faiure
   */
  public void updateOrCreateStream(Stream streamToCache, StreamsCallback cacheStreamsCallback) {
    new Thread() {
      @Override
      public void run() {
        try {
          Statement statement = dbConnection.createStatement();
          String cmd = QueryGenerator.insertOrReplaceStream(streamToCache);
          logger.log("SQLiteDBHelper: update or replace Stream : " + cmd);
          statement.executeUpdate(cmd);
          if (streamToCache.getChildren() != null) {
            Set<Stream> children = new HashSet<Stream>();
            retrieveAllChildren(children, streamToCache);
            for (Stream childStream : children) {
              cmd = QueryGenerator.insertOrReplaceStream(childStream);
              statement.execute(cmd);
              logger.log("SQLiteDBHelper: add child Stream: " + cmd);
            }
          }
          statement.close();
          cacheStreamsCallback.onStreamsSuccess("Stream created");
        } catch (SQLException e) {
          cacheStreamsCallback.onStreamError(e.getMessage());
        }
      }
    }.start();
  }


  /**
   * Update Streams in the SQLite database. used only when the cache receives
   * streams from online.
   *
   * @param streamsToCache
   *          the streams to cache
   * @param cacheStreamsCallback
   *          callback to notify success or failure
   */
  public void updateOrCreateStreams(Collection<Stream> streamsToCache,
    StreamsCallback cacheStreamsCallback) {
    new Thread() {
      @Override
      public void run() {
        logger.log("update streamS called");
        for (Stream stream : streamsToCache) {
          try {
            Statement statement = dbConnection.createStatement();
            String cmd = QueryGenerator.insertOrReplaceStream(stream);
            logger.log("SQLiteDBHelper: update or replace Stream: " + cmd);
            statement.executeUpdate(cmd);
            logger.log("SQLiteDBHelper: add Stream stream: id="
              + stream.getId()
                + ", name="
                + stream.getName());
            if (stream.getChildren() != null) {
              Set<Stream> children = new HashSet<Stream>();
              retrieveAllChildren(children, stream);
              for (Stream childStream : children) {
                cmd = QueryGenerator.insertOrReplaceStream(childStream);
                statement.execute(cmd);
                logger.log("SQLiteDBHelper: add child Stream: " + cmd);
              }
            }
            statement.close();
          } catch (SQLException e) {
            cacheStreamsCallback.onStreamError(e.getMessage());
            e.printStackTrace();
          }
        }
        cacheStreamsCallback.onStreamsSuccess("Streams updated");
      }
    }.start();
  }

  /**
   * gathers all descendants of Stream into allStreams
   *
   * @param childrenStreams
   *          a Set<Stream> into which all children are put
   * @param parentStream
   *          the stream whose children are gathered
   */
  private void retrieveAllChildren(Set<Stream> childrenStreams, Stream parentStream) {
    if (parentStream.getChildren() != null) {
      for (Stream childStream : parentStream.getChildren()) {
        childrenStreams.add(childStream);
        retrieveAllChildren(childrenStreams, childStream);
      }
    }
  }

  /**
   * Delete Stream and all its children Streams from the SQLite database.
   *
   * @param streamToDelete
   *          the stream to delete
   * @param cacheStreamsCallback
   *          callback to notify success or failure
   */
  public void deleteStream(Stream streamToDelete, StreamsCallback cacheStreamsCallback) {
    new Thread() {
      @Override
      public void run() {
        String cmd;
        try {
          Statement statement = dbConnection.createStatement();
          if (streamToDelete.getChildren() != null) {
            for (Stream childStream : streamToDelete.getChildren()) {
              cmd = QueryGenerator.deleteStream(childStream);
              int done = statement.executeUpdate(cmd);
              logger.log("SQLiteDBHelper: delete child Stream with name "
                + childStream.getName()
                  + ": "
                  + cmd);
              // set trashed to true
              if (done == 0) {
                updateOrCreateStream(childStream, cacheStreamsCallback);
              }
            }
          }
          cmd = QueryGenerator.deleteStream(streamToDelete);
          logger.log("SQLiteDBHelper: deleteStream: " + cmd);
          int done = statement.executeUpdate(cmd);
          // set trashed to true
          if (done == 0) {
            updateOrCreateStream(streamToDelete, cacheStreamsCallback);
          }
          statement.close();
          cacheStreamsCallback.onStreamsSuccess("Stream deleted");
        } catch (SQLException e) {
          cacheStreamsCallback.onStreamError(e.getMessage());
          e.printStackTrace();
        }
      }
    }.start();
  }

  /**
   * Retrieves Streams from the SQLite database
   *
   * @param cacheStreamsCallback
   *          callback to which the streams are returned.
   */
  public void getStreams(StreamsCallback cacheStreamsCallback) {
    new Thread() {
      @Override
      public void run() {
        try {
          String cmd = QueryGenerator.retrieveStreams();
          logger.log("SQLiteDBHelper: getStreams: " + cmd);
          Statement statement = dbConnection.createStatement();
          ResultSet result = statement.executeQuery(cmd);
          Map<String, Stream> allStreams = new HashMap<String, Stream>();
          while (result.next()) {
            // get the requested Streams
            Stream retrievedStream = new Stream(result);
            allStreams.put(retrievedStream.getId(), retrievedStream);
          }
          logger.log("SQLiteDBHelper: retrieved " + allStreams.size() + " streams.");
          Map<String, Stream> rootStreams = new HashMap<String, Stream>();
          for (Stream stream : allStreams.values()) {
            String pid = stream.getParentId();
            if (pid != null) {
              // add this stream as a child
              allStreams.get(pid).addChildStream(stream);
            } else {
              rootStreams.put(stream.getId(), stream);
            }
          }
          cacheStreamsCallback.onCacheRetrieveStreamSuccess(rootStreams);
        } catch (SQLException e) {
          cacheStreamsCallback.onStreamError(e.getMessage());
          e.printStackTrace();
        }
      }
    }.start();
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
