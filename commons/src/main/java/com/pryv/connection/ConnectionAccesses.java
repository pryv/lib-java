package com.pryv.connection;

import com.pryv.api.HttpClient;
import com.pryv.model.Access;
import com.pryv.utils.JsonConverter;

import java.io.IOException;
import java.util.List;

public class ConnectionAccesses {

    private static final String PATH = "accesses";
    private static final String ACCESS_KEY = "access";
    private static final String ACCESSES_KEY = "accesses";
    private static final String ACCESS_DELETION_KEY = "accessDeletion";
    private HttpClient httpClient;

    public ConnectionAccesses(HttpClient client) {
        this.httpClient = client;
    }

    public List<Access> get() throws IOException {
        HttpClient.ApiResponse apiResponse = httpClient.getRequest(PATH, null).exec();
        List<Access> receivedAccesses = JsonConverter.retrieveResourcesFromJson(apiResponse.getJsonBody(), ACCESSES_KEY, Access.class);
        // TODO: retrieve accessDeletions
        return receivedAccesses;
    }

    public Access create(Access newAccess) throws IOException {
        HttpClient.ApiResponse apiResponse = httpClient.createRequest(PATH, newAccess, null).exec();
        Access createdAccess = JsonConverter.retrieveResourceFromJson(apiResponse.getJsonBody(), ACCESS_KEY, Access.class);
        return createdAccess;
    }

    public String delete(String accessId) throws IOException {
        HttpClient.ApiResponse apiResponse = httpClient.deleteRequest(PATH, accessId, false).exec();
        String deletedId = JsonConverter.retrieveDeletedResourceId(apiResponse.getJsonBody(), ACCESS_DELETION_KEY);
        return deletedId;
    }

    public Access update(Access updateAccess) throws IOException {
        Access update = updateAccess.cloneMutableFields();
        HttpClient.ApiResponse apiResponse = httpClient.updateRequest(PATH, updateAccess.getId(), update).exec();
        Access updatedAccess = JsonConverter.retrieveResourceFromJson(apiResponse.getJsonBody(), ACCESS_KEY, Access.class);
        return updatedAccess;
    }

}