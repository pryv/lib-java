package com.pryv.api;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;

import com.pryv.Connection;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;
import com.pryv.utils.JsonConverter;
import com.pryv.utils.Logger;

/**
 *
 * OnlineEventsAndStreamsManager fetches data from online Pryv API
 *
 * @author ik
 *
 */
public class OnlineEventsAndStreamsManager implements EventsManager, StreamsManager {

  private String eventsUrl;
  private String streamsUrl;
  private String tokenUrlArgument;

  /**
   * represents the type of reply that is being handled by the
   * ApiResponseHandler
   *
   * @author ik
   *
   */
  private enum RequestType {
    GET_EVENTS, CREATE_EVENT, UPDATE_EVENT, DELETE_EVENT, GET_STREAMS, CREATE_STREAM,
    UPDATE_STREAM, DELETE_STREAM
  }

  private WeakReference<Connection> weakConnection;

  private Logger logger = Logger.getInstance();

  /**
   * Constructor for online module
   *
   * @param pUrl
   *          the url of the remote API
   * @param token
   *          the token passed with each request for auth
   * @param pWeakConnection
   *          weak reference to connection
   */
  public OnlineEventsAndStreamsManager(String pUrl, String token,
    WeakReference<Connection> pWeakConnection) {
    eventsUrl = pUrl + "events"; // ?auth=" + token;
    streamsUrl = pUrl + "streams"; // ?auth=" + token;
    tokenUrlArgument = "?auth=" + token;
    weakConnection = pWeakConnection;
  }

  /*
   * Events management
   */

  @Override
  public void getEvents(final Filter filter, final EventsCallback cacheEventsCallback) {
    new Thread() {
      @Override
      public void run() {
        try {
          logger.log("Online: getEvents: Get request at: "
            + eventsUrl
              + tokenUrlArgument
              + filter.toUrlParameters());
          Request
            .Get(eventsUrl + tokenUrlArgument + filter.toUrlParameters())
            .execute()
            .handleResponse(
              new ApiResponseHandler(RequestType.GET_EVENTS, cacheEventsCallback, null, null, null));
        } catch (ClientProtocolException e) {
          cacheEventsCallback.onEventsError(e.getMessage());
          e.printStackTrace();
        } catch (IOException e) {
          cacheEventsCallback.onEventsError(e.getMessage());
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
          logger.log("Online: createEvent: Post request at: "
            + eventsUrl
              + tokenUrlArgument
              + ", body: "
              + JsonConverter.toJson(newEvent));
          Request
            .Post(eventsUrl + tokenUrlArgument)
            .bodyString(JsonConverter.toJson(newEvent), ContentType.APPLICATION_JSON)
            .execute()
            .handleResponse(
              new ApiResponseHandler(RequestType.CREATE_EVENT, cacheEventsCallback, null, newEvent,
                null));
        } catch (ClientProtocolException e) {
          cacheEventsCallback.onEventsError(e.getMessage());
          e.printStackTrace();
        } catch (IOException e) {
          cacheEventsCallback.onEventsError(e.getMessage());
          e.printStackTrace();
        }
      }
    }.start();
  }

  @Override
  public void deleteEvent(Event eventToDelete, final EventsCallback cacheEventsCallback) {
    new Thread() {
      @Override
      public void run() {
        try {
          String deleteUrl = eventsUrl + "/" + eventToDelete.getId() + tokenUrlArgument;
          logger.log("Online: deleteEvent: Delete request at: " + deleteUrl);
          Request
            .Delete(deleteUrl)
            .execute()
            .handleResponse(
              new ApiResponseHandler(RequestType.DELETE_EVENT, cacheEventsCallback, null,
                eventToDelete, null));
        } catch (ClientProtocolException e) {
          cacheEventsCallback.onEventsError(e.getMessage());
          e.printStackTrace();
        } catch (IOException e) {
          cacheEventsCallback.onEventsError(e.getMessage());
          e.printStackTrace();
        }
      }
    }.start();
  }

