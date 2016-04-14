package com.pryv.connection;


import com.pryv.Filter;
import com.pryv.api.OnlineEventsAndStreamsManager;
import com.pryv.api.database.SQLiteDBHelper;
import com.pryv.model.Stream;
import com.pryv.interfaces.StreamsCallback;
import com.pryv.interfaces.GetStreamsCallback;
import com.pryv.interfaces.StreamsManager;

public class ConnectionStreams implements StreamsManager {

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
            cache.getStreams(getStreamsCallback);
            // to execute in separate Thread
            // can be launched separately since write is not done until all reads are finished.

            cache.update();
        }

        api.getStreams(filter, getStreamsCallback);

    }

    @Override
    public void create(final Stream newStream, final StreamsCallback StreamsCallback) {
        if (cacheScope.hasInScope(newStream.getId())) {
            cache.updateOrCreateStream(newStream, StreamsCallback);

            cache.update();
        }

        api.createStream(newStream, StreamsCallback);
    }

    @Override
    public void delete(final Stream streamToDelete, final boolean mergeEventsWithParent, final StreamsCallback streamsCallback) {
        if (cacheScope.hasInScope(streamToDelete.getId())) {
            cache.deleteStream(streamToDelete, mergeEventsWithParent, streamsCallback);

            cache.update();
        }
        api.deleteStream(streamToDelete, mergeEventsWithParent, streamsCallback);

    }

    @Override
    public void update(final Stream streamToUpdate, final StreamsCallback streamsCallback) {
        if (cacheScope.hasInScope(streamToUpdate.getId())) {
            cache.updateOrCreateStream(streamToUpdate, streamsCallback);

            cache.update();
        }
        api.updateStream(streamToUpdate, streamsCallback);


    }

    public void setCacheScope(Filter scope) {
        this.cacheScope = scope;
    }
}
