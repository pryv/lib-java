package com.pryv.interfaces;

public interface UserCallback {

    void onSuccess(String successMessage, String responseBody, Double serverTime);

    void onError(String errorMessage, Double serverTime);
}
