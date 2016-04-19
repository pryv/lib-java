package com.example;

import com.pryv.auth.AuthView;
import com.pryv.interfaces.EventsCallback;
import com.pryv.interfaces.StreamsCallback;
import com.pryv.model.Event;
import com.pryv.model.Stream;

/**
 * This example lets the user sign in, then retrieves the access information
 * (type and permissions), the available streams structure and the last 20
 * events.
 *
 * @author ik
 *
 */
public class BasicExample implements AuthView, EventsCallback, StreamsCallback {

    /**
     * print informative message
     *
     * @param loginUrl
     */
    private static void printExampleMessage(String loginUrl) {
        System.out.println("#########################################################");
        System.out.println("##                  Basic Example started              ##");
        System.out.println("#########################################################\n");
        System.out.println("Sign in the open web page or copy this link in your browser:\n");
        System.out.println(loginUrl + "\n");
        System.out
                .println("Use your own staging account credentials or user \'perkikiki\' and password \'poilonez\'\n");
    }

    @Override
    public void onApiSuccess(String successMessage, Event event, String stoppedId, Double serverTime) {

    }

    @Override
    public void onApiSuccess(String successMessage, Stream stream, Double serverTime) {

    }

    @Override
    public void onApiError(String errorMessage, Double serverTime) {

    }

    @Override
    public void onCacheSuccess(String successMessage, Stream stream) {

    }

    @Override
    public void onCacheSuccess(String successMessage, Event event) {

    }

    @Override
    public void onCacheError(String errorMessage) {

    }

    @Override
    public void displayLoginView(String loginURL) {

    }

    @Override
    public void onAuthSuccess(String username, String token) {

    }

    @Override
    public void onAuthError(String message) {

    }

    @Override
    public void onAuthRefused(int reasonId, String message, String detail) {

    }
}