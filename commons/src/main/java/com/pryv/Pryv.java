package com.pryv;

/**
 * Pryv class, contains the configuration of the API.
 */
public class Pryv {

  public static String DOMAIN = "pryv.me";
  public static String USERNAME = "javalib";
  public static String REGISTRATION_URL = "https://reg." + DOMAIN + "/access";
  public static String URL = "https://" + USERNAME + "." + DOMAIN;

  /**
   * set the domain
   *
   * @param domain
   */
  public static void setDomain(String domain) {
    DOMAIN = domain;
    updateUrls();
  }

  /**
   * set the username
   *
   * @param username
   */
  public static void setUsername(String username) {
    USERNAME = username;
    updateUrls();
  }

  private static void updateUrls() {
    REGISTRATION_URL = "https://reg." + DOMAIN + "/access";
    URL = "https://" + USERNAME + "." + DOMAIN;
  }

}
