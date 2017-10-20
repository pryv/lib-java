package com.pryv.interfaces;

public interface ApiCallback {

    void onSuccess(String successMessage, String responseBody, Double serverTime);

    void onError(String errorMessage, Double serverTime);
}
