package com.pryv.interfaces;

import com.pryv.api.model.Event;

public interface EventsCallback {

    /**
     * callback method called when create(), update() or delete()
     * execution is successful.
     *
     * @param successMessage the success message
     * @param event          updated or created Event, returned after an update/creation is
     *                       executed on the event.
     * @param stoppedId      Only in singleActivity streams. If set, indicates the id of the
     *                       previously running period event that was stopped as a consequence
     *                       of inserting the new event.
     * @param serverTime     the time of the server in seconds
     */

    void onSuccess(String successMessage, Event event, Integer stoppedId, Double serverTime);


    /**
     * callback method called when an error occurs during create(),
     * update() or delete() execution.
     *
     * @param errorMessage the error message
     * @param serverTime   the time of the server in seconds
     */

    void onError(String errorMessage, Double serverTime);

}

