package com.pryv.api;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.Map;

import com.pryv.Connection;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;
import com.pryv.utils.JsonConverter;
import com.pryv.utils.Logger;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

/**
 * OnlineEventsAndStreamsManager manages data from online Pryv API
 *
 * @author ik
 */
public class OnlineEventsAndStreamsManager implements EventsManager, StreamsManager {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient client = new OkHttpClient();

    private String eventsUrl;
    private String streamsUrl;
    private String tokenUrlArgument;

    /**
     * represents the type of reply that is being handled by the
     * ApiResponseHandler
     *
     * @author ik
     */
    private enum RequestType {
        GET_EVENTS, CREATE_EVENT, UPDATE_EVENT, DELETE_EVENT, GET_STREAMS, CREATE_STREAM,
        UPDATE_STREAM, DELETE_STREAM, ADD_ATTACHMENT, GET_ATTACHMENT, DELETE_ATTACHMENT
    }

    private WeakReference<Connection> weakConnection;

    private Logger logger = Logger.getInstance();

    /**
     * Constructor for online module
     *
     * @param pUrl            the url of the remote API
     * @param token           the token passed with each request for auth
     * @param pWeakConnection weak reference to connection
     */
    public OnlineEventsAndStreamsManager(String pUrl, String token,
                                         WeakReference<Connection> pWeakConnection) {
        eventsUrl = pUrl + "events"; // ?auth=" + token;
        streamsUrl = pUrl + "streams"; // ?auth=" + token;
        tokenUrlArgument = "?auth=" + token;
        weakConnection = pWeakConnection;
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

                    logger.log("OnlineEventsAndStreamsManager: createEventWithAttachment: POST request at: "
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
                    new ApiResponseHandler(RequestType.CREATE_EVENT, cacheEventsCallback, null,
                            eventWithAttachment, null).handleResponse(response);

                } catch (IOException e) {
                    cacheEventsCallback.onEventsError(e.getMessage(), null);
                    e.printStackTrace();
                }
            }
        }.start();
    }

  /*
   * Events management
   */

    @Override
    public void getEvents(final Filter filter, final EventsCallback cacheEventsCallback) {
        new Thread() {
            @Override
            public void run() {
                logger.log("OnlineEventsAndStreamsManager: getEvents: Get request at: "
                        + eventsUrl
                        + tokenUrlArgument
                        + filter.toUrlParameters());

                Request request = new Request.Builder()
                        .url(eventsUrl + tokenUrlArgument + filter.toUrlParameters())
                        .get()
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    new ApiResponseHandler(RequestType.GET_EVENTS, cacheEventsCallback, null, null, null).handleResponse(response);

                } catch (IOException e) {
                    cacheEventsCallback.onEventsError(e.getMessage(), null);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void createEvent(final Event newEvent, final EventsCallback cacheEventsCallback) {
        new Thread() {
            @Override
            public void run() {
                try {
                    String jsonEvent = JsonConverter.toJson(newEvent);
                    logger.log("OnlineEventsAndStreamsManager: createEvent: Post request at: "
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
                    new ApiResponseHandler(RequestType.CREATE_EVENT, cacheEventsCallback, null, newEvent,
                            null).handleResponse(response);

                } catch (IOException e) {
                    cacheEventsCallback.onEventsError(e.getMessage(), null);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void deleteEvent(final Event eventToDelete, final EventsCallback cacheEventsCallback) {
        new Thread() {
            @Override
            public void run() {

                String deleteUrl = eventsUrl + "/" + eventToDelete.getId() + tokenUrlArgument;
                logger.log("OnlineEventsAndStreamsManager: deleteEvent: Delete request at: " + deleteUrl);

                Request request = new Request.Builder()
                        .url(deleteUrl)
                        .delete()
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    new ApiResponseHandler(RequestType.DELETE_EVENT, cacheEventsCallback, null,
                            eventToDelete, null).handleResponse(response);

                } catch (IOException e) {
                    cacheEventsCallback.onEventsError(e.getMessage(), null);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void updateEvent(final Event eventToUpdate, final EventsCallback cacheEventsCallback) {
        new Thread() {
            @Override
            public void run() {
                String updateUrl = eventsUrl + "/" + eventToUpdate.getId() + tokenUrlArgument;
                logger.log("OnlineEventsAndStreamsManager: updateEvent: Update request at: " + updateUrl);

                try {
                    RequestBody bodyString = RequestBody.create(JSON, JsonConverter.toJson(eventToUpdate));
                    Request request = new Request.Builder()
                            .url(updateUrl)
                            .put(bodyString)
                            .build();
                    Response response = client.newCall(request).execute();
                    new ApiResponseHandler(RequestType.UPDATE_EVENT, cacheEventsCallback, null,
                            eventToUpdate, null).handleResponse(response);

                } catch (IOException e) {
                    cacheEventsCallback.onEventsError(e.getMessage(), null);
                    e.printStackTrace();
                }
            }
        }.start();
    }

  /*
   * Streams management
   */

    @Override
    public void getStreams(final Filter filter, final StreamsCallback onlineManagerStreamsCallback) {
        new Thread() {
            @Override
            public void run() {
                logger.log("OnlineEventsAndStreamsManager: getStreams: Get request at: "
                        + streamsUrl
                        + tokenUrlArgument);

                Request request = new Request.Builder()
                        .url(streamsUrl + tokenUrlArgument)
                        .get()
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    new ApiResponseHandler(RequestType.GET_STREAMS, null, onlineManagerStreamsCallback,
                            null, null).handleResponse(response);

                } catch (IOException e) {
                    onlineManagerStreamsCallback.onStreamError(e.getMessage(), null);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void createStream(final Stream newStream, final StreamsCallback cacheStreamsCallback) {
        new Thread() {
            @Override
            public void run() {
                try {
                    String jsonStream = JsonConverter.toJson(newStream);
                    logger.log("OnlineEventsAndStreamsManager: createStream: Post request at: "
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
                    new ApiResponseHandler(RequestType.CREATE_STREAM, null, cacheStreamsCallback, null,
                            newStream).handleResponse(response);

                } catch (IOException e) {
                    cacheStreamsCallback.onStreamError(e.getMessage(), null);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
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
                logger.log("OnlineEventsAndStreamsManager: delete Stream: Delete request at: "
                        + deleteUrl);
                // TODO maybe add mergeEventsWithParent as bodyString

                Request request = new Request.Builder()
                        .url(deleteUrl)
                        .delete()
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    new ApiResponseHandler(RequestType.DELETE_STREAM, null, cacheStreamsCallback, null,
                            streamToDelete).handleResponse(response);

                } catch (IOException e) {
                    cacheStreamsCallback.onStreamError(e.getMessage(), null);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void updateStream(final Stream streamToUpdate, final StreamsCallback cacheStreamsCallback) {
        new Thread() {
            @Override
            public void run() {
                String updateUrl = streamsUrl + "/" + streamToUpdate.getId() + tokenUrlArgument;
                logger.log("OnlineEventsAndStreamsManager: update Stream: Update request at: "
                        + updateUrl);

                try {
                    RequestBody bodyString = RequestBody.create(JSON, JsonConverter.toJson(streamToUpdate));
                    Request request = new Request.Builder()
                            .url(updateUrl)
                            .put(bodyString)
                            .build();
                    Response response = client.newCall(request).execute();
                    new ApiResponseHandler(RequestType.UPDATE_STREAM, null, cacheStreamsCallback, null,
                            streamToUpdate).handleResponse(response);

                } catch (IOException e) {
                    cacheStreamsCallback.onStreamError(e.getMessage(), null);
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

        private RequestType requestType;
        private EventsCallback onlineEventsCallback;
        private StreamsCallback onlineStreamsCallback;
        private Event event;
        private Stream stream;

        /**
         * Constructor for ApiResponseHandler. Depending on wether the requests
         * concerns Streams or Events, a pStreamsCallback or a pEventsCallback needs
         * to be provided. pEvent or pStream is used when updating or creating an
         * item to retrieve the clientId on the server response.
         *
         * @param type             the request type
         * @param pEventsCallback  optional
         * @param pStreamsCallback optional
         * @param pEvent           optional
         * @param pStream          optional
         */
        public ApiResponseHandler(RequestType type, final EventsCallback pEventsCallback,
                                  final StreamsCallback pStreamsCallback, final Event pEvent, final Stream pStream) {
            event = pEvent;
            stream = pStream;
            requestType = type;
            onlineEventsCallback = pEventsCallback;
            onlineStreamsCallback = pStreamsCallback;
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
                switch (requestType) {

                    case GET_EVENTS:
                        Map<String, Event> receivedEvents = JsonConverter.createEventsFromJson(responseBody);
                        for (Event receivedEvent : receivedEvents.values()) {
                            receivedEvent.assignConnection(weakConnection);
                        }
                        logger.log("ApiResponseHandler: received "
                                + receivedEvents.size()
                                + " event(s) from API.");
                        onlineEventsCallback.onEventsRetrievalSuccess(receivedEvents, serverTime);
                        break;

                    case CREATE_EVENT:
                        String stoppedId = JsonConverter.retrieveStoppedIdFromJson(responseBody);
                        Event createdEvent = JsonConverter.retrieveEventFromJson(responseBody);
                        createdEvent.assignConnection(weakConnection);
                        createdEvent.setClientId(event.getClientId());
                        logger.log("ApiResponseHandler: event created successfully: cid="
                                + createdEvent.getClientId()
                                + ", id="
                                + createdEvent.getId());
                        onlineEventsCallback.onEventsSuccess(
                                "Online: event with clientId="
                                        + createdEvent.getClientId()
                                        + ", Id="
                                        + createdEvent.getId()
                                        + " created on API", createdEvent, null, serverTime);
                        break;

                    case UPDATE_EVENT:
                        Event updatedEvent = JsonConverter.retrieveEventFromJson(responseBody);
                        updatedEvent.assignConnection(weakConnection);
                        updatedEvent.setClientId(event.getClientId());
                        logger.log("ApiResponseHandler: event updated successfully: cid="
                                + updatedEvent.getClientId()
                                + ", id="
                                + updatedEvent.getId());
                        onlineEventsCallback.onEventsSuccess(
                                "Online: event with clientId="
                                        + updatedEvent.getClientId()
                                        + ", Id="
                                        + updatedEvent.getId()
                                        + " updated on API", updatedEvent, null, serverTime);
                        break;

                    case DELETE_EVENT:
                        if (JsonConverter.hasEventDeletionField(responseBody)) {
                            // stream was deleted, retrieve streamDeletion id field
                            onlineEventsCallback.onEventsSuccess(
                                    JsonConverter.retrieveDeleteEventId(responseBody), null, null, serverTime);
                        } else {
                            // stream was trashed, forward as an update to callback
                            Event trashedEvent = JsonConverter.retrieveEventFromJson(responseBody);
                            trashedEvent.assignConnection(weakConnection);
                            trashedEvent.setClientId(event.getClientId());
                            onlineEventsCallback.onEventsSuccess(
                                    "Online: event with clientId="
                                            + trashedEvent.getClientId()
                                            + ", Id="
                                            + trashedEvent.getId()
                                            + " trashed on API", trashedEvent, null, serverTime);
                        }
                        break;

                    case GET_STREAMS:
                        Map<String, Stream> receivedStreams =
                                JsonConverter.createStreamsTreeFromJson(responseBody);
                        for (Stream receivedStream : receivedStreams.values()) {
                            receivedStream.assignConnection(weakConnection);
                        }
                        onlineStreamsCallback.onStreamsRetrievalSuccess(receivedStreams, serverTime);
                        break;

                    case CREATE_STREAM:
                        Stream createdStream = JsonConverter.retrieveStreamFromJson(responseBody);
                        createdStream.assignConnection(weakConnection);

                        logger.log("ApiResponseHandler: stream created successfully: id="
                                + createdStream.getId());
                        onlineStreamsCallback.onStreamsSuccess(
                                "Online: stream with Id=" + createdStream.getId() + " created on API", createdStream,
                                serverTime);
                        break;

                    case UPDATE_STREAM:
                        Stream updatedStream = JsonConverter.retrieveStreamFromJson(responseBody);
                        updatedStream.assignConnection(weakConnection);
                        logger.log("ApiResponseHandler: stream updated successfully: id="
                                + updatedStream.getId());
                        onlineStreamsCallback.onStreamsSuccess(
                                "Online: stream with Id=" + updatedStream.getId() + " updated on API", updatedStream,
                                serverTime);
                        break;

                    case DELETE_STREAM:
                        if (JsonConverter.hasStreamDeletionField(responseBody)) {
                            // stream was deleted, retrieve streamDeletion id field
                            onlineStreamsCallback.onStreamsSuccess(
                                    JsonConverter.retrieveDeletedStreamId(responseBody), null, serverTime);
                        } else {
                            // stream was trashed, forward as an update to callback
                            System.out.println("responseBody: " + responseBody);
                            Stream trashedStream = JsonConverter.retrieveStreamFromJson(responseBody);
                            trashedStream.assignConnection(weakConnection);
                            onlineStreamsCallback.onStreamsSuccess(
                                    "Online: stream with Id=" + trashedStream.getId() + " trashed on API",
                                    trashedStream, serverTime);
                        }
                        break;

                    default:

                }

            } else {
                System.out.println("Online: issue in responseHandler");
                if (stream != null) {
                    onlineStreamsCallback.onStreamError(responseBody, serverTime);
                } else {
                    onlineEventsCallback.onEventsError(responseBody, serverTime);
                }
            }
            return null;

        }

    }

}
