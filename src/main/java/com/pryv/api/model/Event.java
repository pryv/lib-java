package com.pryv.api.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rits.cloning.Cloner;

/**
 * Event data structure from Pryv
 *
 * @author ik
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
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

  public String toSQL() {
    String listSeparator = "";
    StringBuilder sb = new StringBuilder();

    primitiveAsSQL(sb, id);
    primitiveAsSQL(sb, streamId);
    longAsSQL(sb, time);
    primitiveAsSQL(sb, type);
    longAsSQL(sb, created);
    primitiveAsSQL(sb, createdBy);
    longAsSQL(sb, modified);
    primitiveAsSQL(sb, modifiedBy);
    longAsSQL(sb, duration);
    primitiveAsSQL(sb, content);
    setAsSQL(sb, tags);
    setAsSQL(sb, references);
    primitiveAsSQL(sb, description);
    // attachments need to go in their own table
    primitiveAsSQL(sb, getClientDataAsString());
    primitiveAsSQL(sb, trashed);
    primitiveAsSQL(sb, tempRefId);
    sb.setLength(sb.length() - 1);

    // // id
    // sb.append("\'" + id + "\',");
    //
    // // streamId
    // sb.append("\'" + streamId + "\',");
    //
    // // time
    // if (time != null) {
    // sb.append(time + ",");
    // } else {
    // sb.append("null,");
    // }
    //
    // // type
    // sb.append("\'" + type + "\',");
    //
    // // created
    // if (created != null) {
    // sb.append(created.toString() + ",");
    // } else {
    // sb.append("null,");
    // }
    //
    // // createdBy
    // sb.append("\'" + createdBy + "\',");
    //
    // // modified
    // if (modified != null) {
    // sb.append(modified.toString() + ",");
    //
    // } else {
    // sb.append("null,");
    // }
    //
    // // modifiedBy
    // sb.append("\'" + modifiedBy + "\',");
    //
    // // duration
    // if (duration != null) {
    // sb.append(duration.toString() + ",");
    // } else {
    // sb.append("null,");
    // }
    //
    // // content
    // if (content != null) {
    // sb.append("\'" + content.toString() + "\',");
    //
    // } else {
    // sb.append("null,");
    // }
    //
    // // tags
    // sb.append("\'");
    // if (tags != null) {
    // for (String tag : tags) {
    // sb.append(listSeparator + tag);
    // listSeparator = ",";
    // }
    // }
    // sb.append("\',");
    // listSeparator = "";
    //
    // // refs
    // sb.append("\'");
    // if (references != null) {
    // for (String ref : references) {
    // sb.append(listSeparator + ref);
    // listSeparator = ",";
    // }
    // }
    // sb.append("\',");
    // listSeparator = "";
    //
    // sb.append("\'" + description + "\',");
    //
    // // attachments
    // sb.append("\'");
    // if (attachments != null) {
    // for (Attachment attachment : attachments) {
    // sb.append(listSeparator + attachment.getId());
    // listSeparator = ",";
    // }
    // } else {
    // sb.append("null");
    // }
    // sb.append("\',");
    // listSeparator = "";
    //
    // // clientData
    // sb.append("\'" + getClientDataAsString() + "\',");
    // sb.append("\'" + trashed + "\',");
    // sb.append("\'" + tempRefId + "\'");

    return sb.toString();
  }

  private void longAsSQL(StringBuilder sb, Long toAdd) {
    if (toAdd != null) {
      sb.append(toAdd);
    } else {
      sb.append("NULL");
    }
    sb.append(",");
  }

  private void primitiveAsSQL(StringBuilder sb, Object prim) {
    if (prim != null) {
      sb.append("\'" + prim + "\'");
    } else {
      sb.append("NULL");
    }
    sb.append(",");
  }

  private void setAsSQL(StringBuilder sb, Set<?> set) {
    String listSeparator = "";
    if (set != null) {
      sb.append("\'");
      for (Object setItem : set) {
        sb.append(listSeparator + setItem);
        listSeparator = ",";
      }
      sb.append("\'");
    } else {
      sb.append("NULL");
    }
    sb.append(",");
  }

  /**
   * format client data to printable.
   *
   * @return client data in readable form as a String.
   */
  public String getClientDataAsString() {
    StringBuilder sb = new StringBuilder();
    if (clientData != null) {
      String separator = "";
      for (String key : clientData.keySet()) {
        sb.append(separator);
        separator = ", ";
        sb.append(key + ": " + clientData.get(key));
      }
      return sb.toString();
    } else {
      return null;
    }
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