  @Override
  public void updateEvent(Event eventToUpdate, final EventsCallback cacheEventsCallback) {
    new Thread() {
      @Override
      public void run() {
        try {
          String updateUrl = eventsUrl + "/" + eventToUpdate.getId() + tokenUrlArgument;
          logger.log("Online: updateEvent: Update request at: " + updateUrl);
          Request
            .Put(updateUrl)
            .bodyString(JsonConverter.toJson(eventToUpdate), ContentType.APPLICATION_JSON)
            .execute()
            .handleResponse(
              new ApiResponseHandler(RequestType.UPDATE_EVENT, cacheEventsCallback, null,
                eventToUpdate, null));
        } catch (ClientProtocolException e) {
          cacheEventsCallback.onEventsError(e.getMessage());
          e.printStackTrace();
        } catch (IOException e) {
          cacheEventsCallback.onEventsError(e.getMessage());
          e.printStackTrace();
        }
      }
    }.start();
  }

  /*
   * Streams management
   */

  @Override
  public void getStreams(Filter filter, final StreamsCallback cacheStreamsCallback) {
    new Thread() {
      @Override
      public void run() {
        try {
          logger.log("Online: getStreams: Get request at: " + streamsUrl + tokenUrlArgument);
          Request
            .Get(streamsUrl + tokenUrlArgument)
            .execute()
            .handleResponse(
              new ApiResponseHandler(RequestType.GET_STREAMS, null, cacheStreamsCallback, null,
                null));

        } catch (ClientProtocolException e) {
          cacheStreamsCallback.onStreamError(e.getMessage());
          e.printStackTrace();
        } catch (IOException e) {
          cacheStreamsCallback.onStreamError(e.getMessage());
          e.printStackTrace();
        }
      }
    }.start();
  }

  @Override
  public void createStream(Stream newStream, final StreamsCallback cacheStreamsCallback) {
    new Thread() {
      @Override
      public void run() {
        try {
          logger.log("Online: createStream: Post request at: "
            + streamsUrl
              + tokenUrlArgument
              + ", body: "
              + JsonConverter.toJson(newStream));
          Request
            .Post(streamsUrl + tokenUrlArgument)
            .bodyString(JsonConverter.toJson(newStream), ContentType.APPLICATION_JSON)
            .execute()
            .handleResponse(
              new ApiResponseHandler(RequestType.CREATE_STREAM, null, cacheStreamsCallback, null,
                newStream));
        } catch (ClientProtocolException e) {
          cacheStreamsCallback.onStreamError(e.getMessage());
          e.printStackTrace();
        } catch (IOException e) {
          cacheStreamsCallback.onStreamError(e.getMessage());
          e.printStackTrace();
        }
      }
    }.start();
  }

  @Override
  public void deleteStream(Stream streamToDelete, boolean mergeEventsWithParent,
    StreamsCallback cacheStreamsCallback) {
    new Thread() {
      @Override
      public void run() {
        try {
          String deleteUrl = streamsUrl + "/" + streamToDelete.getId() + tokenUrlArgument;
          logger.log("Online: delete Stream: Delete request at: " + deleteUrl);
          // TODO maybe add mergeEventsWithParent as bodyString
          Request
            .Delete(deleteUrl)
            .execute()
            .handleResponse(
              new ApiResponseHandler(RequestType.DELETE_STREAM, null, cacheStreamsCallback, null,
                streamToDelete));
        } catch (ClientProtocolException e) {
          cacheStreamsCallback.onStreamError(e.getMessage());
          e.printStackTrace();
        } catch (IOException e) {
          cacheStreamsCallback.onStreamError(e.getMessage());
          e.printStackTrace();
        }
      }
    }.start();
  }

