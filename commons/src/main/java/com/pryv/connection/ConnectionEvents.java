package com.pryv.connection;

import com.pryv.api.ApiResponse;
import com.pryv.api.HttpClient;
import com.pryv.exceptions.ApiException;
import com.pryv.model.Attachment;
import com.pryv.model.Event;
import com.pryv.model.Filter;
import com.pryv.utils.JsonConverter;

import java.io.IOException;
import java.util.List;

public class ConnectionEvents {

    private static final String PATH = "events";
    private HttpClient httpClient;

    public ConnectionEvents(HttpClient client) {
        this.httpClient = client;
    }

    public List<Event> get(Filter filter) throws IOException, ApiException {
        ApiResponse apiResponse = httpClient.getRequest(PATH, filter).exec();
        List<Event> receivedEvents = JsonConverter.createEventsFromJson(apiResponse.getJsonBody());
        // TODO: retrieve eventDeletions
        return receivedEvents;
    }

    public Event create(Event newEvent) throws IOException, ApiException {
        ApiResponse apiResponse;

        Attachment firstAttachment = newEvent.getFirstAttachment();
        if(firstAttachment != null) {
            newEvent.setAttachments(null);
        }
        apiResponse = httpClient.createRequest(PATH, newEvent, firstAttachment).exec();

        String json = apiResponse.getJsonBody();
        Event createdEvent = JsonConverter.retrieveEventFromJson(json);
        // TODO: handle stopid, startid
        return createdEvent;
    }

    public String delete(String eventId) throws IOException, ApiException {
        ApiResponse apiResponse = httpClient.deleteRequest(PATH, eventId, false).exec();
        String json = apiResponse.getJsonBody();
        if (JsonConverter.hasEventDeletionField(json)) {
            // event was deleted
            String deletedEventId = JsonConverter.retrieveDeleteEventId(json);
            return deletedEventId;
        } else {
            // event was trashed
            Event trashedEvent = JsonConverter.retrieveEventFromJson(json);
            return trashedEvent.getId();
        }
    }

    public Event update(Event updateEvent) throws IOException, ApiException {
        Event update = updateEvent.cloneMutableFields();
        ApiResponse apiResponse = httpClient.updateRequest(PATH, updateEvent.getId(), update).exec();
        Event updatedEvent = JsonConverter.retrieveEventFromJson(apiResponse.getJsonBody());
        return updatedEvent;
    }

}