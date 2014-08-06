package com.pryv.api.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;

/**
 *
 * Permission used in authentication
 *
 * @author ik
 *
 */
@JsonSerialize(include = Inclusion.NON_NULL)
public class Permission {


  private String streamId;
  private Level level;
  private String defaultName; // only used when requesting permission

  /**
   *
   *
   * @param pStreamId
   *          id of requested Stream, use "*" for all Streams
   * @param pLevel
   *          The required permission level
   * @param pDefaultName
   *          optional: The name to create the Stream if needed
   */
  public Permission(String pStreamId, Level pLevel, String pDefaultName) {
    streamId = pStreamId;
    level = pLevel;
    defaultName = pDefaultName;
  }

  public Permission() {

  }

  public String getStreamId() {
    return streamId;
  }

  public Level getLevel() {
    return level;
  }

  public String getDefaultName() {
    return defaultName;
  }

  /**
   * represents the requested level of Permission
   */
  public enum Level {
    read("read"), contribute("contribute"), manage("manage");

    private final String field;

    Level(String pField) {
      field = pField;
    }

    @Override
    public String toString() {
      return field;
    }

  }

}
