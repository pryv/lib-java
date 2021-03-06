package com.pryv;

import com.pryv.api.HttpClient;
import com.pryv.connection.ConnectionAccesses;
import com.pryv.connection.ConnectionAccount;
import com.pryv.connection.ConnectionEvents;
import com.pryv.connection.ConnectionProfile;
import com.pryv.connection.ConnectionStreams;
import com.pryv.model.Stream;

import java.util.Map;

/**
 * Pryv API connection - Object used to manipulate Events and Streams data.
 */
public class Connection {

    public ConnectionAccesses accesses;
    public ConnectionAccount account;
    public ConnectionEvents events;
    public ConnectionProfile profile;
    public ConnectionStreams streams;

    private String username;
    private String domain;
    private String urlEndpoint;
    private String registrationUrl;

    /**
     * Main object to manipulate Pryv data, instanciate it with the required parameters.
     *
     * @param username
     * @param token
     * @param domain
     */
    public Connection(String username, String token, String domain) {

        this.username = username;
        this.domain = domain;
        buildUrlEndpoint();
        buildRegistrationUrl();

        HttpClient httpClient = new HttpClient(urlEndpoint, "?auth=" + token);

        this.accesses = new ConnectionAccesses(httpClient);
        this.account = new ConnectionAccount();
        this.events = new ConnectionEvents(httpClient);
        this.profile = new ConnectionProfile();
        this.streams = new ConnectionStreams(httpClient);
    }

    private String buildUrlEndpoint() {
        this.urlEndpoint = "https://" + username + "." + domain + "/";
        return this.urlEndpoint;
    }

    private String buildRegistrationUrl() {
        this.registrationUrl = "https://reg." + domain + "/access";
        return this.registrationUrl;
    }

    private String getUrlRegistration() {
        return this.registrationUrl;
    }

    /**
     * returns the root Streams of the Pryv structure
     *
     * @return
     */
    public Map<String, Stream> getRootStreams() {
        return streams.getRootStreams();
    }

}
