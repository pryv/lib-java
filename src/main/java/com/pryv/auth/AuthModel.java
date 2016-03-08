package com.pryv.auth;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.pryv.Pryv;
import com.pryv.api.model.Permission;
import com.pryv.utils.JsonConverter;
import com.pryv.utils.Logger;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
/**
 *
 * login class
 *
 * @author ik
 *
 */
public class AuthModel {
    private AuthController controller;
    private AuthenticationRequest authRequest;
    private Boolean first = true;
    private Logger logger = Logger.getInstance();
    public AuthModel(AuthController pController, String requestingAppId,
                     List<Permission> permissions, String language, String returnURL) {
        this.controller = pController;
        authRequest = new AuthenticationRequest(requestingAppId, permissions, language, returnURL);
    }

    public void startLogin() {
        try {
            String jsonRequest = JsonConverter.toJson(authRequest);
            logger.log("AuthModelImpl: start login request: " + jsonRequest);
            OkHttpClient client = new OkHttpClient();
            RequestBody bodyString = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonRequest);
            Request request = new Request.Builder()
                    .url(Pryv.REGISTRATION_URL)
                    .post(bodyString)
                    .build();
            Response response = client.newCall(request).execute();
            new SignInResponseHandler().handleResponse(response);
        } catch (JsonProcessingException e) {
            controller.onError(e.getMessage());
        } catch (IOException e) {
            controller.onError(e.getMessage());
        }
    }
    /**
     * handles response to initial authorization request and to polling replies
     */
    public class SignInResponseHandler {
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
        private final static String REASON_ID_KEY = "id";
        private final static String DETAIL_KEY = "detail";
        /**
         * unique class method that retrieves HttpResponse's components and calls
         * the appropriate controller's methods
         */
        public String handleResponse(Response response) throws IOException {
            int statusCode = response.code();
            String reply = response.body().string();
            logger.log("AuthModelImpl: signInResponseHandler: response status code: " + statusCode);
            logger.log("sAuthModelImpl: ignInResponseHandler: handling reply entity : " + reply);
            if (statusCode == HttpURLConnection.HTTP_CREATED || statusCode == HttpURLConnection.HTTP_OK) {
                JsonNode jsonResponse = JsonConverter.toJsonNode(reply);
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
                    new PollingThread(pollURL, rate, this, controller).start();
                } else if (state.equals(ACCEPTED_VALUE)) {
                    String username = jsonResponse.get(USERNAME_KEY).textValue();
                    String token = jsonResponse.get(TOKEN_KEY).textValue();
                    controller.onSuccess(username, token);
                } else if (state.equals(REFUSED_VALUE)) {
                    String message = jsonResponse.get(MESSAGE_KEY).textValue();
                    controller.onError(message);
                } else if (state.equals(ERROR_VALUE)) {
                    int reasonId = jsonResponse.get(REASON_ID_KEY).intValue();
                    String message = jsonResponse.get(MESSAGE_KEY).textValue();
                    String detail = jsonResponse.get(DETAIL_KEY).textValue();
                    controller.onRefused(reasonId, message, detail);
                } else {
                    controller.onError("unknown-error");
                }
            } else {
                controller.onError(reply);
            }
            return null;
        }
    };
}