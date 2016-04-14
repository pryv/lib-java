package com.pryv.connection;


import com.pryv.api.Filter;
import com.pryv.api.OnlineEventsAndStreamsManager;
import com.pryv.api.database.SQLiteDBHelper;
import com.pryv.api.model.Stream;
import com.pryv.interfaces.StreamsCallback;
import com.pryv.interfaces.GetStreamsCallback;
import com.pryv.interfaces.StreamsManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionStreams implements StreamsManager{

    private OnlineEventsAndStreamsManager api;
    private Filter cacheScope;
    private SQLiteDBHelper cache;

    public ConnectionStreams(OnlineEventsAndStreamsManager api, Filter cacheScope, SQLiteDBHelper cache) {
        this.api = api;
        this.cacheScope = cacheScope;
        this.cache = cache;
    }

    @Override
    public void get(final Filter filter, final GetStreamsCallback getStreamsCallback) {
        if (filter.isIncludedInScope(cacheScope)) {

            new Thread() {
                public void run() {
                    cache.getStreams(getStreamsCallback);
                    // to execute in separate Thread
                    // can be launched separately since write is not done until all reads are finished.
                }
            }.start();

            new Thread() {
                public void run() {
                    cache.update();
                }
            }.start();

        } else {
            new Thread() {
                public void run() {
                    api.getStreams(filter, getStreamsCallback);
                }
            }.start();
        }
    }

    @Override
    public void create(final Stream newStream, final StreamsCallback StreamsCallback) {
        if (cacheScope.hasInScope(newStream.getId())) {
            new Thread() {
                public void run() {
                    cache.updateOrCreateStream(newStream, StreamsCallback);
                }
            }.start();

            new Thread() {
                public void run() {
                    cache.update();
                }
            }.start();
        } else {
            new Thread() {
                public void run() {
                    api.createStream(newStream, StreamsCallback);
                }
            }.start();
        }
    }

    @Override
    public void delete(final Stream streamToDelete, final boolean mergeEventsWithParent, final StreamsCallback streamsCallback) {
        if (cacheScope.hasInScope(streamToDelete.getId())) {
            new Thread() {
                public void run() {
                    cache.deleteStream(streamToDelete, mergeEventsWithParent, streamsCallback);
                }
            }.start();

            new Thread() {
                public void run() {
                    cache.update();
                }
            }.start();
        } else {
            new Thread() {
                public void run() {
                    api.deleteStream(streamToDelete, mergeEventsWithParent, streamsCallback);
                }
            }.start();
        }
    }

    @Override
    public void update(final Stream streamToUpdate, final StreamsCallback streamsCallback) {
        if (cacheScope.hasInScope(streamToUpdate.getId())) {
            new Thread() {
                public void run() {
                    cache.updateOrCreateStream(streamToUpdate, streamsCallback);
                }
            }.start();

            new Thread() {
                public void run() {
                    cache.update();
                }
            }.start();
        } else {
            new Thread() {
                public void run() {
                    api.updateStream(streamToUpdate, streamsCallback);
                }
            }.start();
        }
    }
}
