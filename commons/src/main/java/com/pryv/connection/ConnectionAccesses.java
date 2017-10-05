package com.pryv.connection;

import com.pryv.api.OnlineManager;
import com.pryv.model.Access;

public class ConnectionAccesses {

    private OnlineManager api;
    private static final String ENDPOINT = "accesses";


    public ConnectionAccesses(OnlineManager api) {
        this.api = api;
    }

    public void get() {
        api.get(ENDPOINT, null);
    }

    public void create(final Access newAccess) {
        api.create(ENDPOINT, newAccess, null);
    }

    public void delete(final String accessId) {
        api.delete(ENDPOINT, accessId, false);
    }

    public void update(final String accessId, final Access updatedAccess) {
        api.update(ENDPOINT, accessId, updatedAccess);
    }

}