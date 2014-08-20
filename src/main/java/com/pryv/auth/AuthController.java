package com.pryv.auth;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

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
  void signIn() throws ClientProtocolException, IOException;

  /**
   * login successfull callback, returns username and auth token
   *
   * @param username
   * @param token
   */
  void onSuccess(String username, String token);

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
