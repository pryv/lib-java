package com.pryv.connection;

import com.pryv.AbstractConnection;
import com.pryv.api.HttpClient;
import com.pryv.api.OnlineManager;
import com.pryv.interfaces.ApiCallback;
import com.pryv.interfaces.CreateCallback;
import com.pryv.interfaces.DeleteCallback;
import com.pryv.interfaces.GetCallback;
import com.pryv.interfaces.UpdateCallback;
import com.pryv.model.Access;
import com.pryv.utils.JsonConverter;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

public class ConnectionAccesses {

    private WeakReference<AbstractConnection> weakConnection;
    private OnlineManager api;
    private static final String ENDPOINT = "accesses";
    private static final String ACCESS_KEY = "access";
    private static final String ACCESSES_KEY = "accesses";
    private static final String ACCESS_DELETION_KEY = "accessDeletion";
    private HttpClient httpClient;

    public ConnectionAccesses(WeakReference<AbstractConnection> weakConnection, OnlineManager api) {
        this.weakConnection = weakConnection;
        this.api = api;
        this.httpClient = api.getHttpClient();
    }

    public void get(final GetCallback userCallback) {
        ApiCallback getCallback = new ApiCallback() {
            @Override
            public void onSuccess(String successMessage, String responseBody, Double serverTime) {
                try {
                    List<Access> receivedAccesses = JsonConverter.retrieveResourcesFromJson(responseBody, ACCESSES_KEY, Access.class);
                    // TODO: also included in JSON converter?
                    for (Access receivedAccess : receivedAccesses) {
                        receivedAccess.assignConnection(weakConnection);
                        Access.createOrReuse(receivedAccess);
                        userCallback.onSuccess(successMessage, receivedAccesses, serverTime);
                    }
                } catch (IOException e) {
                    userCallback.onError(e.getMessage(), null);
                }
            }

            @Override
            public void onError(String errorMessage, Double serverTime) {
                userCallback.onError(errorMessage, serverTime);
            }
        };
        httpClient.getRequest(ENDPOINT, null, getCallback).exec();
    }

    public void create(final Access newAccess, final CreateCallback userCallback) {
        ApiCallback createCallback = new ApiCallback() {
            @Override
            public void onSuccess(String successMessage, String responseBody, Double serverTime) {
                try {
                    Access createdAccess = JsonConverter.retrieveResourceFromJson(responseBody, ACCESS_KEY, Access.class);
                    createdAccess.assignConnection(weakConnection);
                    Access.createOrReuse(createdAccess);
                    userCallback.onSuccess(successMessage, createdAccess, serverTime);
                } catch (IOException e) {
                    userCallback.onError(e.getMessage(), serverTime);
                }
            }

            @Override
            public void onError(String errorMessage, Double serverTime) {
                userCallback.onError(errorMessage, serverTime);
            }
        };
        httpClient.createRequest(ENDPOINT, newAccess, null, createCallback).exec();
    }

    public void delete(final String accessId, final DeleteCallback userCallback) {
        ApiCallback deleteCallback = new ApiCallback() {
            @Override
            public void onSuccess(String successMessage, String responseBody, Double serverTime) {
                try {
                    String deletedId = JsonConverter.retrieveDeletedResourceId(responseBody, ACCESS_DELETION_KEY);
                    userCallback.onSuccess(successMessage, deletedId, serverTime);

                } catch (IOException e) {
                    userCallback.onError(e.getMessage(), serverTime);
                }
            }

            @Override
            public void onError(String errorMessage, Double serverTime) {
                userCallback.onError(errorMessage, serverTime);
            }
        };
        httpClient.deleteRequest(ENDPOINT, accessId, false, deleteCallback).exec();
    }

    public void update(final String accessId, final Access updatedAccess, final UpdateCallback userCallback) {
        ApiCallback updateCallback = new ApiCallback() {
            @Override
            public void onSuccess(String successMessage, String responseBody, Double serverTime) {
                try {
                    Access updatedAccess = JsonConverter.retrieveResourceFromJson(responseBody, ACCESS_KEY, Access.class);
                    updatedAccess.assignConnection(weakConnection);
                    Access.createOrReuse(updatedAccess);
                    userCallback.onSuccess(successMessage, updatedAccess, serverTime);
                } catch (IOException e) {
                    userCallback.onError(e.getMessage(), serverTime);
                }
            }

            @Override
            public void onError(String errorMessage, Double serverTime) {
                userCallback.onError(errorMessage, serverTime);
            }
        };
        httpClient.updateRequest(ENDPOINT, accessId, updatedAccess, updateCallback).exec();
    }

}