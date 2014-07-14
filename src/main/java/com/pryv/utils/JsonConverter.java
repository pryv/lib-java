package com.pryv.utils;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
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

  public static void initMapper() {
    jsonMapper.setSerializationInclusion(Include.NON_NULL);
  }

  public static String toJson(Object source) throws JsonProcessingException {
    return jsonMapper.writeValueAsString(source);
  }

  public static void updateAttachmentFromJson(String json, Attachment toUpdate)
      throws JsonParseException, JsonMappingException, IOException {
    Attachment temp = jsonMapper.readValue(json, Attachment.class);
    toUpdate.merge(temp);
  }

  public static void updateEventFromJson(String json, Event toUpdate) throws JsonParseException,
      JsonMappingException, IOException {
    Event temp = jsonMapper.readValue(json, Event.class);
    toUpdate.merge(temp, cloner);
    System.out.println("new Event: " + jsonMapper.writeValueAsString(toUpdate));
  }

  public static void updateStreamFromJson(String json, Stream toUpdate) throws JsonParseException,
      JsonMappingException, IOException {
    Stream temp = jsonMapper.readValue(json, Stream.class);
    toUpdate.merge(temp, cloner);
  }

}
