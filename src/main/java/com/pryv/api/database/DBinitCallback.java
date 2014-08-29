package com.pryv.api.database;

/**
 * Callback interface used to forward errors to cache module when DB init isn't
 * working.
 *
 *
 * @author ik
 *
 */
public interface DBinitCallback {

  /**
   * database initialization error.
   *
   * @param message
   *          the error message to display
   */
  void onError(String message);

}
