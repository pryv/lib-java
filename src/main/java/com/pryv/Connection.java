package com.pryv;

import java.util.Map;

import com.pryv.api.CacheEventsAndStreamsManager;
import com.pryv.api.EventsCallback;
import com.pryv.api.EventsManager;
import com.pryv.api.EventsSupervisor;
import com.pryv.api.Filter;
import com.pryv.api.StreamsCallback;
import com.pryv.api.StreamsManager;
import com.pryv.api.StreamsSupervisor;
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

  private long serverTime;
  /**
   * RTT between server and system: deltaTime = serverTime - systemTime
   */
  private long deltaTime = 0;

  private EventsManager cacheEventsManager;
  private StreamsManager cacheStreamsManager;
  private EventsSupervisor supervisor;

  private StreamsSupervisor streams;

  private Logger logger = Logger.getInstance();

  private final long millisToSeconds = 1000;

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
    streams = new StreamsSupervisor();
    supervisor = new EventsSupervisor(streams);
    cacheEventsManager = new CacheEventsAndStreamsManager(url, token, dbInitCallback, streams);
    cacheStreamsManager = (StreamsManager) cacheEventsManager;
  }

  /*
   * Memory Streams management
   */

  public Map<String, Stream> getStreams() {
    return streams.getRootStreams();
  }

  public Stream getStreamById(String streamId) {
    return streams.getStreamById(streamId);
  }

  /*
   * Events management
   */

  @Override
  public void getEvents(Filter filter, EventsCallback userEventsCallback) {
    if (Pryv.isSupervisorActive()) {
      // make sync request to supervisor
      supervisor.getEvents(filter, userEventsCallback);
    }
    if (Pryv.isCacheActive() || Pryv.isOnlineActive()) {
      // forward getEvents() to Cache
      cacheEventsManager
        .getEvents(filter, new ConnectionEventsCallback(userEventsCallback, filter));
    }
  }

  @Override
  public void createEvent(Event newEvent, EventsCallback userEventsCallback) {

    updateCreated(newEvent);

    if (Pryv.isSupervisorActive()) {
      // make sync request to supervisor
      supervisor.updateOrCreateEvent(newEvent, userEventsCallback);
    }
    if (Pryv.isCacheActive() || Pryv.isOnlineActive()) {
      // forward call to cache
      cacheEventsManager.createEvent(newEvent, new ConnectionEventsCallback(userEventsCallback,
        null));
    }
  }

  @Override
  public void deleteEvent(Event eventToDelete, EventsCallback userEventsCallback) {
    updateModified(eventToDelete);
    eventToDelete.setTrashed(true);

    if (Pryv.isSupervisorActive()) {
      // delete Event in Supervisor
      supervisor.deleteEvent(eventToDelete, userEventsCallback);
    }
    if (Pryv.isCacheActive() || Pryv.isOnlineActive()) {
      // forward call to cache
      cacheEventsManager.deleteEvent(eventToDelete, new ConnectionEventsCallback(
        userEventsCallback, null));
    }
  }

  @Override
  public void updateEvent(Event eventToUpdate, EventsCallback userEventsCallback) {
    updateModified(eventToUpdate);

    if (Pryv.isSupervisorActive()) {
      // update Event in Supervisor
      supervisor.updateOrCreateEvent(eventToUpdate, userEventsCallback);
    }
    if (Pryv.isCacheActive() || Pryv.isOnlineActive()) {
      // forward call to cache
      cacheEventsManager.updateEvent(eventToUpdate, new ConnectionEventsCallback(
        userEventsCallback, null));
    }
  }

  /*
   * Streams management
   */

  @Override
  public void getStreams(Filter filter, final StreamsCallback userStreamsCallback) {
    userStreamsCallback.onSupervisorRetrieveStreamsSuccess(streams.getRootStreams());
    if (Pryv.isCacheActive() || Pryv.isSupervisorActive()) {
      // forward call to cache
      cacheStreamsManager.getStreams(filter, new ConnectionStreamsCallback(userStreamsCallback));
    }
  }

  @Override
  public void createStream(Stream newStream, StreamsCallback userStreamsCallback) {
    updateCreated(newStream);
    if (Pryv.isSupervisorActive()) {
      // create Stream in Supervisor
      streams.updateOrCreateStream(newStream, userStreamsCallback);
    }
    if (Pryv.isCacheActive() || Pryv.isOnlineActive()) {
      // forward call to cache
      cacheStreamsManager.createStream(newStream,
        new ConnectionStreamsCallback(userStreamsCallback));
    }
  }

  @Override
  public void deleteStream(Stream streamToDelete, boolean mergeWithParent,
    StreamsCallback userStreamsCallback) {
    updateModified(streamToDelete);
    streamToDelete.setTrashed(true);
    // TODO check what to do with children
    for (Stream childStream : streamToDelete.getChildren()) {
      childStream.setTrashed(true);
    }
    if (Pryv.isSupervisorActive()) {
      // delete Stream in Supervisor
      streams.deleteStream(streamToDelete.getId(), mergeWithParent, userStreamsCallback);
    }
    if (Pryv.isCacheActive() || Pryv.isOnlineActive()) {
      // forward call to cache
      cacheStreamsManager.deleteStream(streamToDelete, mergeWithParent,
        new ConnectionStreamsCallback(userStreamsCallback));
    }
  }

  @Override
  public void updateStream(Stream streamToUpdate, StreamsCallback userStreamsCallback) {
    updateModified(streamToUpdate);
    if (Pryv.isSupervisorActive()) {
      // update Stream in Supervisor
      streams.updateOrCreateStream(streamToUpdate, userStreamsCallback);
    }
    if (Pryv.isCacheActive() || Pryv.isOnlineActive()) {
      // forward call to cache
      cacheStreamsManager.updateStream(streamToUpdate, new ConnectionStreamsCallback(
        userStreamsCallback));
    }
  }

  /**
   * Fill event's "created","createdBy", "modified" and "modifiedBy" fields.
   *
   * @param event
   */
  private void updateCreated(Event event) {
    event.setCreated(System.currentTimeMillis() / millisToSeconds + deltaTime);
    event.setCreatedBy(username);
    updateModified(event);
  }

  /**
   * Fill stream's "created", "createdBy", "modified" and "modifiedBy" fields.
   *
   * @param event
   */
  private void updateCreated(Stream stream) {
    stream.setCreated(System.currentTimeMillis() / millisToSeconds + deltaTime);
    stream.setCreatedBy(username);
    updateModified(stream);
  }

  /**
   * Update event's "modified" and "modifiedBy" fields.
   *
   * @param event
   *          the event to modifiy
   */
  private void updateModified(Event event) {
    event.setModified(System.currentTimeMillis() / millisToSeconds + deltaTime);
    event.setModifiedBy(username);
  }

  /**
   * Update stream's "modified" and modifiedBy" fields.
   *
   * @param stream
   *          the stream to modifiy
   */
  private void updateModified(Stream stream) {
    stream.setModified(System.currentTimeMillis() / millisToSeconds + deltaTime);
    stream.setModifiedBy(username);
  }

  /**
   * calculates the difference between server and system time: deltaTime =
   * serverTime - systemTime
   *
   * @param pServerTime
   */
  private void computeDelta(long pServerTime) {
    deltaTime = pServerTime - System.currentTimeMillis();
  }

  /**
   * EventsCallback used by Connection class
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
    public void onOnlineRetrieveEventsSuccess(Map<String, Event> onlineEvents, long pServerTime) {
      logger.log("Connection: onOnlineRetrieveEventsSuccess");

      // update server time
      serverTime = pServerTime;
      // compute delta time between system time and servertime
      computeDelta(pServerTime);
      // onlineStreams are not received here,

      if (onlineEvents != null) {
        for (Event onlineEvent : onlineEvents.values()) {
          supervisor.updateOrCreateEvent(onlineEvent, userEventsCallback);
        }
        // return merged events from Supervisor
        supervisor.getEvents(filter, this);
      }
    }

    @Override
    public void onCacheRetrieveEventsSuccess(Map<String, Event> cacheEvents) {
      // update existing Events with those retrieved from the cache
      for (Event cacheEvent : cacheEvents.values()) {
        supervisor.updateOrCreateEvent(cacheEvent, userEventsCallback);
      }
      // return merged events from Supervisor
      supervisor.getEvents(filter, this);
    }

    @Override
    public void onEventsRetrievalError(String message) {
      userEventsCallback.onEventsRetrievalError(message);
    }

    // unused
    @Override
    public void onSupervisorRetrieveEventsSuccess(Map<String, Event> supervisorEvents) {
      userEventsCallback.onSupervisorRetrieveEventsSuccess(supervisorEvents);
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
   * StreamsCallback used by Connection class
   *
   * @author ik
   *
   */
  private class ConnectionStreamsCallback implements StreamsCallback {

    private StreamsCallback userStreamsCallback;

    /**
     * public constructor
     *
     * @param pUserStreamsCallback
     *          the class to which the results are forwarder after optional
     *          processing.
     */
    public ConnectionStreamsCallback(StreamsCallback pUserStreamsCallback) {
      userStreamsCallback = pUserStreamsCallback;
    }

    @Override
    public void onOnlineRetrieveStreamsSuccess(Map<String, Stream> onlineStreams, long pServerTime) {
      // update server time
      serverTime = pServerTime;
      // compute delta time between system time and servertime
      computeDelta(pServerTime);
      // onlineStreams are not received here,

      if (onlineStreams != null) {
        for (Stream stream : onlineStreams.values()) {
          streams.updateOrCreateStream(stream, userStreamsCallback);
        }
        // forward updated Streams
        userStreamsCallback.onOnlineRetrieveStreamsSuccess(streams.getRootStreams(), serverTime);
      }
    }

    @Override
    public void onCacheRetrieveStreamSuccess(Map<String, Stream> cacheStream) {
      for (Stream stream : cacheStream.values()) {
        streams.updateOrCreateStream(stream, this);
      }
      // forward updated Streams
      userStreamsCallback.onCacheRetrieveStreamSuccess(streams.getRootStreams());
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

  // /**
  // * StreamsCallback instantiated by Connection at each request.
  // *
  // * @author ik
  // *
  // */
  // public class Connection2StreamsCallback implements StreamsCallback {
  //
  // private StreamsCallback userStreamsCallback;
  //
  // /**
  // * ConnectionStreamsCallback constructor. saves reference to calling
  // * StreamsCallback.
  // *
  // * @param pUserStreamsCallback
  // */
  // public Connection2StreamsCallback(StreamsCallback pUserStreamsCallback) {
  // userStreamsCallback = pUserStreamsCallback;
  // }
  //
  // @Override
  // public void onOnlineRetrieveStreamsSuccess(Map<String, Stream>
  // onlineStreams) {
  // logger.log("ConnectionCallback: received " + onlineStreams.size() +
  // " streams from cloud.");
  // if (Pryv.isCacheActive()) {
  // // merge with cache if cache used:
  // for (Stream onlineStream : onlineStreams.values()) {
  // cacheStreamsManager.updateStream(onlineStream, this);
  // }
  // cacheStreamsManager.getStreams(null, this);
  // } else if (Pryv.isSupervisorActive()) {
  // // if cache not activated merge with Supervisor
  // for (Stream onlineStream : onlineStreams.values()) {
  // try {
  // supervisor.updateOrCreateStream(onlineStream, this);
  // } catch (IncompleteFieldsException e) {
  // onStreamError(e.getMessage());
  // }
  // }
  // } else {
  // // if not local storage is used, foward the result to the caller
  // userStreamsCallback.onOnlineRetrieveStreamsSuccess(onlineStreams);
  // }
  // }
  //
  // @Override
  // public void onCacheRetrieveStreamSuccess(Map<String, Stream> cacheStreams)
  // {
  // if (Pryv.isSupervisorActive()) {
  // // merge with Supervisor if it is active
  // for (Stream cacheStream : cacheStreams.values()) {
  // try {
  // supervisor.updateOrCreateStream(cacheStream, userStreamsCallback);
  // } catch (IncompleteFieldsException e) {
  // userStreamsCallback.onStreamError(e.getMessage());
  // }
  // }
  // } else {
  // // if supervisor is not used, forward the result to the caller
  // userStreamsCallback.onCacheRetrieveStreamSuccess(cacheStreams);
  // }
  // }
  //
  // @Override
  // public void onSupervisorRetrieveStreamsSuccess(Map<String, Stream>
  // supervisorStreams) {
  // // forward to userEventsCallback
  // userStreamsCallback.onSupervisorRetrieveStreamsSuccess(supervisorStreams);
  // }
  //
  // @Override
  // public void onStreamsRetrievalError(String errorMessage) {
  // // forward to userEventsCallback
  // userStreamsCallback.onStreamsRetrievalError(errorMessage);
  // }
  //
  // @Override
  // public void onStreamsSuccess(String successMessage) {
  // // forward to userEventsCallback
  // userStreamsCallback.onStreamsSuccess(successMessage);
  // }
  //
  // @Override
  // public void onStreamError(String errorMessage) {
  // // forward to userEventsCallback
  // userStreamsCallback.onStreamError(errorMessage);
  // }
  //
  // }
  //
  // /**
  // * EventsCallback instantiated by Connection for each request
  // *
  // * @author ik
  // *
  // */
  // public class Connection2EventsCallback implements EventsCallback {
  //
  // private EventsCallback userEventsCallback;
  // private Filter filter;
  //
  // /**
  // * ConnectionEventsCallback constructor. saves reference to calling
  // * EventsCallback, as well as filter.
  // *
  // * @param pUserEventsCallback
  // */
  // public Connection2EventsCallback(EventsCallback pUserEventsCallback, Filter
  // pFilter) {
  // userEventsCallback = pUserEventsCallback;
  // filter = pFilter;
  // }
  //
  // @Override
  // public void onOnlineRetrieveEventsSuccess(Map<String, Event> onlineEvents)
  // {
  // logger.log("ConnectionCallback: received " + onlineEvents.size() +
  // " events from cloud.");
  // if (Pryv.isCacheActive()) {
  // // merge with cache if cache used:
  // for (Event onlineEvent : onlineEvents.values()) {
  // cacheEventsManager.updateEvent(onlineEvent, this);
  // }
  // cacheEventsManager.getEvents(filter, this);
  // } else if (Pryv.isSupervisorActive()) {
  // // if cache not activated merge with Supervisor
  // for (Event onlineEvent : onlineEvents.values()) {
  // try {
  // supervisor.updateOrCreateEvent(onlineEvent, this);
  // } catch (IncompleteFieldsException e) {
  // onEventsError(e.getMessage());
  // }
  // }
  // } else {
  // // if not local storage is used, foward the result to the caller
  // userEventsCallback.onOnlineRetrieveEventsSuccess(onlineEvents);
  // }
  // }
  //
  // @Override
  // public void onCacheRetrieveEventsSuccess(Map<String, Event> cacheEvents) {
  // if (Pryv.isSupervisorActive()) {
  // // merge with Supervisor if it is active
  // for (Event cacheEvent : cacheEvents.values()) {
  // try {
  // supervisor.updateOrCreateEvent(cacheEvent, userEventsCallback);
  // } catch (IncompleteFieldsException e) {
  // userEventsCallback.onEventsError(e.getMessage());
  // }
  // }
  // } else {
  // // if supervisor is not used, forward the result to the caller
  // userEventsCallback.onCacheRetrieveEventsSuccess(cacheEvents);
  // }
  // }
  //
  // @Override
  // public void onSupervisorRetrieveEventsSuccess(Map<String, Event>
  // supervisorEvents) {
  // // forward to userEventsCallback
  // userEventsCallback.onSupervisorRetrieveEventsSuccess(supervisorEvents);
  // }
  //
  // @Override
  // public void onEventsRetrievalError(String errorMessage) {
  // // forward to userEventsCallback
  // userEventsCallback.onEventsRetrievalError(errorMessage);
  // }
  //
  // @Override
  // public void onEventsSuccess(String successMessage) {
  // // forward to userEventsCallback
  // userEventsCallback.onEventsSuccess(successMessage);
  // }
  //
  // @Override
  // public void onEventsError(String errorMessage) {
  // // forward to userEventsCallback
  // userEventsCallback.onEventsError(errorMessage);
  // }
  // }

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

}
