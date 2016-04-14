/*
package com.pryv.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import resources.TestCredentials;

import com.jayway.awaitility.Awaitility;
import com.pryv.ConnectionOld;
import com.pryv.Pryv;
import com.pryv.interfaces.StreamsCallback;
import com.pryv.database.DBinitCallback;
import Event;
import Stream;
import com.pryv.backup.BackupCallback;
import com.pryv.backup.PryvBackup;

*/
/**
 * This class contains test that are meant to direct the implementation of a
 * local backup functionality
 *
 * @author ik
 *
 *//*

public class LocalBackupTest {

  private static ConnectionOld connection;
  private static StreamsCallback streamsCallback;
  private static boolean streamsOperationDone = false;
  private static boolean streamsSuccess = false;
  private static Map<String, Stream> streams = new HashMap<String, Stream>();
  private static Map<Stream, Event> events;
  private static Stream testStream1;
  private static Stream testStream2;

  private static PryvBackup backupTool;
  private static BackupCallback backupCallback;
  private static boolean backupOperationDone = false;
  private static boolean backupSuccess = false;
  private static int backupNumber = 0;

  @BeforeClass
  public static void setUpBeforeAllTests() throws Exception {
    connection =
      new ConnectionOld(TestCredentials.USERNAME, TestCredentials.TOKEN, new DBinitCallback());
    // to simplify things
    Pryv.deactivateCache();
    Pryv.deactivateOnline();
    instanciateStreamsCallback();
    instanciateBackupCallback();
    backupTool = new PryvBackup(connection);
  }

  @AfterClass
  public static void tearDownAfterAllTests() throws Exception {
  }

  @Before
  public void cleanUpBeforeEachTest() throws Exception {
    deleteStreams();
    backupSuccess = false;
    backupOperationDone = false;
    backupTool.clearBackup(backupCallback);
    Awaitility.await().until(isTrue(backupOperationDone));
    backupOperationDone = false;
  }

  //@Test
  public void testSaveStreamsSuccessfully() {
    createStreams();
    for (Stream stream : connection.getRootStreams().values()) {
      System.out.println("created " + stream.getId() + ", " + stream.getName());
    }
    backupTool.saveStreams(backupCallback);
    Awaitility.await().until(isTrue(backupOperationDone));
    assertTrue(backupSuccess);
    assertEquals(backupNumber, 2);
  }

  //@Test
  public void testSaveStreamsWithError() {
    createStreams();
    backupTool.saveStreams(backupCallback);
    Awaitility.await().until(isTrue(backupOperationDone));
    assertFalse(backupSuccess);
  }

  //@Test
  public void testLoadStreamsSuccessfully() {
    createStreams();
    backupTool.saveStreams(backupCallback);
    Awaitility.await().until(isTrue(backupOperationDone));
    backupOperationDone = false;
    assertEquals(backupNumber, 2);
    deleteStreams();
    backupTool.loadStreams(backupCallback);
    Awaitility.await().until(isTrue(backupOperationDone));
    assertEquals(streams.size(), 2);
    assertTrue(streams.keySet().contains(testStream1.getId()));
    assertTrue(streams.keySet().contains(testStream2.getId()));
  }

  //@Test
  public void testLoadStreamsWithError() {
    backupTool.loadStreams(backupCallback);
    Awaitility.await().until(isTrue(backupOperationDone));
    assertFalse(backupSuccess);
  }

  @Test
  public void testSaveEventsSuccessfully() {
    // TODO
  }

  @Test
  public void testSaveEventsWithError() {
    // TODO
  }

  @Test
  public void testLoadEventsSuccessfully() {
    // TODO
  }

  @Test
  public void testLoadEventsWithError() {
    // TODO
  }

  @Test
  public void testSaveAllSuccessfully() {
    // TODO
  }

  @Test
  public void testSaveAllWithError() {
 // TODO
  }

  @Test
  public void testClearBackupSuccessfully() {
 // TODO
  }

  @Test
  public void testClearBackupWithError() {
    // TODO
  }

  private static void createStreams() {
    testStream1 = new Stream("testBackupStreamId1", "testBackupStreamName1");
    testStream2 = new Stream("testBackupStreamId2", "testBackupStreamName2");
    connection.createStream(testStream1, streamsCallback);
    Awaitility.await().until(hasCreatedStream(testStream1.getId()));
    connection.createStream(testStream2, streamsCallback);
    Awaitility.await().until(hasCreatedStream(testStream2.getId()));
  }

  private static void deleteStreams() {
    connection.getStreams(null, streamsCallback);
    Awaitility.await().until(isTrue(streamsOperationDone));
    streamsOperationDone = false;
    for (Stream stream : streams.values()) {
      connection.deleteStream(stream, false, streamsCallback);
      Awaitility.await().until(isTrue(streamsOperationDone));
      streamsOperationDone = false;
    }
  }

  private static void instanciateBackupCallback() {
    backupCallback = new BackupCallback() {

      @Override
      public void onSaveStreamsSuccess(int savedStreamsNumber) {
        backupNumber = savedStreamsNumber;
        backupSuccess = true;
        backupOperationDone = true;
      }

      @Override
      public void onSaveStreamsError(String errorMessage) {
        backupSuccess = false;
        backupOperationDone = true;
      }

      @Override
      public void onSaveEventsSuccess(int savedEventsNumber) {
        backupOperationDone = true;
      }

      @Override
      public void onSaveEventsError(String errorMessage) {
        backupSuccess = false;
        backupOperationDone = true;
      }

      @Override
      public void onSaveAllSuccess(int savedStreamsNumber, int savedEventsNumber) {
        backupSuccess = true;
        backupOperationDone = true;
      }

      @Override
      public void onLoadStreamsSuccess(Map<String, Stream> streams) {
        streams.putAll(streams);
        backupSuccess = true;
        backupOperationDone = true;
      }

      @Override
      public void onLoadStreamsError(String errorMessage) {
        backupSuccess = false;
        backupOperationDone = true;
      }

      @Override
      public void onLoadEventsSucces(Map<String, Event> events) {
        backupSuccess = true;
        backupOperationDone = true;
      }

      @Override
      public void onLoadEventsError(String errorMessage) {
        backupSuccess = false;
        backupOperationDone = true;
      }

      @Override
      public void onClearBackupSuccess() {
        backupSuccess = true;
        backupOperationDone = true;
      }

      @Override
      public void onClearBackupError(String errorMessage) {
        backupSuccess = false;
        backupOperationDone = true;
      }

      @Override
      public void onSaveAllError(String errorMessage) {
        backupSuccess = false;
        backupOperationDone = true;
      }

    };
  }

  private static Callable<Boolean> hasCreatedStream(final String streamId) {
    return new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        return streams.containsKey(streamId);
      }

    };
  }

  private static Callable<Boolean> isTrue(final boolean testedVariable) {
    return new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        return testedVariable;
      }

    };
  }

  private static void instanciateStreamsCallback() {
    streamsCallback = new StreamsCallback() {

      @Override
      public void onStreamsRetrievalSuccess(Map<String, Stream> onlineStreams, Double serverTime) {
        streams.putAll(onlineStreams);
        streamsSuccess = true;
        streamsOperationDone = true;
      }

      @Override
      public void onStreamsRetrievalError(String message, Double pServerTime) {
        streamsSuccess = false;
        streamsOperationDone = true;
      }

      @Override
      public void onStreamsSuccess(String successMessage, Stream stream, Double pServerTime) {
        if (stream != null) {
          streams.put(stream.getId(), stream);
        }
        streamsSuccess = true;
        streamsOperationDone = true;
      }

      @Override
      public void onStreamError(String errorMessage, Double pServerTime) {
        streamsSuccess = false;
        streamsOperationDone = true;
      }

    };
  }

}
*/
