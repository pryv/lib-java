package com.pryv.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.pryv.AbstractConnection;
import com.pryv.database.QueryGenerator;
import com.pryv.utils.Cuid;
import com.pryv.utils.JsonConverter;
import com.rits.cloning.Cloner;

import org.joda.time.DateTime;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

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
   * a weak reference to the connection to which the Event is linked
   */
  @JsonIgnore
  private WeakReference<AbstractConnection> weakConnection;

  /**
   * used in order to prevent instanciating an Event multiple times.
   */
  private static Map<String, Event> supervisor = new WeakHashMap<String, Event>();

  /**
   * empty Event constructor
   */
  // TODO: Time?
  public Event() {
    this.generateId();
    this.updateSupervisor();
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
    this.updateSupervisor();
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
    this.updateSupervisor();
  }

  /**
   * Build an event from a ResultSet, used when retrieving Event objects from the SQLite Cache.
   * This takes care of instanciating a new Event only in the case when it isn't existing yet.
   *
   * @param result The
   * @return
   * @throws SQLException
   * @throws IOException
   */
  public static Event createOrReuse(ResultSet result) throws SQLException, IOException {
    String id = result.getString(QueryGenerator.EVENTS_ID_KEY);
    Event event = supervisor.get(id);
    if (event == null) {
      event = new Event();
    }
    event.setId(result.getString(QueryGenerator.EVENTS_ID_KEY));
    event.setStreamId(result.getString(QueryGenerator.EVENTS_STREAM_ID_KEY));
    event.setTime(result.getDouble(QueryGenerator.EVENTS_TIME_KEY));
    event.setType(result.getString(QueryGenerator.EVENTS_TYPE_KEY));
    event.setCreated(result.getDouble(QueryGenerator.EVENTS_CREATED_KEY));
    event.setCreatedBy(result.getString(QueryGenerator.EVENTS_CREATED_BY_KEY));
    event.setModified(result.getDouble(QueryGenerator.EVENTS_MODIFIED_KEY));
    event.setModifiedBy(result.getString(QueryGenerator.EVENTS_MODIFIED_BY_KEY));
    event.setDuration(result.getDouble(QueryGenerator.EVENTS_DURATION_KEY));
    event.setContent(result.getObject(QueryGenerator.EVENTS_CONTENT_KEY));
    String tagsString = result.getString(QueryGenerator.EVENTS_TAGS_KEY);
    event.setTags(tagsString);
    event.setDescription(result.getString(QueryGenerator.EVENTS_DESCRIPTION_KEY));
    // TODO fetch Attachments elsewhere
    event.setClientDataFromAstring(result.getString(QueryGenerator.EVENTS_CLIENT_DATA_KEY));
    event.setTrashed(result.getBoolean(QueryGenerator.EVENTS_TRASHED_KEY));
    event.setAttachments(JsonConverter.deserializeAttachments(result.getString(QueryGenerator.EVENTS_ATTACHMENTS_KEY)));
    return event;
  }

  /**
   * saves the Event in the supervisor if needed
   *
   * @param event
   * @return
   */
  // TODO: Is it useful or new Event is already adding to supervisor?
  public static Event createOrReuse(Event event) {
    String id = event.getId();
    supervisor.put(id, event);
    return event;
  }

  /**
   * Assign unique identifier to the Event - does nothing if Event has already a id field
   */
  public String generateId() {
    if (this.id == null) {
      this.id = Cuid.createCuid();
    }
    return this.id;
  }

  private void updateSupervisor() {
    String id = this.getId();
    if(supervisor.containsKey(id)) {
      supervisor.get(id).merge(this, JsonConverter.getCloner());
    } else {
      supervisor.put(id,this);
    }
  }

  /**
   * Assign a weak reference to the ConnectionOld
   *
   * @param weakconnection
   */
  public void assignConnection(WeakReference<AbstractConnection> weakconnection) {
    this.weakConnection = weakconnection;
  }

  /**
   * Returns the reference to the ConnectionOld to which the Event is linked if
   * any.
   *
   * @return
   */
  public AbstractConnection getWeakConnection() {
    return weakConnection.get();
  }

  /**
   * Copy all temp Event's values into caller Event.
   *
   * @param temp
   *          the Event from which the fields are merged
   * @param cloner
   *          com.rits.cloning.Cloner instance from JsonConverter util class
   */
  public void merge(Event temp, Cloner cloner) {
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

  public void setTags(String ptags) {
    this.tags = (ptags == null) ? null : new HashSet<String>(Arrays.asList(ptags.split(",")));
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
