package com.pryv.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashSet;
import java.util.List;
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
import com.pryv.interfaces.EventsCallback;
import com.pryv.Filter;
import com.pryv.api.OnlineEventsAndStreamsManager;
import com.pryv.interfaces.GetEventsCallback;
import com.pryv.interfaces.GetStreamsCallback;
import com.pryv.interfaces.StreamsCallback;
import com.pryv.model.Attachment;
import com.pryv.model.Event;
import com.pryv.model.Stream;

/**
 * Test of Online module methods
 *
 * @author ik
 *
 */
public class OnlineEventsAndStreamsManagerTest {

  private static OnlineEventsAndStreamsManager online;

    private static EventsCallback eventsCallback;
    private static GetEventsCallback getEventsCallback;
    private static StreamsCallback streamsCallback;
    private static GetStreamsCallback getStreamsCallback;

    private static List<Event> events;
    private static List<Event> partialEvents;
    private static Map<String, Stream> streams;
    private static Map<String, Stream> partialStreams;

    private static Stream testSupportStream;

    private static String stoppedId;

    private static Event singleEvent;
    private static Stream singleStream;

    private static boolean partialSuccess = false;
    private static boolean partialError = false;
    private static boolean success = false;
    private static boolean error = false;

  @BeforeClass
  public static void setUp() throws Exception {

    instanciateEventsCallback();
    instanciateStreamsCallback();

    Pryv.setDomain("pryv.li");
    String url = "https://" + TestCredentials.USERNAME + "." + Pryv.DOMAIN + "/";

    online = new OnlineEventsAndStreamsManager(url, TestCredentials.TOKEN, null);

    testSupportStream = new Stream("onlineModuleStreamID", "javaLibTestSupportStream");
    online.createStream(testSupportStream, streamsCallback);
    Awaitility.await().until(hasResult());
    assertFalse(error);
    testSupportStream.merge(singleStream, false);
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
    singleEvent = null;
    singleStream = null;
  }

  @Test
  public void testFetchStreams() {
    online.getStreams(null, getStreamsCallback);
    Awaitility.await().until(hasResult());
    assertNotNull(streams);
    assertTrue(streams.size() > 0);
  }

