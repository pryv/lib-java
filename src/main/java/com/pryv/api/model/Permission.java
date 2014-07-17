package com.pryv.api.model;


/**
 *
 * Permission used in authentication
 *
 * @author ik
 *
 */
public class Permission {


  private String streamId;
  private Level level;
  private String defaultName; // only used when requesting permission

  public Permission(String pStreamId, Level pLevel, String pDefaultName) {
    streamId = pStreamId;
    level = pLevel;
    defaultName = pDefaultName;
  }

  /**
   * represents the requested level of
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

  public String getStreamId() {
    return streamId;
  }

  public Level getLevel() {
    return level;
  }

  public String getDefaultName() {
    return defaultName;
  }

}
