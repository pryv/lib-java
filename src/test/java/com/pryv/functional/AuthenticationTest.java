package com.pryv.functional;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.http.client.ClientProtocolException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jayway.awaitility.Awaitility;
import com.pryv.Pryv;
import com.pryv.api.model.Permission;
import com.pryv.auth.AuthController;
import com.pryv.auth.AuthControllerImpl;
import com.pryv.auth.AuthView;

/**
 *
 * Test authentication behavior.
 *
 * @author ik
 *
 */
public class AuthenticationTest {

  private String reqAppId = "web-page-test";
  private List<Permission> permissions = new ArrayList<Permission>();
  private String streamId1 = "pics";
  private Permission.Level perm1 = Permission.Level.contribute;
  private String defaultName1 = "ddd";
  private Permission testPermission1 = new Permission(streamId1, perm1, defaultName1);
  private String streamId2 = "vids";
  private Permission.Level perm2 = Permission.Level.read;
  private String defaultName2 = "eee";
  private Permission testPermission2 = new Permission(streamId2, perm2, defaultName2);
  private String lang = "en";
  private String returnURL = "fakeURL";

  private FakeAuthView fakeAuthView;

  private Boolean displayLoginViewExecuted = false;
  private Boolean displaySuccessExecuted = false;
  private Boolean displayFailureExecuted = false;

  @BeforeClass
  public static void beforeAllTests() {
    Pryv.setStaging();
  }

  @Before
  public void setUp() throws Exception {
    permissions.add(testPermission1);
    permissions.add(testPermission2);
  }

  @Test
  public void testStartLoginAndPoll() {
    AuthController authenticator =
      new AuthControllerImpl(reqAppId, permissions, lang, returnURL, new FakeAuthView());
    try {
      authenticator.signIn();
    } catch (ClientProtocolException e) {
      fail("fail login");
      e.printStackTrace();
    } catch (IOException e) {
      fail("fail login");
      e.printStackTrace();
    }
    Awaitility.await().until(hasDisplayedLoginView());
    assertTrue(displayLoginViewExecuted);
  }

  @Test
  public void testStartLoginWithBadPermissions() {
    AuthController authenticator =
      new AuthControllerImpl(null, null, null, null, new FakeAuthView());
    try {
      authenticator.signIn();
      Awaitility.await().until(hasDisplayedFailure());
      assertTrue(displayFailureExecuted);
    } catch (ClientProtocolException e) {
      fail("fail login with bad arguments");
      e.printStackTrace();
    } catch (IOException e) {
      fail("fail login with bad arguments");
      e.printStackTrace();
    }

  }

  private Callable<Boolean> hasDisplayedLoginView() {
    return new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        return displayLoginViewExecuted;
      }
    };
  }

  private Callable<Boolean> hasDisplaySuccess() {
    return new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        return displaySuccessExecuted;
      }
    };
  }

  private Callable<Boolean> hasDisplayedFailure() {
    return new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        return displayFailureExecuted;
      }
    };
  }


  /**
   * authentication view used for testing
   *
   * @author ik
   *
   */
  private class FakeAuthView implements AuthView {



    @Override
    public void displayLoginVew(String loginURL) {
      displayLoginViewExecuted = true;
    }

    @Override
    public void onDisplaySuccess(String username, String token) {
      displaySuccessExecuted = true;
    }

    @Override
    public void onDisplayFailure(String msg) {
      displayFailureExecuted = true;
    }

  }

}
