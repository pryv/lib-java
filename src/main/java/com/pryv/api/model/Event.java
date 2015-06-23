package com.pryv.api.model;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.pryv.Connection;
import com.pryv.api.database.QueryGenerator;
import com.pryv.utils.JsonConverter;
import com.rits.cloning.Cloner;

/**
 * Event data structure from Pryv
 *
 * @author ik
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Event {

  /**
   * id used to access files locally
   */
  @JsonIgnore
  private String clientId;

  private String id;
  private String streamId;
  private Double time;
  private String type;
  private Double created;
  private String createdBy;
  private Double modified;
  private String modifiedBy;

  /**
   * a weak reference to the connection to which the Event is linked
   */
  @JsonIgnore
  private WeakReference<Connection> weakConnection;

  // optional
  private Double duration;
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
   * @param pClientId
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
  public Event(String pClientId, String pId, String pStreamId, Double pTime, Double pDuration,
    String pType, String pContent, Set<String> pTags, Set<String> pReferences, String pDescription,
    Set<Attachment> pAttachments, Map<String, Object> pClientData, Boolean pTrashed,
    Double pCreated, String pCreatedBy, Double pModified, String pModifiedBy, String pTempRefId) {
    clientId = pClientId;
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
    if (trashed != null) {
      trashed = pTrashed;
    } else {
      trashed = false;
    }
    tempRefId = pTempRefId;
  }

  /**
   * empty Event constructor
   */
  public Event() {
    trashed = false;
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
    clientId = result.getString(QueryGenerator.EVENTS_CLIENT_ID_KEY);
    id = result.getString(QueryGenerator.EVENTS_ID_KEY);
    streamId = result.getString(QueryGenerator.EVENTS_STREAM_ID_KEY);
    time = result.getDouble(QueryGenerator.EVENTS_TIME_KEY);
    type = result.getString(QueryGenerator.EVENTS_TYPE_KEY);
    created = result.getDouble(QueryGenerator.EVENTS_CREATED_KEY);
    createdBy = result.getString(QueryGenerator.EVENTS_CREATED_BY_KEY);
    modified = result.getDouble(QueryGenerator.EVENTS_MODIFIED_KEY);
    modifiedBy = result.getString(QueryGenerator.EVENTS_MODIFIED_BY_KEY);
    duration = result.getDouble(QueryGenerator.EVENTS_DURATION_KEY);
    content = result.getObject(QueryGenerator.EVENTS_CONTENT_KEY);
    String tagsString = result.getString(QueryGenerator.EVENTS_TAGS_KEY);
    if (tagsString != null) {
      tags = new HashSet<String>(Arrays.asList(tagsString.split(",")));
    }
    String referencesString = result.getString(QueryGenerator.EVENTS_REFS_KEY);
    if (referencesString != null) {
      references = new HashSet<String>(Arrays.asList(referencesString.split(",")));
    }

    description = result.getString(QueryGenerator.EVENTS_DESCRIPTION_KEY);
    // TODO fetch Attachments elsewhere
    setClientDataFromAstring(result.getString(QueryGenerator.EVENTS_CLIENT_DATA_KEY));
    trashed = result.getBoolean(QueryGenerator.EVENTS_TRASHED_KEY);
    tempRefId = result.getString(QueryGenerator.EVENTS_TEMP_REF_ID_KEY);
    attachments =
      JsonConverter.deserializeAttachments(result.getString(QueryGenerator.EVENTS_ATTACHMENTS_KEY));
  }

  /**
   * Assign unique identifier to the Event - to execute ONCE upon creation
   */
  public void generateClientId() {
    clientId = UUID.randomUUID().toString();
  }

  /**
   * Assign a weak reference to the Connection
   *
   * @param connection
   */
  public void assignConnection(WeakReference<Connection> pWeakconnection) {
    weakConnection = pWeakconnection;
  }

  /**
   * Returns the reference to the Connection to which the Event is linked if
   * any.
   *
   * @return
   */
  public Connection getWeakConnection() {
    return weakConnection.get();
  }

  /**
   * Copy all temp Event's values into caller Event.
   *
   * @param temp
   *          the Event from which the fields are merged
   * @param Cloner
   *          com.rits.cloning.Cloner instance from JsonConverter util class
   */
  public void merge(Event temp, Cloner cloner) {
    clientId = temp.clientId;
    weakConnection = temp.weakConnection;
    id = temp.id;
    streamId = temp.streamId;
    time = temp.time;
    duration = temp.duration;
    type = temp.type;
    content = temp.content;
    if (temp.tags != null) {
      tags = new HashSet<String>();
      for (String tag : temp.tags) {
        tags.add(tag);
      }
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

    temp = null;
  }

  /**
   * creates a map <attachmentId, Attachment> and returns it
   *
   * @return
   */
  @JsonIgnore
  public Map<String, Attachment> getAttachmentsMap() {
    Map<String, Attachment> attachmentsMap = new HashMap<String, Attachment>();
    for (Attachment attachment : attachments) {
      attachmentsMap.put(attachment.getId(), attachment);
    }
    return attachmentsMap;
  }

  /**
   * returns the first attachment that is retrieved from the Set of attachments.
   * This method is implemented because most events have a single attachment.
   *
   * @return
   */
  @JsonIgnore
  public Attachment getFirstAttachment() {
    for (Attachment attachment : attachments) {
      return attachment;
    }
    return null;
  }

  /**
   * Returns the time of the Event wrapped in a Joda DateTime object
   *
   * @return
   */
  @JsonIgnore
  public DateTime getDate() {
    if (time == null) {
      return null;
    }
    if (weakConnection.get() == null) {
      return new DateTime(time.doubleValue());
    }
    return weakConnection.get().serverTimeInSystemDate(time);
  }

  /**
   * Sets the time of the Event from a provided Joda DateTime object
   *
   * @param date
   */
  @JsonIgnore
  public void setDate(DateTime date) {
    if (date == null) {
      time = null;
    }
    if (weakConnection.get() == null) {
      time = date.getMillis() / 1000.0;
    }
    weakConnection.get().serverTimeInSystemDate(date.getMillis() / 1000.0);
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
  @JsonIgnore
  public void setClientDataFromAstring(String source) {
    if (source != null) {
      String[] cdPairs = source.split(":");
      if (clientData == null) {
        clientData = new HashMap<String, Object>();
      }
      clientData.put(cdPairs[0], cdPairs[1]);
    }
  }

  /**
   * add an attachment to the event.
   *
   * @param attachment
   *          the attachment we wish to add
   */
  public void addAttachment(Attachment attachment) {
    if (attachments == null) {
      attachments = new HashSet<Attachment>();
    }
    attachments.add(attachment);
  }

  /**
   * remove an attachment from the event.
   *
   * @param attachmentId
   *          the id of the attachment we wish to remove
   */
  public void removeAttachment(String attachmentId) {
    for (Attachment attachment : attachments) {
      if (attachment.getId().equals(attachmentId)) {
        attachments.remove(attachment);
        if (attachments.size() == 0) {
          attachments = null;
        }
      }
    }
  }

  @Override
  public String toString() {
    return "Event: id=" + id + ", streamId=" + streamId;
  }

  public String getClientId() {
    return clientId;
  }

  public String getId() {
    return id;
  }

  public String getStreamId() {
    return streamId;
  }

  public Double getTime() {
    return time;
  }

  public String getType() {
    return type;
  }

  public Double getCreated() {
    return created;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public Double getModified() {
    return modified;
  }

  public String getModifiedBy() {
    return modifiedBy;
  }

  public Double getDuration() {
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

  public Boolean isTrashed() {
    return trashed;
  }

  public String getTempRefId() {
    return tempRefId;
  }

  public void setClientId(String pClientId) {
    clientId = pClientId;
  }

  public void setId(String pid) {
    this.id = pid;
  }

  public void setStreamId(String pstreamId) {
    this.streamId = pstreamId;
  }

  public void setTime(Double ptime) {
    this.time = ptime;
  }

  public void setType(String ptype) {
    this.type = ptype;
  }

  public void setCreated(Double pcreated) {
    this.created = pcreated;
  }

  public void setCreatedBy(String pcreatedBy) {
    this.createdBy = pcreatedBy;
  }

  public void setModified(Double pmodified) {
    this.modified = pmodified;
  }

  public void setModifiedBy(String pmodifiedBy) {
    this.modifiedBy = pmodifiedBy;
  }

  public void setDuration(Double pduration) {
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
