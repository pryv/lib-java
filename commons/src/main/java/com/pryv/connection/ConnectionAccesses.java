package com.pryv.connection;

import com.pryv.AbstractConnection;
import com.pryv.api.OnlineManager;
import com.pryv.interfaces.EventsCallback;
import com.pryv.model.Access;

import java.lang.ref.WeakReference;

public class ConnectionAccesses {

    private WeakReference<AbstractConnection> weakConnection;
    private OnlineManager api;

    public ConnectionAccesses(WeakReference<AbstractConnection> weakConnection, OnlineManager api) {
        this.weakConnection = weakConnection;
        this.api = api;
    }

    public void get() {

    }

    public void create(final Access newAccess) {

    }

    public void delete(final Access accessToDelete) {

    }

    public void update(final Access accessToUpdate) {

    }

}