package com.pryv.connection;

import com.pryv.api.database.SQLiteDBHelper;
import com.pryv.interfaces.EventsManager;
import com.pryv.api.Filter;
import com.pryv.api.OnlineEventsAndStreamsManager;
import com.pryv.api.model.Event;
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

            new Thread() {
                public void run() {
                    cache.getEvents(filter, eventsCallback);
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
                    api.getEvents(filter, eventsCallback);
                }
            }.start();
        }
    }

    @Override
    public void create(final Event newEvent, final EventsCallback eventsCallback) {
        if (cacheScope.hasInScope(newEvent)) {
            new Thread() {
                public void run() {
                    cache.createEvent(newEvent, eventsCallback);
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
                    api.createEvent(newEvent, eventsCallback);
                }
            }.start();
        }
    }

    @Override
    public void delete(final Event eventToDelete, final EventsCallback eventsCallback) {
        if (cacheScope.hasInScope(eventToDelete)) {
            new Thread() {
                public void run() {
                    cache.deleteEvent(eventToDelete, eventsCallback);
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
                    api.deleteEvent(eventToDelete, eventsCallback);
                }
            }.start();
        }
    }

    @Override
    public void update(final Event eventToUpdate, final EventsCallback eventsCallback) {
        if (cacheScope.hasInScope(eventToUpdate)) {
            new Thread() {
                public void run() {
                    cache.updateEvent(eventToUpdate, eventsCallback);
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
                    api.updateEvent(eventToUpdate, eventsCallback);
                }
            }.start();
        }
    }

}
