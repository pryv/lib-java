package com.pryv.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.pryv.utils.Cuid;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Event data structure from Pryv
 *
 * @author ik
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Event extends ApiResource {

  private String id;
  private String streamId;
  private Double time;
  private String type;
  private Double created;
  private String createdBy;
  private Double modified;
  private String modifiedBy;

  // optional
  private Double duration;
  private Object content;
  private Set<String> tags;
  private String description;
  private Set<Attachment> attachments;
  private Map<String, Object> clientData;
  private Boolean trashed;

  /**
   * empty Event constructor
   */
  public Event() {
    this.generateId();
  }

  /**
   * Constructor for Event object with mandatory fields
   *
   * @param streamId
   * @param type
   * @param content
   */
  public Event(String streamId, String type, String content) {
    this.generateId();
    this.streamId = streamId;
    this.type = type;
    this.content = content;
  };

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
   * @param pDescription
   *          optional
   * @param pAttachments
   *          optional
   * @param pClientData
   *          optional
   * @param pTrashed
   *          optional
   */
  public Event(String pId, String pStreamId, Double pTime, Double pDuration,
    String pType, String pContent, Set<String> pTags, String pDescription,
    Set<Attachment> pAttachments, Map<String, Object> pClientData, Boolean pTrashed,
    Double pCreated, String pCreatedBy, Double pModified, String pModifiedBy) {
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
    description = pDescription;
    attachments = pAttachments;
    clientData = pClientData;
    trashed = pTrashed;
  }

  /**
   * Assign unique identifier to the Event - does nothing if Event has already a id field
   */
  private String generateId() {
    if (this.id == null) {
      this.id = Cuid.createCuid();
    }
    return this.id;
  }

  /**
   * creates a map {@code Map<attachmentId, Attachment>} and returns it
   *
   * @return
   */
  @JsonIgnore
  public Map<String, Attachment> getAttachmentsMap() {
    Map<String, Attachment> attachmentsMap = new HashMap<String, Attachment>();
    if(attachments != null && !attachments.isEmpty()) {
      for (Attachment attachment : attachments) {
        attachmentsMap.put(attachment.getId(), attachment);
      }
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
    if(attachments != null && !attachments.isEmpty()) {
      return attachments.iterator().next();
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
    /* TODO: Use of server time from HttpClient
        return con.serverTimeInSystemDate(time);
     */
    return new DateTime((long) (time.doubleValue() * 1000));
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
    } else {
      /* TODO: Use of server time from HttpClient
          time = con.serverTimeInSystemDate(date.getMillis() / 1000.0).getMillis() / 1000.0;
       */
      time = date.getMillis() / 1000.0;
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
      for (String key : sortSet(clientData.keySet())) {
        sb.append(separator);
        separator = ",";
        sb.append(key + ":" + clientData.get(key));
      }
      return sb.toString();
    } else {
      return null;
    }
  }

  private List<String> sortSet (Collection<String> c) {
    List<String> list = new ArrayList<String>(c);
    java.util.Collections.sort(list);
    return list;
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
   * add a tag to the event
   *
   * @param tag
   */
  public void addTag(String tag) {
    if (tags == null) {
      tags = new HashSet<String>();
    }
    tags.add(tag);
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
    return "{\"id\":\"" + id + "\","
            + "\"streamId\":\"" + streamId + "\","
            + "\"time\":\"" + time + "\","
            + "\"duration\":\"" + duration + "\","
            + "\"type\":\"" + type + "\","
            + "\"tags\":\"" + tags + "\","
            + "\"description\":\"" + description + "\","
            + "\"attachments\":\"" + attachments + "\","
            + "\"clientData\":\"" + clientData + "\","
            + "\"trashed\":\"" + trashed + "\","
            + "\"created\":\"" + created + "\","
            + "\"createdBy\":\"" + createdBy + "\","
            + "\"modified\":\"" + modified + "\","
            + "\"modifiedBy\":\"" + modifiedBy + "\"}";

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
    if (trashed == null) {
      return false;
    } else {
      return trashed;
    }
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setStreamId(String streamId) {
    this.streamId = streamId;
  }

  public void setTime(Double time) {
    this.time = time;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setCreated(Double created) {
    this.created = created;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public void setModified(Double modified) {
    this.modified = modified;
  }

  public void setModifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
  }

  public void setDuration(Double duration) {
    this.duration = duration;
  }

  public void setContent(Object content) {
    this.content = content;
  }

  public void setTags(Set<String> tags) {
    this.tags = tags;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setAttachments(Set<Attachment> attachments) {
    this.attachments = attachments;
  }

  public void setClientData(Map<String, Object> clientData) {
    this.clientData = clientData;
  }

  public void setTrashed(Boolean trashed) {
    this.trashed = trashed;
  }
}