  @Test
  public void testFetchEventsWithEmptyFilter() {
    online.getEvents(new Filter(), getEventsCallback);
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
      filter.addStream(testSupportStream);

    // fetch events
    online.getEvents(filter, getEventsCallback);
    Awaitility.await().until(hasResult());
    assertFalse(error);
    assertTrue(events.size() > 0);
    for (Event event : events) {
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
    assertNotNull(singleEvent);
    assertNotNull(singleEvent.getId());
    assertNotNull(singleEvent.getCreated());
    assertNotNull(singleEvent.getCreatedBy());
    assertNotNull(singleEvent.getModified());
    assertNotNull(singleEvent.getModifiedBy());

    // update event
    String newContent = "updated content";
    singleEvent.setContent(newContent);
    online.updateEvent(singleEvent, eventsCallback);
    Awaitility.await().until(hasResult());
    assertFalse(error);
    success = false;
    assertEquals(singleEvent.getContent(), newContent);

    // delete event
    online.deleteEvent(singleEvent, eventsCallback);
    Awaitility.await().until(hasResult());
    assertFalse(error);
    success = false;
    assertTrue(singleEvent.isTrashed());
    online.deleteEvent(singleEvent, eventsCallback);
    Awaitility.await().until(hasResult());
    assertFalse(error);
    assertNull(singleEvent);
  }

  @Test
  public void testCreateUpdateAndDeleteStream() {
    Stream testStream = new Stream(null, "onlineModuleTestStream");

    // create
    online.createStream(testStream, streamsCallback);
    Awaitility.await().until(hasResult());
    assertFalse(error);
    success = false;
    assertNotNull(singleStream);
    assertNotNull(singleStream.getId());
    assertNotNull(singleStream.getCreated());
    assertNotNull(singleStream.getCreatedBy());
    assertNotNull(singleStream.getModified());
    assertNotNull(singleStream.getModifiedBy());

    // update
    String nameUpdate = "onlineModuleTestStreamNewName";
    singleStream.setName(nameUpdate);
    online.updateStream(singleStream, streamsCallback);
    Awaitility.await().until(hasResult());
    assertFalse(error);
    success = false;
    assertEquals(nameUpdate, singleStream.getName());

    // trash
    online.deleteStream(singleStream, false, streamsCallback);
    Awaitility.await().until(hasResult());
    assertFalse(error);
    success = false;
    assertNotNull(singleStream);
    assertTrue(singleStream.isTrashed());

    // delete
    online.deleteStream(singleStream, false, streamsCallback);
    Awaitility.await().until(hasResult());
    assertFalse(error);
    assertNull(singleStream);
  }


  @Test
  public void testCreateEventWithAttachment() {
    // create attachment instance
    Attachment attachment = new Attachment();
    File attachmentFile = new File(getClass().getClassLoader().getResource("photo.PNG").getPath());
    attachment.setFile(attachmentFile);
    assertTrue(attachment.getFile().length() > 0);
    attachment.setType("image/png");
    attachment.setFileName(attachmentFile.getName());

    // create encapsulating event
    Event eventWithAttachment = new Event();
      eventWithAttachment.setStreamId(testSupportStream.getId());
    eventWithAttachment.addAttachment(attachment);
    eventWithAttachment.setStreamId(testSupportStream.getId());
    eventWithAttachment.setType("picture/attached");
    eventWithAttachment.setDescription("This is a test event with an image.");

    // create event with attachment
    online.createEventWithAttachment(eventWithAttachment, eventsCallback);
    Awaitility.await().until(hasResult());
    assertFalse(error);
    assertNotNull(singleEvent);
    assertNotNull(singleEvent.getAttachments());
    assertEquals(singleEvent.getAttachments().size(), 1);
    Attachment createdAttachment = singleEvent.getFirstAttachment();
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

    private static void instanciateGetEventsCallback() {
        getEventsCallback = new GetEventsCallback() {
            @Override
            public void cacheCallback(List<Event> events, Map<String, Double> eventDeletions) {
                fail("should not be called");
            }

            @Override
            public void onCacheError(String errorMessage) {
                fail("should not be called");
            }

            @Override
            public void apiCallback(List<Event> events, Map<String, Double> eventDeletions,
                                    Double serverTime) {

            }

            @Override
            public void onApiError(String errorMessage, Double serverTime) {

            }
        };
    }

    private static void instanciateEventsCallback() {
        eventsCallback = new EventsCallback() {

            @Override
            public void onApiSuccess(String successMessage, Event event, String pStoppedId,
                                     Double pServerTime) {
                System.out.println("OnlineEventsManagerTest: eventsSuccess msg: " + successMessage);
                stoppedId = pStoppedId;
                success = true;
                singleEvent = event;
            }

            @Override
            public void onApiError(String errorMessage, Double pServerTime) {
                error = true;
            }

            @Override
            public void onCacheSuccess(String successMessage, Event event) {
                fail("should not be called");
            }

            @Override
            public void onCacheError(String errorMessage) {
                fail("should not be called");
            }
        };
    }

    private static void instanciateStreamsCallback() {
        streamsCallback = new StreamsCallback() {

            @Override
            public void onApiSuccess(String successMessage, Stream stream, Double pServerTime) {
                System.out.println("TestStreamsCallback: success msg: " + successMessage);
                singleStream = stream;
                success = true;
            }

            @Override
            public void onApiError(String errorMessage, Double pServerTime) {
                error = true;
            }

            @Override
            public void onCacheSuccess(String successMessage, Stream stream) {
                fail("should not be called");
            }

            @Override
            public void onCacheError(String errorMessage) {
                fail("should not be called");
            }
        };
    }

    private static void instanciateGetStreamsCallback() {
        getStreamsCallback = new GetStreamsCallback() {
            @Override
            public void cacheCallback(Map<String, Stream> streams,
                                      Map<String, Double> streamDeletions) {
                fail("should not be called");
            }

            @Override
            public void onCacheError(String errorMessage) {
                fail("should not be called");
            }

            @Override
            public void apiCallback(Map<String, Stream> receivedStreams,
                                    Map<String, Double> streamDeletions, Double serverTime) {
                streams = receivedStreams;
                success = true;
            }

            @Override
            public void onApiError(String errorMessage, Double serverTime) {
                error = true;
            }
        };
    }

}
