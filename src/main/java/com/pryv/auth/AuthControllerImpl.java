package com.pryv.auth;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import com.pryv.Connection;
import com.pryv.api.model.Permission;

/**
 *
 * High-level used to Authenticate the user. Upon success, provides a Connection
 * with the appropriate username and token.
 *
 * @author ik
 *
 */
public class AuthControllerImpl implements AuthController {

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
  private AuthView view;
  private AuthModel model;
  // optional
  private String language;
  private String returnURL;

  public AuthControllerImpl() {

  }

  public AuthControllerImpl(String pRequestingAppId, List<Permission> pPermissions, String pLang,
    String pReturnURL) {
    requestingAppId = pRequestingAppId;
    permissions = pPermissions;
    language = pLang;
    returnURL = pReturnURL;
  }

  public void signIn() {
    model = new AuthModelImpl(this, requestingAppId, permissions, language, returnURL);
    try {
      model.startLogin();
    } catch (ClientProtocolException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void accepted(Connection newConnection) {
    state = State.ACCEPTED;
    // acquire ref to new Connection, instanciated
  }

  public void refused(String jsonMessage) {
    state = State.REFUSED;
    // display state/error
  }

  public void error(int errorId, String jsonMessage, String detail) {
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
    view = new AuthBrowserView(this);
    view.displayLoginVew(url);
  }

}
