package com.pryv.backup;

import java.util.HashMap;
import java.util.Map;

import com.pryv.Connection;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;

/**
 * Class used to save and extract Pryv data on local file system
 *
 * @author ik
 *
 */
public class PryvBackup {

  private Connection connection;

  /**
   * Basic constructor, requires a connection object to access the data stored
   * on remote Pryv storage.
   *
   * @param connection
   */
  public PryvBackup(Connection connection) {
    this.connection = connection;
  }

  /**
   * Fetches the streams from remote Pryv storage and stores them on local file
   * system.
   *
   * @param callback
   */
  public void saveStreams(BackupCallback callback) {
    // TODO
  }

  /**
   * Extracts the streams from local file system.
   *
   * @param callback
   *
   * @return
   */
  public Map<String, Stream> loadStreams(BackupCallback callback) {
    // TODO
    return new HashMap<String, Stream>();
  }

  /**
   * Fetches the events from remote Pryv storage and stores them on local file
   * system.
   *
   * @param callback
   */
  public void saveEvents(BackupCallback callback) {
    // TODO
  }

  /**
   * Extracts the events from local file system.
   *
   * @param callback
   *
   * @return
   */
  public Map<String, Event> loadEvents(BackupCallback callback) {
    // TODO
    return new HashMap<String, Event>();
  }

  /**
   * Fetches the events from remote Pryv storage and stores them on local file
   * system.
   *
   * @param callback
   */
  public void saveAll(BackupCallback callback) {
    // TODO
  }

  /**
   * clears locally backed up data
   *
   * @param callback
   */
  public void clearBackup(BackupCallback callback) {
    // TODO
  }

}
