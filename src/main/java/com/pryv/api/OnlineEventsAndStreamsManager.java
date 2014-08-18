package com.pryv.api;

import java.io.IOException;
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

  private Logger logger = Logger.getInstance();

  public OnlineEventsAndStreamsManager(String pUrl, String token) {
    eventsUrl = pUrl + "events?auth=" + token;
    streamsUrl = pUrl + "streams?auth=" + token;
  }

  /*
   * Events management
   */

  @Override
  public void getEvents(Filter filter, EventsCallback eventsCallback) {
    new FetchEventsThread(filter.toUrlParameters(), eventsCallback).start();
  }

  @Override
  public void createEvent(Event newEvent) {
    // TODO Auto-generated method stub
  }

  @Override
  public void deleteEvent(String id) {
    // TODO Auto-generated method stub

  }

  @Override
  public void updateEvent(Event eventToUpdate) {
    // TODO Auto-generated method stub
  }

  /*
   * Streams management
   */

  @Override
  public void getStreams(StreamsCallback streamsCallback) {
    new FetchStreamsThread(streamsCallback).start();
  }

  @Override
  public Stream createStream(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void deleteStream(String id) {
    // TODO Auto-generated method stub

  }

  @Override
  public Stream updateStream(String id) {
    // TODO Auto-generated method stub
    return null;
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

    public FetchStreamsThread(StreamsCallback pStreamsCallback) {
      streamsCallback = pStreamsCallback;
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
          streamsCallback.onOnlineRetrieveStreamsSuccess(JsonConverter.createStreamsFromJson(textResponse));
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

    public FetchEventsThread(String pParams, EventsCallback pEventsCallback) {
      params = pParams;
      eventsCallback = pEventsCallback;
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

    private ResponseHandler<String> eventsResponseHandler = new ResponseHandler<String>() {

      @Override
      public String handleResponse(HttpResponse reply) {
        String response;
        try {
          response = EntityUtils.toString(reply.getEntity());
          logger.log("Online: received events: " + response);
          Map<String, Event> receivedEvents = JsonConverter.createEventsFromJson(response);
          eventsCallback.onOnlineRetrieveEventsSuccess(receivedEvents);
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
