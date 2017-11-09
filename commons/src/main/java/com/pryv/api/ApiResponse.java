package com.pryv.api;

/**
 * Created by thiebaudmodoux on 09.11.17.
 */

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