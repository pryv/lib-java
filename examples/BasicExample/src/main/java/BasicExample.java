

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.pryv.Connection;
import com.pryv.Pryv;
import com.pryv.api.EventsCallback;
import com.pryv.api.EventsManager;
import com.pryv.api.Filter;
import com.pryv.api.StreamsCallback;
import com.pryv.api.StreamsManager;
import com.pryv.api.database.DBinitCallback;
import com.pryv.api.model.Event;
import com.pryv.api.model.Permission;
import com.pryv.api.model.Stream;
import com.pryv.auth.AuthBrowserView;
import com.pryv.auth.AuthController;
import com.pryv.auth.AuthControllerImpl;
import com.pryv.auth.AuthView;
import com.pryv.utils.Logger;

/**
 * This example lets the user sign in, then retrieves the access information
 * (type and permissions), the available streams structure and the last 20
 * events.
 *
 * @author ik
 *
 */
public class BasicExample implements AuthView, EventsCallback, StreamsCallback {

  private EventsManager eventsManager;
  private StreamsManager streamsManager;
  private Map<String, Event> events;
  private Map<String, Stream> streams;

  public static void main(String[] args) {

    System.out
      .println("Use your own staging account credentials or user \'perkikiki\' and password \'poilonez\'");

    // turn off logging
    Logger logger = Logger.getInstance();
    logger.turnOff();

    AuthView exampleUser = new BasicExample();

    // Preliminary step: use staging environment (remove for use on production
    // infrastructure)
    Pryv.setStaging();

    // Authenticate user

    // Application settings
    String reqAppId = "pryv-lib-java-example";
    String language = "en";
    String returnURL = "unused";

    // Permissions settings
    List<Permission> permissions = new ArrayList<Permission>();
    Permission diaryPermission = new Permission("diary", Permission.Level.read, "Journal");
    Permission positionPermission = new Permission("position", Permission.Level.read, "Position");
    permissions.add(diaryPermission);
    permissions.add(positionPermission);

    AuthController authenticator =
      new AuthControllerImpl(reqAppId, permissions, language, returnURL, exampleUser);

    // start authentication. On successful authentication, the Streams structure
    // as well as 20 Events will be retrieved from the API (defined on the
    // onAuthSuccess() method).
    authenticator.signIn();

  }

  @Override
  public void displayLoginVew(String loginURL) {
    new AuthBrowserView().displayLoginVew(loginURL);
  }

  @Override
  public void onAuthSuccess(String userID, String accessToken) {

    // instanciate Connection object - used to access Streams and Events data
    // (through EventsManager and StreamsManager interfaces)
    Connection connection = new Connection(userID, accessToken, new DBinitCallback() {
      @Override
      public void onError(String message) {
        System.out.println(message);
      }
    });

    // assign connection to interfaces
    eventsManager = connection;
    streamsManager = connection;

    // Retrieve the Streams structure
    streamsManager.getStreams(new Filter(), this);

    // Retrieve 20 Events
    eventsManager.getEvents(new Filter(), this);
  }

  @Override
  public void onAuthError(String message) {
    System.out.println("Auth error");
  }

  @Override
  public void onAuthRefused(int reasonId, String message, String detail) {
    System.out.println("Auth refused");
  }

  @Override
  public void onEventsRetrievalSuccess(Map<String, Event> events, Double serverTime) {
    System.out.println("Events retrieved");
    this.events = events;
  }

  @Override
  public void onEventsRetrievalError(String errorMessage, Double serverTime) {
    System.out.println("Error in retrieving events");
  }

  @Override
  public void onEventsSuccess(String successMessage, Event event, Integer stoppedId,
    Double serverTime) {
    // unused in this example
  }

  @Override
  public void onEventsError(String errorMessage, Double serverTime) {
    // unused in this example
  }

  @Override
  public void onStreamsRetrievalSuccess(Map<String, Stream> streams, Double serverTime) {
    System.out.println("Streams retrieved");
    this.streams = streams;
  }

  @Override
  public void onStreamsRetrievalError(String errorMessage, Double serverTime) {
    System.out.println("Error when retrieving Streams.");
  }

  @Override
  public void onStreamsSuccess(String successMessage, Stream stream, Double serverTime) {
    // unused in this example
  }

  @Override
  public void onStreamError(String errorMessage, Double serverTime) {
    // unused in this example
  }

}
