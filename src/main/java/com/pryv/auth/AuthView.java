package com.pryv.auth;

import com.pryv.Connection;

/**
 *
 * view displayed to enter credentials
 *
 * @author ik
 *
 */
public interface AuthView {

  void displayLoginVew(String loginURL);

  void onDisplaySuccess(Connection connection);

  void onDisplayFailure();
}
