package com.pryv.auth;

/**
 *
 * view displayed to enter credentials
 *
 * @author ik
 *
 */
public interface AuthView {

  void displayLoginVew(String loginURL);

  void closeLoginView();
}
