package com.pryv.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pryv.api.model.Attachment;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;
import com.rits.cloning.Cloner;

/**
 *
 * utilitary used to update java objects' parameters from a json
 *
 * @author ik
 *
 */
public class JsonConverter {

  private static ObjectMapper jsonMapper = new ObjectMapper();
  private static Cloner cloner = new Cloner();
  private static Logger logger = Logger.getInstance();

  private final static String EVENTS_KEY = "events";
  private final static String STREAMS_KEY = "streams";

  public static String toJson(Object source) throws JsonProcessingException {
    return jsonMapper.writeValueAsString(source);
  }

  public static JsonNode toJsonNode(String source) throws JsonProcessingException, IOException {
    return jsonMapper.readTree(source);
  }

  public static Map<String, Event> createEventsFromJson(String jsonEventsArray)
    throws JsonParseException, JsonMappingException, IOException {

    JsonNode arrNode = toJsonNode(jsonEventsArray).get(EVENTS_KEY);

    Map<String, Event> newEvents = new HashMap<String, Event>();

    if (arrNode.isArray()) {
      for (final JsonNode objNode : arrNode) {
        Event eventToAdd = new Event();
        resetEventFromJson(objNode.toString(), eventToAdd);
        newEvents.put(eventToAdd.getId(), eventToAdd);
        // logger.log("JsonConverter: event created: id = "
        // + eventToAdd.getId()
        // + ", streamId = "
        // + eventToAdd.getStreamId());
      }
    }
    return newEvents;
  }

  public static Map<String, Stream> createStreamsFromJson(String jsonStreamsArray)
    throws IOException {
    JsonNode arrNode = toJsonNode(jsonStreamsArray).get(STREAMS_KEY);
    Map<String, Stream> newStreams = new HashMap<String, Stream>();
    if (arrNode.isArray()) {
      logger.log("JsonConverter: number of received streams: " + arrNode.size());
      for (final JsonNode objNode : arrNode) {
        // resetStreamFromJson(objNode.toString(), streamToAdd);
        String str = objNode.toString();
        logger.log("JsonConverter: deserializing stream: " + str);
        Stream streamToAdd = jsonMapper.readValue(str, Stream.class);
        newStreams.put(streamToAdd.getId(), streamToAdd);
        logger.log("JsonConverter: stream created: id = " + streamToAdd.getId());
      }
    }

    return newStreams;
  }

  public static void updateAttachmentFromJson(String json, Attachment toUpdate)
    throws JsonParseException, JsonMappingException, IOException {
    Attachment temp = jsonMapper.readValue(json, Attachment.class);
    toUpdate.merge(temp);
  }

  /**
   * resets all fields of Event toUpdate to values from JSON glossary json
   *
   * @param json
   *          The glossary containing the values to which the Event's fields are
   *          updated.
   * @param toUpdate
   *          The Event reference whose fields are reset.
   * @throws JsonParseException
   * @throws JsonMappingException
   * @throws IOException
   */
  public static void resetEventFromJson(String json, Event toUpdate) throws JsonParseException,
    JsonMappingException, IOException {
    Event temp = jsonMapper.readValue(json, Event.class);
    toUpdate.merge(temp, cloner);
  }

  /**
   * reset all fields of Stream toUpdate to values retrieved from JSON glossary
   * json
   *
   * @param json
   * @param toUpdate
   * @throws JsonParseException
   * @throws JsonMappingException
   * @throws IOException
   */
  public static void resetStreamFromJson(String json, Stream toUpdate) throws JsonParseException,
    JsonMappingException, IOException {
    Stream temp = jsonMapper.readValue(json, Stream.class);
    toUpdate.merge(temp, cloner);
  }

  /**
   * Deserializes an array of Attachments into a Set of attachments
   *
   * @param jsonAttachments
   *          JSON array of attachments in JSON glossary format
   *
   * @throws JsonParseException
   * @throws JsonMappingException
   * @throws IOException
   */
  public static Set<Attachment> deserializeAttachments(String jsonAttachments)
    throws JsonParseException, JsonMappingException, IOException {
    if (jsonAttachments != null) {
      logger.log("JsonConverter: deserializing JSON attachments: \'" + jsonAttachments + "\'");
      return jsonMapper.readValue(jsonAttachments, new TypeReference<Set<Attachment>>() {
      });
    } else {
      return null;
    }
  }

  public static Cloner getCloner() {
    return cloner;
  }

}
