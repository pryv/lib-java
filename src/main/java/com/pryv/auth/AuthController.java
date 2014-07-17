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
   * begin login sequence
   */
  void signIn();

  /**
   * login successful
   */
  void accepted(Connection connection);

  /**
   * login refused - bad credentials/cancel
   */
  void refused(String jsonMessage);

  /**
   * error message
   *
   * @param detail
   * @param errorId
   */
  void error(int errorId, String jsonMessage, String detail);

  /**
   * polling
   */
  void inProgress();

  /**
   * display web view
   */
  void displayLoginView(String url);
}
