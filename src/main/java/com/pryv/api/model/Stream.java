package com.pryv.api.model;

import java.lang.ref.WeakReference;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.pryv.Connection;
import com.pryv.api.database.QueryGenerator;

/**
 *
 * Stream object from Pryv API
 *
 * @author ik
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Stream {

  private String id;
  private String name;
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
  private Boolean trashed;
  private Set<Stream> children; // should be removed - but need to find a way
                                 // to serialize map properly
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
   * @param pCreated
   * @param pCreatedBy
   * @param pModified
   * @param pModifiedBy
   */
  public Stream(String pId, String pName, String pParentId, Boolean pSingleActivity,
    Map<String, Object> pClientData, Set<Stream> pChildren, Boolean pTrashed, Double pCreated,
    String pCreatedBy, Double pModified, String pModifiedBy) {
    id = pId;
    name = pName;
    parentId = pParentId;
    singleActivity = pSingleActivity;
    clientData = pClientData;
    children = pChildren;
    if (pChildren != null) {
      for (Stream stream : pChildren) {
        childrenMap.put(stream.getId(), stream);
      }
    }
    trashed = pTrashed;
    created = pCreated;
    createdBy = pCreatedBy;
    modified = pModified;
    modifiedBy = pModifiedBy;
  }

  /**
   * build Stream when retrieved from cache
   *
   * @param result
   * @throws SQLException
   */
  public Stream(ResultSet result) throws SQLException {
    id = result.getString(QueryGenerator.STREAMS_ID_KEY);
    name = result.getString(QueryGenerator.STREAMS_NAME_KEY);
    trashed = result.getBoolean(QueryGenerator.STREAMS_TRASHED_KEY);
    created = result.getDouble(QueryGenerator.STREAMS_CREATED_KEY);
    createdBy = result.getString(QueryGenerator.STREAMS_CREATED_BY_KEY);
    modified = result.getDouble(QueryGenerator.STREAMS_MODIFIED_KEY);
    modifiedBy = result.getString(QueryGenerator.STREAMS_MODIFIED_BY_KEY);
    parentId = result.getString(QueryGenerator.STREAMS_PARENT_ID_KEY);
    singleActivity = result.getBoolean(QueryGenerator.STREAMS_SINGLE_ACTIVITY_KEY);
    setClientDataFromAString(result.getString(QueryGenerator.STREAMS_CLIENT_DATA_KEY));
  }

  /**
   * Empty Constructor
   */
  public Stream() {
    trashed = false;
  }

  /**
   * Assign unique ID to the Stream - to execute ONCE upon creation
   */
  public void generateId() {
    id = UUID.randomUUID().toString();
  }

  /**
   * Assign a weak reference to the Connection
   *
   * @param connection
   */
  public void assignConnection(WeakReference<Connection> pWeakConnection) {
    weakConnection = pWeakConnection;
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
   * Copy all of stream temp's values into the caller Stream.
   *
   * @param temp
   *          the stream whose fields are copied
   * @param withChildren
   *          if set to true, children are also merged
   */
  public void merge(Stream temp, boolean withChildren) {
    weakConnection = temp.weakConnection;
    id = temp.id;
    name = temp.name;
    setParentId(temp.parentId);
    singleActivity = temp.singleActivity;
    if (temp.clientData != null) {
      clientData = new HashMap<String, Object>();
      for (String key : temp.clientData.keySet()) {
        clientData.put(key, temp.clientData.get(key));
      }
    }
    if (temp.children != null && withChildren == true) {
      this.clearChildren();
      for (Stream childStream : temp.children) {
        addChildStream(childStream);
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
  public void setClientDataFromAString(String source) {
    if (source != null) {
      if (source.length() > 0) {
        String[] cdPairs = source.split(":");
        if (clientData == null) {
          clientData = new HashMap<String, Object>();
        }
        clientData.put(cdPairs[0], cdPairs[1]);
      }
    }
  }

  /**
   * Add a stream as child to caller. If children list & map are null,
   * instantiates them. The child's parentId and parentClientId fields are
   * updated.
   *
   * @param childStream
   */
  public void addChildStream(Stream childStream) {
    if (childrenMap == null || children == null) {
      children = new HashSet<Stream>();
      childrenMap = new HashMap<String, Stream>();
    }
    children.add(childStream);
    childrenMap.put(childStream.getId(), childStream);
  }

  /**
   * Removes a child Stream from the Stream. If no more children are left, the
   * children and childrenMap fields are set to null. The child's parentId and
   * parentClientId are set to null.
   *
   * @param childStream
   *          the child Stream to remove
   */
  public void removeChildStream(Stream childStream) {
    // System.out.println("Stream: about to remove child from cid=" + clientId);
    System.out.println("Stream: about to remove child from id=" + id);
    if (children != null && childrenMap != null) {
      System.out.println("childrenSize="
        + children.size()
          + ", childrenMapSize="
          + childrenMap.size());
      childrenMap.remove(childStream.getId());
      children.remove(childStream);
      if (childrenMap.size() == 0 || children.size() == 0) {
        childrenMap = null;
        children = null;
      }
    }
  }

  /**
   * remove all the child Streams of the Stream.
   */
  public void clearChildren() {
    children = null;
    childrenMap = null;
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

  public Boolean getSingleActivity() {
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
    return trashed;
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

  public void setId(String pId) {
    this.id = pId;
  }

  public void setName(String pName) {
    this.name = pName;
  }

  public void setParentId(String pParentId) {
    this.parentId = pParentId;
  }

  public void setSingleActivity(Boolean pSingleActivity) {
    this.singleActivity = pSingleActivity;
  }

  public void setClientData(Map<String, Object> pClientData) {
    this.clientData = pClientData;
  }

  public void setChildren(Set<Stream> pChildren) {
    this.children = pChildren;
    if (pChildren != null) {
      for (Stream stream : pChildren) {
        if (childrenMap == null) {
          childrenMap = new HashMap<String, Stream>();
        }
        childrenMap.put(stream.getId(), stream);
      }
    }
  }

  public void setTrashed(Boolean pTrashed) {
    this.trashed = pTrashed;
  }

  public void setCreated(Double pCreated) {
    this.created = pCreated;
  }

  public void setCreatedBy(String pCreatedBy) {
    this.createdBy = pCreatedBy;
  }

  public void setModified(Double pModified) {
    this.modified = pModified;
  }

  public void setModifiedBy(String pModifiedBy) {
    this.modifiedBy = pModifiedBy;
  }

}
