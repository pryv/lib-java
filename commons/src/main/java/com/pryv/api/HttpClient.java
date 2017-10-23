package com.pryv.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pryv.Filter;
import com.pryv.model.ApiResource;
import com.pryv.model.Attachment;
import com.pryv.utils.JsonConverter;

import java.io.File;
import java.io.IOException;

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
    private String apiUrl;
    private String tokenParameter;

    public HttpClient(String apiUrl, String tokenParameter) {
        client = new OkHttpClient();
        this.apiUrl = apiUrl;
        this.tokenParameter = tokenParameter;
    }

    public ApiRequest getRequest(String endpoint, Filter filter) {
        String url = apiUrl + endpoint + tokenParameter;
        if (filter != null) {
            url += filter.toUrlParameters();
        }
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        return new ApiRequest(request);
    }

    public ApiRequest createRequest(String endpoint, ApiResource newResource, Attachment attachment) throws JsonProcessingException {
        String url = apiUrl + endpoint + tokenParameter;
        String jsonEvent = JsonConverter.toJson(newResource);
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
        return new ApiRequest(request);
    }

    public ApiRequest updateRequest(String endpoint, String resourceId, ApiResource updatedResource) throws JsonProcessingException {
        String url = apiUrl + endpoint + "/" + resourceId + tokenParameter;
        String jsonEvent = JsonConverter.toJson(updatedResource);
        RequestBody body = RequestBody.create(JSON, jsonEvent);
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .build();
        return new ApiRequest(request);
    }

    public ApiRequest deleteRequest(String endpoint, String resourceId, Boolean mergeEventsWithParent) {
        String url = apiUrl + endpoint + "/" + resourceId + tokenParameter;
        if(mergeEventsWithParent) {
            url += "&mergeEventsWithParent=true";
        }
        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();
        return new ApiRequest(request);
    }

    public class ApiRequest {
        private Request httpRequest;

        public ApiRequest(Request httpRequest) {
            this.httpRequest = httpRequest;
        }

        public ApiResponse exec() throws IOException {
            Response response = client.newCall(httpRequest).execute();
            String json = response.body().string();
            double time = JsonConverter.retrieveServerTime(json);
            int status = response.code();
            ApiResponse apiResponse = new ApiResponse(json, time, status);
            return apiResponse;
        }
    }

    public class ApiResponse {
        private String jsonBody;
        private double serverTime;
        private int status;

        public ApiResponse (String httpResponse, double serverTime, int statusCode) {
            this.jsonBody = httpResponse;
            this.serverTime = serverTime;
            this.status = statusCode;
        }

        public String getJsonBody() {
            return jsonBody;
        }

        public double getServerTime() {
            return serverTime;
        }

        public int getStatus() {
            return status;
        }
    }
}
