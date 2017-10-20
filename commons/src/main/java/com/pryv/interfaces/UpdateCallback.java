package com.pryv.interfaces;

import com.pryv.model.ApiResource;

public interface UpdateCallback <T extends ApiResource> {

    void onSuccess(String successMessage, T resource, Double serverTime);

    void onError(String errorMessage, Double serverTime);
}
