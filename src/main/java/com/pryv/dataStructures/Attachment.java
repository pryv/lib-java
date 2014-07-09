package com.pryv.dataStructures;

import org.json.JSONObject;

/**
 *
 * Attachment from Pryv API
 *
 * @author ik
 *
 */
public class Attachment {

  private String id;
  private String filename;
  private String type;
  private int number;
  private String readToken;

  public Attachment() {
  };

  /**
   * instanciate Attachment object from params.
   *
   * @param pId
   * @param pFilename
   * @param pType
   * @param pNumber
   * @param pReadToken
   */
  public Attachment(String pId, String pFilename, String pType, int pNumber, String pReadToken) {
    id = pId;
    filename = pFilename;
    type = pType;
    number = pNumber;
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
    filename = jsonAttachment.getString(JsonFields.FILENAME.toString());
    type = jsonAttachment.getString(JsonFields.TYPE.toString());
    number = jsonAttachment.getInt(JsonFields.NUMBER.toString());
    readToken = jsonAttachment.getString(JsonFields.READ_TOKEN.toString());
  }

  public String getId() {
    return id;
  }

  public String getFilename() {
    return filename;
  }

  public String getType() {
    return type;
  }

  public int getNumber() {
    return number;
  }

  public String getReadToken() {
    return readToken;
  }

  public String toJson() {
    JSONObject jsonAttachment = new JSONObject();
    jsonAttachment.putOpt(JsonFields.ID.toString(), id);
    jsonAttachment.putOpt(JsonFields.FILENAME.toString(), filename);
    jsonAttachment.putOpt(JsonFields.TYPE.toString(), type);
    jsonAttachment.putOpt(JsonFields.NUMBER.toString(), number);
    jsonAttachment.putOpt(JsonFields.READ_TOKEN.toString(), readToken);
    return jsonAttachment.toString();
  }

}
