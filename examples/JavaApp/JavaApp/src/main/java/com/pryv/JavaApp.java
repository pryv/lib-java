package com.pryv;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

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
    connection = newConnection;
    connection.addEventsCallback(this);
    connection.getEvents();
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

  public void displayLoginVew(String loginURL) {
    // TODO Auto-generated method stub

  }

}