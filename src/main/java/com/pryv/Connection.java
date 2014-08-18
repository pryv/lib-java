package com.pryv;

import java.sql.SQLException;
import java.util.Map;

import com.pryv.api.CacheEventsAndStreamsManager;
import com.pryv.api.EventsCallback;
import com.pryv.api.EventsManager;
import com.pryv.api.Filter;
import com.pryv.api.StreamsCallback;
import com.pryv.api.StreamsManager;
import com.pryv.api.database.DBinitCallback;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;
import com.pryv.utils.Logger;
import com.pryv.utils.Supervisor;

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

  public Connection(String pUsername, String pToken, DBinitCallback dbInitCallback) {
    username = pUsername;
    token = pToken;
    url = apiScheme + "://" + username + "." + apiDomain + "/";
    supervisor = new Supervisor();
    try {
      cacheEventsManager = new CacheEventsAndStreamsManager(url, token, dbInitCallback);
      cacheStreamsManager = (StreamsManager) cacheEventsManager;
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
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
  public void getEvents(final Filter filter, final EventsCallback userEventsCallback) {

    // send supervisor's events on User's callback.onEventsPartialResult()
    userEventsCallback.onSuperVisorRetrieveEventsSuccess(supervisor.getEvents(filter));

    // forward getEvents() to Cache
    cacheEventsManager.getEvents(filter, new ConnectionEventsCallback(userEventsCallback, filter));
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
  public void getStreams(final StreamsCallback streamsCallback) {

    cacheStreamsManager.getStreams(new StreamsCallback() {

      @Override
      public void onStreamsSuccess(Map<String, Stream> streams) {
        supervisor.updateStreams(streams);
        streamsCallback.onStreamsSuccess(supervisor.getStreams());
      }

      @Override
      public void onStreamsPartialResult(Map<String, Stream> newStreams) {
        supervisor.updateStreams(newStreams);
        streamsCallback.onStreamsPartialResult(newStreams);
      }

      @Override
      public void onStreamsError(String message) {
        streamsCallback.onStreamsError(message);
      }
    });
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
   * EventsCallback used on EventsManager interface method calls
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
    public void onOnlieRetrieveEventsSuccess(Map<String, Event> onlineEvents) {
      logger.log("Connection: onEventsSuccess");

      // update existing references with JSON received from online
      supervisor.updateEvents(onlineEvents);

      // return merged events from Supervisor
      userEventsCallback.onOnlieRetrieveEventsSuccess(supervisor.getEvents(filter));
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
    public void onSuperVisorRetrieveEventsSuccess(Map<String, Event> supervisorEvents) {
    }

    @Override
    public void onEventsSuccess(String successMessage) {
      // TODO Auto-generated method stub

    }

    @Override
    public void onEventsError(String errorMessage) {
      // TODO Auto-generated method stub

    }
  }

}