  @Override
  public void updateStream(Stream streamToUpdate, final StreamsCallback cacheStreamsCallback) {
    new Thread() {
      @Override
      public void run() {
        try {
          String updateUrl = streamsUrl + "/" + streamToUpdate.getId() + tokenUrlArgument;
          logger.log("Online: update Stream: Update request at: " + updateUrl);
          Request
            .Put(updateUrl)
            .bodyString(JsonConverter.toJson(streamToUpdate), ContentType.APPLICATION_JSON)
            .execute()
            .handleResponse(
              new ApiResponseHandler(RequestType.UPDATE_STREAM, null, cacheStreamsCallback, null,
                streamToUpdate));
        } catch (ClientProtocolException e) {
          cacheStreamsCallback.onStreamError(e.getMessage());
          e.printStackTrace();
        } catch (IOException e) {
          cacheStreamsCallback.onStreamError(e.getMessage());
          e.printStackTrace();
        }
      }
    }.start();
  }

  /**
   * custom response handler to handle replies to API requests.
   *
   * @author ik
   *
   */
  private class ApiResponseHandler implements ResponseHandler<String> {

    private RequestType requestType;
    private EventsCallback onlineEventsCallback;
    private StreamsCallback onlineStreamsCallback;
    private Event event;
    private Stream stream;

    public ApiResponseHandler(RequestType type, final EventsCallback pEventsCallback,
      final StreamsCallback pStreamsCallback, final Event pEvent, final Stream pStream) {
      event = pEvent;
      stream = pStream;
      requestType = type;
      onlineEventsCallback = pEventsCallback;
      onlineStreamsCallback = pStreamsCallback;
    }

