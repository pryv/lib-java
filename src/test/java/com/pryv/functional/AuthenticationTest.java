package com.pryv.functional;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.pryv.Authenticator;
import com.pryv.Authenticator.State;
import com.pryv.Pryv;
import com.pryv.api.model.Permission;
import com.pryv.api.model.Permissions;

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
  private String perm1 = Permissions.CONTRIBUTE.toString();
  private String defaultName1 = "ddd";
  private Permission testPermission1 = new Permission(streamId1, perm1, defaultName1);
  private String streamId2 = "vids";
  private String perm2 = Permissions.READ.toString();
  private String defaultName2 = "eee";
  private Permission testPermission2 = new Permission(streamId2, perm2, defaultName2);
  private String lang = "en";
  private String returnURL = "fakeURL";

  // private final String code = "code";
  // private final String pollField = "poll";
  // private final String pollRateField = "poll_rate_ms";
  // private final int okCode = 201;
  // private String key;
  // private String authResponse;
  // private JsonObject jsonAuthResponse;
  // private String pollURL;
  // private long pollRate;
  private String view = null;

  // private Gson gson = new Gson();
  // private AuthenticationRequest correctAuthRequest;

  @Before
  public void setUp() throws Exception {
    permissions.add(testPermission1);
    permissions.add(testPermission2);
    // correctAuthRequest = new AuthenticationRequest(serverURL, permissions,
    // lang, returnURL);
  }

  @Test
  public void testSendCorrectMessage() {

    // System.out.println("request: " + gson.toJson(correctAuthRequest));
    // authResponse = Connection.startLogin(gson.toJson(correctAuthRequest));
    // System.out.println("response: " + authResponse);
    // jsonAuthResponse = gson.fromJson(authResponse, JsonObject.class);
    // assertEquals(okCode, jsonAuthResponse.get(code).getAsInt());
  }

  @Test
  public void testPollingStarted() {
    // begin
    // authResponse = Connection.startLogin(gson.toJson(correctAuthRequest));
    // jsonAuthResponse = gson.fromJson(authResponse, JsonObject.class);
    // // parse Polling thread params
    // pollURL = jsonAuthResponse.get(pollField).getAsString();
    // pollRate = jsonAuthResponse.get(pollRateField).getAsLong();
    // PollingThread poller = new PollingThread(pollURL, pollRate);
    // poller.start();
    // assertEquals(Constants.NEED_SIGNIN, poller.getStatus());
  }

  @Test
  public void testStartLoginAndPoll() {
    Pryv.setStaging();
    Authenticator authenticator = new Authenticator(reqAppId, permissions, view, lang, returnURL);
    authenticator.authenticate();
    assertEquals(State.POLLING, authenticator.getState());
  }

}
