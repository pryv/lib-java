package com.pryv;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import org.w3c.dom.Document;

import com.pryv.api.EventsCallback;
import com.pryv.api.EventsManager;
import com.pryv.api.model.Event;
import com.pryv.api.model.Permission;
import com.pryv.auth.AuthController;
import com.pryv.auth.AuthControllerImpl;
import com.pryv.auth.AuthView;

public class JavaApp extends Application implements AuthView, EventsCallback<Map<String, Event>> {

  private final static String REQUESTING_APP_ID = "web-app-test";
  private EventsManager<Map<String, Event>> connection;

  private Stage stage;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage pStage) {
    Pryv.setStaging();
    stage = pStage;
    stage.setTitle("Java Example App");
    stage.setHeight(600);
    stage.setWidth(600);

    Permission testPermission = new Permission("*", Permission.Level.manage, null);
    List<Permission> permissions = new ArrayList<Permission>();
    permissions.add(testPermission);

    Scene scene = new Scene(new Group());
    Label text = new Label("Login in browser");
    text.setAlignment(Pos.CENTER);
    scene.setRoot(text);
    stage.setScene(scene);
    stage.show();

    AuthController authenticator =
      new AuthControllerImpl(REQUESTING_APP_ID, permissions, "en", "");
    authenticator.setView(this);
    authenticator.signIn();

  }

  public void displayLoginVew(String loginURL) {
    System.out.println("displaying custom view");
    final WebView webView = new WebView();
    final WebEngine webEngine = webView.getEngine();
    webEngine.setJavaScriptEnabled(true);
    webEngine.documentProperty().addListener(new ChangeListener<Document>() {
      public void changed(ObservableValue<? extends Document> prop, Document oldDoc,
        Document newDoc) {
        enableFirebug(webEngine);
      }
    });
    Scene scene = new Scene(new Group());
    scene.setRoot(webView);
    stage.setScene(scene);
    stage.show();
    webEngine.load(loginURL);
    webView
      .getEngine()
      .executeScript(
        "if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}");

  }

  /**
   * Enables Firebug Lite for debugging a webEngine.
   *
   * @param engine
   *          the webEngine for which debugging is to be enabled.
   */
  private static void enableFirebug(final WebEngine engine) {
    engine
      .executeScript("if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}");
  }

  public void onDisplaySuccess(Connection newConnection) {
    System.out.println("JavaApp: onSignInSuccess");
    Platform.runLater(new Runnable() {

      public void run() {
        Scene scene = new Scene(new Group());
        Label text = new Label("authentication complete");
        text.setAlignment(Pos.CENTER);
        scene.setRoot(text);
        stage.setScene(scene);
        stage.show();
      }
    });
    // controller = new AppControllerImpl(newConnection, this);

    connection = newConnection;
    connection.addEventsCallback(this);
    connection.get();
  }

  public void onDisplayFailure() {
    System.out.println("JavaApp: onDisplayFailure");
    Platform.runLater(new Runnable() {

      public void run() {
        Scene scene = new Scene(new Group());
        Label text = new Label("authentication failed");
        text.setAlignment(Pos.CENTER);
        scene.setRoot(text);
        stage.setScene(scene);
        stage.show();
      }
    });

  }

  // probably useless
  public void displayEvents(Map<String, Event> events) {

  }

  public void onSuccess(final Map<String, Event> events) {
    System.out.println("JavaApp: onSuccess()");
    Platform.runLater(new Runnable() {

      public void run() {
        Scene scene = new Scene(new Group());
        ListView<String> list = new ListView<String>();
        ObservableList<String> items = FXCollections.observableArrayList(events.keySet());
        list.setItems(items);
        scene.setRoot(list);
        stage.setScene(scene);
        stage.show();
      }
    });
  }

  public void onPartialResult(Map<String, Event> newEvents) {
    // TODO Auto-generated method stub

  }

  public void onError(String message) {
    // TODO Auto-generated method stub

  }

}