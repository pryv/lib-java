package com.pryv.backup;

import java.util.Map;

import com.pryv.model.Event;
import com.pryv.model.Stream;

/**
 * callback methods for results of backup operations using PryvBackup.
 *
 * @author ik
 *
 */
public interface BackupCallback {

  /**
   * callback method when streams were successfully saved.
   *
   * @param savedStreamsNumber
   *          the number of saved streams
   *
   */
  void onSaveStreamsSuccess(int savedStreamsNumber);

  /**
   * callback method when streams save operation encountered an error.
   *
   * @param errorMessage
   *          the error message
   */
  void onSaveStreamsError(String errorMessage);

  /**
   * callback method when streams were loaded successfully from local file
   * system.
   *
   * @param streams
   *          the retrieved streams
   */
  void onLoadStreamsSuccess(Map<String, Stream> streams);

  /**
   * callback method when the streams loading operation encountered an error.
   *
   * @param errorMessage
   *          the error message
   */
  void onLoadStreamsError(String errorMessage);

  /**
   * callback method when events were successfully saved.
   *
   * @param savedEventsNumber
   *          the number of saved streams
   */
  void onSaveEventsSuccess(int savedEventsNumber);

  /**
   * callback method when streams save operation encountered an error.
   *
   * @param errorMessage
   *          the error message
   */
  void onSaveEventsError(String errorMessage);

  /**
   * callback method when streams were loaded successfully from local file
   * system.
   *
   * @param events
   *          the retrieved events
   */
  void onLoadEventsSucces(Map<String, Event> events);

  /**
   * callback method when the events loading operation encountered an error.
   *
   * @param errorMessage
   *          the error message
   */
  void onLoadEventsError(String errorMessage);

  /**
   * callback method when all Pryv data was saved successfully to the local file
   * system.
   *
   * @param savedStreamsNumber
   *          the number of saved streams
   * @param savedEventsNumber
   *          the number of saved events
   */
  void onSaveAllSuccess(int savedStreamsNumber, int savedEventsNumber);

  /**
   * callback method when the save all operation encountered an error
   *
   * @param errorMessage
   *          the error message
   */
  void onSaveAllError(String errorMessage);

  /**
   * callback method for when backup has been successfully cleared.
   */
  void onClearBackupSuccess();

  /**
   * callback method for when backup operation encountered an error
   *
   * @param errorMessage
   *          the error message
   */
  void onClearBackupError(String errorMessage);
}