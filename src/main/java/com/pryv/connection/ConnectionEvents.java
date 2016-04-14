package com.pryv.connection;

import com.pryv.Connection;
import com.pryv.Filter;
import com.pryv.api.OnlineEventsAndStreamsManager;
import com.pryv.database.SQLiteDBHelper;
import com.pryv.interfaces.EventsCallback;
import com.pryv.interfaces.EventsManager;
import com.pryv.interfaces.GetEventsCallback;
import com.pryv.interfaces.UpdateCacheCallback;
import com.pryv.model.Event;
import com.pryv.model.Stream;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

public class ConnectionEvents implements EventsManager {

    private WeakReference<Connection> connection;
    private OnlineEventsAndStreamsManager api;
    private Filter cacheScope;
    private SQLiteDBHelper cache;

    public ConnectionEvents(WeakReference<Connection> connection, OnlineEventsAndStreamsManager api, Filter cacheScope, SQLiteDBHelper cache) {
        this.connection = connection;
        this.api = api;
        this.cacheScope = cacheScope;
        this.cache = cache;
    }

    @Override
    public void get(final Filter filter, final GetEventsCallback eventsCallback) {
        if (filter == null || filter.isIncludedInScope(cacheScope)) {
            cache.getEvents(filter, eventsCallback);
            // to execute in separate Thread
            // can be launched separately since write is not done until all reads are finished.

            cache.update(updateCacheCallback);
        }
        api.getEvents(filter, eventsCallback);

    }

    @Override
    public void create(final Event newEvent, final EventsCallback eventsCallback) {
        if (newEvent.getClientId() == null) {
            newEvent.generateClientId();
        }
        if (cacheScope == null || cacheScope.hasInScope(newEvent)) {
            cache.createEvent(newEvent, eventsCallback);

            cache.update(updateCacheCallback);
        }
        api.createEvent(newEvent, eventsCallback);

    }

    @Override
    public void delete(final Event eventToDelete, final EventsCallback eventsCallback) {
        if (cacheScope.hasInScope(eventToDelete)) {
            cache.deleteEvent(eventToDelete, eventsCallback);

            cache.update(updateCacheCallback);
        }
        api.deleteEvent(eventToDelete, eventsCallback);

    }

    @Override
    public void update(final Event eventToUpdate, final EventsCallback eventsCallback) {
        if (cacheScope == null || cacheScope.hasInScope(eventToUpdate)) {
            cache.updateEvent(eventToUpdate, eventsCallback);

            cache.update(updateCacheCallback);
        }
        api.deleteEvent(eventToUpdate, eventsCallback);

    }

    public void setCacheScope(Filter scope) {
        this.cacheScope = scope;
    }

    private UpdateCacheCallback updateCacheCallback = new UpdateCacheCallback() {
        @Override
        public void apiCallback(List<Event> events, Map<String, Double> eventDeletions,
                                Map<String, Stream> streams, Map<String, Double> streamDeletions,
                                Double serverTime) {

        }

        @Override
        public void onError(String errorMessage, Double serverTime) {

        }
    };

}
