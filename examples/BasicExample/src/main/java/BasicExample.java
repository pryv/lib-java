import com.pryv.Connection;
import com.pryv.Filter;
import com.pryv.api.EventsCallback;
import com.pryv.auth.AuthBrowserView;
import com.pryv.auth.AuthController;
import com.pryv.auth.AuthView;
import com.pryv.database.DBinitCallback;
import com.pryv.interfaces.StreamsCallback;
import com.pryv.model.Event;
import com.pryv.model.Permission;
import com.pryv.model.Stream;
import com.pryv.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This example lets the user sign in, then retrieves the access information
 * (type and permissions), the available streams structure and the last 20
 * events.
 *
 * @author ik
 *
 */
public class BasicExample implements AuthView, EventsCallback, StreamsCallback {

  private List<Event> events;
  private Map<String, Stream> streams;

  public static void main(String[] args) {

    // turn off logging
    Logger logger = Logger.getInstance();
    logger.turnOff();

    AuthView exampleUser = new BasicExample();

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
    printExampleMessage(loginURL);
    new AuthBrowserView().displayLoginView(loginURL);
  }

  @Override
  public void onAuthSuccess(String userID, String accessToken) {

    // instanciate ConnectionOld object - used to access Streams and Events data
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

    // Retrieve data
    // the retrieval produces 3 callback executions: a synchronous callback from
    // the Supervisors (volatile memory), an asynchronous callback from the
    // cache database and an asynchronous callback from the online API.

    // Retrieve the Streams structure
    streamsManager.get(null, this);

    // Retrieve 20 Events
    Filter eventsFilter = new Filter();
    eventsFilter.setLimit(20);
    eventsManager.get(eventsFilter, this);
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
    System.out.println(events.size() + " Event(s) retrieved:");
    for (Event event : events.values()) {
      System.out.println(event);
    }
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
    System.out.println(streams.size() + " Stream(s) retrieved:");
    for (Stream stream : streams.values()) {
      System.out.println(stream);
    }
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

  /**
   * print informative message
   *
   * @param loginUrl
   */
  private static void printExampleMessage(String loginUrl) {
    System.out.println("#########################################################");
    System.out.println("##                  Basic Example started              ##");
    System.out.println("#########################################################\n");
    System.out.println("Sign in the open web page or copy this link in your browser:\n");
    System.out.println(loginUrl + "\n");
    System.out
      .println("Use your own staging account credentials or user \'perkikiki\' and password \'poilonez\'\n");
  }

}
