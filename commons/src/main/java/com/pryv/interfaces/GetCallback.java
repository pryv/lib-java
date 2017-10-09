package com.pryv.interfaces;

import com.pryv.model.ApiResource;

import java.util.List;

public interface GetCallback <T extends ApiResource> {

    void onSuccess(String successMessage, List<T> resources, Double serverTime);

    void onError(String errorMessage, Double serverTime);
}
