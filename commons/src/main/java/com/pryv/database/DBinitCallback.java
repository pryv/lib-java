package com.pryv.database;

import com.pryv.utils.Logger;

/**
 * Callback interface used to forward errors to cache module when DB init isn't
 * working.
 *
 *
 * @author ik
 *
 */
public class DBinitCallback {

  private Logger logger = Logger.getInstance();

  /**
   * database initialization error.
   *
   * @param message
   *          the error message to display
   */
  public void onError(String message) {
    logger.log("DBinitCallback: initialization error: " + message);
  }

}
