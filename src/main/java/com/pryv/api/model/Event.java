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
  private long duration;
  private String type;
  private Object content;
  private List<String> tags;
  private List<String> references;
  private String description;
  private List<Attachment> attachments;
  private Map<String, String> clientData;
  private Boolean trashed;
  private long created;
  private String createdBy;
  private long modified;
  private String modifiedBy;
  private String tempRefId;

  /**
   * Construct Event object from parameters
   *
   * @param pId
   * @param pStreamId
   * @param pTime
   * @param pDuration
   * @param pType
   * @param pContent
   * @param pTags
   * @param pReferences
   * @param pDescription
   * @param pAttachments
   * @param pClientData
   * @param pTrashed
   * @param pCreated
   * @param pCreatedBy
   * @param pModified
   * @param pModifiedBy
   */
  public Event(String pId, String pStreamId, long pTime, long pDuration, String pType,
    String pContent, List<String> pTags, List<String> pReferences, String pDescription,
    List<Attachment> pAttachments, Map<String, String> pClientData, Boolean pTrashed,
    long pCreated, String pCreatedBy, long pModified, String pModifiedBy) {
    id = pId;
    streamId = pStreamId;
    time = pTime;
    duration = pDuration;
    type = pType;
    content = pContent;
    tags = pTags;
    references = pReferences;
    description = pDescription;
    attachments = pAttachments;
    clientData = pClientData;
    trashed = pTrashed;
    created = pCreated;
    createdBy = pCreatedBy;
    modified = pModified;
    modifiedBy = pModifiedBy;
  }

  // /**
  // * Construct Event object from JSON saved in a String object
  // *
  // * @param pJsonEvent
  // */
  // public Event(String pJsonEvent) {
  // JSONObject jsonEvent = new JSONObject(pJsonEvent);
  // id = jsonEvent.optString(JsonFields.ID.toString());
  // streamID = jsonEvent.optString(JsonFields.STREAM_ID.toString());
  // JSONObject jsonClientData =
  // jsonEvent.optJSONObject(JsonFields.CLIENT_DATA.toString());
  // if (jsonClientData != null) {
  // String cdKey = (String) jsonClientData.keySet().toArray()[0];
  // clientData = new HashMap<String, String>();
  // clientData.put(cdKey, jsonClientData.optString(cdKey));
  // }
  // JSONArray jsonTags = jsonEvent.optJSONArray(JsonFields.TAGS.toString());
  // if (jsonTags != null) {
  // tags = new ArrayList<String>();
  // for (int i = 0; i < jsonTags.length(); i++) {
  // tags.add(jsonTags.optString(i));
  // }
  // }
  // JSONArray jsonRefs =
  // jsonEvent.optJSONArray(JsonFields.REFERENCES.toString());
  // if (jsonRefs != null) {
  // for (int i = 0; i < jsonRefs.length(); i++) {
  // references = new ArrayList<String>();
  // references.add(jsonRefs.optString(i));
  // }
  // }
  // JSONArray jsonAttachments =
  // jsonEvent.optJSONArray(JsonFields.ATTACHMENTS.toString());
  // if (jsonAttachments != null) {
  // for (int i = 0; i < jsonAttachments.length(); i++) {
  // attachments = new ArrayList<Attachment>();
  // attachments.add(new
  // Attachment(jsonAttachments.optJSONObject(i).toString()));
  // }
  // }
  // time = jsonEvent.optLong(JsonFields.TIME.toString());
  // type = jsonEvent.optString(JsonFields.TYPE.toString());
  // duration = jsonEvent.optLong(JsonFields.DURATION.toString());
  // content = jsonEvent.opt(JsonFields.CONTENT.toString());
  // description = jsonEvent.optString(JsonFields.DESCRIPTION.toString());
  // trashed = jsonEvent.optBoolean(JsonFields.TRASHED.toString());
  // created = jsonEvent.optLong(JsonFields.CREATED.toString());
  // createdBy = jsonEvent.optString(JsonFields.CREATED_BY.toString());
  // modified = jsonEvent.optLong(JsonFields.MODIFIED.toString());
  // modifiedBy = jsonEvent.optString(JsonFields.MODIFIED_BY.toString());
  //
  // }

  // /**
  // * create JSON from Event object.
  // *
  // * @return
  // */
  // public String toJson() {
  // JSONObject jsonEvent = new JSONObject();
  // jsonEvent.putOpt(JsonFields.ID.toString(), id);
  // jsonEvent.putOpt(JsonFields.TIME.toString(), time);
  // jsonEvent.putOpt(JsonFields.STREAM_ID.toString(), streamID);
  // jsonEvent.putOpt(JsonFields.TYPE.toString(), type);
  // JSONArray jsonTags = new JSONArray(tags);
  // jsonEvent.putOpt(JsonFields.TAGS.toString(), jsonTags);
  // jsonEvent.putOpt(JsonFields.DURATION.toString(), duration);
  // jsonEvent.putOpt(JsonFields.CONTENT.toString(), content);
  // jsonEvent.putOpt(JsonFields.DESCRIPTION.toString(), description);
  // JSONArray jsonRefs = new JSONArray(references);
  // jsonEvent.putOpt(JsonFields.REFERENCES.toString(), jsonRefs);
  // JSONArray jsonAttachments = new JSONArray(attachments);
  // jsonEvent.putOpt(JsonFields.ATTACHMENTS.toString(), jsonAttachments);
  // jsonEvent.putOpt(JsonFields.TRASHED.toString(), trashed);
  // jsonEvent.putOpt(JsonFields.CREATED.toString(), created);
  // jsonEvent.putOpt(JsonFields.CREATED_BY.toString(), createdBy);
  // jsonEvent.putOpt(JsonFields.MODIFIED.toString(), modified);
  // jsonEvent.putOpt(JsonFields.MODIFIED_BY.toString(), modifiedBy);
  // jsonEvent.putOpt(JsonFields.CLIENT_DATA.toString(), new
  // JSONObject(clientData));
  // return jsonEvent.toString();
  // }

  /**
   * make deep copy of Event fields, used when updating values of an Event in
   * memory
   *
   * @param temp
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

  public Event() {

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
