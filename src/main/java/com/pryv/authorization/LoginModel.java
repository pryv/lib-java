package com.pryv.authorization;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

/**
 *
 * MVC model for logic of login sequence
 *
 * @author ik
 *
 */
public interface LoginModel {

  void startLogin() throws ClientProtocolException, IOException;
}
