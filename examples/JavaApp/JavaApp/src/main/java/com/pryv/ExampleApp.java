package com.pryv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
  private EventsManager eventsManager;
  private StreamsManager streamsManager;

  private Stage primaryStage;
  private BorderPane rootLayout;

  private ObservableList<Event> eventsObservableList = FXCollections.observableArrayList();
  private ObservableList<TreeItem<Stream>> streamTreeItemsObservableList = FXCollections
    .observableArrayList();

  public static void main(String[] args) {
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
  public void onDisplaySuccess(String username, String token) {
    logger.log("JavaApp: onSignInSuccess");

    Platform.runLater(new Runnable() {
      public void run() {
        showMainView();
      }
    });

    Connection newConnection = new Connection(username, token, new DBinitCallback() {

      public void onError(String message) {
        displayError(message);
      }
    });
    eventsManager = newConnection;
    streamsManager = newConnection;
    streamsManager.getStreams(null, this);
    Filter filter = new Filter();
    filter.setLimit(20);
    eventsManager.getEvents(filter, this);
  }

  /*
   * auth failure
   */
  public void onDisplayFailure(String msg) {
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
   */
  public void showStreamForm(FormController.Mode action) {
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
   */
  public void showEventForm(FormController.Mode action) {
    try {
      // Load the fxml file and set into the center of the main layout
      FXMLLoader loader =
 new FXMLLoader(ExampleApp.class.getResource("view/EventFormWindow.fxml"));
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

      // Show the dialog and wait until the user closes it
      dialogStage.showAndWait();

    } catch (IOException e) {
      // Exception gets thrown if the fxml file could not be loaded
      e.printStackTrace();
    }
  }

  /*
   * Streams Callbacks
   */

  public void onOnlineRetrieveStreamsSuccess(final Map<String, Stream> streams) {
    logger.log("JavaApp: onSuccess()");

    Platform.runLater(new Runnable() {

      public void run() {
        TreeItem<Stream> root = new TreeItem<Stream>();
        root.setExpanded(true);
        buildStreamTree(root, streams.values());

        // for (Stream stream : streams.values()) {
        //
        // TreeItem<Stream> streamTreeItem = new TreeItem<Stream>(stream);
        // if (stream.getChildren() != null) {
        // for (Stream childStream : stream.getChildren()) {
        // streamTreeItem.getChildren().add(new TreeItem<Stream>(childStream));
        // }
        // }
        // root.getChildren().add(streamTreeItem);
        // }
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

  public void onCacheRetrieveStreamSuccess(Map<String, Stream> newStreams) {
    // TODO Auto-generated method stub

  }

  public void onSupervisorRetrieveStreamsSuccess(Map<String, Stream> supervisorStreams) {
    // TODO Auto-generated method stub

  }

  public void onStreamsRetrievalError(String message) {
    displayError(message);
  }

  public void onStreamsSuccess(String successMessage) {
    // TODO Auto-generated method stub

  }

  public void onStreamError(String errorMessage) {
    displayError(errorMessage);
  }

  /**
   * Fetch Events for a specific streamId
   *
   * @param streamId
   */
  public void getEventsForStreamId(String streamId) {
    Set<String> streamIds = new HashSet<String>();
    streamIds.add(streamId);
    Filter filter = new Filter();
    filter.setStreamIds(streamIds);
    eventsManager.getEvents(filter, this);
  }

  /*
   * Events Callbacks
   */

  public void onOnlineRetrieveEventsSuccess(final Map<String, Event> newEvents) {
    addEventsToList(newEvents);
  }

  public void onCacheRetrieveEventsSuccess(Map<String, Event> newEvents) {
    addEventsToList(newEvents);
  }

  public void onSupervisorRetrieveEventsSuccess(Map<String, Event> supervisorEvents) {
    addEventsToList(supervisorEvents);
  }

  public void onEventsSuccess(String successMessage) {
    // TODO Auto-generated method stub

  }

  public void onEventsError(String errorMessage) {
    displayError(errorMessage);
  }

  public void onEventsRetrievalError(String message) {
    displayError(message);
  }

  /**
   * Add Events to ObservableList<Event> eventsObservableList
   *
   * @param newEvents
   */
  private void addEventsToList(final Map<String, Event> newEvents) {
    logger.log("ExampleApp: adding events to List: " + newEvents.values().size());
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
