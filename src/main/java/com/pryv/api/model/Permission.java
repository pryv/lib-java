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
  private String level;
  private String defaultName;

  public Permission(String pStreamId, String pLevel, String pDefaultName) {
    streamId = pStreamId;
    level = pLevel;
    defaultName = pDefaultName;
  }

  public String getStreamId() {
    return streamId;
  }

  public String getPermission() {
    return level;
  }

  public String getDefaultName() {
    return defaultName;
  }

}
