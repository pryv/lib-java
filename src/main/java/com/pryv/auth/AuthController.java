package com.pryv.auth;

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
   * login successfull callback, returns username and auth token
   *
   * @param username
   * @param token
   */
  void onSuccess(String username, String token);

  /**
   * auth error
   *
   * @param message
   */
  void onError(String message);

  /**
   * auth refused
   *
   * @param reasonId
   * @param message
   * @param detail
   */
  void onRefused(int reasonId, String message, String detail);

  /**
   * display web view
   */
  void displayLoginView(String url);

}
