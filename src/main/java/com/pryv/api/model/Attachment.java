package com.pryv.api.model;

import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *
 * Attachment from Pryv API
 *
 * @author ik
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Attachment implements Cloneable {

  private String id;
  private String fileName;
  private String type;
  private int size;
  private String readToken;

  public Attachment() {
  };

  /**
   * instanciate Attachment object from params.
   *
   * @param pId
   * @param pFilename
   * @param pType
   * @param pSize
   * @param pReadToken
   */
  public Attachment(String pId, String pFilename, String pType, int pSize, String pReadToken) {
    id = pId;
    fileName = pFilename;
    type = pType;
    size = pSize;
    readToken = pReadToken;
  }

  /**
   * instanciate Attachment object from JSON
   *
   * @param pJsonAttachmentString
   */
  public Attachment(String pJsonAttachmentString) {
    JSONObject jsonAttachment = new JSONObject(pJsonAttachmentString);
    id = jsonAttachment.optString(JsonFields.ID.toString(), "void");
    fileName = jsonAttachment.getString(JsonFields.FILENAME.toString());
    type = jsonAttachment.getString(JsonFields.TYPE.toString());
    size = jsonAttachment.getInt(JsonFields.NUMBER.toString());
    readToken = jsonAttachment.getString(JsonFields.READ_TOKEN.toString());
  }


  public String getId() {
    return id;
  }

  public String getFileName() {
    return fileName;
  }

  public String getType() {
    return type;
  }

  public int getSize() {
    return size;
  }

  public String getReadToken() {
    return readToken;

  }

  public String toJson() {
    JSONObject jsonAttachment = new JSONObject();
    jsonAttachment.putOpt(JsonFields.ID.toString(), id);
    jsonAttachment.putOpt(JsonFields.FILENAME.toString(), fileName);
    jsonAttachment.putOpt(JsonFields.TYPE.toString(), type);
    jsonAttachment.putOpt(JsonFields.NUMBER.toString(), size);
    jsonAttachment.putOpt(JsonFields.READ_TOKEN.toString(), readToken);
    return jsonAttachment.toString();
  }

  public void merge(Attachment temp) {
    id = temp.getId();
    fileName = temp.getFileName();
    type = temp.getType();
    size = temp.getSize();
    readToken = temp.getReadToken();
  }

}
