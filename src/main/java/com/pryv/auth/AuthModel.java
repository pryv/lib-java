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

  void startLogin() throws ClientProtocolException, IOException;
}
