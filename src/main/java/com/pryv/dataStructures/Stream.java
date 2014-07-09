package com.pryv.dataStructures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

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
  private Map<String, String> clientData;
  private List<Stream> children;
  private Boolean trashed;
  private long created;
  private String createdBy;
  private long modified;
  private String modifiedBy;

  public Stream(String pId, String pName, String pParentId, Boolean pSingleActivity,
      Map<String, String> pClientData, List<Stream> pChildren, Boolean pTrashed, long pCreated,
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

  /**
   * Construct a Stream from a JSON
   *
   * @param pJsonStream
   */
  public Stream(String pJsonStream) {
    JSONObject jsonStream = new JSONObject(pJsonStream);
    id = jsonStream.getString(JsonFields.ID.toString());
    name = jsonStream.optString(JsonFields.NAME.toString());
    parentId = jsonStream.optString(JsonFields.PARENT_ID.toString());
    singleActivity = jsonStream.optBoolean(JsonFields.SINGLE_ACTIVITY.toString());
    JSONObject jsonClientData = jsonStream.optJSONObject(JsonFields.CLIENT_DATA.toString());
    if (jsonClientData != null) {
      String cdKey = (String) jsonClientData.keySet().toArray()[0];
      clientData = new HashMap<String, String>();
      clientData.put(cdKey, jsonClientData.optString(cdKey));
    }
    JSONArray jsonChildren = jsonStream.optJSONArray(JsonFields.CHILDREN.toString());
    if (jsonChildren != null) {
      children = new ArrayList<Stream>();
      for (int i = 0; i < jsonChildren.length(); i++) {
        children.add(new Stream(jsonChildren.optJSONObject(i).toString()));
      }
    }
    trashed = jsonStream.optBoolean(JsonFields.TRASHED.toString());
    created = jsonStream.optLong(JsonFields.CREATED.toString());
    createdBy = jsonStream.optString(JsonFields.CREATED_BY.toString());
    modified = jsonStream.optLong(JsonFields.MODIFIED.toString());
    modifiedBy = jsonStream.optString(JsonFields.MODIFIED_BY.toString());
  }

  public String toJson() {
    JSONObject jsonStream = new JSONObject();
    jsonStream.put(JsonFields.ID.toString(), id);
    jsonStream.put(JsonFields.NAME.toString(), name);
    jsonStream.putOpt(JsonFields.PARENT_ID.toString(), parentId);
    jsonStream.putOpt(JsonFields.SINGLE_ACTIVITY.toString(), singleActivity);
    jsonStream.putOpt(JsonFields.CLIENT_DATA.toString(), new JSONObject(clientData));
    JSONArray jsonChildren = new JSONArray(children);
    jsonStream.putOpt(JsonFields.CHILDREN.toString(), jsonChildren);
    jsonStream.putOpt(JsonFields.TRASHED.toString(), trashed);
    jsonStream.putOpt(JsonFields.CREATED.toString(), created);
    jsonStream.putOpt(JsonFields.CREATED_BY.toString(), createdBy);
    jsonStream.putOpt(JsonFields.MODIFIED.toString(), modified);
    jsonStream.putOpt(JsonFields.MODIFIED_BY.toString(), modifiedBy);
    return jsonStream.toString();
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

  public Map<String, String> getClientData() {
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


}
