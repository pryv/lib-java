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

  public Attachment setId(String id) {
    this.id = id;
    return this;
  }

  public Attachment setFileName(String fileName) {
    this.fileName = fileName;
    return this;
  }

  public Attachment setType(String type) {
    this.type = type;
    return this;
  }

  public Attachment setSize(long size) {
    this.size = size;
    return this;
  }

  public Attachment setReadToken(String readToken) {
    this.readToken = readToken;
    return this;
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

  public Attachment setFile(File file) {
    this.file = file;
    this.size = file.length();
    return this;
  }

}
