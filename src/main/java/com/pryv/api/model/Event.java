package com.pryv.api.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.rits.cloning.Cloner;

/**
 * Event data structure from Pryv
 *
 * @author ik
 *
 */
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
    Set<Attachment> pAttachments, Map<String, Object> pClientData, Boolean pTrashed,
    Long pCreated, String pCreatedBy, Long pModified, String pModifiedBy, String pTempRefId) {
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

  public Event() {

  }

  /**
   * make deep copy of Event fields, used when updating values of an Event in
   * memory
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

  public String getDescription() {
    return description;
  }

  public Set<Attachment> getAttachments() {
    return attachments;
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

  public Long getDuration() {
    return duration;
  }

  public String getType() {
    return type;
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

  public Map<String, Object> getClientData() {
    return clientData;
  }

  public Boolean getTrashed() {
    return trashed;
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

  public void setId(String pId) {
    this.id = pId;
  }

  public String getTempRefId() {
    return tempRefId;
  }

}
