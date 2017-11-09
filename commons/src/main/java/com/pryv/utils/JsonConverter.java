package com.pryv.utils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pryv.exceptions.ApiException;
import com.pryv.model.ApiResource;
import com.pryv.model.Attachment;
import com.pryv.model.Event;
import com.pryv.model.Stream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utilitary class used to do JSON conversions
 *
 * @author ik
 *
 */
public class JsonConverter {

  private static ObjectMapper jsonMapper = new ObjectMapper();
  private static Logger logger = Logger.getInstance();

  private final static String EVENT_KEY = "event";
  private final static String EVENTS_KEY = "events";
  private final static String STREAM_KEY = "stream";
  private final static String STREAMS_KEY = "streams";

  private final static String STREAM_DELETIONS_KEY = "streamDeletions";
  private final static String DELETED_KEY = "deleted";

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
  public static <T> Object fromJson(String jsonSource, Class<T> type) throws IOException {
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
  public static JsonNode toJsonNode(String source) throws IOException {
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
  public static double retrieveServerTime(String jsonResponse) throws IOException {
    double serverTime = toJsonNode(jsonResponse).get(META_KEY).get(SERVER_TIME_KEY).doubleValue();
    logger.log("JsonConverter: retrieved time: " + serverTime);
    return serverTime;
  }

  /**
   * Retrieves the error field from a erroneous response from the API
   *
   * @param jsonResponse
   *          the jsonResponse containing a field "error"
   * @return
   * @throws IOException
   */
  public static ApiException retrieveApiError(String jsonResponse) throws IOException {
    JsonNode error = toJsonNode(jsonResponse).get("error");
    JsonNode msg = error.get("message");
    JsonNode i = error.get(ID_KEY);
    JsonNode d = error.get("data");
    JsonNode subErr = error.get("subErrors");
    ArrayList<String> subErrors = new ArrayList<>();
    if (subErr!=null && subErr.isArray()) {
      for (final JsonNode objNode : subErr) {
        subErrors.add(objNode.textValue());
      }
    }
    String message = !msg.isNull() && msg.isTextual() ? msg.textValue(): null;
    String id = !i.isNull() && i.isTextual() ? i.textValue(): null;
    String data = !d.isNull() ? d.toString(): null;

    return new ApiException(id, message, data, subErrors);
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
  public static String retrieveDeletedStreamId(String json) throws IOException {
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
  public static String retrieveDeleteEventId(String json) throws IOException {
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
  public static Boolean hasEventDeletionField(String json) throws IOException {
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
  public static Boolean hasStreamDeletionField(String json) throws IOException {
    if (toJsonNode(json).findValue(STREAM_DELETION_KEY) != null) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Retrieves the event from a JSON dictionary containing an event entry at root level
   *
   * @param jsonSource
   *          the JSON stored in a String
   * @return
   * @throws JsonProcessingException
   * @throws IOException
   */
  public static Event retrieveEventFromJson(String jsonSource) throws IOException {
    JsonNode eventNode = toJsonNode(jsonSource).get(EVENT_KEY);
    return jsonMapper.treeToValue(eventNode, Event.class);
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
  public static String retrieveStoppedIdFromJson(String jsonSource) throws IOException {
    JsonNode stoppedIdNode = toJsonNode(jsonSource).get(STOPPED_ID_KEY);
    if (stoppedIdNode != null) {
      String stoppedId = stoppedIdNode.textValue();
      logger.log("JsonConverter: retrieved stoppedId: " + stoppedId);
      return stoppedId;
    } else {
      return null;
    }
  }

  /**
   * Deserialize a JSON containing the field "events" into a {@code List<Event>}
   *
   * @param jsonEventsArray
   * @return
   * @throws JsonParseException
   * @throws JsonMappingException
   * @throws IOException
   */
  public static List<Event> createEventsFromJson(String jsonEventsArray) throws IOException {

    JsonNode arrNode = toJsonNode(jsonEventsArray).get(EVENTS_KEY);

    List<Event> newEvents = new ArrayList<>();
    if (arrNode != null) {
      if (arrNode.isArray()) {
        logger.log("JsonConverter: number of received events: " + arrNode.size());
        for (final JsonNode objNode : arrNode) {
          Event eventToAdd = jsonMapper.treeToValue(objNode, Event.class);
          newEvents.add(eventToAdd);
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
  public static Stream retrieveStreamFromJson(String jsonSource) throws IOException {
    JsonNode eventNode = toJsonNode(jsonSource).get(STREAM_KEY);
    return jsonMapper.treeToValue(eventNode, Stream.class);
  }

  /**
   * Deserialize a JSON containing the field "streams" into a {@code Map<String,
   * Stream>} with Stream id as key
   *
   * @param jsonStreamsArray
   * @return
   * @throws IOException
   */
  public static Map<String, Stream> createStreamsTreeFromJson(String jsonStreamsArray) throws IOException {
    JsonNode arrNode = toJsonNode(jsonStreamsArray).get(STREAMS_KEY);
    Map<String, Stream> newStreams = new HashMap<String, Stream>();
    if (arrNode!=null && arrNode.isArray()) {
      logger.log("JsonConverter: number of received root streams: " + arrNode.size());
      for (final JsonNode objNode : arrNode) {
        Stream streamToAdd = jsonMapper.treeToValue(objNode, Stream.class);
        newStreams.put(streamToAdd.getId(), streamToAdd);
        logger.log("JsonConverter: stream created: id = " + streamToAdd.getId());
      }
    }

    return newStreams;
  }

  /**
   * Deserialize a JSON containing the field "streamDeletions" into a {@code Map<String,
   * Double>} with Stream id as key and deletion time as value
   *
   * @param jsonStreamDeletionsArray
   * @return
   * @throws IOException
   */
  public static Map<String, Double> createStreamDeletionsTreeFromJson(String jsonStreamDeletionsArray)
          throws IOException {
    JsonNode arrNode = toJsonNode(jsonStreamDeletionsArray).get(STREAM_DELETIONS_KEY);
    Map<String, Double> deletedStreams = new HashMap<String, Double>();
    if (arrNode!=null && arrNode.isArray()) {
      logger.log("JsonConverter: number of received deleted streams: " + arrNode.size());
      for (final JsonNode objNode : arrNode) {
        String streamId = objNode.get(ID_KEY).textValue();
        Double deletionTime = objNode.get(DELETED_KEY).asDouble();
        deletedStreams.put(streamId, deletionTime);
        logger.log("JsonConverter: stream deleted: id = " + streamId);
      }
    }

    return deletedStreams;
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
  public static Set<Attachment> deserializeAttachments(String jsonAttachments) throws IOException {
    if (jsonAttachments != null) {
      return jsonMapper.readValue(jsonAttachments.replace("\\\'", "\'"),
        new TypeReference<Set<Attachment>>() {
      });
    } else {
      return null;
    }
  }

  public static <T extends ApiResource> List<T> retrieveResourcesFromJson(String jsonResourcesArray, String resourceKey, Class<T> resource) throws IOException {
    JsonNode arrNode = toJsonNode(jsonResourcesArray).get(resourceKey);
    List<T> newResources = new ArrayList<>();
    if (arrNode != null && arrNode.isArray()) {
      for (final JsonNode objNode : arrNode) {
        T newResource = jsonMapper.treeToValue(objNode, resource);
        newResources.add(newResource);
      }
    }
    return newResources;
  }

  public static <T extends ApiResource> T retrieveResourceFromJson(String jsonResource, String resourceKey, Class<T> resource) throws IOException {
    JsonNode eventNode = toJsonNode(jsonResource).get(resourceKey);
    return jsonMapper.treeToValue(eventNode, resource);
  }

  public static String retrieveDeletedResourceId(String jsonResponse, String resourceKey) throws IOException {
    String deletedResourceId = toJsonNode(jsonResponse).get(resourceKey).get(ID_KEY).textValue();
    return deletedResourceId;
  }

}
