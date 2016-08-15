package com.pryv.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.io.File;

/**
 *
 * Attachment from Pryv API
 *
 * @author ik
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Attachment implements Cloneable {

  private String id;
  private String fileName;
  private String type;
  /**
   * the size in Bytes
   */
  private long size;
  private String readToken;

  @JsonIgnore
  private File file;

  /**
   * empty constructor
   */
  public Attachment() {
  };

  /**
   * instantiate Attachment object with parameters
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

  public void setId(String id) {
    this.id = id;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public void setReadToken(String readToken) {
    this.readToken = readToken;
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

  public long getSize() {
    return size;
  }

  public String getReadToken() {
    return readToken;
  }

  public File getFile() {
    return file;
  }

  /**
   * copy all fields from temp to the calling Attachment
   *
   * @param temp
   *          other Attachment whose fields are copied
   */
  public void merge(Attachment temp) {
    id = temp.getId();
    fileName = temp.getFileName();
    type = temp.getType();
    size = temp.getSize();
    readToken = temp.getReadToken();
  }

  public void setFile(File file) {
    this.file = file;
    this.size = file.length();
  }

}
