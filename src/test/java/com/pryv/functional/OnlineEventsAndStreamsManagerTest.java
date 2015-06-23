package com.pryv.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import resources.TestCredentials;

import com.jayway.awaitility.Awaitility;
import com.pryv.Pryv;
import com.pryv.api.EventsCallback;
import com.pryv.api.Filter;
import com.pryv.api.OnlineEventsAndStreamsManager;
import com.pryv.api.StreamsCallback;
import com.pryv.api.model.Attachment;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;

/**
 * Test of Online module methods
 *
 * @author ik
 *
 */
public class OnlineEventsAndStreamsManagerTest {

  private static OnlineEventsAndStreamsManager online;

  private static EventsCallback eventsCallback;
  private static StreamsCallback streamsCallback;

  private static Map<String, Event> events;
  private static Map<String, Stream> streams;

  private static Stream testSupportStream;

  private static Event createdEvent;
  private static Stream createdStream;

  private static boolean success = false;
  private static boolean error = false;

  @BeforeClass
  public static void setUp() throws Exception {
    Pryv.setStaging();

    instanciateEventsCallback();
    instanciateStreamsCallback();

    String url = "https://" + TestCredentials.USERNAME + "." + Pryv.API_DOMAIN + "/";
    online = new OnlineEventsAndStreamsManager(url, TestCredentials.TOKEN, null);

    testSupportStream = new Stream();
    testSupportStream.setId("onlineModuleStreamID");
    testSupportStream.setName("javaLibTestSupportStream");
    online.createStream(testSupportStream, streamsCallback);
    Awaitility.await().until(hasResult());
    assertFalse(error);
    testSupportStream.merge(createdStream, false);
    assertNotNull(testSupportStream.getId());
  }

  @AfterClass
  public static void tearDown() throws Exception {
    online.deleteStream(testSupportStream, false, streamsCallback);
    Awaitility.await().until(hasResult());
    success = false;
    online.deleteStream(testSupportStream, false, streamsCallback);
    Awaitility.await().until(hasResult());
  }

  @Before
  public void beforeEachTest() throws Exception {
    streams = null;
    events = null;
    success = false;
    error = false;
    createdEvent = null;
    createdStream = null;
  }

  @Test
  public void testFetchStreams() {
    online.getStreams(null, streamsCallback);
    Awaitility.await().until(hasResult());
    assertNotNull(streams);
    assertTrue(streams.size() > 0);
  }

  @Test
  public void testFetchEventsWithEmptyFilter() {
    online.getEvents(new Filter(), eventsCallback);
    Awaitility.await().until(hasResult());
    assertNotNull(events);
    assertTrue(events.size() > 0);
  }

  @Test
  public void testFetchEventsForAStream() {
    // create event
    Event testEvent = new Event();
    testEvent.setStreamId(testSupportStream.getId());
    testEvent.setType("note/txt");
    testEvent.setContent("this is a test Event. Please delete");
    online.createEvent(testEvent, eventsCallback);
    Awaitility.await().until(hasResult());
    assertFalse(error);
    success = false;

    // create filter
    Set<String> streamIds = new HashSet<String>();
    streamIds.add(testSupportStream.getId());
    Filter filter = new Filter();
    filter.setStreamIds(streamIds);

    // fetch events
    online.getEvents(filter, eventsCallback);
    Awaitility.await().until(hasResult());
    assertFalse(error);
    assertTrue(events.size() > 0);
    for (Event event : events.values()) {
      assertEquals(event.getStreamId(), testSupportStream.getId());
    }
  }

  @Test
  public void testCreateUpdateAndDeleteEvent() {

    // create event
    Event testEvent = new Event();
    testEvent.setStreamId(testSupportStream.getId());
    testEvent.setType("note/txt");
    testEvent.setContent("this is the content");
    online.createEvent(testEvent, eventsCallback);
    Awaitility.await().until(hasResult());
    assertFalse(error);
    success = false;
    assertNotNull(createdEvent);
    assertNotNull(createdEvent.getId());
    assertNotNull(createdEvent.getCreated());
    assertNotNull(createdEvent.getCreatedBy());
    assertNotNull(createdEvent.getModified());
    assertNotNull(createdEvent.getModifiedBy());

    // update event
    String newContent = "updated content";
    createdEvent.setContent(newContent);
    online.updateEvent(createdEvent, eventsCallback);
    Awaitility.await().until(hasResult());
    assertFalse(error);
    success = false;
    assertEquals(createdEvent.getContent(), newContent);

    // delete event
    online.deleteEvent(createdEvent, eventsCallback);
    Awaitility.await().until(hasResult());
    assertFalse(error);
    success = false;
    assertTrue(createdEvent.isTrashed());
    online.deleteEvent(createdEvent, eventsCallback);
    Awaitility.await().until(hasResult());
    assertFalse(error);
    assertNull(createdEvent);
  }

