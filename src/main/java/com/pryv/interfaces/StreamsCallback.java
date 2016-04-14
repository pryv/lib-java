package com.pryv.interfaces;

import com.pryv.api.model.Stream;

/**
 *
 * callback interface for Streams management methods
 *
 * @author ik
 *
 */
public interface StreamsCallback {

  /**
   * callback method called when create(), update() or delete()
   * execution is successful.
   *
   * @param successMessage the success message
   * @param stream          updated or created stream, returned after an update/creation is
   *                       executed on the stream.
   * @param serverTime     the time of the server in seconds
   */

  void onSuccess(String successMessage, Stream stream, Double serverTime);


  /**
   * callback method called when an error occurs during createstream(),
   * updatestream() or deletestream() execution.
   *
   * @param errorMessage the error message
   * @param serverTime   the time of the server in seconds
   */

  void onError(String errorMessage, Double serverTime);

}
