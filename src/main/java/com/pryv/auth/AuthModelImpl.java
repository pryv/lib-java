package com.pryv.auth;

import java.io.IOException;
import java.util.List;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.pryv.Connection;
import com.pryv.Pryv;
import com.pryv.api.model.Permission;
import com.pryv.utils.JsonConverter;
import com.pryv.utils.Logger;

/**
 *
 * login class
 *
 * @author ik
 *
 */
public class AuthModelImpl implements AuthModel {

  private AuthController controller;
  private AuthenticationRequest authRequest;
  private Boolean first = true;
  private Logger logger = Logger.getInstance();

  public AuthModelImpl(AuthController pController, String requestingAppId,
    List<Permission> permissions, String language, String returnURL) {
    this.controller = pController;
    authRequest = new AuthenticationRequest(requestingAppId, permissions, language, returnURL);
  }

  @Override
  public void startLogin() throws ClientProtocolException, IOException {

    String jsonRequest;
    try {
      jsonRequest = JsonConverter.toJson(authRequest);
      logger.log("AuthController: start login request: " + jsonRequest);
      Request.Post(Pryv.REGISTRATION_URL).bodyString(jsonRequest, ContentType.APPLICATION_JSON)
        .execute().handleResponse(signInResponseHandler);
    } catch (JsonProcessingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  /**
   * handles response to initial authorization request and to polling replies
   */
  private ResponseHandler<String> signInResponseHandler = new ResponseHandler<String>() {

    private final static String SERVER_URL_KEY = "url";
    private final static String STATUS_KEY = "status";
    private final static String POLL_URL_KEY = "poll";
    private final static String POLL_RATE_MS_KEY = "poll_rate_ms";
    private final static String NEED_SIGNIN_VALUE = "NEED_SIGNIN";
    private final static String ACCEPTED_VALUE = "ACCEPTED";
    private final static String REFUSED_VALUE = "REFUSED";
    private final static String ERROR_VALUE = "ERROR";
    private final static String USERNAME_KEY = "username";
    private final static String TOKEN_KEY = "token";
    private final static String MESSAGE_KEY = "message";
    private final static String ERROR_ID_KEY = "id";
    private final static String DETAIL_KEY = "detail";

    /**
     * unique class method that retrieves HttpResponse's components and calls
     * the appropriate controller's methods
     */
    @Override
    public String handleResponse(HttpResponse response) throws ClientProtocolException,
      IOException {

      int statusCode = response.getStatusLine().getStatusCode();
      String reply = EntityUtils.toString(response.getEntity());
      logger.log("signInResponseHandler: response status code: " + statusCode);
      logger.log("signInResponseHandler: handling reply entity : " + reply);
      if (statusCode == HttpStatus.SC_CREATED || statusCode == HttpStatus.SC_OK) {

        JsonNode jsonResponse = JsonConverter.fromJson(reply);

        if (first) {
          String loginUrl = jsonResponse.get(SERVER_URL_KEY).textValue();
          controller.displayLoginView(loginUrl);
          first = false;
          logger.log("signInResponseHandler: start view of address : \'" + loginUrl + "\'");
        }
        String state = jsonResponse.get(STATUS_KEY).textValue();
        logger.log("signInResponseHandler: state retrieved: " + state);

        if (state.equals(NEED_SIGNIN_VALUE)) {
          long rate = jsonResponse.get(POLL_RATE_MS_KEY).longValue();
          String pollURL = jsonResponse.get(POLL_URL_KEY).textValue();
          logger.log("signInResponseHandler: polling at address: " + pollURL);
          new PollingThread(pollURL, rate, signInResponseHandler).start();

        } else if (state.equals(ACCEPTED_VALUE)) {
          String username = jsonResponse.get(USERNAME_KEY).textValue();
          String token = jsonResponse.get(TOKEN_KEY).textValue();
          controller.onSuccess(new Connection(username, token));

        } else if (state.equals(REFUSED_VALUE)) {
          String message = jsonResponse.get(MESSAGE_KEY).textValue();
          controller.onFailure(0, message, null);

        } else if (state.equals(ERROR_VALUE)) {
          int errorId = jsonResponse.get(ERROR_ID_KEY).intValue();
          String message = jsonResponse.get(MESSAGE_KEY).textValue();
          String detail = jsonResponse.get(DETAIL_KEY).textValue();
          controller.onFailure(errorId, message, detail);

        } else {
          controller.onFailure(0, "unknown-error", null);
        }

      } else {
        controller.onFailure(statusCode, reply, null);
      }
      return null;
    }
  };

}
