package com.pryv.model;

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
import com.pryv.database.QueryGenerator;

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
  public void generateId() {
    this.id = UUID.randomUUID().toString();
  }

  /**
   * Assign a weak reference to the ConnectionOld
   *
   * @param weakConnection
   */
  public void assignConnection(WeakReference<Connection> weakConnection) {
    this.weakConnection = weakConnection;
  }

  /**
   * Returns the reference to the ConnectionOld to which the Event is linked if
   * any.
   *
   * @return
   */
  public Connection getWeakConnection() {
    return weakConnection.get();
  }

  /**
   * Copy all of Stream <tt>updatedStream</tt> values into the caller Stream.
   *
   * @param updatedStream
   *          the Stream whose fields are copied
   * @param withChildren
   *          if set to <tt>true</tt>, children are also merged
   */
  public void merge(Stream updatedStream, boolean withChildren) {
    weakConnection = updatedStream.weakConnection;
    setId(updatedStream.id);
    setName(updatedStream.name);
    setParentId(updatedStream.parentId);
    setSingleActivity(updatedStream.singleActivity);
    setTrashed(updatedStream.trashed);
    setCreated(updatedStream.created);
    setCreatedBy(updatedStream.createdBy);
    setModified(updatedStream.modified);
    setModifiedBy(updatedStream.modifiedBy);
    if (updatedStream.clientData != null) {
      clientData = new HashMap<String, Object>();
      for (String key : updatedStream.clientData.keySet()) {
        clientData.put(key, updatedStream.clientData.get(key));
      }
    }
    if (updatedStream.children != null && withChildren == true) {
      this.clearChildren();
      for (Stream childStream : updatedStream.children) {
        addChildStream(childStream);
      }
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
    if (!childStream.hasChild(this.id)) {
      childStream.setParentId(this.id);
      children.add(childStream);
      childrenMap.put(childStream.getId(), childStream);
    } else {
      System.out.println("Error: Stream.addChildStream() - no cycles allowed");
    }
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
    if (trashed == null) {
      return false;
    } else
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
    if(pId != null) {
      this.id = pId;
    }
  }

  public void setName(String pName) {
    if(pName != null) {
      this.name = pName;
    }
  }

  public void setParentId(String pParentId) {
    if(pParentId !=null) {
      this.parentId = pParentId;
    }
  }

  public void setSingleActivity(Boolean pSingleActivity) {
    if(pSingleActivity != null) {
      this.singleActivity = pSingleActivity;
    }
  }

  public void setClientData(Map<String, Object> pClientData) {
    if(pClientData != null) {
      this.clientData = pClientData;
    }
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
    if(pTrashed !=null) {
      this.trashed = pTrashed;
    }
  }

  public void setCreated(Double pCreated) {
    if(created != null) {
      this.created = pCreated;
    }
  }

  public void setCreatedBy(String pCreatedBy) {
    if(pCreatedBy != null) {
      this.createdBy = pCreatedBy;
    }
  }

  public void setModified(Double pModified) {
    if(pModified != null) {
      this.modified = pModified;
    }
  }

  public void setModifiedBy(String pModifiedBy) {
    if(pModifiedBy != null) {
      this.modifiedBy = pModifiedBy;
    }
  }

}
