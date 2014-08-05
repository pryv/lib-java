package com.pryv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import com.pryv.api.EventsCallback;
import com.pryv.api.EventsManager;
import com.pryv.api.Filter;
import com.pryv.api.StreamsCallback;
import com.pryv.api.StreamsManager;
import com.pryv.api.model.Event;
import com.pryv.api.model.Permission;
import com.pryv.api.model.Stream;
import com.pryv.auth.AuthBrowserView;
import com.pryv.auth.AuthController;
import com.pryv.auth.AuthControllerImpl;
import com.pryv.auth.AuthView;

public class ExampleApp extends Application implements AuthView,
  EventsCallback<Map<String, Event>>, StreamsCallback<Map<String, Stream>> {

  private final static String REQUESTING_APP_ID = "web-app-test";
  private EventsManager<Map<String, Event>> eventsManager;
  private StreamsManager streamsManager;

  private Stage primaryStage;
  private BorderPane rootLayout;
  private ObservableList<Stream> streamsList = FXCollections.observableArrayList();
  private ObservableList<Event> eventsList = FXCollections.observableArrayList();

  @Override
  public void start(Stage primaryStage) {
    this.primaryStage = primaryStage;
    this.primaryStage.setTitle("Example App");

    try {
      // Load the root layout from the fxml file
      FXMLLoader loader = new FXMLLoader(ExampleApp.class.getResource("view/RootLayout.fxml"));
      rootLayout = (BorderPane) loader.load();
      Scene scene = new Scene(rootLayout);
      primaryStage.setScene(scene);
      primaryStage.show();
    } catch (IOException e) {
      // Exception gets thrown if the fxml file could not be loaded
      e.printStackTrace();
    }

    showStreamsView();

    Permission testPermission = new Permission("*", Permission.Level.manage, null);
    List<Permission> permissions = new ArrayList<Permission>();
    permissions.add(testPermission);

    AuthController authenticator =
      new AuthControllerImpl(REQUESTING_APP_ID, permissions, "en", "");
    authenticator.setView(this);
    authenticator.signIn();

  }

  /**
   * Returns the main stage.
   *
   * @return
   */
  public Stage getPrimaryStage() {
    return primaryStage;
  }

  /**
   * Shows the person overview scene.
   */
  public void showStreamsView() {
    try {
      // Load the fxml file and set into the center of the main layout
      FXMLLoader loader = new FXMLLoader(ExampleApp.class.getResource("view/StreamsView.fxml"));
      AnchorPane overviewPage = (AnchorPane) loader.load();
      rootLayout.setCenter(overviewPage);

      // Give the controller access to the main app
      AppController controller = loader.getController();
      controller.setMainApp(this);

    } catch (IOException e) {
      // Exception gets thrown if the fxml file could not be loaded
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    launch(args);
  }

  public void onStreamsSuccess(Map<String, Stream> streams) {
    System.out.println("JavaApp: onSuccess()");
    for (Stream stream : streams.values()) {
      System.out.println("ExampleApp - id: " + stream.getId());
      streamsList.add(stream);
    }

  }

  public void onStreamsPartialResult(Map<String, Stream> newStreams) {
    // TODO Auto-generated method stub

  }

  public void onStreamsError(String message) {
    // TODO Auto-generated method stub

  }

  public void onEventsSuccess(final Map<String, Event> events) {
    Platform.runLater(new Runnable() {

      public void run() {
        for (Event event : events.values()) {
          eventsList.add(event);
        }
      }
    });

  }

  public void onEventsPartialResult(Map<String, Event> newEvents) {
    // TODO Auto-generated method stub

  }

  public void onEventsError(String message) {
    // TODO Auto-generated method stub

  }

  /**
   * load auth in browser
   */
  public void displayLoginVew(String loginURL) {
    new AuthBrowserView().displayLoginVew(loginURL);
  }

  /**
   * auth success
   */
  public void onDisplaySuccess(Connection newConnection) {
    System.out.println("JavaApp: onSignInSuccess");
    // Platform.runLater(new Runnable() {
    //
    // public void run() {
    // Scene scene = new Scene(new Group());
    // Label text = new Label("authentication complete");
    // text.setAlignment(Pos.CENTER);
    // scene.setRoot(text);
    // stage.setScene(scene);
    // stage.show();
    // }
    // });
    eventsManager = newConnection;
    streamsManager = newConnection;
    eventsManager.addEventsCallback(this);
    newConnection.addStreamsCallback(this);
    // connection.getEvents();
    streamsManager.getStreams();
  }

  /**
   * auth failure
   */
  public void onDisplayFailure() {
    System.out.println("JavaApp: onDisplayFailure");
    // Platform.runLater(new Runnable() {
    //
    // public void run() {
    // Scene scene = new Scene(new Group());
    // Label text = new Label("authentication failed");
    // text.setAlignment(Pos.CENTER);
    // scene.setRoot(text);
    // stage.setScene(scene);
    // stage.show();
    // }
    // });

  }

  public ObservableList<Stream> getStreamsData() {
    return streamsList;
  }

  public ObservableList<Event> getEventsList() {
    return eventsList;
  }

  public void getEvents(String streamId) {
    eventsList.clear();
    Map<String, String> streams = new HashMap<String, String>();
    streams.put(Filter.STREAMS_KEY, streamId);
    eventsManager.getEvents(streams);

  }

}
