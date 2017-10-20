package com.pryv.interfaces;

import com.pryv.model.ApiResource;

public interface DeleteCallback <T extends ApiResource>  {

    void onSuccess(String successMessage, String deletedId, Double serverTime);

    void onError(String errorMessage, Double serverTime);
}
