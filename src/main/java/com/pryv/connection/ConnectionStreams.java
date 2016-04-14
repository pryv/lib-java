package com.pryv.connection;


import com.pryv.Connection;
import com.pryv.Filter;
import com.pryv.api.OnlineEventsAndStreamsManager;
import com.pryv.database.SQLiteDBHelper;
import com.pryv.model.Stream;
import com.pryv.interfaces.StreamsCallback;
import com.pryv.interfaces.GetStreamsCallback;
import com.pryv.interfaces.StreamsManager;

import java.lang.ref.WeakReference;
import java.util.Map;

public class ConnectionStreams implements StreamsManager {

    private WeakReference<Connection> weakConnection;
    private OnlineEventsAndStreamsManager api;
    private Filter cacheScope;
    private SQLiteDBHelper cache;

    public ConnectionStreams(WeakReference<Connection> connection, OnlineEventsAndStreamsManager api, Filter cacheScope, SQLiteDBHelper cache) {
        this.weakConnection = connection;
        this.api = api;
        this.cacheScope = cacheScope;
        this.cache = cache;
    }

    @Override
    public void get(final Filter filter, final GetStreamsCallback getStreamsCallback) {
        if (filter == null || filter.isIncludedInScope(cacheScope)) {
            cache.getStreams(new RootStreamsUpdater(getStreamsCallback));
            // to execute in separate Thread
            // can be launched separately since write is not done until all reads are finished.

            cache.update();
        }

        api.getStreams(filter, new RootStreamsUpdater(getStreamsCallback));
    }

    @Override
    public void create(final Stream newStream, final StreamsCallback StreamsCallback) {
        if (cacheScope == null || cacheScope.hasInScope(newStream.getId())) {
            cache.updateOrCreateStream(newStream, StreamsCallback);

            cache.update();
        }

        api.createStream(newStream, StreamsCallback);
    }

    @Override
    public void delete(final Stream streamToDelete, final boolean mergeEventsWithParent, final StreamsCallback streamsCallback) {
        if (cacheScope == null || cacheScope.hasInScope(streamToDelete.getId())) {
            cache.deleteStream(streamToDelete, mergeEventsWithParent, streamsCallback);

            cache.update();
        }
        api.deleteStream(streamToDelete, mergeEventsWithParent, streamsCallback);

    }

    @Override
    public void update(final Stream streamToUpdate, final StreamsCallback streamsCallback) {
        if (cacheScope == null || cacheScope.hasInScope(streamToUpdate.getId())) {
            cache.updateOrCreateStream(streamToUpdate, streamsCallback);

            cache.update();
        }
        api.updateStream(streamToUpdate, streamsCallback);


    }

    public void setCacheScope(Filter scope) {
        this.cacheScope = scope;
    }

    private class RootStreamsUpdater implements GetStreamsCallback {

        private GetStreamsCallback getStreamsCallback;

        public RootStreamsUpdater(GetStreamsCallback getStreamsCallback) {
            this.getStreamsCallback = getStreamsCallback;
        }

        @Override
        public void cacheCallback(Map<String, Stream> streams, Map<String, Stream> deletedStreams) {
            weakConnection.get().updateRootStreams(streams);
            getStreamsCallback.cacheCallback(streams, deletedStreams);
        }

        @Override
        public void onCacheError(String errorMessage) {
            getStreamsCallback.onCacheError(errorMessage);
        }

        @Override
        public void apiCallback(Map<String, Stream> streams, Double serverTime) {
            weakConnection.get().updateRootStreams(streams);
            getStreamsCallback.apiCallback(streams, serverTime);
        }

        @Override
        public void onApiError(String errorMessage, Double serverTime) {
            getStreamsCallback.onApiError(errorMessage, serverTime);
        }
    }
}
