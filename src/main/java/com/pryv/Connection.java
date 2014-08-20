package com.pryv;

import java.util.Map;

import com.pryv.api.CacheEventsAndStreamsManager;
import com.pryv.api.EventsCallback;
import com.pryv.api.EventsManager;
import com.pryv.api.Filter;
import com.pryv.api.StreamsCallback;
import com.pryv.api.StreamsManager;
import com.pryv.api.Supervisor;
import com.pryv.api.database.DBinitCallback;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;
import com.pryv.utils.Logger;

/**
 *
 * Pryv API connection
 *
 * @author ik
 *
 */
public class Connection implements EventsManager, StreamsManager {

  private String username;
  private String token;
  private String apiDomain = Pryv.API_DOMAIN; // pryv.io or pryv.in
  private String apiScheme = "https";
  private String url;
  private EventsManager cacheEventsManager;
  private StreamsManager cacheStreamsManager;
  private Supervisor supervisor;

  private Logger logger = Logger.getInstance();

  /**
   * Connection constructor. builds the Url to which the online requests are
   * done using the provided username and API domain and Scheme from the Pryv
   * object. Instanciates Supervisor and CacheEventsAndStreamsManager.
   *
   * @param pUsername
   *          username used
   * @param pToken
   * @param dbInitCallback
   */
  public Connection(String pUsername, String pToken, DBinitCallback dbInitCallback) {
    username = pUsername;
    token = pToken;
    url = apiScheme + "://" + username + "." + apiDomain + "/";
    supervisor = new Supervisor();
    cacheEventsManager = new CacheEventsAndStreamsManager(url, token, dbInitCallback);
    cacheStreamsManager = (StreamsManager) cacheEventsManager;
  }

  public String getUsername() {
    return username;
  }

  public String getToken() {
    return token;
  }

  public String getApiDomain() {
    return apiDomain;
  }

  public String getApiScheme() {
    return apiScheme;
  }

  public String getUrl() {
    return url;
  }

  /*
   * Events management
   */

  @Override
  public void getEvents(final Filter filter, EventsCallback userEventsCallback) {
    // send supervisor's events on User's callback.onEventsPartialResult()
    userEventsCallback.onSupervisorRetrieveEventsSuccess(supervisor.getEvents(filter));

    // forward getEvents() to Cache
    cacheEventsManager.getEvents(filter, new ConnectionEventsCallback(userEventsCallback, filter));
  }

  @Override
  public void createEvent(Event newEvent, EventsCallback userEventsCallback) {
    // create event in Supervisor

    // forward call to cache
    cacheEventsManager
      .createEvent(newEvent, new ConnectionEventsCallback(userEventsCallback, null));
  }

  @Override
  public void deleteEvent(String id, EventsCallback userEventsCallback) {
    // delete Event in Supervisor

    // forward call to cache
    cacheEventsManager.deleteEvent(id, new ConnectionEventsCallback(userEventsCallback, null));
  }

  @Override
  public void updateEvent(Event eventToUpdate, EventsCallback userEventsCallback) {
    // update Event in Supervisor

    // forward call to cache
    cacheEventsManager.updateEvent(eventToUpdate, new ConnectionEventsCallback(userEventsCallback,
      null));
  }

  /*
   * Streams management
   */

  @Override
  public void getStreams(Filter filter, final StreamsCallback userStreamsCallback) {

    // send Streams retrieved from Supervisor
    userStreamsCallback.onSupervisorRetrieveStreamsSuccess(supervisor.getStreams());

    cacheStreamsManager.getStreams(filter, new ConnectionStreamsCallback(userStreamsCallback));
  }

  @Override
  public void createStream(Stream newStream, StreamsCallback userStreamCallback) {
    // TODO Auto-generated method stub
  }

  @Override
  public void deleteStream(String id, StreamsCallback userStreamCallback) {
    // TODO Auto-generated method stub

  }

  @Override
  public void updateStream(Stream streamToUpdate, StreamsCallback userStreamCallback) {
    // TODO Auto-generated method stub
  }

  /**
   * EventsCallback used in Connection class
   *
   * @author ik
   *
   */
  private class ConnectionEventsCallback implements EventsCallback {

    private EventsCallback userEventsCallback;
    private Filter filter;

    public ConnectionEventsCallback(EventsCallback pUserEventsCallback, Filter pFilter) {
      userEventsCallback = pUserEventsCallback;
      filter = pFilter;
    }

    @Override
    public void onOnlineRetrieveEventsSuccess(Map<String, Event> onlineEvents) {
      logger.log("Connection: onEventsSuccess");
      // update existing references with JSON received from online
      supervisor.updateEvents(onlineEvents);
      // return merged events from Supervisor
      userEventsCallback.onOnlineRetrieveEventsSuccess(supervisor.getEvents(filter));
    }

    @Override
    public void onCacheRetrieveEventsSuccess(Map<String, Event> cacheEvents) {
      // update existing Events with those retrieved from the cache
      supervisor.updateEvents(cacheEvents);
      // return merged events from Supervisor
      userEventsCallback.onCacheRetrieveEventsSuccess(supervisor.getEvents(filter));
    }

    @Override
    public void onEventsRetrievalError(String message) {
      userEventsCallback.onEventsRetrievalError(message);
    }

    // unused
    @Override
    public void onSupervisorRetrieveEventsSuccess(Map<String, Event> supervisorEvents) {
    }

    @Override
    public void onEventsSuccess(String successMessage) {
      userEventsCallback.onEventsSuccess(successMessage);
    }

    @Override
    public void onEventsError(String errorMessage) {
      userEventsCallback.onEventsError(errorMessage);
    }
  }

  /**
   * StreamsCallback used in Connection class
   *
   * @author ik
   *
   */
  private class ConnectionStreamsCallback implements StreamsCallback {

    private StreamsCallback userStreamsCallback;

    public ConnectionStreamsCallback(StreamsCallback pUserStreamsCallback) {
      userStreamsCallback = pUserStreamsCallback;
    }

    @Override
    public void onOnlineRetrieveStreamsSuccess(Map<String, Stream> streams) {
      supervisor.updateStreams(streams);
      // forward updated Streams
      userStreamsCallback.onOnlineRetrieveStreamsSuccess(supervisor.getStreams());
    }

    @Override
    public void onCacheRetrieveStreamSuccess(Map<String, Stream> newStreams) {
      supervisor.updateStreams(newStreams);
      // forward updated Streams
      userStreamsCallback.onCacheRetrieveStreamSuccess(supervisor.getStreams());
    }

    @Override
    public void onStreamsRetrievalError(String message) {
      userStreamsCallback.onStreamsRetrievalError(message);
    }

    // unused
    @Override
    public void onSupervisorRetrieveStreamsSuccess(Map<String, Stream> supervisorStreams) {
    }

    @Override
    public void onStreamsSuccess(String successMessage) {
      userStreamsCallback.onStreamsSuccess(successMessage);
    }

    @Override
    public void onStreamError(String errorMessage) {
      userStreamsCallback.onStreamError(errorMessage);
    }

  }

}
