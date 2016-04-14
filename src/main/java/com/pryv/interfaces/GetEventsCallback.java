package com.pryv.interfaces;

import com.pryv.api.model.Event;

import java.util.List;
import java.util.Map;

public interface GetEventsCallback {

    /**
     * Used for cache callback
     *
     * @param events
     * @param deletedEvents
     */
    void cacheCallback(List<Event> events, Map<String, Event> deletedEvents);

    /**
     * Callback used to indicate an error on cache operation
     *
     * @param errorMessage
     */
    void onCacheError(String errorMessage);

    /**
     * used for api callback
     *
     * @param events
     * @param serverTime
     */
    void apiCallback(List<Event> events, Double serverTime);

    /**
     * when there is an error
     *
     * @param errorMessage
     * @param serverTime
     */
    void onApiError(String errorMessage, Double serverTime);
}