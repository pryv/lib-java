package com.pryv.api;

import com.pryv.AbstractConnection;
import com.pryv.Filter;
import com.pryv.interfaces.EventsCallback;
import com.pryv.interfaces.GetEventsCallback;
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

    private String eventsUrl;
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
        eventsUrl = pUrl + "events"; // ?auth=" + token;
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
   * Attachments
   */

    /**
     * Create a new Event with an attachment
     *
     * @param eventWithAttachment
     * @param cacheEventsCallback
     */
    public void createEventWithAttachment(final Event eventWithAttachment,
                                          final EventsCallback cacheEventsCallback) {
        new Thread() {
            @Override
            public void run() {
                try {
                    Event eventWithoutAttachments = new Event();
                    eventWithoutAttachments.merge(eventWithAttachment, JsonConverter.getCloner());
                    eventWithoutAttachments.setAttachments(null);
                    String jsonEvent = JsonConverter.toJson(eventWithoutAttachments);

                    logger.log("OnlineManager: createEventWithAttachment: POST request at: "
                            + eventsUrl
                            + tokenUrlArgument
                            + ", body: "
                            + jsonEvent);

                    File file = eventWithAttachment.getFirstAttachment().getFile();

                    // create Multipart HTTP Entity
                    RequestBody body = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("event", jsonEvent)
                            .addFormDataPart("file", file.getName(), RequestBody.create(null, file))
                            .build();

                    Request request = new Request.Builder()
                            .url(eventsUrl + tokenUrlArgument)
                            .post(body)
                            .build();

                    Response response = client.newCall(request).execute();
                    new ApiResponseHandler(ApiMethod.EVENTS_CREATE, cacheEventsCallback, null, null, null,
                            eventWithAttachment, null).handleResponse(response);

                } catch (IOException e) {
                    cacheEventsCallback.onApiError(e.getMessage(), null);
                    e.printStackTrace();
                }
            }
        }.start();
    }

  /*
   * Events management
   */

    public void getEvents(final Filter filter, final GetEventsCallback getEventsCallback) {
        new Thread() {
            @Override
            public void run() {
                String url = eventsUrl+tokenUrlArgument;
                if (filter != null) {
                    url += filter.toUrlParameters();
                }

                logger.log("OnlineManager: get: Get request at: " + url);

                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    new OnlineManager.ApiResponseHandler(OnlineManager.ApiMethod.EVENTS_GET, null, getEventsCallback, null,
                            null, null, null).handleResponse(response);

                } catch (IOException e) {
                    getEventsCallback.onApiError(e.getMessage(), null);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void createEvent(final Event newEvent, final EventsCallback cacheEventsCallback) {
        new Thread() {
            @Override
            public void run() {
                try {
                    String jsonEvent = JsonConverter.toJson(newEvent);
                    logger.log("OnlineManager: create: Post request at: "
                            + eventsUrl
                            + tokenUrlArgument
                            + ", body: "
                            + jsonEvent);

                    RequestBody bodyString = RequestBody.create(JSON, jsonEvent);
                    Request request = new Request.Builder()
                            .url(eventsUrl + tokenUrlArgument)
                            .post(bodyString)
                            .build();
                    Response response = client.newCall(request).execute();
                    new ApiResponseHandler(ApiMethod.EVENTS_CREATE, cacheEventsCallback, null,
                            null, null, newEvent, null).handleResponse(response);

                } catch (IOException e) {
                    cacheEventsCallback.onApiError(e.getMessage(), null);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void deleteEvent(final Event eventToDelete, final EventsCallback cacheEventsCallback) {
        new Thread() {
            @Override
            public void run() {

                String deleteUrl = eventsUrl + "/" + eventToDelete.getId() + tokenUrlArgument;
                logger.log("OnlineManager: delete: Delete request at: " + deleteUrl);

                Request request = new Request.Builder()
                        .url(deleteUrl)
                        .delete()
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    new ApiResponseHandler(ApiMethod.EVENTS_DELETE, cacheEventsCallback, null,
                            null, null, eventToDelete, null).handleResponse(response);

                } catch (IOException e) {
                    cacheEventsCallback.onApiError(e.getMessage(), null);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void updateEvent(final Event eventToUpdate, final EventsCallback cacheEventsCallback) {
        new Thread() {
            @Override
            public void run() {
                String updateUrl = eventsUrl + "/" + eventToUpdate.getId() + tokenUrlArgument;
                logger.log("OnlineManager: update: Update request at: " + updateUrl);

                try {
                    RequestBody bodyString = RequestBody.create(JSON, JsonConverter.toJson(eventToUpdate));
                    Request request = new Request.Builder()
                            .url(updateUrl)
                            .put(bodyString)
                            .build();
                    Response response = client.newCall(request).execute();
                    new ApiResponseHandler(ApiMethod.EVENTS_UPDATE, cacheEventsCallback, null,
                            null, null, eventToUpdate, null).handleResponse(response);

                } catch (IOException e) {
                    cacheEventsCallback.onApiError(e.getMessage(), null);
                    e.printStackTrace();
                }
            }
        }.start();
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
        private EventsCallback eventsCallback;
        private GetEventsCallback getEventsCallback;
        private StreamsCallback streamsCallback;
        private GetStreamsCallback getStreamsCallback;
        private Event event;
        private Stream stream;

        /**
         * Constructor for ApiResponseHandler. Depending on wether the requests
         * concerns Streams or Events, a pStreamsCallback or a pEventsCallback needs
         * to be provided. pEvent or pStream is used when updating or creating an
         * item to retrieve the id on the server response.
         *
         * @param apiMethod
         * @param eventsCallback
         * @param getEventsCallback
         * @param streamsCallback
         * @param getStreamsCallback
         * @param event
         * @param stream
         */
        public ApiResponseHandler(ApiMethod apiMethod, final EventsCallback eventsCallback,
                                  final GetEventsCallback getEventsCallback,
                                  final StreamsCallback streamsCallback,
                                  final GetStreamsCallback getStreamsCallback,
                                  final Event event, final Stream stream) {
            this.event = event;
            this.stream = stream;
            this.apiMethod = apiMethod;
            this.eventsCallback = eventsCallback;
            this.getEventsCallback = getEventsCallback;
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

                    case EVENTS_GET:
                        List<Event> receivedEvents = JsonConverter.createEventsFromJson(responseBody);
                        for (Event receivedEvent : receivedEvents) {
                            receivedEvent.assignConnection(weakConnection);
                            Event.createOrReuse(receivedEvent);
                        }
                        Map<String, Double> eventDeletions = null;
                        logger.log("ApiResponseHandler: received "
                                + receivedEvents.size()
                                + " event(s) from API.");
                        getEventsCallback.apiCallback(receivedEvents, eventDeletions, serverTime);
                        break;

                    case EVENTS_CREATE:
                        String stoppedId = JsonConverter.retrieveStoppedIdFromJson(responseBody);
                        Event createdEvent = JsonConverter.retrieveEventFromJson(responseBody);
                        createdEvent.assignConnection(weakConnection);
                        Event.createOrReuse(createdEvent);
                        logger.log("ApiResponseHandler: event created successfully: Id="
                                + createdEvent.getId());
                        eventsCallback.onApiSuccess(
                                "Online: event with Id="
                                        + createdEvent.getId()
                                        + " created on API", createdEvent, stoppedId, serverTime);
                        break;

                    case EVENTS_UPDATE:
                        Event updatedEvent = JsonConverter.retrieveEventFromJson(responseBody);
                        updatedEvent.assignConnection(weakConnection);
                        updatedEvent.setId(event.getId());
                        Event.createOrReuse(updatedEvent);
                        logger.log("ApiResponseHandler: event updated successfully: Id="
                                + updatedEvent.getId());
                        eventsCallback.onApiSuccess(
                                "Online: event with Id="
                                        + updatedEvent.getId()
                                        + " updated on API", updatedEvent, null, serverTime);
                        break;

                    case EVENTS_DELETE:
                        if (JsonConverter.hasEventDeletionField(responseBody)) {
                            // event was deleted, retrieve streamDeletion id field
                            eventsCallback.onApiSuccess(
                                    JsonConverter.retrieveDeleteEventId(responseBody), null, null, serverTime);
                        } else {
                            // event was trashed, forward as an update to callback
                            Event trashedEvent = JsonConverter.retrieveEventFromJson(responseBody);
                            trashedEvent.assignConnection(weakConnection);
                            trashedEvent.setId(event.getId());
                            Event.createOrReuse(trashedEvent);
                            eventsCallback.onApiSuccess(
                                    "Online: event with Id="
                                            + trashedEvent.getId()
                                            + " trashed on API", trashedEvent, null, serverTime);
                        }
                        break;

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
                } else if (eventsCallback != null) {
                    eventsCallback.onApiError(responseBody, serverTime);
                } else if (getEventsCallback != null) {
                    getEventsCallback.onApiError(responseBody, serverTime);
                }
            }
            return null;

        }

    }

}
