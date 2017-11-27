package com.pryv.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 *
 * Stream object from Pryv API
 *
 * @author ik
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Stream extends ApiResource {

  private String id;
  private String name;
  private Double created;
  private String createdBy;
  private Double modified;
  private String modifiedBy;

  // optional
  private Boolean trashed;
  @JsonIgnore
  private Boolean deleted;
  private Set<Stream> children;

  @JsonIgnore
  private Map<String, Stream> childrenMap;

  private String parentId;

  private Boolean singleActivity;
  private Map<String, Object> clientData;

  /**
   * Stream object Constructor with all fields
   *
   * @param pId
   * @param pName
   * @param pParentId
   *          optional
   * @param pSingleActivity
   *          optional
   * @param pClientData
   *          optional
   * @param pChildren
   * @param pTrashed
   * @param pDeleted
   * @param pCreated
   * @param pCreatedBy
   * @param pModified
   * @param pModifiedBy
   */
  public Stream(String pId, String pName, String pParentId, Boolean pSingleActivity,
    Map<String, Object> pClientData, Set<Stream> pChildren, Boolean pTrashed, Boolean pDeleted, Double pCreated,
    String pCreatedBy, Double pModified, String pModifiedBy) {
    id = pId;
    name = pName;
    parentId = pParentId;
    singleActivity = pSingleActivity;
    clientData = pClientData;
    children = pChildren;
    if (pChildren != null) {
      for (Stream stream : pChildren) {
        addChildStream(stream);
      }
    }
    trashed = pTrashed;
    deleted = pDeleted;
    created = pCreated;
    createdBy = pCreatedBy;
    modified = pModified;
    modifiedBy = pModifiedBy;
  }

  /**
   * minimal Constructor. Requires only mandatory fields. If id field is null a
   * random id is generated using the UUID algorithm.
   *
   * @param id
   *          optional (leave null if not useful)
   * @param name
   *          mandatory
   */
  public Stream(String id, String name) {
    if (id == null) {
      generateId();
    } else {
      this.id = id;
    }
    this.name = name;
  }

  /**
   * empty constructor
   */
  public Stream() {
  }

  /**
   * Assign unique ID to the Stream - to execute ONCE upon creation
   */
  private void generateId() {
    this.id = UUID.randomUUID().toString();
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
  public Stream setClientDataFromAString(String source) {
    if (source != null) {
      if (source.length() > 0) {
        String[] cdPairs = source.split(":");
        if (clientData == null) {
          clientData = new HashMap<String, Object>();
        }
        clientData.put(cdPairs[0], cdPairs[1]);
      }
    }
    return this;
  }

  /**
   * Returns <tt>true</tt> if the stream with id <tt>streamId</tt> is a child or
   * descendent, <tt>false</tt> otherwise.
   *
   * @param streamId
   *          the id of the potential child stream
   * @return
   */
  public boolean hasChild(String streamId) {
    if (this.id.equals(streamId)) {
      return true;
    }
    boolean res = false;
    if (children != null) {
      for (Stream child : children) {
        res = child.hasChild(streamId);
      }
    }
    return res;
  }

  /**
   * Add a stream as child to caller. If children list {@literal &} map are null,
   * instantiates them. The child's parentId field is updated.
   *
   * @param childStream
   */
  public Stream addChildStream(Stream childStream) {
    if (childrenMap == null || children == null) {
      children = new HashSet<Stream>();
      childrenMap = new HashMap<String, Stream>();
    }
    if (!childStream.hasChild(this.id)) {
      childStream.setParentId(this.id);
      children.add(childStream);
      childrenMap.put(childStream.getId(), childStream);
    } else {
      System.out.println("Error: Stream.addChildStream() - no cycles allowed");
    }
    return this;
  }

  /**
   * Removes a child Stream from the Stream. If no more children are left, the
   * children and childrenMap fields are set to null. The child's parentId is set to null.
   *
   * @param childStream
   *          the child Stream to remove
   */
  public Stream removeChildStream(Stream childStream) {
    if (children != null && childrenMap != null) {
      childrenMap.remove(childStream.getId());
      children.remove(childStream);
      childStream.setParentId(null);
      if (childrenMap.size() == 0 || children.size() == 0) {
        childrenMap = null;
        children = null;
      }
    } else {
      System.out.println("Error: Trying to remove a child that is not registered as such.");
    }
    return this;
  }

  /**
   * remove all the child Streams of the Stream.
   */
  public Stream clearChildren() {
    children = null;
    childrenMap = null;
    return this;
  }

  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer("Stream: id= " + id);
    if (children != null) {
      buffer.append("[");
      String prefix = "";
      for (Stream child : children) {
        buffer.append(prefix + child);
        prefix = ",";
      }
      buffer.append("]");
    }
    return buffer.toString();
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getParentId() {
    return parentId;
  }

  public Boolean isSingleActivity() {
    return singleActivity;
  }

  public Map<String, Object> getClientData() {
    return clientData;
  }

  public Map<String, Stream> getChildrenMap() {
    return childrenMap;
  }

  public Set<Stream> getChildren() {
    return children;
  }

  public Boolean isTrashed() {
    return trashed == null ? false: trashed;
  }

  public Boolean isDeleted() {
    return deleted == null ? false: deleted;
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

  public Stream setId(String id) {
    this.id = id;
    return this;
  }

  public Stream setName(String name) {
    this.name = name;
    return this;
  }

  public Stream setParentId(String parentId) {
    this.parentId = parentId;
    return this;
  }

  public Stream setSingleActivity(Boolean singleActivity) {
    this.singleActivity = singleActivity;
    return this;
  }

  public Stream setClientData(Map<String, Object> clientData) {
    this.clientData = clientData;
    return this;
  }

  public Stream setChildren(Set<Stream> children) {
    this.children = children;
    if (children != null) {
      for (Stream stream : children) {
        if (childrenMap == null) {
          childrenMap = new HashMap<String, Stream>();
        }
        childrenMap.put(stream.getId(), stream);
      }
    }
    return this;
  }

  public Stream setTrashed(Boolean trashed) {
    this.trashed = trashed;
    return this;
  }

  public Stream setDeleted(Boolean deleted) {
    this.deleted = deleted;
    return this;
  }

  public Stream setCreated(Double created) {
    this.created = created;
    return this;
  }

  public Stream setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
    return this;
  }

  public Stream setModified(Double modified) {
    this.modified = modified;
    return this;
  }

  public Stream setModifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
    return this;
  }

  public Stream cloneMutableFields() {
    return new Stream()
            .setId(null)
            .setParentId(this.parentId)
            .setName(this.name)
            .setTrashed(this.trashed)
            .setSingleActivity(this.singleActivity)
            .setClientData(this.clientData);
  }

}
