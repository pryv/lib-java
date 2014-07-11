package com.pryv.authorization;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Request;

/**
 *
 * Polling thread that polls the URL for login sequence state
 *
 * @author ik
 *
 */
public class PollingThread extends Thread {
  private String pollURL;
  private long pollRate;
  private ResponseHandler<String> responseHandler;

  public PollingThread(String url, long rate, ResponseHandler<String> handler) {
    System.out.println("PollingThread instanciated");
    pollURL = url;
    pollRate = rate;
    responseHandler = handler;
  }

  @Override
  public void run() {

    try {
      System.out.println("sending poll request on thread: " + Thread.currentThread().getName());
      sleep(pollRate);
      Request.Get(pollURL).execute().handleResponse(responseHandler);

      System.out.println("PollingThread: polling request sent");


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
