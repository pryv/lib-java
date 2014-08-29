package com.pryv.api;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.pryv.Connection;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;
import com.pryv.utils.JsonConverter;
import com.pryv.utils.Logger;

/**
 *
 * OnlineEventsAndStreamsManager fetches objects from online Pryv API
 *
 * @author ik
 *
 */
public class OnlineEventsAndStreamsManager implements EventsManager, StreamsManager {

  private String eventsUrl;
  private String streamsUrl;

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
   *          weak reference to connectino
   */
  public OnlineEventsAndStreamsManager(String pUrl, String token,
    WeakReference<Connection> pWeakConnection) {
    eventsUrl = pUrl + "events?auth=" + token;
    streamsUrl = pUrl + "streams?auth=" + token;
    weakConnection = pWeakConnection;
  }

  /*
   * Events management
   */

  @Override
  public void getEvents(Filter filter, EventsCallback cacheEventsCallback) {
    new FetchEventsThread(filter.toUrlParameters(), cacheEventsCallback).start();
  }

  @Override
  public void createEvent(Event newEvent, EventsCallback cacheEventsCallback) {
    // TODO Auto-generated method stub
  }

  @Override
  public void deleteEvent(Event eventToDelete, EventsCallback cacheEventsCallback) {
    // TODO Auto-generated method stub

  }

  @Override
  public void updateEvent(Event eventToUpdate, EventsCallback cacheEventsCallback) {
    // TODO Auto-generated method stub
  }

  /*
   * Streams management
   */

  @Override
  public void getStreams(Filter filter, StreamsCallback cacheStreamsCallback) {
    new FetchStreamsThread(filter, cacheStreamsCallback).start();
  }

  @Override
  public void createStream(Stream newStream, StreamsCallback cacheStreamsCallback) {
    // TODO Auto-generated method stub
  }

  @Override
  public void deleteStream(Stream streamToDelete, boolean mergeWithParent,
    StreamsCallback cacheStreamsCallback) {
    // TODO Auto-generated method stub

  }

  @Override
  public void updateStream(Stream streamToUpdate, StreamsCallback cacheStreamsCallback) {
    // TODO Auto-generated method stub
  }

  /**
   * Thread that executes the Get Streams request to the Pryv server and returns
   * the response to the StreamsCallback as a String.
   *
   * @author ik
   *
   */
  private class FetchStreamsThread extends Thread {

    private StreamsCallback streamsCallback;
    private Filter filter;

    public FetchStreamsThread(Filter pFilter, StreamsCallback pStreamsCallback) {
      streamsCallback = pStreamsCallback;
      filter = pFilter;
    }

    @Override
    public void run() {
      try {
        Request.Get(streamsUrl).execute().handleResponse(streamsResponseHandler);
      } catch (ClientProtocolException e) {
        streamsCallback.onStreamsRetrievalError(e.getMessage());
        e.printStackTrace();
      } catch (IOException e) {
        streamsCallback.onStreamsRetrievalError(e.getMessage());
        e.printStackTrace();
      }
    }

    private ResponseHandler<String> streamsResponseHandler = new ResponseHandler<String>() {

      @Override
      public String handleResponse(HttpResponse response) {
        String textResponse;
        try {
          textResponse = EntityUtils.toString(response.getEntity());
          logger.log("Online received streams: " + textResponse);
          long serverTime = JsonConverter.retrieveServerTime(textResponse);
          logger.log("retrieved time: " + serverTime);
          Map<String, Stream> receivedStreams = JsonConverter.createStreamsFromJson(textResponse);
          for (Stream receivedStream : receivedStreams.values()) {
            receivedStream.assignConnection(weakConnection);
          }
          streamsCallback.onOnlineRetrieveStreamsSuccess(receivedStreams, serverTime);
        } catch (ParseException e) {
          streamsCallback.onStreamsRetrievalError(e.getMessage());
          e.printStackTrace();
        } catch (JsonProcessingException e) {
          streamsCallback.onStreamsRetrievalError(e.getMessage());
          e.printStackTrace();
        } catch (IOException e) {
          streamsCallback.onStreamsRetrievalError(e.getMessage());
          e.printStackTrace();
        }
        return null;
      }
    };
  }

  /**
   * Thread that executes the Get Events request to the Pryv server and returns
   * the response to the EventsCallback as a String.
   *
   * @author ik
   *
   */
  private class FetchEventsThread extends Thread {
    private String params = "";
    private EventsCallback eventsCallback;
    private ResponseHandler<String> eventsResponseHandler;

    public FetchEventsThread(String pParams, EventsCallback pEventsCallback) {
      params = pParams;
      eventsCallback = pEventsCallback;
      instanciateResponseHandler();
    }

    @Override
    public void run() {
      logger.log("Online: fetching events at " + eventsUrl + params);
      try {
        Request.Get(eventsUrl + params).execute().handleResponse(eventsResponseHandler);
      } catch (ClientProtocolException e) {
        eventsCallback.onEventsRetrievalError(e.getMessage());
        e.printStackTrace();
      } catch (IOException e) {
        eventsCallback.onEventsRetrievalError(e.getMessage());
        e.printStackTrace();
      }
    }

    private void instanciateResponseHandler() {
      eventsResponseHandler = new ResponseHandler<String>() {

        @Override
        public String handleResponse(HttpResponse reply) {
          String response;
          try {
            response = EntityUtils.toString(reply.getEntity());
            logger.log("Online: received events: " + response);
            long serverTime = JsonConverter.retrieveServerTime(response);
            logger.log("retrieved time: " + serverTime);
            Map<String, Event> receivedEvents = JsonConverter.createEventsFromJson(response);
            for (Event receivedEvent : receivedEvents.values()) {
              receivedEvent.assignConnection(weakConnection);
            }
            eventsCallback.onOnlineRetrieveEventsSuccess(receivedEvents, serverTime);
          } catch (ParseException e) {
            eventsCallback.onEventsRetrievalError(e.getMessage());
            e.printStackTrace();
          } catch (JsonParseException e) {
            eventsCallback.onEventsRetrievalError(e.getMessage());
            e.printStackTrace();
          } catch (JsonMappingException e) {
            eventsCallback.onEventsRetrievalError(e.getMessage());
            e.printStackTrace();
          } catch (IOException e) {
            eventsCallback.onEventsRetrievalError(e.getMessage());
            e.printStackTrace();
          }
          return null;
        }
      };
    }
  }

}
