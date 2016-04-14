package com.pryv;

import com.pryv.api.Filter;
import com.pryv.api.OnlineEventsAndStreamsManager;
import com.pryv.api.database.DBinitCallback;
import com.pryv.api.database.SQLiteDBHelper;
import com.pryv.api.model.Stream;
import com.pryv.connection.ConnectionAccesses;
import com.pryv.connection.ConnectionAccount;
import com.pryv.connection.ConnectionEvents;
import com.pryv.connection.ConnectionProfile;
import com.pryv.connection.ConnectionStreams;
import com.pryv.utils.Logger;

import org.apache.commons.codec.digest.DigestUtils;
import org.joda.time.DateTime;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    private String token;
    private String domain;
    private String urlEndpoint;
    private String registrationUrl;

    private boolean isApiActive = true;
    private boolean isCacheActive = true;

    private OnlineEventsAndStreamsManager api;

    private Filter cacheScope;
    private SQLiteDBHelper cache;

    private WeakReference<Connection> weakConnection;

    /**
     * Streams with no parent stream. the key is the id
     */
    private Map<String, Stream> rootStreams;

    /**
     * All Streams stored in the Supervisor. the key is the id
     */
    private Map<String, Stream> flatStreams;

    private Logger logger = Logger.getInstance();

    private double serverTime;
    /**
     * RTT between server and system: deltaTime = serverTime - systemTime
     */
    private double deltaTime = 0;
    private final Double millisToSeconds = 1000.0;

    /**
     * Main object to manipulate Pryv data, instanciate it with the required parameters.
     *
     * @param username
     * @param token
     * @param domain
     * @param dBinitCallback
     */
    public Connection(String username, String token, String domain, DBinitCallback dBinitCallback) {

        this.username = username;
        this.token = token;
        this.domain = domain;
        buildUrlEndpoint();
        buildRegistrationUrl();

        this.weakConnection = new WeakReference<Connection>(this);

        rootStreams = new ConcurrentHashMap<String, Stream>();
        flatStreams = new ConcurrentHashMap<String, Stream>();

        if (isApiActive) {
            api = new OnlineEventsAndStreamsManager(urlEndpoint, token, this.weakConnection);
        }

        if (isCacheActive) {
            String cacheFolder = "cache/" + generateCacheFolderName() + "/";
            new File(cacheFolder).mkdirs();
            cache = new SQLiteDBHelper(cacheFolder, weakConnection, dBinitCallback);
        }

        this.accesses = new ConnectionAccesses();
        this.account = new ConnectionAccount();
        this.events = new ConnectionEvents(api, cacheScope, cache);
        this.profile = new ConnectionProfile();
        this.streams = new ConnectionStreams(api, cacheScope, cache);
    }

    /**
     * activate API calls
     */
    public void activateApi() {
        this.isApiActive = true;
    }

    /**
     * disable usage of API calls
     */
    public void deactivateApi() {
        this.isApiActive = false;
    }

    /**
     * activate usage of local cache
     */
    public void activateCache() {
        this.isCacheActive = true;
    }

    /**
     * deactive local cache
     */
    public void deactivateCache() {
        this.isCacheActive = false;
    }

    /**
     * Assign a scope to the cache. This defines the scope of the data stored in the local cache.
     * Activates the cache.
     *
     * @param scope
     */
    public void setupCacheScope(Filter scope) {
        this.cacheScope = scope;
        activateCache();
    };

    private String buildUrlEndpoint() {
        this.urlEndpoint = "https://" + username + "." + domain;
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
     * Returns a DateTime object representing the time in the system reference.
     *
     * @param time
     *          the time in the server reference
     * @return
     */
    public DateTime serverTimeInSystemDate(double time) {
        return new DateTime(System.currentTimeMillis() / millisToSeconds + deltaTime);
    }

    /**
     * calculates the difference between server and system time: deltaTime =
     * serverTime - systemTime
     *
     * @param pServerTime
     */
    private void computeDelta(Double pServerTime) {
        if (pServerTime != null) {
            deltaTime = pServerTime - System.currentTimeMillis() / millisToSeconds;
        }
    }

    /**
     * returns the a name for the cache folder
     *
     * @return
     */
    public String generateCacheFolderName() {
        return DigestUtils.md5Hex(this.urlEndpoint + "/" + token) + "_" + username + "_"
                +  domain + "_" + token;
    }

    /**
     * returns the root Streams of the Pryv structure
     *
     * @return
     */
    public Map<String, Stream> getRootStreams() {
        return rootStreams;
    }

    /**
     * fixes Streams' children properties based on parentIds
     */
    private void recomputeRootStreamsTree() {
        rootStreams.clear();

        String parentId = null;
        // set root streams
        for (Stream potentialRootStream : flatStreams.values()) {
            // clear children fields
            potentialRootStream.clearChildren();
            parentId = potentialRootStream.getParentId();
            if (parentId == null) {
                logger.log("StreamsSupervisor: adding rootStream: id="
                        + potentialRootStream.getId()
                        + ", name="
                        + potentialRootStream.getName());
                rootStreams.put(potentialRootStream.getId(), potentialRootStream);
            }
        }

        // assign children
        for (Stream childStream : flatStreams.values()) {
            parentId = childStream.getParentId();
            if (parentId != null) {
                if (flatStreams.containsKey(parentId)) {
                    logger.log("StreamsSupervisor: adding childStream: id="
                            + childStream.getId()
                            + ", name="
                            + childStream.getName()
                            + " to "
                            + parentId);
                    Stream parent = flatStreams.get(parentId);
                    if (parent != null) {
                        parent.addChildStream(childStream);
                    }
                }
            }
        }
    }

}
