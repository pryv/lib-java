package com.pryv.api;

/**
 * Model a HTTP Response from Pryv API
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