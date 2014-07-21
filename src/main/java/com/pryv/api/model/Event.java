package com.pryv.api.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  private long time;
  private String type;
  private long created;
  private String createdBy;
  private long modified;
  private String modifiedBy;

  // optional
  private long duration;
  private Object content;
  private List<String> tags;
  private List<String> references;
  private String description;
  private List<Attachment> attachments;
  private Map<String, String> clientData;
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
  public Event(String pId, String pStreamId, long pTime, long pDuration, String pType,
    String pContent, List<String> pTags, List<String> pReferences, String pDescription,
    List<Attachment> pAttachments, Map<String, String> pClientData, Boolean pTrashed,
    long pCreated, String pCreatedBy, long pModified, String pModifiedBy, String pTempRefId) {
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
    tags = new ArrayList<String>();
    for (String tag : temp.tags) {
      tags.add(tag);
    }

    if (temp.references != null) {
      references = new ArrayList<String>();
      for (String ref : temp.references) {
        references.add(ref);
      }
    }
    description = temp.description;
    if (temp.attachments != null) {
      attachments = new ArrayList<Attachment>();
      for (Attachment attachment : temp.attachments) {
        attachments.add(cloner.deepClone(attachment));
      }
    }
    if (temp.clientData != null) {
      clientData = new HashMap<String, String>();
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

  public List<Attachment> getAttachments() {
    return attachments;
  }

  public String getId() {
    return id;
  }

  public String getStreamId() {
    return streamId;
  }

  public long getTime() {
    return time;
  }

  public long getDuration() {
    return duration;
  }

  public String getType() {
    return type;
  }

  public Object getContent() {
    return content;
  }

  public List<String> getTags() {
    return tags;
  }

  public List<String> getReferences() {
    return references;
  }

  public Map<String, String> getClientData() {
    return clientData;
  }

  public Boolean getTrashed() {
    return trashed;
  }

  public long getCreated() {
    return created;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public long getModified() {
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
