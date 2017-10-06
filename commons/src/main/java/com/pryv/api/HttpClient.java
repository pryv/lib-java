package com.pryv.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pryv.Filter;
import com.pryv.interfaces.ApiCallback;
import com.pryv.model.ApiResource;
import com.pryv.model.Attachment;
import com.pryv.utils.JsonConverter;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by thiebaudmodoux on 05.10.17.
 */

public class HttpClient {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient client;
    private String baseUrl;

    public HttpClient(String apiUrl, String tokenParameter) {
        client = new OkHttpClient();
        baseUrl = apiUrl + tokenParameter;
    }

    public ApiRequest getRequest(String endpoint, Filter filter, ApiCallback callback) {
        String url = baseUrl + endpoint;
        if (filter != null) {
            url += filter.toUrlParameters();
        }
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        return new ApiRequest(request, callback);
    }

    public ApiRequest createRequest(String endpoint, ApiResource newResource, Attachment attachment, ApiCallback callback) {
        String url = baseUrl + endpoint;
        String jsonEvent = null;
        try {
            jsonEvent = JsonConverter.toJson(newResource);
        } catch (JsonProcessingException e) {
            callback.onError(e.getMessage(), null);
        }
        RequestBody body;
        // TODO: handle multiple attachments
        if(attachment != null) {
            File file = attachment.getFile();
            body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("event", jsonEvent)
                    .addFormDataPart("file", file.getName(), RequestBody.create(null, file))
                    .build();
        } else {
            body = RequestBody.create(JSON, jsonEvent);
        }

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        return new ApiRequest(request, callback);
    }

    public ApiRequest updateRequest(String endpoint, String resourceId, ApiResource updatedResource, ApiCallback callback) {
        String url = baseUrl + endpoint + "/" + resourceId;
        String jsonEvent = null;
        try {
            jsonEvent = JsonConverter.toJson(updatedResource);
        } catch (JsonProcessingException e) {
            callback.onError(e.getMessage(), null);
        }
        RequestBody body = RequestBody.create(JSON, jsonEvent);
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .build();
        return new ApiRequest(request, callback);
    }

    public ApiRequest deleteRequest(String endpoint, String resourceId, Boolean mergeEventsWithParent, ApiCallback callback) {
        String url = baseUrl + endpoint + "/" + resourceId;
        if(mergeEventsWithParent) {
            url += "&mergeEventsWithParent=true";
        }
        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();
        return new ApiRequest(request, callback);
    }

    public class ApiRequest {
        private Request httpRequest;
        private ApiCallback callback;

        public ApiRequest(Request httpRequest, ApiCallback callback) {
            this.httpRequest = httpRequest;
            this.callback = callback;
        }

        public void exec() {
            client.newCall(httpRequest)
                .enqueue(new Callback() {

                    @Override
                    public void onFailure(final Call call, IOException e) {
                        callback.onError(e.getMessage(), null);
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {

                        String responseBody = "";
                        double serverTime = 0.0;

                        if (response.body() != null) {
                            responseBody = response.body().string();
                            serverTime = JsonConverter.retrieveServerTime(responseBody);
                        }

                        int statusCode = response.code();
                        if (statusCode == HttpURLConnection.HTTP_CREATED || statusCode == HttpURLConnection.HTTP_OK) {
                            callback.onSuccess(""+statusCode, responseBody, serverTime);
                        } else {
                            callback.onError(""+statusCode, serverTime);
                        }
                    }
                });
        }
    }
}
