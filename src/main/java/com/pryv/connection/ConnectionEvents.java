package com.pryv.connection;

import com.pryv.Connection;
import com.pryv.Filter;
import com.pryv.api.OnlineEventsAndStreamsManager;
import com.pryv.database.SQLiteDBHelper;
import com.pryv.interfaces.EventsCallback;
import com.pryv.interfaces.EventsManager;
import com.pryv.interfaces.GetEventsCallback;
import com.pryv.model.Event;

import java.lang.ref.WeakReference;

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

            cache.update();
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

            cache.update();
        }
        api.createEvent(newEvent, eventsCallback);

    }

    @Override
    public void delete(final Event eventToDelete, final EventsCallback eventsCallback) {
        if (cacheScope.hasInScope(eventToDelete)) {
            cache.deleteEvent(eventToDelete, eventsCallback);

            cache.update();
        }
        api.deleteEvent(eventToDelete, eventsCallback);

    }

    @Override
    public void update(final Event eventToUpdate, final EventsCallback eventsCallback) {
        if (cacheScope == null || cacheScope.hasInScope(eventToUpdate)) {
            cache.updateEvent(eventToUpdate, eventsCallback);

            cache.update();
        }
        api.deleteEvent(eventToUpdate, eventsCallback);

    }

    public void setCacheScope(Filter scope) {
        this.cacheScope = scope;
    }

}
