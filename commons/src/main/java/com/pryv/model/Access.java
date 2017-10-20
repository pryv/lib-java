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

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

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
   * a weak reference to the connection to which the Access is linked
   */
  @JsonIgnore
  private WeakReference<AbstractConnection> weakConnection;

  /**
   * used in order to prevent instantiating an Access multiple times.
   */
  private static Map<String, Access> supervisor = new WeakHashMap<String, Access>();

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

    this.updateSupervisor();
  }

  /**
   * Build an Access from a ResultSet, used when retrieving Access objects from the SQLite Cache.
   * This takes care of instantiating a new Access only in the case when it isn't existing yet.
   *
   * @param result The ResultSet
   * @return The Acccess
   * @throws SQLException
   * @throws IOException
   */
  public static Access createOrReuse(ResultSet result) throws SQLException, IOException {
    // TODO: Add query keys
    String id = result.getString(QueryGenerator.ACCESSES_ID_KEY);
    Access access = supervisor.get(id);
    if (access == null) {
      access = new Access();
    }

    access.setId(result.getString(QueryGenerator.ACCESSES_ID_KEY));
    access.setToken(result.getString(QueryGenerator.ACCESSES_TOKEN_KEY));
    access.setName(result.getString(QueryGenerator.ACCESSES_NAME_KEY));
    // TODO: Deserialize and set permissions
    access.setCreated(result.getDouble(QueryGenerator.ACCESSES_CREATED_KEY));
    access.setCreatedBy(result.getString(QueryGenerator.ACCESSES_CREATED_BY_KEY));
    access.setModified(result.getDouble(QueryGenerator.ACCESSES_MODIFIED_KEY));
    access.setModifiedBy(result.getString(QueryGenerator.ACCESSES_MODIFIED_BY_KEY));
    access.setType(result.getString(QueryGenerator.ACCESSES_TYPE_KEY));
    access.setDeviceName(result.getString(QueryGenerator.ACCESSES_DEVICE_NAME_KEY));
    access.setLastUsed(result.getDouble(QueryGenerator.ACCESSES_LAST_USED_KEY));

    return access;
  }

  /**
   * saves the Access in the supervisor if needed
   *
   * @return the Access
   */
  public static Access createOrReuse(Access access) {
    String id = access.getId();
    // TODO: merge - not replace
    supervisor.put(id, access);
    return access;
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
   * Returns the reference to the ConnectionOld to which the Access is linked if
   * any.
   *
   * @return
   */
  public AbstractConnection getWeakConnection() {
    return weakConnection.get();
  }

  /**
   * Copy all temp Access's values into caller Access.
   *
   * @param temp
   *          the Access from which the fields are merged
   * @param cloner
   *          com.rits.cloning.Cloner instance from JsonConverter util class
   */
  public void merge(Access temp, Cloner cloner) {
    weakConnection = temp.weakConnection;

    id = temp.id;
    token = temp.token;
    name = temp.name;

    permissions = new ArrayList<>();
    for (Permission permission : temp.permissions) {
      permissions.add(cloner.deepClone(permission));
    }

    created = temp.created;
    createdBy = temp.createdBy;
    modified = temp.modified;
    modifiedBy = temp.modifiedBy;
    type = temp.type;
    deviceName = temp.deviceName;
    lastUsed = temp.lastUsed;

    temp = null;
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