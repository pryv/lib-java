package com.pryv.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import resources.TestCredentials;

import com.pryv.Connection;
import com.pryv.Pryv;
import com.pryv.api.EventsCallback;
import com.pryv.api.EventsManager;
import com.pryv.api.Filter;
import com.pryv.api.database.DBinitCallback;
import com.pryv.api.model.Event;
import com.pryv.api.model.Permission;
import com.pryv.auth.AuthBrowserView;
import com.pryv.auth.AuthController;
import com.pryv.auth.AuthControllerImpl;
import com.pryv.auth.AuthView;

public class BasicExample implements AuthView, EventsCallback {

  private EventsManager eventsManager;
  private Map<String, Event> eventsMap;

  public static void main(String[] args) {

    AuthView exampleUser = new BasicExample();

    String reqAppId = "pryv-lib-java-example";
    String language = "en";
    String returnURL = "unused";

    List<Permission> permissions = new ArrayList<Permission>();
    String streamId1 = "pics";
    Permission.Level perm1 = Permission.Level.contribute;
    String defaultName1 = "default";
    Permission testPermission1 = new Permission(streamId1, perm1, defaultName1);
    permissions.add(testPermission1);

    Pryv.setStaging();
    AuthController authenticator =
      new AuthControllerImpl(reqAppId, permissions, language, returnURL, exampleUser);

    authenticator.signIn();
  }

  @Override
  public void displayLoginVew(String loginURL) {
    new AuthBrowserView().displayLoginVew(loginURL);
  }

  @Override
  public void onAuthSuccess(String username, String token) {
    Connection connection =
      new Connection(TestCredentials.USERNAME, TestCredentials.TOKEN, new DBinitCallback() {

        @Override
        public void onError(String message) {
          System.out.println(message);
        }
      });
    eventsManager = connection;
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
    this.eventsMap = events;
  }

  @Override
  public void onEventsRetrievalError(String errorMessage, Double serverTime) {
    System.out.println("Error in retrieving events");
  }

  @Override
  public void onEventsSuccess(String successMessage, Event event, Integer stoppedId,
    Double serverTime) {
  }

  @Override
  public void onEventsError(String errorMessage, Double serverTime) {
  }

}
