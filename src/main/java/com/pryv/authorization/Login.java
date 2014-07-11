package com.pryv.authorization;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.pryv.Pryv;
import com.pryv.api.model.Permission;

/**
 *
 * login class
 *
 * @author ik
 *
 */
public class Login {

  private LoginController controller;
  private Gson gson = new Gson();
  private final int errorStatusLimit = 450; // used in temporary implementation

  public Login(LoginController pController, String appId, List<Permission> perms, String view,
      String lang, String returnURL) {
    this.controller = pController;
    AuthenticationRequest authRequest = new AuthenticationRequest(appId, perms, lang, returnURL);
    String jsonRequest = gson.toJson(authRequest);
    System.out.println("first request: " + jsonRequest);
    startLogin(jsonRequest);

    // start polling thread upon succesful response

    //

  }

  public void startLogin(String jsonAuthRequest) {
    try {
      System.out.println("executing startlogin on thread: " + Thread.currentThread().getName());

      Request.Post(Pryv.REGISTRATION_URL)
          .bodyString(jsonAuthRequest, ContentType.APPLICATION_JSON).execute()
          .handleResponse(loginResponseHandler);

    } catch (ClientProtocolException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   *
   * handles response to initial authorization request and to polling replies
   *
   */
  private ResponseHandler<String> loginResponseHandler = new ResponseHandler<String>() {

    /**
     * handles
     */
    public String handleResponse(HttpResponse response) throws ClientProtocolException,
        IOException {
      // read status
      int status = response.getStatusLine().getStatusCode();
      if (status < errorStatusLimit) {

        System.out.println("handling response on thread: " + Thread.currentThread().getName());

        String reply = EntityUtils.toString(response.getEntity());

        System.out.println("handling reply entity : " + reply);

        JsonObject jsonResponse = gson.fromJson(reply, JsonObject.class);

        String pollURL = jsonResponse.get(Constants.POLL_URL).getAsString();
        String state = jsonResponse.get(Constants.STATUS).getAsString();

        long rate = jsonResponse.get(Constants.POLL_RATE_MS).getAsLong();

        if (pollURL != null && state.equals(Constants.NEED_SIGNIN)) {

          System.out.println("polling at address: " + pollURL);

          new PollingThread(pollURL, rate, loginResponseHandler).start();

          controller.inProgress();
        } else {
          System.out.println("login-error");
        }
      }

      return null;
    }
  };

}
