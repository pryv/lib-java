package com.pryv.interfaces;


import com.pryv.api.model.Stream;

import java.util.Map;

public interface GetStreamsCallback {

    /**
     * Used for cache callback
     *
     * @param streams
     * @param deletedStreams
     */
    void partialCallback(Map<String, Stream> streams, Map<String, Stream> deletedStreams);

    /**
     * used for api callback
     *
     * @param streams
     * @param serverTime
     */
    void doneCallback(Map<String, Stream> streams, Double serverTime);

    /**
     * when there is an error
     *
     * @param errorMessage
     * @param serverTime
     */
    void onError(String errorMessage, Double serverTime);
}
