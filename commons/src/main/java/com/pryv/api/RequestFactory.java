package com.pryv.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pryv.Filter;
import com.pryv.model.ApiResource;
import com.pryv.model.Attachment;
import com.pryv.utils.JsonConverter;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by thiebaudmodoux on 05.10.17.
 */

public class RequestFactory {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static Request getRequest(String endpoint, String tokenParameter, Filter filter) {
        String url = endpoint + tokenParameter;
        if (filter != null) {
            url += filter.toUrlParameters();
        }
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        return request;
    }

    public static Request createRequest(String endpoint, String tokenParameter, ApiResource newResource, Attachment attachment) throws JsonProcessingException {
        String url = endpoint + tokenParameter;
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
        return request;
    }

    public static Request updateRequest(String endpoint, String tokenParameter, String resourceId, ApiResource updatedResource) throws JsonProcessingException {
        String url = endpoint + "/" + resourceId + tokenParameter;
        String jsonEvent = JsonConverter.toJson(updatedResource);
        RequestBody body = RequestBody.create(JSON, jsonEvent);
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .build();
        return request;
    }

    public static Request deleteRequest(String endpoint, String tokenParameter, String resourceId, Boolean mergeEventsWithParent) {
        String url = endpoint + "/" + resourceId + tokenParameter;
        if(mergeEventsWithParent) {
            url += "&mergeEventsWithParent=true";
        }
        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();
        return request;
    }
}
