package com.pryv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.apache.http.client.ClientProtocolException;
import org.controlsfx.dialog.Dialogs;

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
 * Example JavaFX application to demonstrate the way to use the Pryv Java
 * library.
 *
 * @author ik
 *
 */
public class ExampleApp extends Application implements AuthView, EventsCallback, StreamsCallback {

  private AppController controller;
  private Logger logger = Logger.getInstance();

  private final static String REQUESTING_APP_ID = "pryv-lib-java-example";

  private Connection connection;
  private EventsManager eventsManager;
  private StreamsManager streamsManager;
  private Collection<Stream> streams;

  private Stage primaryStage;
  private BorderPane rootLayout;

  private ObservableList<Event> eventsObservableList = FXCollections.observableArrayList();
  private ObservableList<TreeItem<Stream>> streamTreeItemsObservableList = FXCollections
    .observableArrayList();

  public static void main(String[] args) {
    Pryv.setStaging();
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {
    this.primaryStage = primaryStage;
    this.primaryStage.setTitle("Example App");

    try {
      FXMLLoader loader = new FXMLLoader(ExampleApp.class.getResource("view/RootLayout.fxml"));
      rootLayout = (BorderPane) loader.load();
      Scene scene = new Scene(rootLayout);
      primaryStage.setScene(scene);
      primaryStage.show();
    } catch (IOException e) {
      displayError(e.getMessage());
      e.printStackTrace();
    }

    // authentication
    showAuthView();

    Permission testPermission = new Permission("*", Permission.Level.manage, null);
    List<Permission> permissions = new ArrayList<Permission>();
    permissions.add(testPermission);

    AuthController authenticator =
      new AuthControllerImpl(REQUESTING_APP_ID, permissions, "en", "", this);
    try {
      authenticator.signIn();
    } catch (ClientProtocolException e) {
      displayError(e.getMessage());
      e.printStackTrace();
    } catch (IOException e) {
      displayError(e.getMessage());
      e.printStackTrace();
    }

  }

  /*
   * Authentication
   */

  /*
   * Displays simple view during auth phase.
   */
  public void showAuthView() {

    try {
      FXMLLoader loader =
        new FXMLLoader(ExampleApp.class.getResource("view/AuthenticationView.fxml"));
      BorderPane overviewPage = (BorderPane) loader.load();
      rootLayout.setCenter(overviewPage);
    } catch (IOException e) {
      displayError(e.getMessage());
      e.printStackTrace();
    }
  }

  /*
   * load auth in browser
   */
  public void displayLoginVew(String loginURL) {
    new AuthBrowserView().displayLoginVew(loginURL);
  }

  /*
   * auth success, start main view, load all Streams and 20 random Events
   */
  public void onAuthSuccess(String username, String token) {
    logger.log("JavaApp: onSignInSuccess");

    Platform.runLater(new Runnable() {
      public void run() {
        showMainView();
      }
    });
    connection = new Connection(username, token, new DBinitCallback() {
      public void onError(String message) {
        displayError(message);
      }
    });

    streams = new HashSet<Stream>();
    eventsManager = connection;
    streamsManager = connection;
    streamsManager.getStreams(null, this);
    Filter filter = new Filter();
    filter.setLimit(20);
    eventsManager.getEvents(filter, this);
  }

  /*
   * auth failure
   */
  public void onAuthFailure(String msg) {
    logger.log("JavaApp: onDisplayFailure");
    displayError("auth failure: " + msg);
  }

  /*
   * Main components of Example App
   */

  /**
   * Shows the main View, sets the controller.
   */
  private void showMainView() {
    try {
      // Load the fxml file and set into the center of the main layout
      FXMLLoader loader = new FXMLLoader(ExampleApp.class.getResource("view/MainView.fxml"));
      AnchorPane overviewPage = (AnchorPane) loader.load();
      rootLayout.setCenter(overviewPage);

      // Give the controller access to the main app
      controller = loader.getController();
      controller.setMainApp(this);

    } catch (IOException e) {
      // Exception gets thrown if the fxml file could not be loaded
      e.printStackTrace();
    }
  }

  /**
   * show Stream form window
   *
   * @param action
   *          "Edit" or "Create"
   * @param streamToUpdate
   *          the stream to update
   */
  public void showStreamForm(FormController.Mode action, Stream streamToUpdate) {
    try {
      // Load the fxml file and set into the center of the main layout
      FXMLLoader loader =
        new FXMLLoader(ExampleApp.class.getResource("view/StreamFormWindow.fxml"));
      AnchorPane page = (AnchorPane) loader.load();
      // rootLayout.setCenter(overviewPage);

      Stage dialogStage = new Stage();
      dialogStage.initModality(Modality.WINDOW_MODAL);
      dialogStage.initOwner(primaryStage);
      Scene scene = new Scene(page);
      dialogStage.setScene(scene);

      // Give the controller access to the main app
      FormController controller = loader.getController();
      controller.setMainApp(this);
      controller.setMode(action);
      controller.setStream(streamToUpdate);
      controller.loadParentStreams(streams);

      // Show the dialog and wait until the user closes it
      dialogStage.showAndWait();

    } catch (IOException e) {
      // Exception gets thrown if the fxml file could not be loaded
      e.printStackTrace();
    }
  }

  /**
   * show Event form window
   *
   * @param action
   *          "Edit" or "Create"
   * @param eventToUpdate
   *          the Event to update
   */
  public void showEventForm(FormController.Mode action, Event eventToUpdate) {
    try {
      // Load the fxml file and set into the center of the main layout
      FXMLLoader loader =
 new FXMLLoader(ExampleApp.class.getResource("view/EventFormWindow.fxml"));
      AnchorPane page = (AnchorPane) loader.load();

      Stage dialogStage = new Stage();
      dialogStage.initModality(Modality.WINDOW_MODAL);
      dialogStage.initOwner(primaryStage);
      Scene scene = new Scene(page);
      dialogStage.setScene(scene);

      // Give the controller access to the main app
      FormController controller = loader.getController();
      controller.setMainApp(this);
      controller.setMode(action);
      controller.setEvent(eventToUpdate);

      // Show the dialog and wait until the user closes it
      dialogStage.showAndWait();

    } catch (IOException e) {
      // Exception gets thrown if the fxml file could not be loaded
      e.printStackTrace();
    }
  }

  public void showDeleteStreamDialog(Stream streamToDelete) {
    FXMLLoader loader =
      new FXMLLoader(ExampleApp.class.getResource("view/DeleteStreamDialog.fxml"));
    AnchorPane page;
    try {
      page = (AnchorPane) loader.load();
      Stage dialogStage = new Stage();
      dialogStage.initModality(Modality.WINDOW_MODAL);
      dialogStage.initOwner(primaryStage);
      Scene scene = new Scene(page);
      dialogStage.setScene(scene);

      // Give the controller access to the main app
      DeleteDialogController controller = loader.getController();
      controller.setMainApp(this);
      controller.setStream(streamToDelete);

      dialogStage.showAndWait();
    } catch (IOException e) {
      displayError(e.getMessage());
      e.printStackTrace();
    }

//    An
  }

  /*
   * Streams management
   */

  public void createStream(Stream newStream) {
    streamsManager.createStream(newStream, this);
  }

  public void updateStream(Stream streamToUpdate) {
    streamsManager.updateStream(streamToUpdate, this);
  }

  public void deleteStream(Stream streamToDelete, boolean mergeEventsWithParent) {
    streamsManager.deleteStream(streamToDelete, mergeEventsWithParent, this);
  }

  /*
   * Streams Callbacks
   */

  public void onStreamsRetrievalSuccess(Map<String, Stream> streams, Double serverTime) {
    logger.log("JavaApp: received " + streams.size() + " streams.");
    this.streams = streams.values();
    addStreamsToTree(streams);
  }

  private void addStreamsToTree(final Map<String, Stream> newStreams) {
    Platform.runLater(new Runnable() {

      public void run() {
        TreeItem<Stream> root = new TreeItem<Stream>();
        root.setExpanded(true);
        buildStreamTree(root, newStreams.values());
        streamTreeItemsObservableList.add(root);
        controller.showStreams(root);
      }
    });
  }

  private void buildStreamTree(TreeItem<Stream> root, Collection<Stream> streams) {
    for (Stream stream : streams) {

      TreeItem<Stream> streamTreeItem = new TreeItem<Stream>(stream);
      root.getChildren().add(streamTreeItem);
      if (stream.getChildrenMap() != null) {
        buildStreamTree(streamTreeItem, stream.getChildrenMap().values());
      }
    }
  }

  public void onStreamsRetrievalError(String message, Double pServerTime) {
    displayError(message);
  }

  public void onStreamsSuccess(String successMessage, Stream stream, Double pServerTime) {
  }

  public void onStreamError(String errorMessage, Double pServerTime) {
    displayError(errorMessage);
  }

  /*
   * Events management
   */

  /**
   * Fetch Events for a specific stream id
   *
   * @param streamId
   */
  public void getEventsForStreamId(String streamId) {
    Filter filter = new Filter();
    filter.addStreamId(streamId);
    eventsManager.getEvents(filter, this);
  }

  public void createEvent(Event newEvent) {
    eventsManager.createEvent(newEvent, this);
  }

  public void updateEvent(Event eventToUpdate) {
    eventsManager.updateEvent(eventToUpdate, this);
  }

  /*
   * Events Callbacks
   */

  public void onEventsRetrievalSuccess(final Map<String, Event> newEvents, Double serverTime) {
    logger.log("JavaApp: retrieved " + newEvents.size() + " events.");
    addEventsToList(newEvents);
  }

  public void onEventsSuccess(String successMessage, Event event, Integer stoppedId,
    Double pServerTime) {

  }


  public void onEventsError(String errorMessage, Double pServerTime) {
    displayError(errorMessage);
  }

  public void onEventsRetrievalError(String message, Double pServerTime) {
    displayError(message);
  }

  /**
   * Add Events to ObservableList<Event> eventsObservableList
   *
   * @param newEvents
   */
  private void addEventsToList(final Map<String, Event> newEvents) {
    logger.log("ExampleApp: adding " + newEvents.values().size() + " events to List.");
    Platform.runLater(new Runnable() {
      public void run() {
        eventsObservableList.removeAll(eventsObservableList);
        controller.getEventsListView().setItems(null);
        eventsObservableList.addAll(newEvents.values());
        controller.getEventsListView().setItems(eventsObservableList);
        controller.getEventsListView().setItems(null);
        controller.getEventsListView().setItems(eventsObservableList);
      }
    });
  }

  /**
   * Returns the ObservableList<Event>
   *
   * @return
   */
  public ObservableList<Event> getEventsList() {
    return eventsObservableList;
  }

  /**
   * displays error message in custom Alert Dialog.
   *
   * @param message
   *          the message to display
   */
  public void displayError(final String message) {
    Platform.runLater(new Runnable() {

      public void run() {
        Dialogs.create().owner(getPrimaryStage()).title("error").message(message).showInformation();
      }
    });
  }

  /**
   * Returns the main stage.
   *
   * @return
   */
  public Stage getPrimaryStage() {
    return primaryStage;
  }

}