    @Override
    public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {

      int statusCode = response.getStatusLine().getStatusCode();

      logger.log("ApiResponseHandler: response status code: " + statusCode);
      String responseBody = null;
      double serverTime = 0;
      if (response.getEntity() != null) {
        responseBody = EntityUtils.toString(response.getEntity());
        logger.log("ApiResponseHandler: handling reply entity : " + responseBody);
        serverTime = JsonConverter.retrieveServerTime(responseBody);
      }

      if (statusCode == HttpStatus.SC_CREATED
        || statusCode == HttpStatus.SC_OK
          || statusCode == HttpStatus.SC_NO_CONTENT) {
        // saul good
        switch (requestType) {

          case GET_EVENTS:
            Map<String, Event> receivedEvents = JsonConverter.createEventsFromJson(responseBody);
            for (Event receivedEvent : receivedEvents.values()) {
              receivedEvent.assignConnection(weakConnection);
            }
            logger.log("Online: received " + receivedEvents.size() + " event(s) from API.");
            onlineEventsCallback.onEventsRetrievalSuccess(receivedEvents, serverTime);
            break;

          case CREATE_EVENT:
            Event createdEvent = JsonConverter.retrieveEventFromJson(responseBody);
            createdEvent.assignConnection(weakConnection);
            createdEvent.setClientId(event.getClientId());
            createdEvent.setStreamClientId(event.getStreamClientId());
            logger.log("Online: event created successfully: cid="
              + createdEvent.getClientId()
                + ", id="
                + createdEvent.getId());
            onlineEventsCallback.onEventsSuccess(
              "Online: event with clientId="
                + createdEvent.getClientId()
                  + ", Id="
                  + createdEvent.getId()
                  + " created on API", createdEvent, null);
            break;

          case UPDATE_EVENT:
            Event updatedEvent = JsonConverter.retrieveEventFromJson(responseBody);
            updatedEvent.assignConnection(weakConnection);
            updatedEvent.setClientId(event.getClientId());
            updatedEvent.setStreamClientId(event.getStreamClientId());
            logger.log("Online: event updated successfully: cid="
              + updatedEvent.getClientId()
                + ", id="
                + updatedEvent.getId());
            onlineEventsCallback.onEventsSuccess(
              "Online: event with clientId="
                + updatedEvent.getClientId()
                  + ", Id="
                  + updatedEvent.getId()
                  + " updated on API", updatedEvent, null);
            break;

          case DELETE_EVENT:
            // si deleted, pas de body?
            if (statusCode == HttpStatus.SC_NO_CONTENT) {
              // deleted
              onlineEventsCallback.onEventsSuccess(
                "Online: event with clientId="
                  + event.getClientId()
                    + ", Id="
                    + event.getId()
                    + " deleted on API", null, null);
            } else {
              // trashed
              Event trashedEvent = JsonConverter.retrieveEventFromJson(responseBody);
              trashedEvent.assignConnection(weakConnection);
              trashedEvent.setClientId(event.getClientId());
              trashedEvent.setStreamClientId(event.getStreamClientId());
              onlineEventsCallback.onEventsSuccess(
                "Online: event with clientId="
                  + trashedEvent.getClientId()
                    + ", Id="
                    + trashedEvent.getId()
                    + " trashed on API", trashedEvent, null);
            }
            break;

          case GET_STREAMS:
            Map<String, Stream> receivedStreams = JsonConverter.createStreamsFromJson(responseBody);
            for (Stream receivedStream : receivedStreams.values()) {
              receivedStream.assignConnection(weakConnection);
            }
            onlineStreamsCallback.onStreamsRetrievalSuccess(receivedStreams, serverTime);
            break;

          case CREATE_STREAM:
            Stream createdStream = JsonConverter.retrieveStreamFromJson(responseBody);
            createdStream.assignConnection(weakConnection);
            createdStream.setClientId(stream.getClientId());
            // // reset parent and children client IDs
            // createdStream.setParentClientId(stream.getParentClientId());
            // if (createdStream.getChildren() != null) {
            // for (Stream childStream : createdStream.getChildren()) {
            //
            // }
            // }
            logger.log("Online: stream created successfully: cid="
              + createdStream.getClientId()
                + ", id="
                + createdStream.getId());
            onlineStreamsCallback.onStreamsSuccess(
              "Online: stream with clientId="
                + createdStream.getClientId()
                  + ", Id="
                  + createdStream.getId()
                  + " created on API", createdStream);
            break;

          case UPDATE_STREAM:
            Stream updatedStream = JsonConverter.retrieveStreamFromJson(responseBody);
            updatedStream.assignConnection(weakConnection);
            updatedStream.setClientId(stream.getClientId());
            logger.log("Online: stream updated successfully: cid="
              + updatedStream.getClientId()
                + ", id="
                + updatedStream.getId());
            onlineStreamsCallback.onStreamsSuccess(
              "Online: stream with clientId="
                + updatedStream.getClientId()
                  + ", Id="
                  + updatedStream.getId()
                  + " updated on API", updatedStream);
            break;

          case DELETE_STREAM:
            // si deleted, pas de body?
            if (statusCode == HttpStatus.SC_NO_CONTENT) {
              // deleted
              onlineStreamsCallback.onStreamsSuccess(
                "Online: stream with clientId="
                  + stream.getClientId()
                    + ", Id="
                    + stream.getId()
                    + " deleted on API", null);
            } else {
              // trashed
              Stream trashedStream = JsonConverter.retrieveStreamFromJson(responseBody);
              trashedStream.assignConnection(weakConnection);
              trashedStream.setClientId(stream.getClientId());
              onlineStreamsCallback.onStreamsSuccess("Online: stream with clientId="
                + trashedStream.getClientId()
                  + ", Id="
                  + trashedStream.getId()
                  + " trashed on API", trashedStream);
            }
            break;

          default:

        }

      } else {
        System.out.println("Online: issue in responseHandler");
      }
      return null;

    }

  }

