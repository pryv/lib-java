package com.pryv.interfaces;


import com.pryv.model.Event;
import com.pryv.model.Stream;

import java.util.List;
import java.util.Map;

public interface UpdateCacheCallback {

    /**
     * Used by the cache to update itself
     *
     * @param events
     * @param eventDeletions
     * @param streams
     * @param streamDeletions
     * @param serverTime
     */
    void apiCallback(List<Event> events, Map<String, Double> eventDeletions,
                     Map<String, Stream> streams, Map<String, Double> streamDeletions,
                     Double serverTime);

    /**
     * error callback called when an error occurs.
     *
     * @param errorMessage
     * @param serverTime
     */
    void onError(String errorMessage, Double serverTime);
}
