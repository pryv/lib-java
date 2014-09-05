package com.pryv.auth;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import com.pryv.api.model.Permission;
import com.pryv.utils.Logger;

/**
 *
 * High-level used to Authenticate the user. Upon success, provides a Connection
 * with the appropriate username and token.
 *
 * @author ik
 *
 */
public class AuthControllerImpl implements AuthController {

  private String requestingAppId;
  private List<Permission> permissions;
  private AuthView view;
  private AuthModel model;
  // optional
  private String language = "en";
  private String returnURL = "";

  private Logger logger = Logger.getInstance();

  /**
   *
   * @param pRequestingAppId
   *          Your app's identifier
   * @param pPermissions
   *          Array of permission request objects
   * @param pLang
   *          optional: The two-letter ISO (639-1) code of the language in which
   *          to display user instructions, if possible. Default: en.
   * @param pReturnURL
   *          optional: The URL to redirect the user to after auth completes
   * @param pView
   *          the view in which the URL for login is dislpayed
   */
  public AuthControllerImpl(String pRequestingAppId, List<Permission> pPermissions, String pLang,
    String pReturnURL, AuthView pView) {
    requestingAppId = pRequestingAppId;
    permissions = pPermissions;
    if (pLang != null) {
      language = pLang;
    }
    if (pReturnURL != null) {
      returnURL = pReturnURL;
    }
    view = pView;
  }

  @Override
  public void signIn() throws ClientProtocolException, IOException {
    model = new AuthModelImpl(this, requestingAppId, permissions, language, returnURL);
    model.startLogin();
  }

  @Override
  public void onSuccess(String username, String token) {
    view.onAuthSuccess(username, token);
    // acquire ref to new Connection, instanciated
  }

  @Override
  public void onFailure(int errorId, String jsonMessage, String detail) {
    logger.log("AuthController: failure: id=" + errorId + ", message=" + jsonMessage);
    view.onAuthFailure("AuthController: failure: id=" + errorId + ", message=" + jsonMessage);
  }

  @Override
  public void displayLoginView(String url) {
    view.displayLoginVew(url);
  }

}
