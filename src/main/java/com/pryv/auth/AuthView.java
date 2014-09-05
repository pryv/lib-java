package com.pryv.auth;


/**
 *
 * view displayed to enter credentials
 *
 * @author ik
 *
 */
public interface AuthView {

  /**
   * displays the external web page where the user inputs his username and
   * password
   *
   * @param loginURL
   */
  void displayLoginVew(String loginURL);

  /**
   * callback method when auth is successful. Displays success message.
   *
   * @param username
   * @param token
   */
  void onAuthSuccess(String username, String token);

  /**
   * callback method when auth failed.
   *
   * @param message
   */
  void onAuthFailure(String message);
}
