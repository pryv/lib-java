package com.pryv.api.model;

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

  public void merge(Attachment temp) {
    id = temp.getId();
    fileName = temp.getFileName();
    type = temp.getType();
    size = temp.getSize();
    readToken = temp.getReadToken();
  }

}
