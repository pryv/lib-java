package com.pryv.connection;


import com.pryv.Filter;
import com.pryv.api.HttpClient;
import com.pryv.model.Stream;
import com.pryv.utils.JsonConverter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    public Map<String, Stream> get(Filter filter) throws IOException {
        HttpClient.ApiResponse apiResponse = httpClient.getRequest(PATH, filter).exec();
        String json = apiResponse.getJsonBody();
        Map<String, Stream> receivedStreams =
                JsonConverter.createStreamsTreeFromJson(json);
        // TODO: retrieve streamDeletions
        Map<String, Double> streamDeletions =
                JsonConverter.createStreamDeletionsTreeFromJson(json);
        rootStreams = receivedStreams;
        return receivedStreams;
    }

    public Stream create(Stream newStream) throws IOException {
        HttpClient.ApiResponse apiResponse = httpClient.createRequest(PATH, newStream, null).exec();
        Stream createdStream = JsonConverter.retrieveStreamFromJson(apiResponse.getJsonBody());
        return createdStream;
    }

    public String delete(String streamId, boolean mergeEventsWithParent) throws IOException {
        HttpClient.ApiResponse apiResponse = httpClient.deleteRequest(PATH, streamId, mergeEventsWithParent).exec();
        String json = apiResponse.getJsonBody();
        if (JsonConverter.hasStreamDeletionField(json)) {
            // stream was deleted
            String deletionId = JsonConverter.retrieveDeletedStreamId(json);
            return deletionId;
        } else {
            // stream was trashed
            Stream trashedStream = JsonConverter.retrieveStreamFromJson(json);
            return trashedStream.getId();
        }
    }

    public Stream update(Stream streamToUpdate) throws IOException {
        HttpClient.ApiResponse apiResponse = httpClient.updateRequest(PATH, streamToUpdate.getId(), streamToUpdate).exec();
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