  // // TODO remove old implementation
  // /**
  // * Thread that executes the Get Streams request to the Pryv server and
  // returns
  // * the response to the StreamsCallback as a String.
  // *
  // * @author ik
  // *
  // */
  // private class FetchStreamsThread extends Thread {
  //
  // private StreamsCallback streamsCallback;
  // private Filter filter;
  //
  // public FetchStreamsThread(Filter pFilter, StreamsCallback pStreamsCallback)
  // {
  // streamsCallback = pStreamsCallback;
  // filter = pFilter;
  // }
  //
  // @Override
  // public void run() {
  // try {
  // Request.Get(streamsUrl).execute().handleResponse(streamsResponseHandler);
  // } catch (ClientProtocolException e) {
  // streamsCallback.onStreamsRetrievalError(e.getMessage());
  // e.printStackTrace();
  // } catch (IOException e) {
  // streamsCallback.onStreamsRetrievalError(e.getMessage());
  // e.printStackTrace();
  // }
  // }
  //
  // private ResponseHandler<String> streamsResponseHandler = new
  // ResponseHandler<String>() {
  //
  // @Override
  // public String handleResponse(HttpResponse response) {
  // String textResponse;
  // try {
  // textResponse = EntityUtils.toString(response.getEntity());
  // logger.log("Online received streams: " + textResponse);
  // double serverTime = JsonConverter.retrieveServerTime(textResponse);
  // logger.log("retrieved time: " + serverTime);
  // Map<String, Stream> receivedStreams =
  // JsonConverter.createStreamsFromJson(textResponse);
  // for (Stream receivedStream : receivedStreams.values()) {
  // receivedStream.assignConnection(weakConnection);
  // }
  // streamsCallback.onStreamsRetrievalSuccess(receivedStreams, serverTime);
  // } catch (ParseException e) {
  // streamsCallback.onStreamsRetrievalError(e.getMessage());
  // e.printStackTrace();
  // } catch (JsonProcessingException e) {
  // streamsCallback.onStreamsRetrievalError(e.getMessage());
  // e.printStackTrace();
  // } catch (IOException e) {
  // streamsCallback.onStreamsRetrievalError(e.getMessage());
  // e.printStackTrace();
  // }
  // return null;
  // }
  // };
  // }
  //
  // /**
  // * Thread that executes the Get Events request to the Pryv server and
  // returns
  // * the response to the EventsCallback as a String.
  // *
  // * @author ik
  // *
  // */
  // private class FetchEventsThread extends Thread {
  // private String params = "";
  // private EventsCallback eventsCallback;
  // private ResponseHandler<String> eventsResponseHandler;
  //
  // public FetchEventsThread(String pParams, final EventsCallback
  // pEventsCallback) {
  // params = pParams;
  // eventsCallback = pEventsCallback;
  // instanciateResponseHandler();
  // }
  //
  // @Override
  // public void run() {
  // logger.log("Online: fetching events at " + eventsUrl + params);
  // try {
  // Request.Get(eventsUrl +
  // params).execute().handleResponse(eventsResponseHandler);
  // } catch (ClientProtocolException e) {
  // eventsCallback.onEventsRetrievalError(e.getMessage());
  // e.printStackTrace();
  // } catch (IOException e) {
  // eventsCallback.onEventsRetrievalError(e.getMessage());
  // e.printStackTrace();
  // }
  // }
  //
  // private void instanciateResponseHandler() {
  // eventsResponseHandler = new ResponseHandler<String>() {
  //
  // @Override
  // public String handleResponse(HttpResponse reply) {
  // String response;
  // try {
  // response = EntityUtils.toString(reply.getEntity());
  // logger.log("Online: received events: " + response);
  // double serverTime = JsonConverter.retrieveServerTime(response);
  // logger.log("retrieved time: " + serverTime);
  // Map<String, Event> receivedEvents =
  // JsonConverter.createEventsFromJson(response);
  // for (Event receivedEvent : receivedEvents.values()) {
  // receivedEvent.assignConnection(weakConnection);
  // }
  // logger.log("Online: received " + receivedEvents.size() +
  // " event(s) from API.");
  // eventsCallback.onEventsRetrievalSuccess(receivedEvents, serverTime);
  // } catch (ParseException e) {
  // eventsCallback.onEventsRetrievalError(e.getMessage());
  // e.printStackTrace();
  // } catch (JsonParseException e) {
  // eventsCallback.onEventsRetrievalError(e.getMessage());
  // e.printStackTrace();
  // } catch (JsonMappingException e) {
  // eventsCallback.onEventsRetrievalError(e.getMessage());
  // e.printStackTrace();
  // } catch (IOException e) {
  // eventsCallback.onEventsRetrievalError(e.getMessage());
  // e.printStackTrace();
  // }
  // return null;
  // }
  // };
  // }
  // }

}
