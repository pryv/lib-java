package com.pryv.dataStructures;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Event data structure from Pryv
 *
 * @author ik
 *
 */
public class Event {

  private String id;
  private String streamID;
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

  public Event() {

  }

  public Event(String pId, String pStreamID, long pTime, long pDuration, String pType,
      Object pContent, List<String> pTags, List<String> pReferences, String pDescription,
      List<Attachment> pAttachments, Map<String, String> pClientData, Boolean pTrashed,
      long pCreated,
      String pCreatedBy, long pModified, String pModifiedBy) {
    id = pId;
    streamID = pStreamID;
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

  public String toJson() {
    JSONObject jsonEvent = new JSONObject();
    jsonEvent.putOpt(JsonFields.ID.toString(), id);
    jsonEvent.putOpt(JsonFields.TIME.toString(), time);
    jsonEvent.putOpt(JsonFields.STREAM_ID.toString(), streamID);
    jsonEvent.putOpt(JsonFields.TYPE.toString(), type);
    JSONArray jsonTags = new JSONArray(tags);
    jsonEvent.putOpt(JsonFields.TAGS.toString(), jsonTags);
    jsonEvent.putOpt(JsonFields.DURATION.toString(), duration);
    jsonEvent.putOpt(JsonFields.CONTENT.toString(), content);
    jsonEvent.putOpt(JsonFields.DESCRIPTION.toString(), description);
    JSONArray jsonRefs = new JSONArray(references);
    jsonEvent.putOpt(JsonFields.REFERENCES.toString(), jsonRefs);
    JSONArray jsonAttachments = new JSONArray(attachments);
    jsonEvent.putOpt(JsonFields.ATTACHMENTS.toString(), jsonAttachments);
    jsonEvent.putOpt(JsonFields.TRASHED.toString(), trashed);
    jsonEvent.putOpt(JsonFields.CREATED.toString(), created);
    jsonEvent.putOpt(JsonFields.CREATED_BY.toString(), createdBy);
    jsonEvent.putOpt(JsonFields.MODIFIED.toString(), modified);
    jsonEvent.putOpt(JsonFields.MODIFIED_BY.toString(), modifiedBy);
    jsonEvent.putOpt(JsonFields.CLIENT_DATA.toString(), new JSONObject(clientData));
    return jsonEvent.toString();
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


  public String getStreamID() {
    return streamID;
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

}
