package com.pryv.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.pryv.api.model.Attachment;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;
import com.pryv.utils.JsonConverter;

/**
 * Class used to test JsoncConverter methods
 *
 * @author ik
 *
 */
public class JsonConverterTest {

  @Test
  public void testCreateStreamsTreeFromJson() {
    try {
      Map<String, Stream> streams = JsonConverter.createStreamsTreeFromJson(jsonStreams);
      assertEquals(streams.size(), 2);
      assertNotNull(streams.get("parent1"));
      assertEquals(streams.get("parent1").getCreated(), new Double(1369009268.827185));
      assertEquals(streams.get("parent1").getCreatedBy(), "ci9wp5fxf0000tcjx5wndexgb");
      assertEquals(streams.get("parent1").getModified(), new Double(1369009407.2173114));
      assertEquals(streams.get("parent1").getModifiedBy(), "ci9wp5fxf0000tcjx5wndexgb");
      assertEquals(streams.get("parent1").getChildren().size(), 2);
      assertNotNull(streams.get("parent1").getChildrenMap().get("child1"));
      assertNotNull(streams.get("parent1").getChildrenMap().get("child2"));
      assertNotNull(streams.get("parent2"));
      assertEquals(streams.get("parent2").getCreated(), new Double(1369009268.827185));
      assertEquals(streams.get("parent2").getCreatedBy(), "ci9wp5fxf0000tcjx5wndexgb");
      assertEquals(streams.get("parent2").getModified(), new Double(1369009333.1197622));
      assertEquals(streams.get("parent2").getModifiedBy(), "ci9wp5fxf0000tcjx5wndexgb");
      assertNull(streams.get("parent2").getChildren());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testMarshallStream() {
    Stream parent = new Stream(null, null);
    Stream child1 = new Stream(null, null);
    Stream child2 = new Stream(null, null);
    parent.addChildStream(child1);
    parent.addChildStream(child2);
    try {
      System.out.println("marshall stream result: " + JsonConverter.toJson(parent));
    } catch (JsonProcessingException e) {
      fail("error in marshalling");
      e.printStackTrace();
    }
  }

  @Test
  public void testUnmarshallStream() {
    File file =
      new File(getClass().getClassLoader().getResource("resources/testStream.json").getPath());
    try {
      byte[] encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
      String jsonStream = new String(encoded, StandardCharsets.UTF_8);
      Stream parsedStream = JsonConverter.retrieveStreamFromJson(jsonStream);
      System.out.println(parsedStream);
      assertEquals(parsedStream.getId(), "sport");
      assertEquals(parsedStream.getName(), "Sport");
      assertNull(parsedStream.getParentId());
      assertEquals(parsedStream.getCreated(), new Double(1369009252.78));
      assertEquals(parsedStream.getCreatedBy(), "ci9wp5fxf0000tcjx5wndexgb");
      assertEquals(parsedStream.getModified(), new Double(1369009580.458333));
      assertEquals(parsedStream.getModifiedBy(), "ci9wp5fxf0000tcjx5wndexgb");
      assertNotNull(parsedStream.getChildren());
      assertEquals(parsedStream.getChildren().size(), 2);
      for (Stream childStream : parsedStream.getChildren()) {
        if (childStream.getId().equals("jogging")) {
          assertEquals(childStream.getName(), "Jogging");
          assertEquals(childStream.getParentId(), "sport");
          assertEquals(childStream.getCreated(), new Double(1369009263.7133985));
          assertEquals(childStream.getCreatedBy(), "ci9wp5fxf0000tcjx5wndexgb");
          assertEquals(childStream.getModified(), new Double(1369009511.6225054));
          assertEquals(childStream.getModifiedBy(), "ci9wp5fxf0000tcjx5wndexgb");
          assertNull(childStream.getChildren());
        } else if (childStream.getId().equals("bicycling")) {
          assertEquals(childStream.getName(), "Bicycling");
          assertEquals(childStream.getParentId(), "sport");
          assertEquals(childStream.getCreated(), new Double(1369009263.7133985));
          assertEquals(childStream.getCreatedBy(), "ci9wp5fxf0000tcjx5wndexgb");
          assertEquals(childStream.getModified(), new Double(1369009523.911155));
          assertEquals(childStream.getModifiedBy(), "ci9wp5fxf0000tcjx5wndexgb");
          assertNull(childStream.getChildren());
        }
      }
    } catch (JsonParseException e) {
      e.printStackTrace();
    } catch (JsonMappingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testMarshallEvent() {
    Event testEvent = new Event();
    String id = "testId";
    String streamId = "testStream";
    String content = "";
    double created = 100.0;
    String testerId = "testCreator";
    String description = "this is the description";
    double duration = 100.0;
    double modified = 200.0;
    double time = 300.0;
    String type = "note/txt";
    testEvent.setId(id);
    Map<String, Object> clientData = new HashMap<String, Object>();
    clientData.put("color", "blue");
    clientData.put("height", 100);
    testEvent.setClientData(clientData);
    testEvent.setContent(content);
    testEvent.setCreated(created);
    testEvent.setCreatedBy(testerId);
    testEvent.setDescription(description);
    testEvent.setDuration(duration);
    testEvent.setModified(modified);
    testEvent.setModifiedBy(testerId);
    Set<String> refs = new HashSet<String>();
    refs.add("ref1");
    refs.add("ref2");
    testEvent.setReferences(refs);
    testEvent.setStreamId(streamId);
    Set<String> tags = new HashSet<String>();
    tags.add("tag1");
    tags.add("tag2");
    testEvent.setTags(tags);
    testEvent.setTime(time);
    testEvent.setTrashed(false);
    testEvent.setType(type);
    try {
      System.out.println("Marshall event result: " + JsonConverter.toJson(testEvent));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testUnmarshallEvent() {
    File file =
      new File(getClass().getClassLoader().getResource("resources/testEvent.json").getPath());
    try {
      byte[] encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
      String jsonEvent = new String(encoded, Charset.forName("UTF-8"));
      Event parsedEvent = JsonConverter.retrieveEventFromJson(jsonEvent);
      assertEquals(parsedEvent.getId(), "cib83213g002f1nmw1kspsrzn");
      assertEquals(parsedEvent.getStreamId(), "onlineModuleStreamID");
      assertEquals(parsedEvent.getType(), "picture/attached");
      assertEquals(parsedEvent.getDescription(), "This is a test event with an image.");
      assertEquals(parsedEvent.isTrashed(), false);
      assertEquals(parsedEvent.getTime(), new Double(1434988422.412));
      assertEquals(parsedEvent.getTags().size(), 0);
      assertEquals(parsedEvent.getCreated(), new Double(1434988422.412));
      assertEquals(parsedEvent.getCreatedBy(), "ci2ewnv820444t5w4ei2m6m0s");
      assertEquals(parsedEvent.getModified(), new Double(1434988422.412));
      assertEquals(parsedEvent.getModifiedBy(), "ci2ewnv820444t5w4ei2m6m0s");
      assertEquals(parsedEvent.getAttachments().size(), 1);
      Attachment attachment = parsedEvent.getFirstAttachment();
      assertEquals(attachment.getId(), "cib83213j002g1nmwr9kjn7ns");
      assertEquals(attachment.getFileName(), "photo.PNG");
      assertEquals(attachment.getType(), "application/octet-stream");
      assertEquals(attachment.getSize(), 265767);
      assertEquals(attachment.getReadToken(),
        "ci2ewnv820444t5w4ei2m6m0s-i1875pVtiZ7ZWbiaT3Ns4TmpiBM");
    } catch (JsonParseException e) {
      e.printStackTrace();
    } catch (JsonMappingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private String jsonStreams =
    "{"
      + "\"streams\": [{\"id\": \"parent1\",\"children\": [{\"id\": \"child1\"},{\"id\": \"child2\"}],"
        + "\"created\": 1369009268.827185,\"createdBy\": \"ci9wp5fxf0000tcjx5wndexgb\",\"modified\": 1369009407.2173114,\"modifiedBy\": \"ci9wp5fxf0000tcjx5wndexgb\""
        + "},{\"id\": \"parent2\",\"created\": 1369009268.827185,\"createdBy\": \"ci9wp5fxf0000tcjx5wndexgb\","
        + "\"modified\": 1369009333.1197622,\"modifiedBy\": \"ci9wp5fxf0000tcjx5wndexgb\"}]}";

}
