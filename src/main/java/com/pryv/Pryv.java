package com.pryv;

/**
 *
 * high-level class
 *
 * @author ik
 *
 */
public class Pryv {

  public static String REGISTRATION_URL = "https://reg.pryv.io/access";
  public static String API_DOMAIN = "pryv.io";
  public static final String DATABASE_NAME = "pryv-sqlite.db";

  public static void setStaging() {
    REGISTRATION_URL = "https://reg.pryv.in/access";
    API_DOMAIN = "pryv.in";
  }

  public static void getAuthorization() {

  }



}
