package com.pryv;

/**
 *
 * Pryv class, contains the configuration of the API.
 *
 * @author ik
 *
 */
public class Pryv {

  public static String DOMAIN = "pryv.io";
  public static String USERNAME = "javalib";
  public static String REGISTRATION_URL = "https://reg." + DOMAIN + "/access";
  public static String URL = "https://" + USERNAME + "." + DOMAIN;

  public static final String DATABASE_NAME = "pryv-sqlite.db";

  private static boolean cacheActivated = true;
  private static boolean onlineActivated = true;
  private static boolean supervisorActivated = true;

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

  /**
   * This function returns if the cache is activated or not.
   *
   * @return
   */
  public static boolean isCacheActive() {
    return cacheActivated;
  }

  /**
   * This function returns if the online module is activated or not.
   *
   * @return
   */
  public static boolean isOnlineActive() {
    return onlineActivated;
  }

  /**
   * This function returns if the Supervisor is activated or not.
   *
   * @return
   */
  public static boolean isSupervisorActive() {
    return supervisorActivated;
  }

  /**
   * activates the cache
   */
  public static void activateCache() {
    cacheActivated = true;
  }

  /**
   * stop using the cache
   */
  public static void deactivateCache() {
    cacheActivated = false;
  }

  /**
   * activates the online module
   */
  public static void activateOnline() {
    onlineActivated = true;
  }

  /**
   * Stops using the online module
   */
  public static void deactivateOnline() {
    onlineActivated = false;
  }

  /**
   * activates the Supervisor
   */
  public static void activatedSupervisor() {
    supervisorActivated = true;
  }

  /**
   * stop using the Supervisor
   */
  public static void deactivateSupervisor() {
    supervisorActivated = false;
  }

}
