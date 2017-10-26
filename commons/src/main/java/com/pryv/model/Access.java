package com.pryv.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.pryv.utils.Cuid;

import java.util.ArrayList;

/**
 * Access data structure from Pryv
 *
 * @author tm
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Access extends ApiResource {

  private String id;
  private String token;
  private String name;
  private ArrayList<Permission> permissions;
  private Double created;
  private String createdBy;
  private Double modified;
  private String modifiedBy;

  // optional
  private String type;
  private String deviceName;
  private Double lastUsed;

  /**
   * empty Access constructor
   */
  public Access() {
  }

  /**
   * Constructor for Access object with mandatory fields
   *
   * @param name
   * @param permissions
   */
  public Access(String name, ArrayList<Permission> permissions) {
    this.name = name;
    this.permissions = permissions;
  }

  /**
   * Construct Access object from parameters
   *
   * @param pId
   * @param pToken
   * @param pName
   * @param pPermissions
   * @param pCreated
   * @param pCreatedBy
   * @param pModified
   * @param pModifiedBy
   *
   * @param pType
   *          optional
   * @param pDeviceName
   *          optional
   * @param pLastUsed
   *          optional
   */
  public Access(String pId, String pToken, String pName, ArrayList<Permission> pPermissions,
                Double pCreated, String pCreatedBy, Double pModified, String pModifiedBy,
                String pType, String pDeviceName, Double pLastUsed) {
    id = pId;
    token = pToken;
    name = pName;
    permissions = pPermissions;
    created = pCreated;
    createdBy = pCreatedBy;
    modified = pModified;
    modifiedBy = pModifiedBy;

    type = pType;
    deviceName = pDeviceName;
    lastUsed = pLastUsed;
  }

  /**
   * Assign unique identifier to the Access - does nothing if Access has already a id field
   */
  private String generateId() {
    if (this.id == null) {
      this.id = Cuid.createCuid();
    }
    return this.id;
  }

  @Override
  public String toString() {
    return "{\"id\":\"" + id + "\","
            + "\"token\":\"" + token + "\","
            + "\"name\":\"" + name + "\","
            + "\"permissions\":\"" + permissions + "\","
            + "\"created\":\"" + created + "\","
            + "\"createdBy\":\"" + createdBy + "\","
            + "\"modified\":\"" + modified + "\","
            + "\"modifiedBy\":\"" + modifiedBy + "\","
            + "\"type\":\"" + type + "\","
            + "\"deviceName\":\"" + deviceName + "\","
            + "\"lastUsed\":\"" + lastUsed + "\"}";
  }

  public String getId() {
    return id;
  }

  public String getToken() {
    return token;
  }

  public String getName() {
    return name;
  }

  public ArrayList<Permission> getPermissions() {
    return permissions;
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

  public String getType() {
    return type;
  }

  public String getDeviceName() {
    return deviceName;
  }

  public Double getLastUsed() {
    return lastUsed;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPermissions(ArrayList<Permission> permissions) {
    this.permissions = permissions;
  }

  public void addPermission(Permission permission) {
    if (permissions == null) {
      permissions = new ArrayList<Permission>();
    }
    permissions.add(permission);
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

  public void setType(String type) {
    this.type = type;
  }

  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }

  public void setLastUsed(Double lastUsed) {
    this.lastUsed = lastUsed;
  }

}