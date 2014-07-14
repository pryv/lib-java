package com.pryv.examples;

import java.util.ArrayList;
import java.util.List;

import com.pryv.Pryv;
import com.pryv.api.model.Permission;
import com.pryv.authorization.Authenticator;
import com.pryv.authorization.LoginController;
import com.pryv.authorization.LoginView;

/**
 *
 * example code displaying how to use the library to authorize the app
 *
 * @author ik
 *
 */
public class LoginExample {

  public static void main(String[] args) {

    String reqAppId = "web-page-test";
    List<Permission> permissions = new ArrayList<Permission>();
    String lang = "en";
    String returnURL = "fakeURL";
    LoginView view = null;

    Pryv.setStaging();
    LoginController authenticator =
        new Authenticator(reqAppId, permissions, view, lang, returnURL);
    authenticator.startLogin();
  }

}
