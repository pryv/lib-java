package com.pryv.functional;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.pryv.Pryv;
import com.pryv.api.model.Permission;
import com.pryv.auth.AuthControllerImpl;

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

  @Before
  public void setUp() throws Exception {
    permissions.add(testPermission1);
    permissions.add(testPermission2);
  }

  @Test
  public void testStartLoginAndPoll() {
    Pryv.setStaging();
    AuthControllerImpl authenticator = new AuthControllerImpl(reqAppId, permissions, lang, returnURL);
    authenticator.signIn();
  }

}
