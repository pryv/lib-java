package com.pryv.database;

import com.pryv.Filter;
import com.pryv.interfaces.GetStreamsCallback;
import com.pryv.interfaces.StreamsCallback;
import com.pryv.model.Event;
import com.pryv.model.Stream;

public interface DBHelper {
    void setScope(Filter scope);

    void update(UpdateCacheCallback updateCacheCallback);

    void updateEvent(Event eventToUpdate, EventsCallback eventsCallback);

    void deleteEvent(Event eventToDelete, EventsCallback eventsCallback);

    void createEvent(Event newEvent, EventsCallback eventsCallback);

    void getEvents(Filter filter, GetEventsCallback eventsCallback);

    void getStreams(GetStreamsCallback rootStreamsUpdater);

    void updateOrCreateStream(Stream newStream, StreamsCallback streamsCallback);

    void deleteStream(Stream streamToDelete, boolean mergeEventsWithParent, StreamsCallback streamsCallback);
}
