package com.pryv.auth;

import com.pryv.Connection;

/**
 *
 * callback used by poller after login successful
 *
 * @author ik
 *
 */
public interface AuthController {

  /**
   * set custom View, default is a browser tab.
   * @param view
   */
  void setView(AuthView view);

  /**
   * begin login sequence
   */
  void signIn();

  /**
   * login successful
   */
  void onSuccess(Connection connection);

  /**
   * error message
   *
   * @param detail
   * @param errorId
   */
  void onFailure(int errorId, String jsonMessage, String detail);

  /**
   * display web view
   */
  void displayLoginView(String url);

}
