package com.pryv.database;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.pryv.Pryv;
import com.pryv.api.OnlineEventsAndStreamsManager;
import com.pryv.interfaces.EventsCallback;
import com.pryv.Filter;
import com.pryv.interfaces.GetStreamsCallback;
import com.pryv.interfaces.StreamsCallback;
import com.pryv.interfaces.UpdateCacheCallback;
import com.pryv.model.Event;
import com.pryv.model.Stream;
import com.pryv.interfaces.GetEventsCallback;
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

  private final String initDBerrorMessage = "Database initialization error: ";

  // the DB Connection
  private Connection dbConnection;

  // weak reference to Pryv's Connection
  private WeakReference<com.pryv.Connection> weakConnection;

  private Filter scope;
  private OnlineEventsAndStreamsManager api;
  private Double lastUpdate;

  private Logger logger = Logger.getInstance();

  /**
   * SQLiteDBHelper constructor. Creates and Connects to the SQLite database
   * located in ./sqlite-db/name. Creates the tables if required.
   *
   * @param cacheFolderPath
   *          the path to the caching folder
   * @param weakConnection
   * @param initCallback
   *          callback to notify failure
   */
  public SQLiteDBHelper(Filter scope, String cacheFolderPath, OnlineEventsAndStreamsManager api,
                        WeakReference<com.pryv.Connection> weakConnection,
                        DBinitCallback initCallback) {
    this.scope = scope;
    this.api = api;
    this.weakConnection = weakConnection;
    logger.log("SQLiteDBHelper: init DB in: " + cacheFolderPath + Pryv.DATABASE_NAME);
    initDB(cacheFolderPath + Pryv.DATABASE_NAME, initCallback);
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
   * method used to update the cache with data obtained from the Pryv online API
   *
   * @param updateCacheCallback
   */
  public void update(final UpdateCacheCallback updateCacheCallback) {
    Filter filter = new Filter();
    filter.setIncludeDeletions(true);
    filter.setModifiedSince(lastUpdate);
    if (scope != null && scope.getStreams() != null) {
      for (Stream scopeStream : scope.getStreams()) {
        filter.addStream(scopeStream);
      }
    }
    api.getEvents(filter, new GetEventsCallback() {

      @Override
      public void cacheCallback(List<Event> events, Map<String, Double> eventDeletions) {}

      @Override
      public void onCacheError(String errorMessage) {}

      @Override
      public void apiCallback(List<Event> events, Map<String, Double> eventDeletions, Double serverTime) {
        /*for (Event event: events) {

        }

        for (String deletionId: eventDeletions.keySet()) {

        }*/

        updateCacheCallback.apiCallback(null, null, null, null, serverTime);


      }

      @Override
      public void onApiError(String errorMessage, Double serverTime) {
        updateCacheCallback.onError(errorMessage, serverTime);
      }
    });
  }

  /**
   * Inserts Event into the SQLite database.
   *
   * @param eventToCache
   *          the event to insert
   * @param eventsCallback
   *          callback to notify succeeventsCallback.onCacheError(e.getMessage());ss or failure
   */
  public void createEvent(final Event eventToCache, final EventsCallback eventsCallback) {
    new Thread() {
      @Override
      public void run() {
        try {
          String cmd = QueryGenerator.insertOrReplaceEvent(eventToCache);
          logger.log("SQLiteDBHelper: create event: " + cmd);
          Statement statement = dbConnection.createStatement();
          statement.execute(cmd);
          statement.close();
          if (eventsCallback != null) {
            eventsCallback.onCacheSuccess("SQLiteDBHelper: Event cached", eventToCache);
          }
        } catch (SQLException e) {
          if (eventsCallback != null) {
            eventsCallback.onCacheError(e.getMessage());
          }
        } catch (JsonProcessingException e) {
          if (eventsCallback != null) {
            eventsCallback.onCacheError(e.getMessage());
          }
        }
      }
    }.start();
  }

  /**
   * update Event in the SQLite database
   *
   * @param eventToUpdate
   * @param cacheEventsCallback
   */
  public void updateEvent(final Event eventToUpdate, final EventsCallback cacheEventsCallback) {
    new Thread() {

      public void run() {
        try {
          String cmd = QueryGenerator.updateEvent(eventToUpdate);
          logger.log("SQLiteDBHelper: update event: " + cmd);
          Statement statement = dbConnection.createStatement();
          int num = statement.executeUpdate(cmd);
            if (cacheEventsCallback != null) {
              cacheEventsCallback.onCacheSuccess("SQLiteDBHelper: " + num
                      + " event(s) updated in cache", eventToUpdate);
            }
          statement.close();
        } catch (SQLException e) {
          if (cacheEventsCallback != null) {
            cacheEventsCallback.onCacheError(e.getMessage());
          }
        } catch (JsonProcessingException e) {
          if (cacheEventsCallback != null) {
            cacheEventsCallback.onCacheError(e.getMessage());
          }
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
  public void updateOrCreateEvents(final Collection<Event> eventsToCache,
    final EventsCallback cacheEventsCallback) {
    new Thread() {
      @Override
      public void run() {

        for (Event event : eventsToCache) {
          try {
            Statement statement = dbConnection.createStatement();
            String cmd = QueryGenerator.insertOrReplaceEvent(event);
            logger.log("SQLiteDBHelper: update or create event : " + cmd);
            statement.execute(cmd);
            logger.log("SQLiteDBHelper: inserted " + event.getClientId() + " into DB.");
            statement.close();
          } catch (SQLException e) {
            cacheEventsCallback.onCacheError(e.getMessage());
            e.printStackTrace();
          } catch (JsonProcessingException e) {
            cacheEventsCallback.onCacheError(e.getMessage());
            e.printStackTrace();
          }
        }
        cacheEventsCallback.onCacheSuccess("SQLiteDBHelper: Events updated", null);

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
            Event retrievedEvent = Event.createOrReuse(result);
            if (retrievedEvent.isTrashed() == true) {
              // delete really
              String cmd = QueryGenerator.deleteEvent(retrievedEvent);
              statement.executeUpdate(cmd);
              cacheEventsCallback.onCacheSuccess("SQLiteDBHelper: Event with clientId="
                      + eventToDelete.getClientId()
                      + " is deleted.", null);
            } else {
              // set to trashed
              retrievedEvent.setTrashed(true);
              String cmd = QueryGenerator.insertOrReplaceEvent(retrievedEvent);
              statement.executeUpdate(cmd);
              logger.log("SQLiteDBHelper: delete - set trashed=true for clientId="
                + retrievedEvent.getClientId());
              cacheEventsCallback.onCacheSuccess("SQLiteDBHelper: Event with clientId="
                      + retrievedEvent.getClientId()
                      + " is trashed.", retrievedEvent);
            }
          }
          statement.close();
        } catch (SQLException e) {
          cacheEventsCallback.onCacheError(e.getMessage());
          e.printStackTrace();
        } catch (JsonParseException e) {
          cacheEventsCallback.onCacheError(e.getMessage());
          e.printStackTrace();
        } catch (JsonMappingException e) {
          cacheEventsCallback.onCacheError(e.getMessage());
          e.printStackTrace();
        } catch (IOException e) {
          cacheEventsCallback.onCacheError(e.getMessage());
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
  public void getEvents(final Filter filter, final GetEventsCallback cacheEventsCallback) {
    new Thread() {
      @Override
      public void run() {
        try {
          String cmd = QueryGenerator.retrieveEvents(filter);
          logger.log("SQLiteDBHelper: get: " + cmd);
          Statement statement = dbConnection.createStatement();
          ResultSet result = statement.executeQuery(cmd);
          List<Event> retrievedEvents = new ArrayList<Event>();
          while (result.next()) {
            Event retrievedEvent = Event.createOrReuse(result);
            retrievedEvents.add(retrievedEvent);
          }

          // TODO add deleted events somehow
          cacheEventsCallback.cacheCallback(retrievedEvents, null);
        } catch (SQLException e) {
          cacheEventsCallback.onCacheError(e.getMessage());
          e.printStackTrace();
        } catch (JsonParseException e) {
          cacheEventsCallback.onCacheError(e.getMessage());
          e.printStackTrace();
        } catch (JsonMappingException e) {
          cacheEventsCallback.onCacheError(e.getMessage());
          e.printStackTrace();
        } catch (IOException e) {
          cacheEventsCallback.onCacheError(e.getMessage());
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
          cacheStreamsCallback.onCacheSuccess("SQLiteDBHelper: Stream updated or created",
                  streamToCache);
        } catch (SQLException e) {
          cacheStreamsCallback.onCacheError(e.getMessage());
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
        logger.log("SQLiteDBHelper: update or create streams");
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
            cacheStreamsCallback.onCacheSuccess(
                    "SQLiteDBHelper: child stream updated or created", stream);
            if (stream.getChildren() != null) {
              Set<Stream> children = new HashSet<Stream>();
              retrieveAllChildren(children, stream);
              for (Stream childStream : children) {
                cmd = QueryGenerator.insertOrReplaceStream(childStream);
                logger.log("SQLiteDBHelper: add child Stream: " + cmd);

                statement.execute(cmd);
                cacheStreamsCallback.onCacheSuccess(
                        "SQLiteDBHelper: child stream updated or created", childStream);
              }
            }
            statement.close();
          } catch (SQLException e) {
            cacheStreamsCallback.onCacheError(e.getMessage());
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
  public void deleteStream(final Stream streamToDelete, final boolean mergeEventsWithParent,
    final StreamsCallback cacheStreamsCallback) {
    new Thread() {
      @Override
      public void run() {
        String cmd;
        try {
          // retrieve Stream
          cmd = QueryGenerator.retrieveStream(streamToDelete.getId());
          Statement statement = dbConnection.createStatement();
          ResultSet result = statement.executeQuery(cmd);
          while (result.next()) {
            Stream retrievedStreamToDelete = new Stream(result);
            // check trashed field
            if (retrievedStreamToDelete.isTrashed()) {
              // if true: delete Really
              logger.log("SQLiteDBHelper: delete Stream with id=" + streamToDelete.getId());
              // find parent Stream and remove streamToDelete from its children
              String parentId = retrievedStreamToDelete.getParentId();

              // delete Stream
              cmd = QueryGenerator.deleteStream(retrievedStreamToDelete);
              statement.executeUpdate(cmd);

              if (mergeEventsWithParent == true && parentId != null) {
                // if mergeEventsWithParent is true
                // fetch all these events, modify their parent stream id
                // and save them
                Filter deleteFilter = new Filter();
                deleteFilter.addStream(retrievedStreamToDelete);
                cmd = QueryGenerator.retrieveEvents(deleteFilter);
                result = statement.executeQuery(cmd);
                Event updateEvent = null;
                while (result.next()) {
                  try {
                    updateEvent = Event.createOrReuse(result);
                    updateEvent.setStreamId(parentId);
                    updateEvent(updateEvent, null);
                  } catch (JsonParseException e) {
                    e.printStackTrace();
                    cacheStreamsCallback.onCacheError(e.getMessage());
                  } catch (JsonMappingException e) {
                    e.printStackTrace();
                    cacheStreamsCallback.onCacheError(e.getMessage());
                  } catch (IOException e) {
                    e.printStackTrace();
                    cacheStreamsCallback.onCacheError(e.getMessage());
                  }
                }

              } else {
                // else do nothing (or delete them?)
              }
              // delete child streams
              if (retrievedStreamToDelete.getChildren() != null) {
                for (Stream childstream : retrievedStreamToDelete.getChildren()) {
                  cmd = QueryGenerator.deleteStream(childstream);
                  statement.executeUpdate(cmd);
                }
              }
              cacheStreamsCallback.onCacheSuccess("SQLiteDBHelper: Stream with id="
                      + retrievedStreamToDelete.getId()
                      + " deleted.", null);
            } else {
              // set its trashed field to true and save it
              logger.log("SQLiteDBHelper: trash Stream with id=" + streamToDelete.getId());
              retrievedStreamToDelete.setTrashed(true);
              updateOrCreateStream(retrievedStreamToDelete, cacheStreamsCallback);
              cacheStreamsCallback.onCacheSuccess("SQLiteDBHelper: Stream with id="
                      + retrievedStreamToDelete.getId()
                      + " trashed.", retrievedStreamToDelete);
              // set child streams' trashed field to true

            }
          }

          // behaviour not defined in API - may be added later (should also
          // delete
          // these streams' events)
          // if (streamToDelete.getChildren() != null) {
          // for (Stream childStream : streamToDelete.getChildren()) {
          // cmd = QueryGenerator.delete(childStream);
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
          // cmd = QueryGenerator.delete(streamToDelete);
          // logger.log("SQLiteDBHelper: delete: " + cmd);
          // int done = statement.executeUpdate(cmd);
          // // set trashed to true
          // if (done == 0) {
          // updateOrCreateStream(streamToDelete, cacheStreamsCallback);
          // }
          statement.close();
        } catch (SQLException e) {
          cacheStreamsCallback.onCacheError(e.getMessage());
          e.printStackTrace();
        }
      }
    }.start();
  }

  /**
   * Retrieves Streams tree from the SQLite database (streams unreachable from
   * the root are not included)
   *
   * @param cacheStreamsCallback
   *          callback to which the streams are returned.
   */
  public void getStreams(final GetStreamsCallback cacheStreamsCallback) {
    new Thread() {
      @Override
      public void run() {
        try {
          String cmd = QueryGenerator.retrieveStreams();
          logger.log("SQLiteDBHelper: get: "
            + cmd
              + " - "
              + Thread.currentThread().getName());
          Statement statement = dbConnection.createStatement();
          ResultSet result = statement.executeQuery(cmd);
          Map<String, Stream> allStreams = new HashMap<String, Stream>();
          while (result.next()) {
            // get the requested Streams
            Stream retrievedStream = new Stream(result);
            retrievedStream.assignConnection(weakConnection);
            allStreams.put(retrievedStream.getId(), retrievedStream);
          }
          logger.log("SQLiteDBHelper: retrieved " + allStreams.size() + " stream(s).");
          Map<String, Stream> rootStreams = new HashMap<String, Stream>();

          String pid = null;
          for (Stream parentStream : allStreams.values()) {
            pid = parentStream.getParentId();
            if (pid == null) {
              logger.log("SQLiteDBHelper: adding rootStream: id="
                + parentStream.getId()
                  + ", name="
                  + parentStream.getName());
              rootStreams.put(parentStream.getId(), parentStream);
            }
          }

          for (Stream childStream : allStreams.values()) {
            pid = childStream.getParentId();
            if (pid != null) {
              if (allStreams.containsKey(pid)) {
                logger.log("SQLiteDBHelper: adding childStream: id="
                  + childStream.getId()
                    + ", name="
                    + childStream.getName()
                    + " to "
                    + pid);
                allStreams.get(pid).addChildStream(childStream);
              }
            }
          }

          cacheStreamsCallback.cacheCallback(rootStreams, null);
        } catch (SQLException e) {
          cacheStreamsCallback.onCacheError(e.getMessage());
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
