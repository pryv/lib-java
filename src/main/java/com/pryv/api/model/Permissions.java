package com.pryv.api.model;

/**
 *
 * Permissions for Streams
 *
 * @author ik
 *
 */
public enum Permissions {

  READ("read"), CONTRIBUTE("contribute"), MANAGE("manage");

  private final String field;

  Permissions(String pField) {
    field = pField;
  }

  @Override
  public String toString() {
    return field;
  }

}
