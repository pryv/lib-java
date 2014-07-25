package com.pryv.auth;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

/**
 *
 * MVC model for logic of login sequence
 *
 * @author ik
 *
 */
public interface AuthModel {

  /**
   * start login protocol
   *
   * @throws ClientProtocolException
   * @throws IOException
   */
  void startLogin() throws ClientProtocolException, IOException;
}
