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
    void partialCallback(List<Event> events, Map<String, Event> deletedEvents);

    /**
     * used for api callback
     *
     * @param events
     * @param serverTime
     */
    void doneCallback(List<Event> events, Double serverTime);

    /**
     * when there is an error
     *
     * @param errorMessage
     * @param serverTime
     */
    void onError(String errorMessage, Double serverTime);
}