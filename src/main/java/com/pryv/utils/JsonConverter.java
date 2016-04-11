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
 * Utilitary class used to do JSON conversions
 *
 * @author ik
 *
 */
public class JsonConverter {

  private static ObjectMapper jsonMapper = new ObjectMapper();
  private static Cloner cloner = new Cloner();
  private static Logger logger = Logger.getInstance();

  private final static String EVENT_KEY = "event";
  private final static String EVENTS_KEY = "events";
  private final static String STREAM_KEY = "stream";
  private final static String STREAMS_KEY = "streams";

  private final static String STOPPED_ID_KEY = "stoppedId";

  private final static String META_KEY = "meta";
  private final static String SERVER_TIME_KEY = "serverTime";

  private final static String STREAM_DELETION_KEY = "streamDeletion";
  private final static String EVENT_DELETION_KEY = "eventDeletion";
  private final static String ID_KEY = "id";

  /**
   * Deserialize JSON into an object
   *
   * @param jsonSource
   *          the object in the form of a JSON dictionary stored in a String.
   * @param type
   *          the class into which the JSON will be deserialized.
   * @return
   * @throws JsonParseException
   * @throws JsonMappingException
   * @throws IOException
   */
  public static <T> Object fromJson(String jsonSource, Class<T> type) throws JsonParseException,
    JsonMappingException, IOException {
    return jsonMapper.readValue(jsonSource, type);
  }

  /**
   * Serializes the Object parameter into JSON
   *
   * @param source
   *          the Object to serialize
   * @return the Object's parameters in JSON format stored in a String
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
  public static double retrieveServerTime(String jsonResponse) throws JsonProcessingException,
    IOException {
    double serverTime = toJsonNode(jsonResponse).get(META_KEY).get(SERVER_TIME_KEY).doubleValue();
    logger.log("JsonConverter: retrieved time: " + serverTime);
    return serverTime;
  }

  /**
   * Retrieves the streamDeletion field from a response from the API
   *
   * @param json
   *          the JSON reponse body
   * @return
   * @throws JsonProcessingException
   * @throws IOException
   */
  public static String retrieveDeletedStreamId(String json) throws JsonProcessingException,
    IOException {
    String deletedStreamId = toJsonNode(json).get(STREAM_DELETION_KEY).get(ID_KEY).textValue();
    logger.log("JsonConverter: retrieved stream deletion id: " + deletedStreamId);
    return deletedStreamId;
  }

  /**
   * Retrieves the eventDeletion field from a response from the API
   *
   * @param json
   *          the JSON response body
   * @return
   * @throws JsonProcessingException
   * @throws IOException
   */
  public static String retrieveDeleteEventId(String json) throws JsonProcessingException,
    IOException {
    String deletedEventId = toJsonNode(json).get(EVENT_DELETION_KEY).get(ID_KEY).textValue();
    logger.log("JsonConverter: retrieved event deletion id: " + deletedEventId);
    return deletedEventId;
  }

