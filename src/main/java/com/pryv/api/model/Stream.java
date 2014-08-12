package com.pryv.api.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rits.cloning.Cloner;

/**
 *
 * Stream object from Pryv API
 *
 * @author ik
 *
 */
public class Stream {

  private String id;
  private String name;
  private String parentId;
  private Boolean singleActivity;
  private Map<String, Object> clientData;
  private List<Stream> children;
  private Boolean trashed;
  private long created;
  private String createdBy;
  private long modified;
  private String modifiedBy;

  public Stream(String pId, String pName, String pParentId, Boolean pSingleActivity,
    Map<String, Object> pClientData, List<Stream> pChildren, Boolean pTrashed, long pCreated,
    String pCreatedBy, long pModified, String pModifiedBy) {
    id = pId;
    name = pName;
    parentId = pParentId;
    singleActivity = pSingleActivity;
    clientData = pClientData;
    children = pChildren;
    trashed = pTrashed;
    created = pCreated;
    createdBy = pCreatedBy;
    modified = pModified;
    modifiedBy = pModifiedBy;
  }

  public Stream() {
  }

  public void merge(Stream temp, Cloner cloner) {
    id = temp.id;
    name = temp.name;
    parentId = temp.parentId;
    singleActivity = temp.singleActivity;
    clientData = new HashMap<String, Object>();
    for (String key : temp.clientData.keySet()) {
      clientData.put(key, temp.clientData.get(key));
    }
    // maybe do recursive merge (compare ids, if exists merge, else new...)
    children = new ArrayList<Stream>();
    for (Stream stream : temp.children) {
      children.add(cloner.deepClone(stream));
    }
    trashed = temp.trashed;
    created = temp.created;
    createdBy = temp.createdBy;
    modified = temp.modified;
    modifiedBy = temp.modifiedBy;
  }

  /**
   * format client data to printable.
   *
   * @param clientData
   *          the client data to format
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

    }
    return sb.toString();
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

  public List<Stream> getChildren() {
    return children;
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
  }

  public void setTrashed(Boolean pTrashed) {
    this.trashed = pTrashed;
  }

  public void setCreated(long pCreated) {
    this.created = pCreated;
  }

  public void setCreatedBy(String pCreatedBy) {
    this.createdBy = pCreatedBy;
  }

  public void setModified(long pModified) {
    this.modified = pModified;
  }

  public void setModifiedBy(String pModifiedBy) {
    this.modifiedBy = pModifiedBy;
  }

}
