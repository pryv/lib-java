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
	private AuthController controller;
	private String pollURL;
	private long pollRate;
	private ResponseHandler<String> responseHandler;
	private Logger logger = Logger.getInstance();

	public PollingThread(String url, long rate, ResponseHandler<String> handler, AuthController pController) {
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
			Request.Get(pollURL).execute().handleResponse(responseHandler);

			logger.log("PollingThread: polling request sent");

		} catch (ClientProtocolException e) {
			controller.onError(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			controller.onError(e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			controller.onError(e.getMessage());
			e.printStackTrace();
		}
	}
}
