package com.pryv.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.pryv.api.model.Attachment;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;
import com.rits.cloning.Cloner;

/**
 *
 * utilitary used to do JSON conversions
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

  private final static String META = "meta";
  private final static String SERVER_TIME = "serverTime";

  /**
   * Serializes the Object parameter into JSON
   *
   * @param source
   *          the Object to serialize
   * @return
   * @throws JsonProcessingException
   */
  public static String toJson(Object source) throws JsonProcessingException {
    return jsonMapper.writeValueAsString(source);
  }

  /**
   * Converts the json in String format into a JsonNode for field-by-field
   * deserialization
   *
   * @param source
   *          the json in String format
   * @return
   * @throws JsonProcessingException
   * @throws IOException
   */
  public static JsonNode toJsonNode(String source) throws JsonProcessingException, IOException {
    return jsonMapper.readTree(source);
  }

  /**
   * Retrieves the serverTime field from a response from the API
   *
   * @param jsonResponse
   *          the jsonResponse containing a field "serverTime"
   * @return
   * @throws JsonProcessingException
   * @throws IOException
   */
  public static long retrieveServerTime(String jsonResponse) throws JsonProcessingException,
    IOException {
    long serverTime = toJsonNode(jsonResponse).get(META).get(SERVER_TIME).longValue();

    logger.log("JsonConverter: retrieved time: " + serverTime);
    return serverTime;
  }

  /**
   * Deserializes a JSON containing the field "events" into a Map<String, Event>
   * with Event id as key
   *
   * @param jsonEventsArray
   * @return
   * @throws JsonParseException
   * @throws JsonMappingException
   * @throws IOException
   */
  public static Map<String, Event> createEventsFromJson(String jsonEventsArray)
    throws JsonParseException, JsonMappingException, IOException {

    JsonNode arrNode = toJsonNode(jsonEventsArray).get(EVENTS_KEY);

    Map<String, Event> newEvents = new HashMap<String, Event>();

    if (arrNode.isArray()) {
      for (final JsonNode objNode : arrNode) {
        Event eventToAdd = new Event();
        resetEventFromJson(objNode.toString(), eventToAdd);
        newEvents.put(eventToAdd.getId(), eventToAdd);
      }
    }
    return newEvents;
  }

  /**
   * Deserializes a JSON containing the field "streams" into a Map<String,
   * Stream> with Stream id as key
   *
   * @param jsonStreamsArray
   * @return
   * @throws IOException
   */
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
        // streamToAdd.initChildMap();
        newStreams.put(streamToAdd.getId(), streamToAdd);
        logger.log("JsonConverter: stream created: id = " + streamToAdd.getId());
      }
    }

    return newStreams;
  }

  /**
   *
   *
   * @param json
   * @param toUpdate
   * @throws JsonParseException
   * @throws JsonMappingException
   * @throws IOException
   */
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
    toUpdate.merge(temp);
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
      // logger.log("JsonConverter: deserializing JSON attachments: \'" +
      // jsonAttachments + "\'");
      return jsonMapper.readValue(jsonAttachments, new TypeReference<Set<Attachment>>() {
      });
    } else {
      return null;
    }
  }

  public static Cloner getCloner() {
    return cloner;
  }

  /**
   * Custom Serializer used to properly convert Map<String, Stream> to JSON
   * array
   *
   * @author ik
   *
   */
  public class ChildrenSerializer extends JsonSerializer<Map<String, Stream>> {

    @Override
    public void
      serialize(Map<String, Stream> value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException, JsonProcessingException {

    }

  }

  /**
   * Custom Deserializer used to retrieve JSON arrays of streams into Map<id,
   * stream>
   *
   * @author ik
   *
   */
  public class ChildrenDeserializer extends StdDeserializer<Map<String, Stream>> {

    public ChildrenDeserializer(Class<?> vc) {
      super(vc);
    }

    // public ChildrenDeserializer() {
    // this(Map<String, Stream>.class);
    // }

    @Override
    public Map<String, Stream> deserialize(JsonParser jp, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
      JsonNode streamsNodeArray = jp.getCodec().readTree(jp);
      Map<String, Stream> newChildren = null;
      if (streamsNodeArray.isArray() && streamsNodeArray.size() > 0) {
        newChildren = new HashMap<String, Stream>();
        for (JsonNode streamNode : streamsNodeArray) {
          String id = streamNode.get("id").asText();
          // jsonMapper.readv
          newChildren.put(id, jsonMapper.readValue(streamNode.toString(), Stream.class));
        }
      }
      return newChildren;
    }

  }

}
