package com.pryv.api;

import com.pryv.AbstractConnection;
import com.pryv.Filter;
import com.pryv.interfaces.GetStreamsCallback;
import com.pryv.interfaces.StreamsCallback;
import com.pryv.model.Event;
import com.pryv.model.Stream;
import com.pryv.utils.JsonConverter;
import com.pryv.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * OnlineManager manages data from online Pryv API
 *
 * @author ik
 */
public class OnlineManager {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient client = new OkHttpClient();
    private HttpClient httpClient;

    private String streamsUrl;
    private String apiUrl;
    private String tokenUrlArgument;

    /**
     * represents the API method called, used by the ApiResponseHandler
     */
    private enum ApiMethod {
        EVENTS_GET, EVENTS_CREATE, EVENTS_UPDATE, EVENTS_DELETE, STREAMS_GET, STREAMS_CREATE,
        STREAMS_UPDATE, STREAMS_DELETE, ADD_ATTACHMENT, GET_ATTACHMENT, DELETE_ATTACHMENT
    }

    private WeakReference<AbstractConnection> weakConnection;

    private Logger logger = Logger.getInstance();

    /**
     * Constructor for online module
     *
     * @param pUrl            the url of the remote API
     * @param token           the token passed with each request for auth
     * @param pWeakConnection weak reference to connection
     */
    public OnlineManager(String pUrl, String token,
                         WeakReference<AbstractConnection> pWeakConnection) {
        apiUrl = pUrl;
        streamsUrl = pUrl + "streams"; // ?auth=" + token;
        tokenUrlArgument = "?auth=" + token;
        weakConnection = pWeakConnection;
    }

    public HttpClient getHttpClient() {
        if(httpClient == null) {
            httpClient = new HttpClient(apiUrl, tokenUrlArgument);
        }
        return httpClient;
    }

  /*
   * Streams management
   */

