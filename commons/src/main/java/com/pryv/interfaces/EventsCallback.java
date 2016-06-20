package com.pryv.interfaces;

import com.pryv.model.Event;

public interface EventsCallback {

    /**
     * callback method called when create(), update() or delete()
     * execution is successful on the API.
     *
     * @param successMessage the success message
     * @param event          updated or created Event, returned after an update/creation is
     *                       executed on the event.
     * @param stoppedId      Only in singleActivity streams. If set, indicates the id of the
     *                       previously running period event that was stopped as a consequence
     *                       of inserting the new event.
     * @param serverTime     the time of the server in seconds
     */
    void onApiSuccess(String successMessage, Event event, String stoppedId, Double serverTime);


    /**
     * callback method called when an error occurs during create(),
     * update() or delete() execution on the API.
     *
     * @param errorMessage the error message
     * @param serverTime   the time of the server in seconds
     */
    void onApiError(String errorMessage, Double serverTime);


    /**
     * callback method called when create(), update() or delete()
     * execution is successful on the cache.
     *
     * @param successMessage the success message
     * @param event          updated or created Event, returned after an update/creation is
     *                       executed on the event.
     */
    void onCacheSuccess(String successMessage, Event event);

    /**
     * callback method called when an error occurs during create(),
     * update() or delete() execution on the cache.
     *
     * @param errorMessage the error message
     */
    void onCacheError(String errorMessage);

}

