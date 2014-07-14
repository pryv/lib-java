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
import com.pryv.Connection;
import com.pryv.Pryv;
import com.pryv.api.model.Permission;
import com.pryv.utils.JsonConverter;

/**
 *
 * login class
 *
 * @author ik
 *
 */
public class Login implements LoginModel {

  private LoginController controller;
  private final int errorStatusLimit = 450; // used in temporary implementation
  private AuthenticationRequest authRequest;
  private Boolean first = true;
  private Gson gson = new Gson();

  public Login(LoginController pController, String appId, List<Permission> perms, String lang,
      String returnURL) {
    this.controller = pController;
    authRequest = new AuthenticationRequest(appId, perms, lang, returnURL);
  }

  public void startLogin() {
    try {
      String jsonRequest = JsonConverter.toJson(authRequest);
      System.out.println("first request: " + jsonRequest);
      System.out.println("executing startlogin on thread: " + Thread.currentThread().getName());

      Request.Post(Pryv.REGISTRATION_URL).bodyString(jsonRequest, ContentType.APPLICATION_JSON)
          .execute().handleResponse(loginResponseHandler);

    } catch (ClientProtocolException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * handles response to initial authorization request and to polling replies
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

        String reply = EntityUtils.toString(response.getEntity());
        System.out.println("handling reply entity : " + reply);
        JsonObject jsonResponse = gson.fromJson(reply, JsonObject.class);

        if (first) {
          String loginURL = jsonResponse.get(Constants.SERVER_URL).getAsString();
          controller.displayLoginView(loginURL);
          first = false;
          System.out.println("start view");
        }
        String state = jsonResponse.get(Constants.STATUS).getAsString();

        if (state.equals(Constants.NEED_SIGNIN)) {

          long rate = jsonResponse.get(Constants.POLL_RATE_MS).getAsLong();
          String pollURL = jsonResponse.get(Constants.POLL_URL).getAsString();
          System.out.println("polling at address: " + pollURL);
          new PollingThread(pollURL, rate, loginResponseHandler).start();
          controller.inProgress();
        } else if (state.equals(Constants.ACCEPTED)) {
          String username = jsonResponse.get(Constants.USERNAME).getAsString();
          String token = jsonResponse.get(Constants.TOKEN).getAsString();
          controller.accepted(new Connection(username, token));
        } else if (state.equals(Constants.REFUSED)) {
          String message = jsonResponse.get(Constants.MESSAGE).getAsString();
          controller.refused(message);
        } else if (state.equals(Constants.ERROR)) {
          int errorId = jsonResponse.get(Constants.ERROR_ID).getAsInt();
          String message = jsonResponse.get(Constants.MESSAGE).getAsString();
          String detail = jsonResponse.get(Constants.DETAIL).getAsString();
          controller.error(message);
        } else {
          System.out.println("login-error");
        }
      }

      return null;
    }
  };

}