    public void getStreams(final Filter filter, final GetStreamsCallback onlineManagerStreamsCallback) {
        new Thread() {
            @Override
            public void run() {
                String url = streamsUrl + tokenUrlArgument;
                if (filter != null) {
                    url += filter.toUrlParameters();
                }

                logger.log("OnlineManager: get: Get request at: "
                        + url);

                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    new ApiResponseHandler(ApiMethod.STREAMS_GET, null, null, null,
                            onlineManagerStreamsCallback, null, null).handleResponse(response);

                } catch (IOException e) {
                    onlineManagerStreamsCallback.onApiError(e.getMessage(), null);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void createStream(final Stream newStream, final StreamsCallback cacheStreamsCallback) {
        new Thread() {
            @Override
            public void run() {
                try {
                    String jsonStream = JsonConverter.toJson(newStream);
                    logger.log("OnlineManager: create: Post request at: "
                            + streamsUrl
                            + tokenUrlArgument
                            + ", body: "
                            + jsonStream);

                    RequestBody bodyString = RequestBody.create(JSON, jsonStream);
                    Request request = new Request.Builder()
                            .url(streamsUrl + tokenUrlArgument)
                            .post(bodyString)
                            .build();
                    Response response = client.newCall(request).execute();
                    new ApiResponseHandler(ApiMethod.STREAMS_CREATE, null, null, cacheStreamsCallback, null, null,
                            newStream).handleResponse(response);

                } catch (IOException e) {
                    cacheStreamsCallback.onApiError(e.getMessage(), null);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void deleteStream(final Stream streamToDelete, final boolean mergeEventsWithParent,
                             final StreamsCallback cacheStreamsCallback) {
        new Thread() {
            @Override
            public void run() {
                String deleteUrl =
                        streamsUrl
                                + "/"
                                + streamToDelete.getId()
                                + tokenUrlArgument
                                + "&mergeEventsWithParent="
                                + mergeEventsWithParent;
                logger.log("OnlineManager: delete Stream: Delete request at: "
                        + deleteUrl);
                // TODO maybe add mergeEventsWithParent as bodyString

                Request request = new Request.Builder()
                        .url(deleteUrl)
                        .delete()
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    new ApiResponseHandler(ApiMethod.STREAMS_DELETE, null, null, cacheStreamsCallback, null, null,
                            streamToDelete).handleResponse(response);

                } catch (IOException e) {
                    cacheStreamsCallback.onApiError(e.getMessage(), null);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void updateStream(final Stream streamToUpdate, final StreamsCallback cacheStreamsCallback) {
        new Thread() {
            @Override
            public void run() {
                String updateUrl = streamsUrl + "/" + streamToUpdate.getId() + tokenUrlArgument;
                logger.log("OnlineManager: update Stream: Update request at: "
                        + updateUrl);

                try {
                    RequestBody bodyString = RequestBody.create(JSON, JsonConverter.toJson(streamToUpdate));
                    Request request = new Request.Builder()
                            .url(updateUrl)
                            .put(bodyString)
                            .build();
                    Response response = client.newCall(request).execute();
                    new ApiResponseHandler(ApiMethod.STREAMS_UPDATE, null, null, cacheStreamsCallback, null, null,
                            streamToUpdate).handleResponse(response);

                } catch (IOException e) {
                    cacheStreamsCallback.onApiError(e.getMessage(), null);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * custom response handler to handle replies to API requests.
     *
     * @author ik
     */
    private class ApiResponseHandler {

        private ApiMethod apiMethod;
        private StreamsCallback streamsCallback;
        private GetStreamsCallback getStreamsCallback;

        /**
         * Constructor for ApiResponseHandler. Depending on wether the requests
         * concerns Streams or Events, a pStreamsCallback or a pEventsCallback needs
         * to be provided. pEvent or pStream is used when updating or creating an
         * item to retrieve the id on the server response.
         *
         * @param apiMethod
         * @param streamsCallback
         * @param getStreamsCallback
         * @param stream
         */
        public ApiResponseHandler(ApiMethod apiMethod,
                                  final StreamsCallback streamsCallback,
                                  final GetStreamsCallback getStreamsCallback, final Stream stream) {
            this.apiMethod = apiMethod;
            this.streamsCallback = streamsCallback;
            this.getStreamsCallback = getStreamsCallback;
        }

        public String handleResponse(Response response) throws IOException {

            int statusCode = response.code();

            logger.log("ApiResponseHandler: response status code: " + statusCode);
            String responseBody = null;
            double serverTime = 0;
            if (response.body() != null) {
                responseBody = response.body().string();
                logger.log("ApiResponseHandler: handling reply entity : " + responseBody);
                serverTime = JsonConverter.retrieveServerTime(responseBody);
            }

            if (statusCode == HttpURLConnection.HTTP_CREATED || statusCode == HttpURLConnection.HTTP_OK) {
                // saul good
                switch (apiMethod) {

                    case STREAMS_GET:
                        Map<String, Stream> receivedStreams =
                                JsonConverter.createStreamsTreeFromJson(responseBody);
                        for (Stream receivedStream : receivedStreams.values()) {
                            receivedStream.assignConnection(weakConnection);
                        }
                        Map<String, Double> streamDeletions =
                                JsonConverter.createStreamDeletionsTreeFromJson(responseBody);
                        getStreamsCallback.apiCallback(receivedStreams, streamDeletions, serverTime);
                        break;

                    case STREAMS_CREATE:
                        Stream createdStream = JsonConverter.retrieveStreamFromJson(responseBody);
                        createdStream.assignConnection(weakConnection);

                        logger.log("ApiResponseHandler: stream created successfully: id="
                                + createdStream.getId());
                        streamsCallback.onApiSuccess(
                                "Online: stream with Id=" + createdStream.getId() + " created on API", createdStream,
                                serverTime);
                        break;

                    case STREAMS_UPDATE:
                        Stream updatedStream = JsonConverter.retrieveStreamFromJson(responseBody);
                        updatedStream.assignConnection(weakConnection);
                        logger.log("ApiResponseHandler: stream updated successfully: id="
                                + updatedStream.getId());
                        streamsCallback.onApiSuccess(
                                "Online: stream with Id=" + updatedStream.getId() + " updated on API", updatedStream,
                                serverTime);
                        break;

                    case STREAMS_DELETE:
                        if (JsonConverter.hasStreamDeletionField(responseBody)) {
                            // stream was deleted, retrieve streamDeletion id field
                            streamsCallback.onApiSuccess(
                                    JsonConverter.retrieveDeletedStreamId(responseBody), null, serverTime);
                        } else {
                            // stream was trashed, forward as an update to callback
                            Stream trashedStream = JsonConverter.retrieveStreamFromJson(responseBody);
                            trashedStream.assignConnection(weakConnection);
                            streamsCallback.onApiSuccess(
                                    "Online: stream with Id=" + trashedStream.getId() + " trashed on API",
                                    trashedStream, serverTime);
                        }
                        break;

                    default:

                }

            } else {
                logger.log("Online: issue in responseHandler");
                if (streamsCallback != null) {
                    streamsCallback.onApiError(responseBody, serverTime);
                } else if (getStreamsCallback != null) {
                    getStreamsCallback.onApiError(responseBody, serverTime);
                }
            }
            return null;

        }

    }

}