  /**
   * verify if the JSON has a "eventDeletion" field
   *
   * @param json
   *          the JSON response body
   * @return
   * @throws JsonProcessingException
   * @throws IOException
   */
  public static Boolean hasEventDeletionField(String json) throws JsonProcessingException,
    IOException {
    if (toJsonNode(json).findValue(EVENT_DELETION_KEY) != null) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * verify if the JSON has a "streamDeletion" field
   *
   * @param json
   *          the JSON response body
   * @return
   * @throws JsonProcessingException
   * @throws IOException
   */
  public static Boolean hasStreamDeletionField(String json) throws JsonProcessingException,
    IOException {
    if (toJsonNode(json).findValue(STREAM_DELETION_KEY) != null) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Retrieves the event from a JSON dictionary containing an event entry at
   * root level
   *
   * @param jsonSource
   *          the JSON stored in a String
   * @return
   * @throws JsonProcessingException
   * @throws IOException
   */
  public static Event retrieveEventFromJson(String jsonSource) throws JsonProcessingException,
    IOException {
    JsonNode eventNode = toJsonNode(jsonSource).get(EVENT_KEY);
    return jsonMapper.readValue(eventNode.toString(), Event.class);
  }

  /**
   * Retrieves the stoppedId value from a JSON dictionnary containing a stoppedId entry at
   * root level
   *
   * @param jsonSource the JSON stored in a String
   * @return
   * @throws JsonProcessingException
   * @throws IOException
   */
  public static String retrieveStoppedIdFromJson(String jsonSource) throws JsonProcessingException,
          IOException {
    JsonNode stoppedIdNode = toJsonNode(jsonSource).get(STOPPED_ID_KEY);
    if (stoppedIdNode != null) {
      String stoppedId = stoppedIdNode.toString();
      logger.log("JsonConverter: retrieved stoppedId: " + stoppedId);
      return stoppedId;
    } else {
      return null;
    }
  }

  /**
   * Deserialize a JSON containing the field "events" into a Map<String, Event>
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
    if (arrNode != null) {
      if (arrNode.isArray()) {
        logger.log("JsonConverter: number of received events: " + arrNode.size());
        for (final JsonNode objNode : arrNode) {
          String str = objNode.toString();
          logger.log("JsonConverter: deserializing event: " + str);
          Event eventToAdd = jsonMapper.readValue(str, Event.class);
          newEvents.put(eventToAdd.getId(), eventToAdd);
          logger.log("JsonConverter: event created: id = " + eventToAdd.getId());
        }
      }
    }
    return newEvents;
  }

  /**
   * Retrieves the stream from a JSON dictionary containing a stream entry at
   * root level
   *
   * @param jsonSource
   *          the JSON stored in a String
   * @return
   * @throws JsonProcessingException
   * @throws IOException
   */
  public static Stream retrieveStreamFromJson(String jsonSource) throws JsonProcessingException,
    IOException {
    JsonNode eventNode = toJsonNode(jsonSource).get(STREAM_KEY);
    return jsonMapper.readValue(eventNode.toString(), Stream.class);
  }

  /**
   * Deserialize a JSON containing the field "streams" into a Map<String,
   * Stream> with Stream id as key
   *
   * @param jsonStreamsArray
   * @return
   * @throws IOException
   */
  public static Map<String, Stream> createStreamsTreeFromJson(String jsonStreamsArray)
    throws IOException {
    JsonNode arrNode = toJsonNode(jsonStreamsArray).get(STREAMS_KEY);
    Map<String, Stream> newStreams = new HashMap<String, Stream>();
    if (arrNode.isArray()) {
      logger.log("JsonConverter: number of received root streams: " + arrNode.size());
      for (final JsonNode objNode : arrNode) {
        String str = objNode.toString();
        logger.log("JsonConverter: deserializing stream: " + str);
        Stream streamToAdd = jsonMapper.readValue(str, Stream.class);
        newStreams.put(streamToAdd.getId(), streamToAdd);
        logger.log("JsonConverter: stream created: id = " + streamToAdd.getId());
      }
    }

    return newStreams;
  }

  /**
   * reset all fields of an attachments to values from JSON glossary json
   *
   * @param json
   * @param toUpdate
   * @throws JsonParseException
   * @throws JsonMappingException
   * @throws IOException
   */
  public static void resetAttachmentFromJson(String json, Attachment toUpdate)
    throws JsonParseException, JsonMappingException, IOException {
    Attachment temp = jsonMapper.readValue(json, Attachment.class);
    toUpdate.merge(temp);
  }

  /**
   * reset all fields of Event toUpdate to values from JSON glossary json
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
    toUpdate.merge(temp, true);
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
      return jsonMapper.readValue(jsonAttachments.replace("\\\'", "\'"),
        new TypeReference<Set<Attachment>>() {
      });
    } else {
      return null;
    }
  }

  /**
   * Returns Cloner object
   *
   * @return
   */
  public static Cloner getCloner() {
    return cloner;
  }

}
