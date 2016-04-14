package com.pryv.interfaces;


import com.pryv.model.Stream;

import java.util.Map;

public interface GetStreamsCallback {

    /**
     * Used for cache callback
     *
     * @param streams root streams
     * @param deletedStreams
     */
    void cacheCallback(Map<String, Stream> streams, Map<String, Stream> deletedStreams);

    /**
     * Callback used to indicate an error on cache operation
     *
     * @param errorMessage
     */
    void onCacheError(String errorMessage);

    /**
     * used for api callback
     *
     * @param streams root streams
     * @param serverTime
     */
    void apiCallback(Map<String, Stream> streams, Double serverTime);

    /**
     * when there is an error
     *
     * @param errorMessage
     * @param serverTime
     */
    void onApiError(String errorMessage, Double serverTime);
}
