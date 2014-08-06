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
    String pReturnURL, AuthView pView) {
    requestingAppId = pRequestingAppId;
    permissions = pPermissions;
    language = pLang;
    returnURL = pReturnURL;
    view = pView;
  }

  @Override
  public void setView(AuthView pView) {
    view = pView;
  }

  @Override
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

  @Override
  public void onSuccess(Connection newConnection) {
    state = State.ACCEPTED;
    view.onDisplaySuccess(newConnection);
    // acquire ref to new Connection, instanciated
  }

  @Override
  public void onFailure(int errorId, String jsonMessage, String detail) {
    state = State.REFUSED;
    System.out.println("failure: id=" + errorId + ", message=" + jsonMessage);
    view.onDisplayFailure();
    // display error/reason
  }

  public State getState() {
    return state;
  }

  @Override
  public void displayLoginView(String url) {
    view.displayLoginVew(url);
  }

}
