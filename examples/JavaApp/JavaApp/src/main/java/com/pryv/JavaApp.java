package com.pryv;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import org.w3c.dom.Document;

import com.pryv.api.model.Permission;
import com.pryv.auth.AuthController;
import com.pryv.auth.AuthControllerImpl;
import com.pryv.auth.AuthView;

public class JavaApp extends Application implements AuthView {

  private final static String REQUESTING_APP_ID = "web-app-test";
  private Connection connection;

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

    Permission testPermission =
      new Permission("picsStream", Permission.Level.manage, "defaultPicsStream");
    List<Permission> permissions = new ArrayList<Permission>();
    permissions.add(testPermission);

    Scene scene = new Scene(new Group());
    TextField text = new TextField("Login in browser");
    scene.setRoot(text);
    stage.setScene(scene);
    stage.show();

    AuthController authenticator =
      new AuthControllerImpl(REQUESTING_APP_ID, permissions, "en", "");
    authenticator.setView(this);
    authenticator.signIn();
  }

  public void onSignInSuccess() {
    System.out.println("JavaApp: onSignInSuccess");
    Scene scene = new Scene(new Group());
    TextField text = new TextField("authentication complete");
    scene.setRoot(text);
    stage.setScene(scene);
    stage.show();
  }

  public void displayLoginVew(String loginURL) {
    System.out.println("displaying custom view");
    final WebView webView = new WebView();
    // browser.
    final WebEngine webEngine = webView.getEngine();
    // browser.set
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

}