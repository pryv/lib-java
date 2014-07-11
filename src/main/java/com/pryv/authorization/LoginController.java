package com.pryv.authorization;

/**
 *
 * callback used by poller after login successful
 *
 * @author ik
 *
 */
public interface LoginController {

  /**
   * login successful
   */
  void accepted();

  /**
   * login refused - bad credentials/cancel
   */
  void refused();

  /**
   * error message
   */
  void error();

  /**
   * polling
   */
  void inProgress();
}
