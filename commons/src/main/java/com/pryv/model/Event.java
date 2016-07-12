package com.pryv.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.pryv.AbstractConnection;
import com.pryv.database.QueryGenerator;
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
import java.util.concurrent.ConcurrentHashMap;

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

  // optional
  private Double duration;
  private Object content;
  private Set<String> tags;
  private Set<String> references;
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
   * Used to map ids to clientIds
   */
  private static Map<String, String> idToClientId = new ConcurrentHashMap<String, String>();

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
   */
  public Event(String pClientId, String pId, String pStreamId, Double pTime, Double pDuration,
    String pType, String pContent, Set<String> pTags, Set<String> pReferences, String pDescription,
    Set<Attachment> pAttachments, Map<String, Object> pClientData, Boolean pTrashed,
    Double pCreated, String pCreatedBy, Double pModified, String pModifiedBy) {
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
    trashed = pTrashed;
    // TODO: check for non null?
    supervisor.put(this.clientId, this);
    idToClientId.put(this.id, this.clientId);
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
    String clientId = result.getString(QueryGenerator.EVENTS_CLIENT_ID_KEY);
    Event event = supervisor.get(clientId);
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
    event.setTags(new HashSet<String>(Arrays.asList(tagsString.split(","))));
    String referencesString = result.getString(QueryGenerator.EVENTS_REFS_KEY);
    event.setReferences(new HashSet<String>(Arrays.asList(referencesString.split(","))));

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
  public static Event createOrReuse(Event event) {
    String id = event.getId();
    String clientId = event.getClientId();
    if (id != null && clientId == null) {
      clientId = idToClientId.get(id);
    }

    if (clientId == null) {
      clientId = event.generateClientId();
    }

    // TODO: Check if already existing?
    supervisor.put(clientId, event);

    if (id != null) {
      idToClientId.put(id, clientId);
    }
    return event;
  }

  /**
   * Assign unique identifier to the Event - does nothing if Event has already a id field
   */
  public String generateId() {
    if (this.id == null) {
      // TODO find better way to generate CUID
      this.id = "c" + UUID.randomUUID().toString().substring(0,24);
    }
    return this.id;
  }

  /**
   * Assign unique identifier to the Event - does nothing if Event has already a clientId field
   */
  public String generateClientId() {
    if (this.clientId == null) {
      this.clientId = UUID.randomUUID().toString();
    }
    return this.clientId;
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
   * add a reference to the event
   *
   * @param reference
   */
  public void addReference(String reference) {
    if (references == null) {
      references = new HashSet<String>();
    }
    references.add(reference);
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
    return "{\"cid\":\"" + clientId + "\","
            + "\"id\":\"" + id + "\","
            + "\"streamId\":\"" + streamId + "\","
            + "\"time\":\"" + time + "\","
            + "\"duration\":\"" + duration + "\","
            + "\"type\":\"" + type + "\","
            + "\"tags\":\"" + tags + "\","
            + "\"references\":\"" + references + "\","
            + "\"description\":\"" + description + "\","
            + "\"attachments\":\"" + attachments + "\","
            + "\"clientData\":\"" + clientData + "\","
            + "\"trashed\":\"" + trashed + "\","
            + "\"created\":\"" + created + "\","
            + "\"createdBy\":\"" + createdBy + "\","
            + "\"modified\":\"" + modified + "\","
            + "\"modifiedBy\":\"" + modifiedBy + "\"}";

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
    if (trashed == null) {
      return false;
    } else {
      return trashed;
    }
  }

  /**
   * Assigns a clientId and saves it in the supervisor
   *
   * @param clientId
   */
  public void setClientId(String clientId) {
    if(clientId != null) {
      this.clientId = clientId;
      supervisor.put(this.clientId, this);
      if (id != null) {
        idToClientId.put(this.id, this.clientId);
      }
    }
  }

  /**
   * Assigns an id and puts an entry in the idToClientId table
   *
   * @param pid
   */
  public void setId(String pid) {
    if(pid != null) {
      this.id = pid;
      if (this.clientId != null) {
        idToClientId.put(this.id, this.clientId);
      }
    }
  }

  public void setStreamId(String pstreamId) {
    if(pstreamId != null) {
      this.streamId = pstreamId;
    }
  }

  public void setTime(Double ptime) {
    if(ptime != null) {
      this.time = ptime;
    }
  }

  public void setType(String ptype) {
    if(ptype != null) {
      this.type = ptype;
    }
  }

  public void setCreated(Double pcreated) {
    if(pcreated != null) {
      this.created = pcreated;
    }
  }

  public void setCreatedBy(String pcreatedBy) {
    if(pcreatedBy != null) {
      this.createdBy = pcreatedBy;
    }
  }

  public void setModified(Double pmodified) {
    if(pmodified != null) {
      this.modified = pmodified;
    }
  }

  public void setModifiedBy(String pmodifiedBy) {
    if(pmodifiedBy != null) {
      this.modifiedBy = pmodifiedBy;
    }
  }

  public void setDuration(Double pduration) {
    if(pduration != null) {
      this.duration = pduration;
    }
  }

  public void setContent(Object pcontent) {
    if(pcontent != null) {
      this.content = pcontent;
    }
  }

  public void setTags(Set<String> ptags) {
    if(ptags != null) {
      this.tags = ptags;
    }
  }

  public void setReferences(Set<String> preferences) {
    if(preferences != null) {
      this.references = preferences;
    }
  }

  public void setDescription(String pdescription) {
    if(pdescription != null) {
      this.description = pdescription;
    }
  }

  public void setAttachments(Set<Attachment> pattachments) {
    if(pattachments != null) {
      this.attachments = pattachments;
    }
  }

  public void setClientData(Map<String, Object> pclientData) {
    if(clientData != null) {
      this.clientData = pclientData;
    }
  }

  public void setTrashed(Boolean ptrashed) {
    if(ptrashed != null) {
      this.trashed = ptrashed;
    }
  }


}
