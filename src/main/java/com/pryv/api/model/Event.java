package com.pryv.api.model;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.pryv.api.database.QueryGenerator;
import com.pryv.utils.JsonConverter;
import com.rits.cloning.Cloner;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Event data structure from Pryv
 *
 * @author ik
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Event {

  private String id;
  private String streamId;
  private Long time;
  private String type;
  private Long created;
  private String createdBy;
  private Long modified;
  private String modifiedBy;

  // optional
  private Long duration;
  private Object content;
  private Set<String> tags;
  private Set<String> references;
  private String description;
  private Set<Attachment> attachments;
  private Map<String, Object> clientData;
  private Boolean trashed;
  private String tempRefId;

  /**
   * Construct Event object from parameters
   *
   * @param pId
   * @param pStreamId
   * @param pTime
   * @param pType
   * @param pCreated
   * @param pCreatedBy
   * @param pModified
   * @param pModifiedBy
   *
   * @param pDuration
   *          optional
   * @param pContent
   *          optional
   * @param pTags
   *          optional
   * @param pReferences
   *          optional
   * @param pDescription
   *          optional
   * @param pAttachments
   *          optional
   * @param pClientData
   *          optional
   * @param pTrashed
   *          optional
   * @param pTempRefId
   *          optional
   */
  public Event(String pId, String pStreamId, Long pTime, Long pDuration, String pType,
    String pContent, Set<String> pTags, Set<String> pReferences, String pDescription,
    Set<Attachment> pAttachments, Map<String, Object> pClientData, Boolean pTrashed, Long pCreated,
    String pCreatedBy, Long pModified, String pModifiedBy, String pTempRefId) {
    id = pId;
    streamId = pStreamId;
    time = pTime;
    type = pType;
    created = pCreated;
    createdBy = pCreatedBy;
    modified = pModified;
    modifiedBy = pModifiedBy;

    duration = pDuration;
    content = pContent;
    tags = pTags;
    references = pReferences;
    description = pDescription;
    attachments = pAttachments;
    clientData = pClientData;
    trashed = pTrashed;
    tempRefId = pTempRefId;
  }

  /**
   * empty Event constructor
   */
  public Event() {

  }

  /**
   * build Event when retrieved from cache
   *
   * @param result
   * @throws SQLException
   * @throws IOException
   * @throws JsonMappingException
   * @throws JsonParseException
   */
  public Event(ResultSet result) throws SQLException, JsonParseException, JsonMappingException,
    IOException {
    id = result.getString(QueryGenerator.EVENTS_ID_KEY);
    streamId = result.getString(QueryGenerator.EVENTS_STREAM_ID_KEY);
    time = result.getLong(QueryGenerator.EVENTS_TIME_KEY);
    type = result.getString(QueryGenerator.EVENTS_TYPE_KEY);
    created = result.getLong(QueryGenerator.EVENTS_CREATED_KEY);
    createdBy = result.getString(QueryGenerator.EVENTS_CREATED_BY_KEY);
    modified = result.getLong(QueryGenerator.EVENTS_MODIFIED_KEY);
    modifiedBy = result.getString(QueryGenerator.EVENTS_MODIFIED_BY_KEY);
    duration = result.getLong(QueryGenerator.EVENTS_DURATION_KEY);
    content = result.getObject(QueryGenerator.EVENTS_CONTENT_KEY);
    tags =
      new HashSet<String>(
        Arrays.asList(result.getString(QueryGenerator.EVENTS_TAGS_KEY).split(",")));
    references =
      new HashSet<String>(
        Arrays.asList(result.getString(QueryGenerator.EVENTS_REFS_KEY).split(",")));
    description = result.getString(QueryGenerator.EVENTS_DESCRIPTION_KEY);
    // TODO fetch Attachments elsewhere
    setClientDataFromAstring(result.getString(QueryGenerator.EVENTS_CLIENT_DATA_KEY));
    trashed = result.getBoolean(QueryGenerator.EVENTS_TRASHED_KEY);
    tempRefId = result.getString(QueryGenerator.EVENTS_TEMP_REF_ID_KEY);
    attachments =
      JsonConverter.deserializeAttachments(result.getString(QueryGenerator.EVENTS_ATTACHMENTS_KEY));
  }

  // private

  /**
   * Copy all temp Event's values into caller Event.
   *
   * @param temp
   *          the Event from which the fields are merged
   * @param Cloner
   *          com.rits.cloning.Cloner instance from JsonConverter util class
   */
  public void merge(Event temp, Cloner cloner) {
    id = temp.id;
    streamId = temp.streamId;
    time = temp.time;
    duration = temp.duration;
    type = temp.type;
    content = temp.content;
    tags = new HashSet<String>();
    for (String tag : temp.tags) {
      tags.add(tag);
    }

    if (temp.references != null) {
      references = new HashSet<String>();
      for (String ref : temp.references) {
        references.add(ref);
      }
    }
    description = temp.description;
    if (temp.attachments != null) {
      attachments = new HashSet<Attachment>();
      for (Attachment attachment : temp.attachments) {
        attachments.add(cloner.deepClone(attachment));
      }
    }
    if (temp.clientData != null) {
      clientData = new HashMap<String, Object>();
      for (String key : temp.clientData.keySet()) {
        clientData.put(key, temp.clientData.get(key));
      }
    }
    trashed = temp.trashed;
    created = temp.created;
    createdBy = temp.createdBy;
    modified = temp.modified;
    modifiedBy = temp.modifiedBy;
  }

  /**
   * used for testing purposes
   */
  public void publishValues() {
    List<String> eventFields = new ArrayList<String>();
    Field[] fields = Event.class.getDeclaredFields();
    for (Field field : fields) {
      eventFields.add(field.getName().toUpperCase());
      try {
        System.out.print(field.getName().toUpperCase() + ": ");
        if (String.class.isAssignableFrom(field.getType())) {
          System.out.print("\'" + field.get(this) + "\'");
        } else if (Long.class.isAssignableFrom(field.getType())) {
          System.out.print(field.get(this));
        } else if (Collection.class.isAssignableFrom(field.getType())) {
          if (field.get(this) != null) {
            for (Object item : (Iterable) field.get(this)) {
              System.out.print(field.get(this) + ",");
            }
          }
        } else {
          System.out.print(field.get(this));
        }

        System.out.println("");
      } catch (IllegalArgumentException | IllegalAccessException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  /**
   * format client data to printable. eg.: "keyA:valueA,keyB:valueB, ..."
   *
   * @return client data in readable form as a String.
   */
  public String formatClientDataAsString() {
    StringBuilder sb = new StringBuilder();
    if (clientData != null) {
      String separator = "";
      for (String key : clientData.keySet()) {
        sb.append(separator);
        separator = ",";
        sb.append(key + ":" + clientData.get(key));
      }
      return sb.toString();
    } else {
      return null;
    }
  }

  /**
   * setter for client data previously formatted using getClientDataAsString()
   * method.
   *
   * @param source
   */
  public void setClientDataFromAstring(String source) {
    if (source != null) {
      String[] cdPairs = source.split(":");
      if (clientData == null) {
        clientData = new HashMap<String, Object>();
      }
      clientData.put(cdPairs[0], cdPairs[1]);
    }
  }

  public String getId() {
    return id;
  }

  public String getStreamId() {
    return streamId;
  }

  public Long getTime() {
    return time;
  }

  public String getType() {
    return type;
  }

  public Long getCreated() {
    return created;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public Long getModified() {
    return modified;
  }

  public String getModifiedBy() {
    return modifiedBy;
  }

  public Long getDuration() {
    return duration;
  }

  public Object getContent() {
    return content;
  }

  public Set<String> getTags() {
    return tags;
  }

  public Set<String> getReferences() {
    return references;
  }

  public String getDescription() {
    return description;
  }

  public Set<Attachment> getAttachments() {
    return attachments;
  }

  public Map<String, Object> getClientData() {
    return clientData;
  }

  public Boolean getTrashed() {
    return trashed;
  }

  public String getTempRefId() {
    return tempRefId;
  }

  public void setId(String pid) {
    this.id = pid;
  }

  public void setStreamId(String pstreamId) {
    this.streamId = pstreamId;
  }

  public void setTime(Long ptime) {
    this.time = ptime;
  }

  public void setType(String ptype) {
    this.type = ptype;
  }

  public void setCreated(Long pcreated) {
    this.created = pcreated;
  }

  public void setCreatedBy(String pcreatedBy) {
    this.createdBy = pcreatedBy;
  }

  public void setModified(Long pmodified) {
    this.modified = pmodified;
  }

  public void setModifiedBy(String pmodifiedBy) {
    this.modifiedBy = pmodifiedBy;
  }

  public void setDuration(Long pduration) {
    this.duration = pduration;
  }

  public void setContent(Object pcontent) {
    this.content = pcontent;
  }

  public void setTags(Set<String> ptags) {
    this.tags = ptags;
  }

  public void setReferences(Set<String> preferences) {
    this.references = preferences;
  }

  public void setDescription(String pdescription) {
    this.description = pdescription;
  }

  public void setAttachments(Set<Attachment> pattachments) {
    this.attachments = pattachments;
  }

  public void setClientData(Map<String, Object> pclientData) {
    this.clientData = pclientData;
  }

  public void setTrashed(Boolean ptrashed) {
    this.trashed = ptrashed;
  }

  public void setTempRefId(String ptempRefId) {
    this.tempRefId = ptempRefId;
  }

}
