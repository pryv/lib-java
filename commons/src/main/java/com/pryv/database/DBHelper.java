package com.pryv.database;

import com.pryv.Filter;
import com.pryv.connection.ConnectionStreams;
import com.pryv.interfaces.EventsCallback;
import com.pryv.interfaces.GetEventsCallback;
import com.pryv.interfaces.GetStreamsCallback;
import com.pryv.interfaces.StreamsCallback;
import com.pryv.interfaces.UpdateCacheCallback;
import com.pryv.model.Event;
import com.pryv.model.Stream;

/**
 * Created by Thieb on 20.06.2016.
 */
public abstract class DBHelper {
    public abstract void setScope(Filter scope);

    public abstract void update(UpdateCacheCallback updateCacheCallback);

    public abstract void updateEvent(Event eventToUpdate, EventsCallback eventsCallback);

    public abstract void deleteEvent(Event eventToDelete, EventsCallback eventsCallback);

    public abstract void createEvent(Event newEvent, EventsCallback eventsCallback);

    public abstract void getEvents(Filter filter, GetEventsCallback eventsCallback);

    public abstract void getStreams(GetStreamsCallback rootStreamsUpdater);

    public abstract void updateOrCreateStream(Stream newStream, StreamsCallback streamsCallback);

    public abstract void deleteStream(Stream streamToDelete, boolean mergeEventsWithParent, StreamsCallback streamsCallback);
}
