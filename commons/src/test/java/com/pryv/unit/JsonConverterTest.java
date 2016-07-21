package com.pryv.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
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
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.pryv.model.Attachment;
import com.pryv.model.Event;
import com.pryv.model.Stream;
import com.pryv.utils.JsonConverter;

/**
 * Class used to test JsonConverter methods
 *
 * @author ik
 */
public class JsonConverterTest {

    private String jsonStreamTree;
    private String jsonStream;
    private String jsonEvent;

    @Before
    public void setup() {

        try {
            File streamTreeFile =
                    new File(getClass().getClassLoader().getResource("resources/streamsTree.json").getPath());
            byte[] encodedTree = Files.readAllBytes(Paths.get(streamTreeFile.getAbsolutePath()));
            jsonStreamTree = new String(encodedTree, StandardCharsets.UTF_8);
            File streamFile =
                    new File(getClass().getClassLoader().getResource("resources/testStream.json").getPath());
            byte[] encodedStream = Files.readAllBytes(Paths.get(streamFile.getAbsolutePath()));
            jsonStream = new String(encodedStream, StandardCharsets.UTF_8);
            File eventFile =
                    new File(getClass().getClassLoader().getResource("resources/testEvent.json").getPath());
            byte[] encodedEvent = Files.readAllBytes(Paths.get(eventFile.getAbsolutePath()));
            String jsonEvent = new String(encodedEvent, Charset.forName("UTF-8"));
        } catch (NullPointerException e) {
            System.err.println("Test file not found. Root folder for loading test file: " + getClass().getClassLoader().getResource("."));
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateStreamsTreeFromJson() {
        try {
            Map<String, Stream> streams = JsonConverter.createStreamsTreeFromJson(jsonStreamTree);
            assertEquals(streams.size(), 24);
            Stream activityStream = streams.get("activity");
            assertNotNull(activityStream);
            assertEquals(new Double(1409328827.942), activityStream.getCreated());
            assertEquals("chxoqauy71d5uc4zqvdekucij", activityStream.getCreatedBy());
            assertEquals(new Double(1433332913.88), activityStream.getModified());
            assertEquals("chxoppo8v1d3uc4zqhqdthudf", activityStream.getModifiedBy());
            assertEquals(6, activityStream.getChildren().size());
            assertNotNull(activityStream.getChildrenMap().get("moves-8371604354526466-cycling"));
            assertNotNull(activityStream.getChildrenMap().get("moves-8371604354526466-places"));
            assertNotNull(activityStream.getChildrenMap().get("moves-8371604354526466-running"));
            assertNotNull(activityStream.getChildrenMap().get("moves-8371604354526466-transport"));
            assertNotNull(activityStream.getChildrenMap().get("moves-8371604354526466-walking"));
            assertNotNull(activityStream.getChildrenMap().get("cij4ga8us0qd51fyqzv317cms"));
            Stream diaryStream = streams.get("diary");
            assertNotNull(diaryStream);
            assertEquals(diaryStream.getCreated(), new Double(1453454806.33));
            assertEquals(diaryStream.getCreatedBy(), "chxoqauy71d5uc4zqvdekucij");
            assertEquals(diaryStream.getModified(), new Double(1454432686.39));
            assertEquals(diaryStream.getModifiedBy(), "chxoqauy71d5uc4zqvdekucij");
            assertEquals(0, diaryStream.getChildren().size());
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
        try {
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
        testEvent.setReferences("ref1,ref2");
        testEvent.setStreamId(streamId);
        testEvent.setTags("tag1,tag2");
        testEvent.setTime(time);
        testEvent.setTrashed(false);
        testEvent.setType(type);
        try {
            System.out.println("Marshall event result: " + JsonConverter.toJson(testEvent));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    //@Test
    public void testUnmarshallEvent() {
        try {
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

}
