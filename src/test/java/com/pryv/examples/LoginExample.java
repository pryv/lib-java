package com.pryv.examples;

import java.util.ArrayList;
import java.util.List;

import com.pryv.Pryv;
import com.pryv.api.model.Permission;
import com.pryv.auth.AuthController;
import com.pryv.auth.AuthControllerImpl;
import com.pryv.functional.FakeAuthView;

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
    String lang = "en";
    String returnURL = "fakeURL";

    List<Permission> permissions = new ArrayList<Permission>();
    String streamId1 = "pics";
    Permission.Level perm1 = Permission.Level.contribute;
    String defaultName1 = "ddd";
    Permission testPermission1 = new Permission(streamId1, perm1, defaultName1);
    permissions.add(testPermission1);

    Pryv.setStaging();
    AuthController authenticator =
      new AuthControllerImpl(reqAppId, permissions, lang, returnURL, new FakeAuthView());

    authenticator.signIn();
  }

}