  // @Test
  public void testCreateUpdateAndDeleteStream() {
    Stream testStream = new Stream();
    testStream.setName("onlineModuleTestStream");

    // create
    online.createStream(testStream, streamsCallback);
    Awaitility.await().until(hasResult());
    assertFalse(error);
    success = false;
    assertNotNull(createdStream);
    assertNotNull(createdStream.getId());
    assertNotNull(createdStream.getCreated());
    assertNotNull(createdStream.getCreatedBy());
    assertNotNull(createdStream.getModified());
    assertNotNull(createdStream.getModifiedBy());

    // update
    String nameUpdate = "onlineModuleTestStreamNewName";
    createdStream.setName(nameUpdate);
    online.updateStream(createdStream, streamsCallback);
    Awaitility.await().until(hasResult());
    assertFalse(error);
    success = false;
    assertEquals(nameUpdate, createdStream.getName());

    // trash
    online.deleteStream(createdStream, false, streamsCallback);
    Awaitility.await().until(hasResult());
    assertFalse(error);
    success = false;
    assertNotNull(createdStream);
    assertTrue(createdStream.isTrashed());

    // delete
    online.deleteStream(createdStream, false, streamsCallback);
    Awaitility.await().until(hasResult());
    assertFalse(error);
    assertNull(createdStream);
  }

  @Test
  public void testCreateEventWithAttachment() {
    // create attachment instance
    Attachment attachment = new Attachment();
    File attachmentFile =
      new File(getClass().getClassLoader().getResource("resources/photo.PNG").getPath());
    attachment.setFile(attachmentFile);
    assertTrue(attachment.getFile().length() > 0);
    attachment.setType("image/png");
    attachment.setFileName("testImage");

    // create encapsulating event
    Event eventWithAttachment = new Event();
    eventWithAttachment.addAttachment(attachment);
    eventWithAttachment.setStreamId(testSupportStream.getId());
    eventWithAttachment.setType("picture/attached");
    eventWithAttachment.setDescription("This is a test event with an image.");

    // create event with attachment
    online.createEventWithAttachment(eventWithAttachment, eventsCallback);
    Awaitility.await().until(hasResult());
    assertFalse(error);
    assertNotNull(createdEvent);
    assertNotNull(createdEvent.getAttachments());
    assertEquals(createdEvent.getAttachments().size(), 1);
    Attachment createdAttachment = createdEvent.getFirstAttachment();
    assertNotNull(createdAttachment.getId());
  }

  private static Callable<Boolean> hasResult() {
    return new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        return (success || error);
      }
    };
  }

  private static void instanciateEventsCallback() {
    eventsCallback = new EventsCallback() {

      @Override
      public void onEventsRetrievalSuccess(Map<String, Event> newEvents, Double serverTime) {
        System.out.println("TestEventsCallback: success with "
          + newEvents.values().size()
            + " events");
        events = newEvents;
        success = true;
      }

      @Override
      public void onEventsRetrievalError(String message, Double pServerTime) {
        error = true;
      }

      @Override
      public void onEventsSuccess(String successMessage, Event event, Integer stoppedId,
        Double pServerTime) {
        System.out.println("OnlineEventsManagerTest: eventsSuccess msg: " + successMessage);
        success = true;
        createdEvent = event;
      }

      @Override
      public void onEventsError(String errorMessage, Double pServerTime) {
        error = true;
      }
    };
  }

  private static void instanciateStreamsCallback() {
    streamsCallback = new StreamsCallback() {

      @Override
      public void onStreamsRetrievalSuccess(Map<String, Stream> newStreams, Double serverTime) {
        System.out.println("TestStreamsCallback: success for "
          + newStreams.values().size()
            + " streams");
        streams = newStreams;
        success = true;
      }

      @Override
      public void onStreamsRetrievalError(String message, Double pServerTime) {
        error = true;
      }

      @Override
      public void onStreamsSuccess(String successMessage, Stream stream, Double pServerTime) {
        System.out.println("TestStreamsCallback: success msg: " + successMessage);
        createdStream = stream;
        success = true;
      }

      @Override
      public void onStreamError(String errorMessage, Double pServerTime) {
        error = true;
      }
    };
  }

}
