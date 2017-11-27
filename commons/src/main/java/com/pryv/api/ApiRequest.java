package com.pryv.api;

import com.pryv.exceptions.ApiException;
import com.pryv.utils.JsonConverter;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Model a HTTP request to Pryv API
 */
public class ApiRequest {
    private Request httpRequest;
    private OkHttpClient httpClient;

    public ApiRequest(Request httpRequest, OkHttpClient httpClient) {
        this.httpRequest = httpRequest;
        this.httpClient = httpClient;
    }

    public ApiResponse exec() throws IOException, ApiException {
        Response response = httpClient.newCall(httpRequest).execute();
        String json = response.body().string();
        double time = JsonConverter.retrieveServerTime(json);
        int status = response.code();
        if (status != HttpURLConnection.HTTP_CREATED && status != HttpURLConnection.HTTP_OK) {
            ApiException exception = JsonConverter.retrieveApiError(json);
            throw exception;
        }
        ApiResponse apiResponse = new ApiResponse(json, time, status);
        return apiResponse;
    }
}