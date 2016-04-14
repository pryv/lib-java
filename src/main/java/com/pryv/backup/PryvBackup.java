package com.pryv.backup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.pryv.Connection;
import com.pryv.model.Event;
import com.pryv.model.Stream;

/**
 * Class used to save and extract Pryv data on local file system
 *
 * @author ik
 *
 */
public class PryvBackup {

  private Connection connection;
  private CsvMapper mapper = new CsvMapper();
  private CsvSchema streamsSchema = mapper.schemaFor(Stream.class);
  private CsvSchema eventsSchema = mapper.schemaFor(Event.class);
  private String backupFolderName;

  /**
   * Basic constructor, requires a connection object to access the data stored
   * on remote Pryv storage.
   *
   * @param connection
   */
  public PryvBackup(Connection connection) {
    this.connection = connection;
    backupFolderName = "backup/" + connection.generateCacheFolderName() + "/";
    new File(backupFolderName).mkdirs();
  }

  /**
   * Fetches the streams from remote Pryv storage and stores them on local file
   * system.
   *
   * @param callback
   */
  public void saveStreams(BackupCallback callback) {
    File streamsBackup = new File(backupFolderName + "streams.csv");
    try {
      FileOutputStream streamsFos = new FileOutputStream(streamsBackup);
      for (Stream stream : connection.getRootStreams().values()) {
        mapper.writer(streamsSchema).writeValue(streamsFos, stream);
      }
    } catch (FileNotFoundException e) {
      callback.onSaveStreamsError(e.getMessage());
    } catch (JsonGenerationException e) {
      callback.onSaveStreamsError(e.getMessage());
    } catch (JsonMappingException e) {
      callback.onSaveStreamsError(e.getMessage());
    } catch (IOException e) {
      callback.onSaveStreamsError(e.getMessage());
    }
    callback.onSaveStreamsSuccess(connection.getRootStreams().size());
  }

  /**
   * Extracts the streams from local file system.
   *
   * @param callback
   *
   */
  public void loadStreams(BackupCallback callback) {
    // TODO
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
   */
  public void loadEvents(BackupCallback callback) {
    // TODO
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
    callback.onClearBackupSuccess();
  }

}
