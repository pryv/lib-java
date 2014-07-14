package com.pryv.authorization;

import java.util.List;

import com.pryv.Connection;
import com.pryv.api.model.Permission;

/**
 *
 * High-level class used for login
 *
 * @author ik
 *
 */
public class Authenticator implements LoginController {

  /**
   *
   * represents authentication state
   *
   * @author ik
   *
   */
  public enum State {
    POLLING, ACCEPTED, REFUSED, CANCELLED
  };

  private State state;
  private String requestingAppId;
  private List<Permission> permissions;
  private LoginView view;
  private LoginModel model;
  // optional
  private String language;
  private String returnURL;

  public Authenticator() {

  }

  public Authenticator(String reqAppId, List<Permission> pPermissions, LoginView pView,
      String pLang, String pReturnURL) {
    requestingAppId = reqAppId;
    permissions = pPermissions;
    view = pView;
    language = pLang;
    returnURL = pReturnURL;
  }

  public void startLogin() {
    model = new Login(this, requestingAppId, permissions, language, returnURL);
    model.startLogin();
  }

  public void accepted(Connection newConnection) {
    state = State.ACCEPTED;
    // instanciate/acquire Connection (username/token)
  }

  public void refused(String jsonMessage) {
    state = State.REFUSED;
    // display state/error
  }

  public void error(String jsonMessage) {
    state = State.REFUSED;
    // display error/reason
  }

  public void inProgress() {
    state = State.POLLING;
    System.out.println("Authenticator in progress");
  }

  public State getState() {
    return state;
  }

  public void displayLoginView(String url) {
    view = new LoginWebView(this);
    view.displayLoginVew(url);
  }

}
