package com.pryv;

import java.util.List;

import com.pryv.api.model.Permission;
import com.pryv.authorization.Login;
import com.pryv.authorization.LoginController;

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
  private String view;
  // optional
  private String language;
  private String returnURL;

  public Authenticator() {

  }

  public Authenticator(String reqAppId, List<Permission> pPermissions, String pView, String pLang,
      String pReturnURL) {
    requestingAppId = reqAppId;
    permissions = pPermissions;
    view = pView;
    language = pLang;
    returnURL = pReturnURL;
  }

  public void authenticate() {
    new Login(this, requestingAppId, permissions, view, language, returnURL);
    // open view
  }

  public void accepted() {
    state = State.ACCEPTED;

  }

  public void refused() {
    state = State.REFUSED;

  }

  public void error() {
    state = State.REFUSED;

  }

  public void inProgress() {
    state = State.POLLING;
    System.out.println("Authenticator in progress");
  }

  public State getState() {
    return state;
  }

}
