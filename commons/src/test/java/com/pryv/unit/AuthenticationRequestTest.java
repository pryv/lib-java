package com.pryv.unit;

import com.pryv.Pryv;
import com.pryv.auth.AuthenticationRequest;
import com.pryv.model.Permission;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 * Unit test for AuthenticationRequest object
 *
 * @author ik
 *
 */
public class AuthenticationRequestTest {

  private String serverURL = Pryv.REGISTRATION_URL;
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
  }

  @Test
  public void testCreateAuthenticationRequest() {
    permissions.add(testPermission1);
    permissions.add(testPermission2);
    AuthenticationRequest authReq =
        new AuthenticationRequest(reqAppId, permissions, lang, returnURL);
    assertEquals(reqAppId, authReq.getRequestingAppId());
    assertEquals(lang, authReq.getLanguageCode());
    assertEquals(returnURL, authReq.getReturnURL());
    assertEquals(serverURL, authReq.getServerURL());
    assertEquals(permissions, authReq.getRequestedPermissions());
  }

}
