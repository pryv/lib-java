package com.pryv.connection;


import com.pryv.AbstractConnection;
import com.pryv.Filter;
import com.pryv.api.HttpClient;
import com.pryv.api.OnlineManager;
import com.pryv.model.Stream;
import com.pryv.utils.JsonConverter;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;

public class ConnectionStreams {

    private WeakReference<AbstractConnection> weakConnection;
    private HttpClient httpClient;
    private static final String PATH = "streams";

    public ConnectionStreams(WeakReference<AbstractConnection> weakConnection, OnlineManager api) {
        this.weakConnection = weakConnection;
        this.httpClient = api.getHttpClient();
    }

    public Map<String, Stream> get(final Filter filter) throws IOException {
        HttpClient.ApiResponse apiResponse = httpClient.getRequest(PATH, filter).exec();
        String json = apiResponse.getJsonBody();
        Map<String, Stream> receivedStreams =
                JsonConverter.createStreamsTreeFromJson(json);
        for (Stream receivedStream : receivedStreams.values()) {
            receivedStream.assignConnection(weakConnection);
        }
        Map<String, Double> streamDeletions =
                JsonConverter.createStreamDeletionsTreeFromJson(json);
        weakConnection.get().updateRootStreams(receivedStreams);
        return receivedStreams;
    }

    public Stream create(final Stream newStream) throws IOException {
        HttpClient.ApiResponse apiResponse = httpClient.createRequest(PATH, newStream, null).exec();
        Stream createdStream = JsonConverter.retrieveStreamFromJson(apiResponse.getJsonBody());
        createdStream.assignConnection(weakConnection);
        return createdStream;
    }

    public String delete(final String streamId, final boolean mergeEventsWithParent) throws IOException {
        HttpClient.ApiResponse apiResponse = httpClient.deleteRequest(PATH, streamId, mergeEventsWithParent).exec();
        String json = apiResponse.getJsonBody();
        if (JsonConverter.hasStreamDeletionField(json)) {
            // stream was deleted
            String deletionId = JsonConverter.retrieveDeletedStreamId(json);
            return deletionId;
        } else {
            // stream was trashed
            Stream trashedStream = JsonConverter.retrieveStreamFromJson(json);
            trashedStream.assignConnection(weakConnection);
            return trashedStream.getId();
        }
    }

    public Stream update(final Stream streamToUpdate) throws IOException {
        HttpClient.ApiResponse apiResponse = httpClient.updateRequest(PATH, streamToUpdate.getId(), streamToUpdate).exec();
        Stream updatedStream = JsonConverter.retrieveStreamFromJson(apiResponse.getJsonBody());
        updatedStream.assignConnection(weakConnection);
        return updatedStream;
    }

}
