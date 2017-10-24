package com.pryv.connection;

import com.pryv.AbstractConnection;
import com.pryv.Filter;
import com.pryv.api.HttpClient;
import com.pryv.model.Event;
import com.pryv.utils.JsonConverter;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

public class ConnectionEvents {

    private WeakReference<AbstractConnection> weakConnection;
    private static final String PATH = "events";
    private HttpClient httpClient;

    public ConnectionEvents(WeakReference<AbstractConnection> weakConnection, HttpClient client) {
        this.weakConnection = weakConnection;
        this.httpClient = client;
    }

    public List<Event> get(Filter filter) throws IOException {
        HttpClient.ApiResponse apiResponse = httpClient.getRequest(PATH, filter).exec();
        List<Event> receivedEvents = JsonConverter.createEventsFromJson(apiResponse.getJsonBody());
        for (Event receivedEvent : receivedEvents) {
            receivedEvent.assignConnection(weakConnection);
            Event.createOrReuse(receivedEvent);
        }
        return receivedEvents;
    }

    public Event create(Event newEvent) throws IOException {
        HttpClient.ApiResponse apiResponse;

        if(newEvent.getFirstAttachment() != null) {
            Event eventWithoutAttachments = new Event();
            eventWithoutAttachments.merge(newEvent, JsonConverter.getCloner());
            eventWithoutAttachments.setAttachments(null);
            apiResponse = httpClient.createRequest(PATH, eventWithoutAttachments, newEvent.getFirstAttachment()).exec();
        } else {
            apiResponse = httpClient.createRequest(PATH, newEvent, null).exec();
        }

        String json = apiResponse.getJsonBody();
        Event createdEvent = JsonConverter.retrieveEventFromJson(json);
        createdEvent.assignConnection(weakConnection);
        Event.createOrReuse(createdEvent);
        return createdEvent;
    }

    public String delete(String eventId) throws IOException {
        HttpClient.ApiResponse apiResponse = httpClient.deleteRequest(PATH, eventId, false).exec();
        String json = apiResponse.getJsonBody();
        if (JsonConverter.hasEventDeletionField(json)) {
            // event was deleted
            String deletedEventId = JsonConverter.retrieveDeleteEventId(json);
            return deletedEventId;
        } else {
            // event was trashed
            Event trashedEvent = JsonConverter.retrieveEventFromJson(json);
            trashedEvent.assignConnection(weakConnection);
            trashedEvent.setId(eventId);
            Event.createOrReuse(trashedEvent);
            return eventId;
        }
    }

    public Event update(Event updateEvent) throws IOException {
        HttpClient.ApiResponse apiResponse = httpClient.updateRequest(PATH, updateEvent.getId(), updateEvent).exec();
        Event updatedEvent = JsonConverter.retrieveEventFromJson(apiResponse.getJsonBody());
        updatedEvent.assignConnection(weakConnection);
        updatedEvent.setId(updateEvent.getId());
        Event.createOrReuse(updatedEvent);
        return updatedEvent;
    }

}