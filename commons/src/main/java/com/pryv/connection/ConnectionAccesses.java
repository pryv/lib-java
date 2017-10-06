package com.pryv.connection;

import com.pryv.api.HttpClient;
import com.pryv.api.OnlineManager;
import com.pryv.interfaces.ApiCallback;
import com.pryv.interfaces.UserCallback;
import com.pryv.model.Access;

public class ConnectionAccesses {

    private OnlineManager api;
    private static final String ENDPOINT = "accesses";
    private HttpClient httpClient;


    public ConnectionAccesses(OnlineManager api) {
        this.api = api;
        this.httpClient = api.getHttpClient();
    }

    public void get(final UserCallback userCallback) {
        ApiCallback getCallback = new ApiCallback() {
            @Override
            public void onSuccess(String successMessage, String responseBody, Double serverTime) {
                userCallback.onSuccess(successMessage, responseBody, serverTime);
            }

            @Override
            public void onError(String errorMessage, Double serverTime) {
                userCallback.onError(errorMessage, serverTime);
            }
        };
        httpClient.getRequest(ENDPOINT, null, getCallback).exec();
    }

    public void create(final Access newAccess, final UserCallback userCallback) {
        ApiCallback createCallback = new ApiCallback() {
            @Override
            public void onSuccess(String successMessage, String responseBody, Double serverTime) {
                userCallback.onSuccess(successMessage, responseBody, serverTime);
            }

            @Override
            public void onError(String errorMessage, Double serverTime) {
                userCallback.onError(errorMessage, serverTime);
            }
        };
        httpClient.createRequest(ENDPOINT, newAccess, null, createCallback).exec();
    }

    public void delete(final String accessId, final UserCallback userCallback) {
        ApiCallback deleteCallback = new ApiCallback() {
            @Override
            public void onSuccess(String successMessage, String responseBody, Double serverTime) {
                userCallback.onSuccess(successMessage, responseBody, serverTime);
            }

            @Override
            public void onError(String errorMessage, Double serverTime) {
                userCallback.onError(errorMessage, serverTime);
            }
        };
        httpClient.deleteRequest(ENDPOINT, accessId, false, deleteCallback).exec();
    }

    public void update(final String accessId, final Access updatedAccess, final UserCallback userCallback) {
        ApiCallback updateCallback = new ApiCallback() {
            @Override
            public void onSuccess(String successMessage, String responseBody, Double serverTime) {
                userCallback.onSuccess(successMessage, responseBody, serverTime);
            }

            @Override
            public void onError(String errorMessage, Double serverTime) {
                userCallback.onError(errorMessage, serverTime);
            }
        };
        httpClient.updateRequest(ENDPOINT, accessId, updatedAccess, updateCallback).exec();
    }

}