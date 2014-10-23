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
  public void
    updateOrCreateEvent(final Event eventToCache, final EventsCallback cacheEventsCallback) {
    new Thread() {
      @Override
      public void run() {
        try {
          String cmd = QueryGenerator.insertOrReplaceEvent(eventToCache);
          logger.log("SQLiteDBHelper: update or replace event: " + cmd);
          Statement statement = dbConnection.createStatement();
          statement.execute(cmd);
          statement.close();
          if (cacheEventsCallback != null) {
            cacheEventsCallback.onEventsSuccess("SQLiteDBHelper: Event cached", eventToCache, null);
          }
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
  public void updateOrCreateEvents(final Collection<Event> eventsToUpdate,
    final EventsCallback cacheEventsCallback) {
    new Thread() {
      @Override
      public void run() {

        for (Event event : eventsToUpdate) {
          try {
            Statement statement = dbConnection.createStatement();
            String cmd = QueryGenerator.insertOrReplaceEvent(event);
            logger.log("SQLiteDBHelper: update or create event : " + cmd);
            statement.execute(cmd);
            logger.log("inserted " + event.getClientId() + " into DB.");
            statement.close();
          } catch (SQLException e) {
            cacheEventsCallback.onEventsError(e.getMessage());
            e.printStackTrace();
          } catch (JsonProcessingException e) {
            cacheEventsCallback.onEventsError(e.getMessage());
            e.printStackTrace();
          }
        }
        cacheEventsCallback.onEventsSuccess("SQLiteDBHelper: Events updated", null, null);

      }
    }.start();
  }

  /**
   * Delete event from the SQLite database if its Trashed field in the DB is
   * already true
   *
   * @param eventToDelete
   *          the event to delete
   *
   * @param cacheEventsCallback
   *          callback to notify success or failure
   */
  public void deleteEvent(final Event eventToDelete, final EventsCallback cacheEventsCallback) {
    new Thread() {
      @Override
      public void run() {
        try {
          System.out.println("SQLiteDBHelper: deleting event with cid="
            + eventToDelete.getClientId());
          String fetchCmd = QueryGenerator.retrieveEvent(eventToDelete.getClientId());
          System.out.println("SQLiteDBHelper: fetching request: " + fetchCmd);
          Statement statement = dbConnection.createStatement();
          ResultSet result = statement.executeQuery(fetchCmd);
          while (result.next()) {
            Event retrievedEvent = new Event(result);
            if (retrievedEvent.getTrashed() == true) {
              // delete really
              String cmd = QueryGenerator.deleteEvent(retrievedEvent);
              statement.executeUpdate(cmd);
              cacheEventsCallback.onEventsSuccess("SQLiteDBHelper: Event with clientId="
                + eventToDelete.getClientId()
                  + " is deleted.", null, null);
            } else {
              // set to trashed
              retrievedEvent.setTrashed(true);
              String cmd = QueryGenerator.insertOrReplaceEvent(retrievedEvent);
              statement.executeUpdate(cmd);
              logger.log("SQLiteDBHelper: delete - set trashed=true for clientId="
                + retrievedEvent.getClientId());
              cacheEventsCallback.onEventsSuccess("SQLiteDBHelper: Event with clientId="
                + retrievedEvent.getClientId()
                  + " is trashed.", retrievedEvent, null);
            }
          }
          statement.close();
        } catch (SQLException e) {
          cacheEventsCallback.onEventsError(e.getMessage());
          e.printStackTrace();
        } catch (JsonParseException e) {
          cacheEventsCallback.onEventsError(e.getMessage());
          e.printStackTrace();
        } catch (JsonMappingException e) {
          cacheEventsCallback.onEventsError(e.getMessage());
          e.printStackTrace();
        } catch (IOException e) {
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
  public void getEvents(final Filter filter, final EventsCallback cacheEventsCallback) {
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
            retrievedEvents.put(retrievedEvent.getClientId(), retrievedEvent);
          }
          cacheEventsCallback.onEventsRetrievalSuccess(retrievedEvents, 0);
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
  public void updateOrCreateStream(final Stream streamToCache,
    final StreamsCallback cacheStreamsCallback) {
    new Thread() {
      @Override
      public void run() {
        try {
          Statement statement = dbConnection.createStatement();
          String cmd = QueryGenerator.insertOrReplaceStream(streamToCache);
          logger.log("SQLiteDBHelper: update or create Stream : " + cmd);
          statement.executeUpdate(cmd);
          if (streamToCache.getChildren() != null) {
            // TODO do recursively maybe
            Set<Stream> children = new HashSet<Stream>();
            retrieveAllChildren(children, streamToCache);
            for (Stream childStream : children) {
              cmd = QueryGenerator.insertOrReplaceStream(childStream);
              statement.execute(cmd);
              logger.log("SQLiteDBHelper: add child Stream: " + cmd);
            }
          }
          statement.close();
          cacheStreamsCallback.onStreamsSuccess("SQLiteDBHelper: Stream updated or created",
            streamToCache);
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
  public void updateOrCreateStreams(final Collection<Stream> streamsToCache,
    final StreamsCallback cacheStreamsCallback) {
    new Thread() {
      @Override
      public void run() {
        logger.log("update streamS called");
        for (Stream stream : streamsToCache) {
          try {
            Statement statement = dbConnection.createStatement();
            String cmd = QueryGenerator.insertOrReplaceStream(stream);
            logger.log("SQLiteDBHelper: update or create Stream stream: id="
              + stream.getId()
                + ", name="
                + stream.getName());
            logger.log("SQLiteDBHelper: update or create Stream: " + cmd);
            statement.executeUpdate(cmd);
            cacheStreamsCallback.onStreamsSuccess(
              "SQLiteDBHelper: child stream updated or created", stream);
            if (stream.getChildren() != null) {
              Set<Stream> children = new HashSet<Stream>();
              retrieveAllChildren(children, stream);
              for (Stream childStream : children) {
                cmd = QueryGenerator.insertOrReplaceStream(childStream);
                logger.log("SQLiteDBHelper: add child Stream: " + cmd);

                statement.execute(cmd);
                cacheStreamsCallback.onStreamsSuccess(
                  "SQLiteDBHelper: child stream updated or created", childStream);
              }
            }
            statement.close();
          } catch (SQLException e) {
            cacheStreamsCallback.onStreamError(e.getMessage());
            e.printStackTrace();
          }
        }
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
   * @param mergeEventsWithParent
   * @param cacheStreamsCallback
   *          callback to notify success or failure
   *
   */
  public void deleteStream(final Stream streamToDelete, boolean mergeEventsWithParent,
    final StreamsCallback cacheStreamsCallback) {
    new Thread() {
      @Override
      public void run() {
        String cmd;
        try {
          // retrieve Stream
          cmd = QueryGenerator.retrieveStream(streamToDelete.getClientId());
          Statement statement = dbConnection.createStatement();
          ResultSet result = statement.executeQuery(cmd);
          while (result.next()) {
            Stream retrievedStreamToDelete = new Stream(result);
            // check trashed field
            if (retrievedStreamToDelete.getTrashed() == true) {
              // if true: delete Really
              logger.log("SQLiteDBHelper: delete Stream with id="
                + streamToDelete.getId()
                  + ", cid="
                  + streamToDelete.getClientId());
              // find parent Stream and remove streamToDelete from its children
              String parentStreamClientId = retrievedStreamToDelete.getParentClientId();
              String parentId = retrievedStreamToDelete.getParentId();
              if (parentStreamClientId != null) {
                cmd = QueryGenerator.retrieveStream(parentStreamClientId);
                result = statement.executeQuery(cmd);
                while (result.next()) {
                  Stream parentStream = new Stream(result);
                  parentStream.removeChildStream(retrievedStreamToDelete);
                  cmd = QueryGenerator.insertOrReplaceStream(parentStream);
                }
              }
              // delete Stream
              cmd = QueryGenerator.deleteStream(retrievedStreamToDelete);
              statement.executeUpdate(cmd);

              if (mergeEventsWithParent == true && parentStreamClientId != null) {
                // if mergeEventsWithParent is true
                // fetch all these events, modify their parent stream id and cid
                // and save them
                Filter deleteFilter = new Filter();
                deleteFilter.addStreamClientId(retrievedStreamToDelete.getClientId());
                cmd = QueryGenerator.retrieveEvents(deleteFilter);
                result = statement.executeQuery(cmd);
                Event updateEvent = null;
                while (result.next()) {
                  try {
                    updateEvent = new Event(result);
                    updateEvent.setStreamClientId(parentStreamClientId);
                    updateEvent.setStreamId(parentId);
                    updateOrCreateEvent(updateEvent, null);
                  } catch (JsonParseException e) {
                    e.printStackTrace();
                    cacheStreamsCallback.onStreamError(e.getMessage());
                  } catch (JsonMappingException e) {
                    e.printStackTrace();
                    cacheStreamsCallback.onStreamError(e.getMessage());
                  } catch (IOException e) {
                    e.printStackTrace();
                    cacheStreamsCallback.onStreamError(e.getMessage());
                  }
                }

              } else {
                // else do nothing (or delete them?)
              }
              cacheStreamsCallback.onStreamsSuccess("SQLiteDBHelper: Stream with cid="
                + retrievedStreamToDelete.getClientId()
                  + ", id="
                  + retrievedStreamToDelete.getId()
                  + " deleted.", retrievedStreamToDelete);
            } else {
              // set its trashed field to true and save it
              logger.log("SQLiteDBHelper: trash Stream with id="
                + streamToDelete.getId()
                  + ", cid="
                  + streamToDelete.getClientId());
              retrievedStreamToDelete.setTrashed(true);
              updateOrCreateStream(retrievedStreamToDelete, cacheStreamsCallback);
              cacheStreamsCallback.onStreamsSuccess("SQLiteDBHelper: Stream with cid="
                + retrievedStreamToDelete.getClientId()
                  + ", id="
                  + retrievedStreamToDelete.getId()
                  + " trashed.", retrievedStreamToDelete);
            }
          }

          // behaviour not defined in API - may be added later (should also
          // delete
          // these streams' events)
          // if (streamToDelete.getChildren() != null) {
          // for (Stream childStream : streamToDelete.getChildren()) {
          // cmd = QueryGenerator.deleteStream(childStream);
          // int done = statement.executeUpdate(cmd);
          // logger.log("SQLiteDBHelper: delete child Stream with name "
          // + childStream.getName()
          // + ": "
          // + cmd);
          // // set trashed to true
          // if (done == 0) {
          // updateOrCreateStream(childStream, cacheStreamsCallback);
          // logger.log("SQLiteDBHelper: delete - set trashed=true for clientId="
          // + streamToDelete.getClientId());
          // }
          // }
          // }
          // cmd = QueryGenerator.deleteStream(streamToDelete);
          // logger.log("SQLiteDBHelper: deleteStream: " + cmd);
          // int done = statement.executeUpdate(cmd);
          // // set trashed to true
          // if (done == 0) {
          // updateOrCreateStream(streamToDelete, cacheStreamsCallback);
          // }
          statement.close();
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
  public void getStreams(final StreamsCallback cacheStreamsCallback) {
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
            allStreams.put(retrievedStream.getClientId(), retrievedStream);
          }
          logger.log("SQLiteDBHelper: retrieved " + allStreams.size() + " stream(s).");
          Map<String, Stream> rootStreams = new HashMap<String, Stream>();
          for (Stream stream : allStreams.values()) {
            String pid = stream.getParentClientId();
            if (pid != null) {
              // add this stream as a child
              logger.log("SQLiteDBHelper: adding childStream: cid="
                + stream.getClientId()
                  + ", name="
                  + stream.getName());
              allStreams.get(pid).addChildStream(stream);
            } else {
              logger.log("SQLiteDBHelper: adding rootStream: cid="
                + stream.getClientId()
                  + ", name="
                  + stream.getName());
              rootStreams.put(stream.getClientId(), stream);
            }
          }
          cacheStreamsCallback.onStreamsRetrievalSuccess(rootStreams, 0);
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
