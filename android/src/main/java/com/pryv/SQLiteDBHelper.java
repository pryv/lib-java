package com.pryv;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.pryv.api.OnlineEventsAndStreamsManager;
import com.pryv.database.DBHelper;
import com.pryv.database.DBinitCallback;
import com.pryv.database.QueryGenerator;
import com.pryv.interfaces.EventsCallback;
import com.pryv.interfaces.GetEventsCallback;
import com.pryv.interfaces.GetStreamsCallback;
import com.pryv.interfaces.StreamsCallback;
import com.pryv.interfaces.UpdateCacheCallback;
import com.pryv.model.Event;
import com.pryv.model.Stream;
import com.pryv.utils.JsonConverter;
import com.pryv.utils.Logger;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SQLiteDBHelper extends SQLiteOpenHelper implements DBHelper{

    private final String initDBerrorMessage = "Database initialization error: ";
    // weak reference to Pryv's Connection
    private WeakReference<AbstractConnection> weakConnection;
    private Filter scope;
    private OnlineEventsAndStreamsManager api;
    private Double lastUpdate;
    private Logger logger = Logger.getInstance();
    private DBinitCallback initCallback;

    private static final int DATABASE_VERSION = 1;

    private SQLiteDatabase db;

    /**
     * SQLiteDBHelper constructor. Creates and Connects to the SQLite database
     *
     * @param cacheFolderPath the path to the caching folder
     * @param weakConnection
     * @param initCallback    callback to notify failure
     */
    public SQLiteDBHelper(Context context, Filter scope, String cacheFolderPath, OnlineEventsAndStreamsManager api,
                          WeakReference<AbstractConnection> weakConnection,
                          DBinitCallback initCallback) {
        super(context, Pryv.DATABASE_NAME, null, DATABASE_VERSION);
        this.scope = scope;
        this.api = api;
        this.weakConnection = weakConnection;
        this.initCallback = initCallback;
        logger.log("SQLiteDBHelper: init DB in: " + cacheFolderPath + Pryv.DATABASE_NAME);
    }

    /**
     * Creates tables if required.
     */
    public void onCreate(SQLiteDatabase db) {
        this.db = db;
        try {
            createEventsTable();
            createSteamsTable();
        } catch (SQLException e) {
            initCallback.onError(initDBerrorMessage + e.getMessage());
            e.printStackTrace();
        }
    }

    // Upgrading database
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: Drop older table if existing
        // db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        // TODO: Create tables again
        // onCreate(db);
    }

    /**
     * Set a scope of data stored in the cache.
     *
     * @param scope
     */
    public void setScope(Filter scope) {
        this.scope = scope;
    }

    /**
     * Create Events table in the SQLite database.
     *
     * @throws SQLException
     */
    private void createEventsTable() throws SQLException {
        SQLiteDatabase db = getWritableDatabase();
        String cmd = QueryGenerator.createEventsTable();
        logger.log("SQLiteDBHelper: createEventsTable: " + cmd);
        db.execSQL(cmd);
    }

    /**
     * Create Streams table in the SQLite database.
     *
     * @throws SQLException
     */
    private void createSteamsTable() throws SQLException {
        SQLiteDatabase db = getWritableDatabase();
        String cmd = QueryGenerator.createStreamsTable();
        logger.log("SQLiteDBHelper: createStreamsTable: " + cmd);
        db.execSQL(cmd);
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
            public void cacheCallback(List<Event> events, Map<String, Double> eventDeletions) {
            }

            @Override
            public void onCacheError(String errorMessage) {
            }

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
     * @param eventToCache   the event to insert
     * @param eventsCallback callback to notify succeeventsCallback.onCacheError(e.getMessage());ss or failure
     */
    public void createEvent(final Event eventToCache, final EventsCallback eventsCallback) {
        new Thread() {
            @Override
            public void run() {
                try {
                    SQLiteDatabase db = getWritableDatabase();
                    String cmd = QueryGenerator.insertOrReplaceEvent(eventToCache);
                    logger.log("SQLiteDBHelper: create event: " + cmd);
                    db.execSQL(cmd);

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
                    SQLiteDatabase db = getWritableDatabase();

                    String cmd = QueryGenerator.updateEvent(eventToUpdate);
                    logger.log("SQLiteDBHelper: update event: " + cmd);
                    db.execSQL(cmd);
                    if (cacheEventsCallback != null) {
                        // TODO: print number of events updated
                        cacheEventsCallback.onCacheSuccess("SQLiteDBHelper: Event(s) updated in cache", eventToUpdate);
                    }
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
     * @param eventsToCache       the events to insert in the cache
     * @param cacheEventsCallback callback to notify success or failure
     */
    public void updateOrCreateEvents(final Collection<Event> eventsToCache,
                                     final EventsCallback cacheEventsCallback) {
        new Thread() {
            @Override
            public void run() {

                for (Event event : eventsToCache) {
                    try {
                        SQLiteDatabase db = getWritableDatabase();
                        String cmd = QueryGenerator.insertOrReplaceEvent(event);
                        logger.log("SQLiteDBHelper: update or create event : " + cmd);
                        db.execSQL(cmd);
                        logger.log("SQLiteDBHelper: inserted " + event.getId() + " into DB.");
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
                    SQLiteDatabase db = getWritableDatabase();
                    String fetchCmd = QueryGenerator.retrieveEvent(eventToDelete.getId());
                    Cursor result = db.rawQuery(fetchCmd,null);
                    if (result.moveToFirst()) {
                        do {
                            Event retrievedEvent = getEventFromCursor(result);
                            if (retrievedEvent.isTrashed() == true) {
                                // delete really
                                String cmd = QueryGenerator.deleteEvent(retrievedEvent);
                                db.execSQL(cmd);
                                cacheEventsCallback.onCacheSuccess("SQLiteDBHelper: Event with Id="
                                        + eventToDelete.getId()
                                        + " is deleted.", null);
                            } else {
                                // set to trashed
                                retrievedEvent.setTrashed(true);
                                String cmd = QueryGenerator.insertOrReplaceEvent(retrievedEvent);
                                db.execSQL(cmd);
                                logger.log("SQLiteDBHelper: delete - set trashed=true for Id="
                                        + retrievedEvent.getId());
                                cacheEventsCallback.onCacheSuccess("SQLiteDBHelper: Event with Id="
                                        + retrievedEvent.getId()
                                        + " is trashed.", retrievedEvent);
                            }
                        } while (result.moveToNext());
                        result.close();
                    }
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
                    SQLiteDatabase db = getWritableDatabase();
                    String cmd = QueryGenerator.retrieveEvents(filter);
                    logger.log("SQLiteDBHelper: get: " + cmd);
                    Cursor result = db.rawQuery(cmd,null);
                    List<Event> retrievedEvents = new ArrayList<Event>();
                    if (result.moveToFirst()) {
                        do {
                            Event retrievedEvent = getEventFromCursor(result);
                            retrievedEvents.add(retrievedEvent);
                        } while (result.moveToNext());
                        result.close();
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
     * @param streamToCache        the stream to insert
     * @param cacheStreamsCallback callback to notify success or faiure
     */
    public void updateOrCreateStream(final Stream streamToCache,
                                     final StreamsCallback cacheStreamsCallback) {
        new Thread() {
            @Override
            public void run() {
                try {
                    SQLiteDatabase db = getWritableDatabase();
                    String cmd = QueryGenerator.insertOrReplaceStream(streamToCache);
                    logger.log("SQLiteDBHelper: update or create Stream : " + cmd);
                    db.execSQL(cmd);
                    if (streamToCache.getChildren() != null) {
                        // TODO do recursively maybe
                        Set<Stream> children = new HashSet<Stream>();
                        retrieveAllChildren(children, streamToCache);
                        for (Stream childStream : children) {
                            cmd = QueryGenerator.insertOrReplaceStream(childStream);
                            db.execSQL(cmd);
                            logger.log("SQLiteDBHelper: add child Stream: " + cmd);
                        }
                    }
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
     * @param streamsToCache       the streams to cache
     * @param cacheStreamsCallback callback to notify success or failure
     */
    public void updateOrCreateStreams(final Collection<Stream> streamsToCache,
                                      final StreamsCallback cacheStreamsCallback) {
        new Thread() {
            @Override
            public void run() {
                logger.log("SQLiteDBHelper: update or create streams");
                for (Stream stream : streamsToCache) {
                    try {
                        SQLiteDatabase db = getWritableDatabase();
                        String cmd = QueryGenerator.insertOrReplaceStream(stream);
                        logger.log("SQLiteDBHelper: update or create Stream stream: id="
                                + stream.getId()
                                + ", name="
                                + stream.getName());
                        logger.log("SQLiteDBHelper: update or create Stream: " + cmd);
                        db.execSQL(cmd);
                        cacheStreamsCallback.onCacheSuccess(
                                "SQLiteDBHelper: child stream updated or created", stream);
                        if (stream.getChildren() != null) {
                            Set<Stream> children = new HashSet<Stream>();
                            retrieveAllChildren(children, stream);
                            for (Stream childStream : children) {
                                cmd = QueryGenerator.insertOrReplaceStream(childStream);
                                logger.log("SQLiteDBHelper: add child Stream: " + cmd);
                                db.execSQL(cmd);
                                cacheStreamsCallback.onCacheSuccess(
                                        "SQLiteDBHelper: child stream updated or created", childStream);
                            }
                        }
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
     * @param childrenStreams a Set<Stream> into which all children are put
     * @param parentStream    the stream whose children are gathered
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
                    SQLiteDatabase db = getWritableDatabase();
                    // retrieve Stream
                    cmd = QueryGenerator.retrieveStream(streamToDelete.getId());
                    Cursor result = db.rawQuery(cmd,null);
                    if (result.moveToFirst()) {
                        do {
                            Stream retrievedStreamToDelete = getStreamFromCursor(result);
                            // check trashed field
                            if (retrievedStreamToDelete.isTrashed()) {
                                // if true: delete Really
                                logger.log("SQLiteDBHelper: delete Stream with id=" + streamToDelete.getId());
                                // find parent Stream and remove streamToDelete from its children
                                String parentId = retrievedStreamToDelete.getParentId();

                                // delete Stream
                                cmd = QueryGenerator.deleteStream(retrievedStreamToDelete);
                                db.execSQL(cmd);

                                if (mergeEventsWithParent == true && parentId != null) {
                                    // if mergeEventsWithParent is true
                                    // fetch all these events, modify their parent stream id
                                    // and save them
                                    Filter deleteFilter = new Filter();
                                    deleteFilter.addStream(retrievedStreamToDelete);
                                    cmd = QueryGenerator.retrieveEvents(deleteFilter);
                                    result = db.rawQuery(cmd,null);
                                    Event updateEvent = null;
                                    if (result.moveToFirst()) {
                                        do {
                                            try {
                                                updateEvent = getEventFromCursor(result);
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
                                        } while (result.moveToNext());
                                        result.close();
                                    }

                                } else {
                                    // else do nothing (or delete them?)
                                }
                                // delete child streams
                                if (retrievedStreamToDelete.getChildren() != null) {
                                    for (Stream childstream : retrievedStreamToDelete.getChildren()) {
                                        cmd = QueryGenerator.deleteStream(childstream);
                                        db.execSQL(cmd);
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
                        } while (result.moveToNext());
                        result.close();
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
     * @param cacheStreamsCallback callback to which the streams are returned.
     */
    public void getStreams(final GetStreamsCallback cacheStreamsCallback) {
        new Thread() {
            @Override
            public void run() {
                try {
                    SQLiteDatabase db = getWritableDatabase();

                    String cmd = QueryGenerator.retrieveStreams();
                    logger.log("SQLiteDBHelper: get: "
                            + cmd
                            + " - "
                            + Thread.currentThread().getName());
                    Cursor result = db.rawQuery(cmd, null);
                    Map<String, Stream> allStreams = new HashMap<String, Stream>();
                    if (result.moveToFirst()) {
                        do {
                            // get the requested Streams
                            Stream retrievedStream = getStreamFromCursor(result);
                            retrievedStream.assignConnection(weakConnection);
                            allStreams.put(retrievedStream.getId(), retrievedStream);
                            } while (result.moveToNext());
                        result.close();
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

    private Stream getStreamFromCursor(Cursor c) {
        String id = c.getString(c.getColumnIndex(QueryGenerator.STREAMS_ID_KEY));
        String name = c.getString(c.getColumnIndex(QueryGenerator.STREAMS_NAME_KEY));
        Boolean trashed = c.getInt(c.getColumnIndex(QueryGenerator.STREAMS_TRASHED_KEY)) > 0;
        Double created = c.getDouble(c.getColumnIndex(QueryGenerator.STREAMS_CREATED_KEY));
        String createdBy = c.getString(c.getColumnIndex(QueryGenerator.STREAMS_CREATED_BY_KEY));
        Double modified = c.getDouble(c.getColumnIndex(QueryGenerator.STREAMS_MODIFIED_KEY));
        String modifiedBy = c.getString(c.getColumnIndex(QueryGenerator.STREAMS_MODIFIED_BY_KEY));
        String parentId = c.getString(c.getColumnIndex(QueryGenerator.STREAMS_PARENT_ID_KEY));
        Boolean singleActivity = c.getInt(c.getColumnIndex(QueryGenerator.STREAMS_SINGLE_ACTIVITY_KEY)) > 0;
        String clientData = c.getString(c.getColumnIndex(QueryGenerator.STREAMS_CLIENT_DATA_KEY));
        Stream stream = new Stream(id, name, parentId, singleActivity, null, null, trashed, created, createdBy, modified, modifiedBy);
        stream.setClientDataFromAString(clientData);
        return stream;
    }

    private Event getEventFromCursor(Cursor c) throws IOException {
        String id = c.getString(c.getColumnIndex(QueryGenerator.EVENTS_ID_KEY));
        String streamId = c.getString(c.getColumnIndex(QueryGenerator.EVENTS_STREAM_ID_KEY));
        Double time = c.getDouble(c.getColumnIndex(QueryGenerator.EVENTS_TIME_KEY));
        String type = c.getString(c.getColumnIndex(QueryGenerator.EVENTS_TYPE_KEY));
        Double created = c.getDouble(c.getColumnIndex(QueryGenerator.EVENTS_CREATED_KEY));
        String createdBy = c.getString(c.getColumnIndex(QueryGenerator.EVENTS_CREATED_BY_KEY));
        Double modified = c.getDouble(c.getColumnIndex(QueryGenerator.EVENTS_MODIFIED_KEY));
        String modifiedBy = c.getString(c.getColumnIndex(QueryGenerator.EVENTS_MODIFIED_BY_KEY));
        Double duration = c.getDouble(c.getColumnIndex(QueryGenerator.EVENTS_DURATION_KEY));
        String content = c.getString(c.getColumnIndex(QueryGenerator.EVENTS_CONTENT_KEY));
        String tagsString = c.getString(c.getColumnIndex(QueryGenerator.EVENTS_TAGS_KEY));
        String description = c.getString(c.getColumnIndex(QueryGenerator.EVENTS_DESCRIPTION_KEY));
        String clientData = c.getString(c.getColumnIndex(QueryGenerator.EVENTS_CLIENT_DATA_KEY));
        Boolean trashed = c.getInt(c.getColumnIndex(QueryGenerator.EVENTS_TRASHED_KEY)) > 0 ;
        String attachment = c.getString(c.getColumnIndex(QueryGenerator.EVENTS_ATTACHMENTS_KEY));
        Event event = new Event(id, streamId, time, duration, type, content, null, description, null, null, trashed, created, createdBy, modified, modifiedBy);
        if (tagsString != null) {
            Set <String> tags = new HashSet<String>(Arrays.asList(tagsString.split(",")));
            event.setTags(tags);
        }
        event.setAttachments(JsonConverter.deserializeAttachments(attachment));
        event.setClientDataFromAstring(clientData);
        return event;
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        if(db != null){
            return db;
        }
        return super.getWritableDatabase();
    }

}