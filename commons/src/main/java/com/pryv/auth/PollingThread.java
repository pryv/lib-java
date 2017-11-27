package com.pryv.auth;

import com.pryv.utils.Logger;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Polling thread that polls the URL for login sequence state.
 */
public class PollingThread extends Thread {
  private AuthController controller;
  private String pollURL;
  private long pollRate;
  private AuthModel.SignInResponseHandler responseHandler;
  private Logger logger = Logger.getInstance();

  public PollingThread(String url, long rate, AuthModel.SignInResponseHandler handler,
    AuthController pController) {
    controller = pController;
    logger.log("PollingThread instanciated");
    pollURL = url;
    pollRate = rate;
    responseHandler = handler;
  }

  @Override
  public void run() {

    try {
      logger.log("PollingThread: sending poll request");
      sleep(pollRate);

      OkHttpClient client = new OkHttpClient();
      Request request = new Request.Builder()
              .url(pollURL)
              .get()
              .build();
      Response response = client.newCall(request).execute();
      responseHandler.handleResponse(response);

      logger.log("PollingThread: polling request sent");

    } catch (IOException e) {
      controller.onError(e.getMessage());
      e.printStackTrace();
    } catch (InterruptedException e) {
      controller.onError(e.getMessage());
      e.printStackTrace();
    }
  }
}
