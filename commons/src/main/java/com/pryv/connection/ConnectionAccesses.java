package com.pryv.connection;

import com.pryv.api.ApiResponse;
import com.pryv.api.HttpClient;
import com.pryv.exceptions.ApiException;
import com.pryv.model.Access;
import com.pryv.utils.JsonConverter;

import java.io.IOException;
import java.util.List;

/**
 * Encapsulate CRUD operations to Pryv API for Accesses
 */
public class ConnectionAccesses {

    private static final String PATH = "accesses";
    private static final String ACCESS_KEY = "access";
    private static final String ACCESSES_KEY = "accesses";
    private HttpClient httpClient;

    public ConnectionAccesses(HttpClient client) {
        this.httpClient = client;
    }

    public List<Access> get() throws IOException, ApiException {
        ApiResponse apiResponse = httpClient.getRequest(PATH, null).exec();
        List<Access> receivedAccesses = JsonConverter.retrieveResourcesFromJson(apiResponse.getJsonBody(), ACCESSES_KEY, Access.class);
        return receivedAccesses;
    }

    public Access create(Access newAccess) throws IOException, ApiException {
        ApiResponse apiResponse = httpClient.createRequest(PATH, newAccess, null).exec();
        Access createdAccess = JsonConverter.retrieveResourceFromJson(apiResponse.getJsonBody(), ACCESS_KEY, Access.class);
        return createdAccess;
    }

    public Access delete(Access deleteAccess) throws IOException, ApiException {
        httpClient.deleteRequest(PATH, deleteAccess.getId(), false).exec();
        return deleteAccess.setDeleted(true);
    }

    public Access update(Access updateAccess) throws IOException, ApiException {
        Access update = updateAccess.cloneMutableFields();
        ApiResponse apiResponse = httpClient.updateRequest(PATH, updateAccess.getId(), update).exec();
        Access updatedAccess = JsonConverter.retrieveResourceFromJson(apiResponse.getJsonBody(), ACCESS_KEY, Access.class);
        return updatedAccess;
    }

}