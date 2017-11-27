package com.pryv.connection;


import com.pryv.api.ApiResponse;
import com.pryv.api.HttpClient;
import com.pryv.exceptions.ApiException;
import com.pryv.model.Filter;
import com.pryv.model.Stream;
import com.pryv.utils.JsonConverter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Encapsulate CRUD operations to Pryv API for Streams
 */
public class ConnectionStreams {

    private HttpClient httpClient;
    private static final String PATH = "streams";

    private Map<String, Stream> rootStreams;
    private Map<String, Stream> flatStreams;

    public ConnectionStreams(HttpClient client) {
        this.httpClient = client;
        this.rootStreams = new ConcurrentHashMap<>();
        // TODO: Init streams structure with an initial get call + use of recomputeRootStreamsTree
        this.flatStreams = new ConcurrentHashMap<>();
    }

    public Map<String, Stream> get(Filter filter) throws IOException, ApiException {
        ApiResponse apiResponse = httpClient.getRequest(PATH, filter).exec();
        String json = apiResponse.getJsonBody();
        Map<String, Stream> receivedStreams =
                JsonConverter.createStreamsTreeFromJson(json);
        rootStreams = receivedStreams;
        return receivedStreams;
    }

    public Stream create(Stream newStream) throws IOException, ApiException {
        ApiResponse apiResponse = httpClient.createRequest(PATH, newStream, null).exec();
        Stream createdStream = JsonConverter.retrieveStreamFromJson(apiResponse.getJsonBody());
        return createdStream;
    }

    public Stream delete(Stream deleteStream, boolean mergeEventsWithParent) throws IOException, ApiException {
        ApiResponse apiResponse = httpClient.deleteRequest(PATH, deleteStream.getId(), mergeEventsWithParent).exec();
        String json = apiResponse.getJsonBody();
        if (JsonConverter.hasStreamDeletionField(json)) {
            // stream was deleted
            return deleteStream.setDeleted(true);
        } else {
            // stream was trashed
            return JsonConverter.retrieveStreamFromJson(json);
        }
    }

    public Stream update(Stream streamToUpdate) throws IOException, ApiException {
        Stream update = streamToUpdate.cloneMutableFields();
        ApiResponse apiResponse = httpClient.updateRequest(PATH, streamToUpdate.getId(), update).exec();
        Stream updatedStream = JsonConverter.retrieveStreamFromJson(apiResponse.getJsonBody());
        return updatedStream;
    }

    public Map<String, Stream> getRootStreams() {
        return rootStreams;
    }

    /**
     * fixes Streams' children properties based on parentIds
     */
    private void recomputeRootStreamsTree() {
        rootStreams.clear();

        String parentId = null;
        // set root streams
        for (Stream potentialRootStream : flatStreams.values()) {
            // clear children fields
            potentialRootStream.clearChildren();
            parentId = potentialRootStream.getParentId();
            if (parentId == null) {
                rootStreams.put(potentialRootStream.getId(), potentialRootStream);
            }
        }

        // assign children
        for (Stream childStream : flatStreams.values()) {
            parentId = childStream.getParentId();
            if (parentId != null) {
                if (flatStreams.containsKey(parentId)) {
                    Stream parent = flatStreams.get(parentId);
                    if (parent != null) {
                        parent.addChildStream(childStream);
                    }
                }
            }
        }
    }

}
