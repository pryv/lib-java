package com.pryv.connection;

import com.pryv.api.database.SQLiteDBHelper;
import com.pryv.interfaces.EventsManager;
import com.pryv.Filter;
import com.pryv.api.OnlineEventsAndStreamsManager;
import com.pryv.model.Event;
import com.pryv.interfaces.GetEventsCallback;
import com.pryv.interfaces.EventsCallback;

public class ConnectionEvents implements EventsManager {

    private OnlineEventsAndStreamsManager api;
    private Filter cacheScope;
    private SQLiteDBHelper cache;

    public ConnectionEvents(OnlineEventsAndStreamsManager api, Filter cacheScope, SQLiteDBHelper cache) {
        this.api = api;
        this.cacheScope = cacheScope;
        this.cache = cache;
    }

    @Override
    public void get(final Filter filter, final GetEventsCallback eventsCallback) {
        if (filter.isIncludedInScope(cacheScope)) {
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
        if (cacheScope.hasInScope(newEvent)) {
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
        if (cacheScope.hasInScope(eventToUpdate)) {
            cache.updateEvent(eventToUpdate, eventsCallback);

            cache.update();
        }
        api.deleteEvent(eventToUpdate, eventsCallback);

    }

    public void setCacheScope(Filter scope) {
        this.cacheScope = scope;
    }

}
