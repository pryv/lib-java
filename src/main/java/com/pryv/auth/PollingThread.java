package com.pryv.auth;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Request;

import com.pryv.utils.Logger;

/**
 *
 * Polling thread that polls the URL for login sequence state.
 *
 * @author ik
 *
 */
public class PollingThread extends Thread {
  private String pollURL;
  private long pollRate;
  private ResponseHandler<String> responseHandler;
  private Logger logger = Logger.getInstance();

  public PollingThread(String url, long rate, ResponseHandler<String> handler) {
    logger.log("PollingThread instanciated");
    pollURL = url;
    pollRate = rate;
    responseHandler = handler;
  }

  @Override
  public void run() {

    try {
      logger.log("PollingThread: sending poll request on thread: "
        + Thread.currentThread().getName());
      sleep(pollRate);
      Request.Get(pollURL).execute().handleResponse(responseHandler);

      logger.log("PollingThread: polling request sent by thread: "
        + Thread.currentThread().getName());

    } catch (ClientProtocolException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
