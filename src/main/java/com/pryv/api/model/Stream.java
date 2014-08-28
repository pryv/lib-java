package com.pryv.api.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pryv.api.database.QueryGenerator;

/**
 *
 * Stream object from Pryv API
 *
 * @author ik
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Stream {

  private String id;
  private String name;
  private Long created;
  private String createdBy;
  private Long modified;
  private String modifiedBy;

  // optional
  private Boolean trashed;
  private List<Stream> children;
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
    Map<String, Object> pClientData, List<Stream> pChildren, Boolean pTrashed, Long pCreated,
    String pCreatedBy, Long pModified, String pModifiedBy) {
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
    created = result.getLong(QueryGenerator.STREAMS_CREATED_KEY);
    createdBy = result.getString(QueryGenerator.STREAMS_CREATED_BY_KEY);
    modified = result.getLong(QueryGenerator.STREAMS_MODIFIED_KEY);
    modifiedBy = result.getString(QueryGenerator.STREAMS_MODIFIED_BY_KEY);
    parentId = result.getString(QueryGenerator.STREAMS_PARENT_ID_KEY);
    singleActivity = result.getBoolean(QueryGenerator.STREAMS_SINGLE_ACTIVITY_KEY);
    setClientDataFromAstring(result.getString(QueryGenerator.STREAMS_CLIENT_DATA_KEY));
  }

  /**
   * Empty Constructor
   */
  public Stream() {
  }

  /**
   * Copy all temp Stream's values into caller Stream.
   *
   * @param temp
   */
  public void merge(Stream temp) {
    id = temp.id;
    name = temp.name;
    parentId = temp.parentId;
    singleActivity = temp.singleActivity;
    if (temp.clientData != null) {
      clientData = new HashMap<String, Object>();
      for (String key : temp.clientData.keySet()) {
        clientData.put(key, temp.clientData.get(key));
      }
    }
    if (temp.children != null) {
      children = new ArrayList<Stream>();
      for (Stream childStream : temp.children) {
        children.add(childStream);
      }
      childrenMap = new HashMap<String, Stream>();
      for (Stream childStream : temp.childrenMap.values()) {
        childrenMap.put(childStream.getId(), childStream);
      }
    }
    trashed = temp.trashed;
    created = temp.created;
    createdBy = temp.createdBy;
    modified = temp.modified;
    modifiedBy = temp.modifiedBy;
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
   * Add a stream as child to caller. If children list is null, instanciates it.
   *
   * @param childStream
   */
  public void addChildStream(Stream childStream) {
    if (childrenMap == null || children == null) {
      children = new ArrayList<Stream>();
      childrenMap = new HashMap<String, Stream>();
    }
    children.add(childStream);
    childrenMap.put(childStream.getId(), childStream);
  }

  /**
   * Removes a child Stream from the Stream. If no more children are left, the
   * children and childrenMap fields are set to null.
   *
   * @param childStream
   *          the child Stream to remove
   */
  public void removeChildStream(Stream childStream) {
    childrenMap.remove(childStream.getId());
    children.remove(childStream);
    if (childrenMap.size() == 0 || children.size() == 0) {
      childrenMap = null;
      children = null;
    }
  }

  /**
   * remove all the child Streams of the Stream.
   */
  public void clearChildren() {
    children = null;
    childrenMap = null;
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

  public List<Stream> getChildren() {
    return children;
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

  public void setChildren(List<Stream> pChildren) {
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

  public void setCreated(Long pCreated) {
    this.created = pCreated;
  }

  public void setCreatedBy(String pCreatedBy) {
    this.createdBy = pCreatedBy;
  }

  public void setModified(Long pModified) {
    this.modified = pModified;
  }

  public void setModifiedBy(String pModifiedBy) {
    this.modifiedBy = pModifiedBy;
  }

}
